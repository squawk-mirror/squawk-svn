/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.translator.ir;

import com.sun.squawk.util.Assert;
import com.sun.squawk.util.SquawkHashtable;
import java.util.Enumeration;
import com.sun.squawk.translator.Translator;
import com.sun.squawk.translator.Arg;
import com.sun.squawk.translator.ir.instr.*;
import com.sun.squawk.translator.ci.CodeParser;
import com.sun.squawk.util.Tracer;
import com.sun.squawk.util.SquawkVector;
import com.sun.squawk.vm.OPC;
import com.sun.squawk.translator.util.ConstantEvaluator;
import com.sun.squawk.*;

/**
 * Optional phase that performs simple optimization described below.
 * This phase must come after spill/fills have been generated, and should 
 * occur during inlining, such that a callee has it's own inlining and 
 * IROptimization phase before determining if it is small enough to inline 
 * into a caller. After inlining the caller should be optimized.
 *
 * Note that the primrary goal of these optimizations are code size reduction,
 * so optimizations such as copy propagation only work on simple copies (loads and constants).
 *
 * Also, we currently are not doing typical versions of these optimizations
 * with basic blocks and def/use chains. Instead we reason about static global
 * properties, such as the number of assigments to a local within a method is zero, or one.
 * Occasionally we check that if there is a "linear sequence" without additional stores within a 
 * between a store and a load. A "linear sequence" exists between two instruction if there are no 
 * backward branch targets anywhere between the two instructions
 *
 * OPTIMIZATIONS:
 *
 * 1) Dead stack value elimination:
 *    If a stack value is simply popped from the stack, and the code
 *    generating the value has no side-effects, then both the pop and the 
 *    code that created the stack value may be deleted from the IR.
 *    This kind of code can be found when inlinging a function that returns a value,
 *    but the caller doesn't use the returned value.
 *
 * 2) Dead variable elimination.
 *    If a value is stored into a local variable and never read, then the value and
 *    store can be deleted if the value has no side effects, or the store can be 
 *    turned into a pop if it does. In either case, the local variable can be eliminated.
 *    This kind of code can be found when inlining a call to a method that doesn't use 
 *    one of its parameters (among othehr cases)
 *
 * 3) Copy propagation (trivial global):
 *    A simplified version targeted at eliminating redundant spill/fills:
 *    Given assignemnt X = Y, and both X and Y are both only assigned to once, then replace all
 *    uses of X with Y. 
 *
 * 4) Constant propagation (trivial global):
 *    Given assignemnt X = c, and X is only assigned to once, then replace all
 *    uses of X with c.
 *
 * 5) Unnecessary variable elimination:
 *    Given an adjacent Store/Load pair to a variable, where there is only one read of the
 *    variable, then the Store and Load can be eliminated, leaving the value on the stack for
 *    following instruction. The local variable can be eliminated.
 *
 * 6) Constant Folding (NOT DONE)
 *    If all operands of certain instructions are constant, replace the operation with the
 *    appropriate constant.
 *
 * 7) Constant conditional (NOT STARTED)
 *    If the oprnads to a conditional instruction are constant, evaluate the control flow, and replace 
 *    with unconditional control flow.
 *
 * 8) Dead code elimination (Ed's code not fixed & integrated)
 *    If there is no entry into a basic block, then delete it.
 *
 */
public class IROptimizer extends AbstractInstructionVisitor {
    /**
     * The method being transformed.
     */
    private final Method method;
    
    /**
     * The IR of the method being transformed.
     */
    private final IR _ir_;

    /**
     * Keep around reference to CodeParser.
     */
    private final CodeParser codeParser;
    
    /**
     * COunt how many times we do an optimization pass on the code.
     */
    private int optPass = 0;
    
    /**
     * Cached information of the static read and write counts of local variables.
     */
    private LocalUseCounter localVarUsage;
    
    /**
     * Cached information of instructions that target other instructions.
     */
    private TargetTable targetTable;
    
    /**
     * Instruction remover used to keep caches up to date.
     */
    private OptInstructionRemover remover;
    
    /** True if traceoptimize is enabled.*/
    private final boolean trace;
    
    /**
     * Creates an IRTransformer.
     *
     * @param ir       the IR to be transformed
     * @param method   the method encapsulating the IR
     */
    public IROptimizer(IR ir, Method method, CodeParser codeParser) {
        this._ir_     = ir;
        this.method = method;
        this.codeParser = codeParser;
        this.trace = Translator.TRACING_ENABLED && Translator.isTracing("optimize", method);

        //Count uses of local variables
        localVarUsage = new LocalUseCounter(trace);
        
        targetTable = new TargetTable();
        this.remover = new OptInstructionRemover(ir, codeParser, localVarUsage, targetTable);

    }

    
    private final static boolean CONSERVATIVE_COPY_PROP = true;
    
    /**
     * Can make more assumptions when we know that the value doesn't change.
     * NOTE: Currently only can notice final parameters. Other local variables may be written
     * to once statically, but in a loop such that the value may change.
     *
     * @param local the local to check
     * @return true is the value is final
     */
    private boolean isFinalLocal(Local local) {
        return (local.isParameter() && localVarUsage.numLocalWrites(local) == 1);
    }
    
    /**
     * Determines if a given local variable is written to within a sequence of code, and no backbranches
     *
     * This is simple conservative linear def/use analysis:
     * 1) If we are loading from variable "local" stored to only once, AND
     * 2) The store is in the same "linear block"*** as the load, AND
     * 3) The original variable "local2" is not stored to between the store and load
     *    of "local"
     * THEN we can replace the load of "local" with a load of "local2".
     * The goal is to end up with "local" dead, which can be eliminated in a
     * later pass of optimze().
     *
     * @param  start   the first instruction in the sequence of code
     * @param  end     the last instruction in the sequence of code
     * @param  local   the local variable to test
     * @param  copyfrom  the original local that <code>local</code> is a copy of.
     * @return true only if <code>local</code> is not written to between
     *                <code>start</code> and <code>end</code>
     */
    private boolean canCopyPropLocal(Instruction start, LoadLocal end,  Local copyfrom) {
        // if original local is a parameter that is not written to in the body,
        // then we can copy prop it:
        if (isFinalLocal(copyfrom)) {
            return true;
        }
        return isSafePathFromStoreToLoad(start, end, copyfrom);
    }
    
    /**
     * Determines if a given global location (static or instance field) is written to within a sequence of code, and no backbranches
     *
     * @param  start   the first instruction in the sequence of code
     * @param  end     the last instruction in the sequence of code
     * @param  local  the local variable to test
     * @return true only if <code>local</code> is not written to between
     *                <code>start</code> and <code>end</code>
     */
    private boolean canCopyPropGlobal(Instruction start, Instruction end, Field field) {
        Instruction instruction = start;
        boolean isStatic = field.isStatic();
        
        while (instruction != end) {
            if (isStatic) {
                if (instruction instanceof PutStatic) {
                    PutStatic p = (PutStatic)instruction;
                    if (p.getField().equals(field)) {
                        return false;
                    }
                }
            } else {
                if (instruction instanceof PutField) {
                    PutField p = (PutField)instruction;
                    if (p.getField().equals(field)) {
                        return false;
                    }
                }
            }
            
            if (instruction instanceof Invoke ||
                instruction instanceof MonitorEnter ||
                instruction instanceof Phi ||
                instruction instanceof Try ||
                instruction instanceof TryEnd) {
                return false;
            }
            Assert.that(instruction.getNext() != null);
            instruction = instruction.getNext();
        }
        return true;
    }
    
    /**
     * Used by the instruction visitor methods to indicate changes to the IR so the the main loop in
     * optimize() will loop correctly.
     */
    private Instruction nextInstruction;
    
    /**
     * Eliminate pops of idempotent values.
     */
    public void doPop(Pop pop) {
        StackProducer producer = pop.value();
        if (producer.isIdempotent(_ir_, method)) {
            // no one will miss source of pop, so delete.
            
            if (trace) {
                Tracer.traceln("Pop of idempotent value " + producer + ". Removing pop and value.");
            }
            Instruction next = pop.getNext();
            remover.removeClosure(pop);
            nextInstruction = next;
        }
    }
    
    void removeDeadStore(StoreLocal store) {
        Local local = store.getLocal();
        StackProducer producer = store.getValue();
        nextInstruction = store.getNext();
        
        if (producer.isIdempotent(_ir_, method)) {
            if (trace) {
                Tracer.traceln("Dead store " + local + ". Removing store and value.");
            }
            remover.removeClosure(store);
        } else {
            if (producer.isSpilt()) {
                producer.cancelSpilling();
            }
            if (trace) {
                Tracer.traceln("Dead store " + local + ". Removing store.");
            }
            remover.replace(new Pop(producer), store);
        }
    }

    /**
     * Eliminate dead stores and STORE/LOAD pairs.
     */
    public void doStoreLocal(StoreLocal store) {
        Local local = store.getLocal();
        int numReads = localVarUsage.numLocalReads(local);
        
        if (numReads == 0) {
             /*
              * Dead variable elimination. If value store was idempotent, then can remove that,
              * otherwise replace store wih pop.
              */
            removeDeadStore(store);
        } else if (numReads == 1) {
              /*
               * Eliminate redundant, back-to-back STORE/LOAD pairs.
               * Since value is still on the stack, this works for non-idempotent values.
               */
            nextInstruction = store.getNext();
            while (nextInstruction instanceof Position ||
                    nextInstruction instanceof InlinedInvoke ||
                    nextInstruction instanceof InlinedEnd) {
                nextInstruction = nextInstruction.getNext();
            }
            
            if (nextInstruction instanceof LoadLocal) {
                LoadLocal load = (LoadLocal)nextInstruction;
                if ((load.getLocal() == local)) {
                    StackProducer producer = store.getValue();
                    nextInstruction = load.getNext();
                    if (trace) {
                        Tracer.traceln("Back-to-back STORE/LOAD pair.");
                    }
                    remover.remove(store);
                    remover.remove(load);
                    // some downstream instr had load as an input. Replace with the stack producer for store.
                    OperandReplacer.replace(producer, load, nextInstruction);
                }
            }
        }
    }
    
    private void propagateConstant(LoadLocal load, Constant c) {
        Constant newC = Constant.create(c.getValue());
        newC.setBytecodeOffset(load.getBytecodeOffset());
        nextInstruction = load.getNext();
        if (trace) {
            Tracer.traceln("Propagate constant " + newC);
        }
        remover.replace(newC, load);
        OperandReplacer.replace(newC, load, nextInstruction);
        nextInstruction = newC;
    }
    
    private void propagateLoad(LoadLocal load, LoadLocal originalLoad) {
        Local originalLocal = originalLoad.getLocal();
        if (originalLocal.getJavacIndex() == load.getLocal().getJavacIndex()) {
            // it's the same local, so don't bother
            // TODO: Make sure that we can get rid of
            // LOAD n
            // STORE n
            return;
        }
        LoadLocal newLoad = new LoadLocal(originalLoad.getType(), originalLocal);
        newLoad.setBytecodeOffset(load.getBytecodeOffset());
        if (trace) {
            Tracer.traceln("Copy propagate " + originalLocal + " instead of " + load.getLocal());
        }
        nextInstruction = load.getNext();
        remover.replace(newLoad, load);
        OperandReplacer.replace(newLoad, load, nextInstruction);
        Assert.that(newLoad.getNext() == nextInstruction);
        nextInstruction = newLoad;
    }
    
    /**
     * Do constant and copy propagation.
     */
    public void doLoadLocal(LoadLocal load ) {
        Local local1 = load.getLocal();
        
        if (local1.isParameter()) {
            // Don't bother trying copy propagation on a parameter
            return;
        }
        
        int numWrites = localVarUsage.numLocalWrites(local1);

        StoreLocal store = localVarUsage.lastLocalWrite(local1);
        if (store != null) {
            // only one write in method is conservative approx. of only write in basic block!
            StackProducer producer = store.getValue();
            if (producer instanceof Constant) {
                    /*
                     * Simple constant propagation. If Constant is only value stored through a variable,
                     * then simply use the constant instead of the variable.
                     * If the variable is dead, a second pass of optimize() will catch it.
                     *
                     * Note also that previous optimization passes may constant evaluate various expressions,
                     * so this case also will catch some producer == ArithmeticOp (etc), eventually.
                     */
                propagateConstant(load, (Constant)producer);
            } else if (producer instanceof LoadLocal) {
                LoadLocal originalLoad = (LoadLocal)producer;
                Local originalLocal = originalLoad.getLocal();
                
                if (canCopyPropLocal(store.getNext(), load, originalLocal)) {
                    propagateLoad(load, originalLoad);
                }
            } else {
                 // NOTE: Can't easily propagate instructions that have side-effects, or implicit null checks, or
                // do implicit clinits.
                
                if (localVarUsage.numLocalReads(local1) > 1) {
                    // these hoisting will increase space or time if done more than once, so don't bother
                    return;
                }
                
                /** Can special case getstatic of global-static classes - no side effects, and no "empty stack restriction. */
                if (producer instanceof GetStatic) {
                    GetStatic get = (GetStatic)producer;
                    Field field = get.getField();
                    Klass klass = field.getDefiningClass();
                    
                    // WARNING: This only works for GlobalStatics - these have no clnit, and the runtime doesn't require that
                    // the statck be empty/
                    if (klass.hasGlobalStatics() &&
                            canCopyPropGlobal(store.getNext(), load, field)) {
                        Assert.that(!klass.mustClinit());
                        if (trace) {
                            Tracer.traceln("Copy propagate " + producer + " instead of " + load.getLocal());
                        }
                        GetStatic newGet = new GetStatic(field);
                        nextInstruction = load.getNext();
                        remover.replace(newGet, load);
                        OperandReplacer.replace(newGet, load, nextInstruction);
                        Assert.that(newGet.getNext() == nextInstruction);
                        nextInstruction = newGet;
                    }
                }

                    /* NOT READY------------------------
                     
                     // these do implicit null checks or clinits, or have requirements that the stack is empty:

                    if (producer instanceof ArrayLength) {
System.out.println("can we copy propagate this " + producer + " instead of using " + load);
                    }
                     
                    if (producer instanceof GetStatic) {
                        GetStatic get = (GetStatic)producer;
                        Field f = get.getField();
                     
                        // TODO: getStatic has requirement that stack is empty, and we might be trying to hoist it to a place
                        //       where that is not true! How can we tell?
                        if (false && canCopyPropGlobal(store.getNext(), load, f)) {
                            GetStatic newGet = new GetStatic(f);
                            nextInstruction = load.getNext();
                            remover.replace(newGet, load);
                            OperandReplacer.replace(newGet, load, nextInstruction);
                            Assert.that(newGet.getNext() == nextInstruction);
                            nextInstruction = newGet;
                        }
                    } else if (producer instanceof GetField) {
                        GetField get = (GetField)producer;
                        Field f = get.getField();
                     
                        // TODO: Figure out how to copy the producer of the old getfield to the new getfield location.
                        if (false && canCopyPropGlobal(store.getNext(), load, f)) {
                            GetField newGet = new GetField(f, get.getObject());
                            nextInstruction = load.getNext();
                            remover.remove(newGet, load);
                            OperandReplacer.replace(newGet, load, nextInstruction);
                            Assert.that(newGet.getNext() == nextInstruction);
                            nextInstruction = newGet;
                        }
                    }
                     *---------------------------------*/
            }
        } else if (numWrites > 1) {
            
            // Look for redundant stores:
            //   x = x
            // this doesn't seem to heppen, so ignore:'
//            Instruction peekNext = load.getNext();
//            while (peekNext instanceof Position ||
//                    peekNext instanceof InlinedInvoke ||
//                    peekNext instanceof InlinedEnd) {
//                peekNext = peekNext.getNext();
//            }
//            if (peekNext instanceof StoreLocal) {
//                store = (StoreLocal)peekNext;
//                if (store.getValue() == load && load.getLocal().getJavacIndex() == store.getLocal().getJavacIndex()) {
//                    System.out.println("REDUNDANT STORE of " + load + " by " + store);
//                }
//            }
          
            // look for the "closest" store to this load, and
            // try to propagate that value...
            SquawkVector stores = localVarUsage.localWrites(local1);
            for (int i = 0; i < stores.size(); i++) {
                store = (StoreLocal)stores.elementAt(i);
                if (store != null) {
                    StackProducer producer = store.getValue();
                    if (producer instanceof Constant) {
                        if (isSafePathFromStoreToLoad(store.getNext(), load, null)) {
                            propagateConstant(load, (Constant)producer);
                        }
                    } else if (producer instanceof LoadLocal) {
                        LoadLocal originalLoad = (LoadLocal)producer;
                        Local originalLocal = originalLoad.getLocal();
                        // don't copy propagate a temporary (loosing debug info, and not likely to optimize code.
                        // also don't extend life of temp
                        if ((!originalLocal.isTemp() || local1.isTemp()) && isSafePathFromStoreToLoad(store.getNext(), load, originalLocal)) {
                            propagateLoad(load, originalLoad);
                        }
                    }
                }
            }
        }
        
    }
    
    /**
     * Is there a "straight path" from the store instruction to the load instruction, with no possible jumps to the middle of the path,
     * that doesn't modify the variable being loaded, or the optional variable we are trying to copy propagate?
     *
     * @param from instruction after the store
     * @param to the load instruction we're trying to replace
     * @param copyFrom (optional) the local variable we're trying to use instead of the original load instruction, or null 
     */
    boolean isSafePathFromStoreToLoad(Instruction from, LoadLocal to, Local copyFrom) {
        Instruction instruction = from;
        while (instruction != to) {
            if (instruction == null) {
                return false;
            } else if (instruction instanceof LocalVariable) {
                LocalVariable lvi = (LocalVariable) instruction;
                if (lvi.writesValue()) {
                    Local modifiedLocal = lvi.getLocal();
                    if (modifiedLocal == to.getLocal() 
                        || modifiedLocal == copyFrom) {
                        // the original local, or the local that we are trying to copy from was modified on this path
                        return false;
                    }
                }
            } else if (instruction instanceof Phi) {
                 return false;
            /*    Target target = ((Phi)instruction).getTarget();
                if (target.isBackwardBranchTarget()) {
                    return false;
                }*/
            } else if (CONSERVATIVE_COPY_PROP) {
                if (/*instruction instanceof Try ||*/
                    instruction instanceof TryEnd) {
//System.out.println("Bailing out of canCopyPropLocal because of " + instruction);                  
                    return false;
                }
            }
            // we may wander off the end of the code, becuase there may not be a straight line from start to end (eg. start > end)
            instruction = instruction.getNext();
        }
        return true;
    }
    
     /**
     * Is there a "straight path" from the store instruction to the another store to sthe same variable, and no reads the the variable in between?
      *
      * Note that we are trying to prove that ALL reads will see the second store, not the first.
     *
     * @param from instruction after the store
     * @param to the load instruction we're trying to replace
     */
    boolean isSafePathFromStoreToStore(Instruction from, StoreLocal to) {
        Instruction instruction = from;
        while (instruction != to) {
            if (instruction == null) {
// System.out.println("Store list out of order. " + from + " should be before " + to);
                return false;
            } else if (instruction.mayCauseGC(method.isStatic())) {
                return false;
            } else if (instruction instanceof LocalVariable) {
                LocalVariable lvi = (LocalVariable) instruction;
                if (lvi.readsValue()) {
                    Local modifiedLocal = lvi.getLocal();
                    if (modifiedLocal == to.getLocal()) {
                        // the local was read between the two stores.
                        return false;
                    }
                }
            } else if (instruction instanceof Phi) {
                return false;
            } else if (instruction instanceof Branch) {
                return false;
            } else if (instruction instanceof Invoke) {
                return false;
            } else if (instruction instanceof Throw) { // a throw could mean that the 2nd store doesn't happen
                return false;
            } else if (instruction instanceof Catch) {
                return false;
            } else if (instruction instanceof Try) {
                return false;
            } else if (instruction instanceof TryEnd) {
                return false;
            }
            // we may wander off the end of the code, becuase there may not be a straight line from start to end (eg. start > end)
            instruction = instruction.getNext();
        }
        return true;
    }
    
     private void lookForDeadStores(Local local) {
        SquawkVector stores = localVarUsage.localWrites(local);
        if (stores == null) {
            return;
        }

        for (int i = 1; i < stores.size(); i++) {
            StoreLocal store1 = (StoreLocal)stores.elementAt(i-1);
            StoreLocal store2 = (StoreLocal)stores.elementAt(i);
            if (isSafePathFromStoreToStore(store1.getNext(), store2)) {
                removeDeadStore(store1);
            }
        }
    }
        
    /**
     * Look at all of the stores to the same variable. If there are no reads between two stores, then can delete the first store.
     *
     * @return true if one or more stores were deleted.
     */
    private void lookForDeadStores() {
        Enumeration e = localVarUsage.getLocals();
        while (e.hasMoreElements()) {
            Local l = (Local)e.nextElement();
            lookForDeadStores(l);
        }
    }
    
    /**
     * Detect branches to the following instruction.
     *
     * @param insn some kind of branch instrunction.
     * @return true if the target was the next instruction.
     */
    private boolean isBranchToNext(Branch insn) {
        Instruction targetInstruction = (Instruction)(insn.getTarget().getTargetedInstruction());
        Instruction next = insn.getNext();
        Assert.that(targetInstruction != null);
        
        if (next != null) {
            next = next.skipPseudo();
        }
        
        if (targetInstruction == next) {
            return true;
        }
        return false;
    }
    

    /**
     * Replace branches to unconditional branches with branch to final target.
     *
     * @param insn some kind of branch instrunction.
     * @return true if the target was an unconditional branch.
     *
     * @todo: this didn't occur often enough to complete
     */
    private boolean isBranchToBranch(Branch insn) {
        /*Instruction targetInstruction = (Instruction)(insn.getTarget().getTargetedInstruction());
        
        Instruction realTarget = targetInstruction;
        if (realTarget instanceof Phi) {
            realTarget = realTarget.getNext();
        }
        if (realTarget instanceof Branch) {
            Assert.that(!(realTarget instanceof If), "Should be jumping to a conditional branch (skipping the operands)");
            System.out.println("BranchRemover Found branch to a branch " + insn + ", target was: " + realTarget);
            return true;
        }*/
        return false;
    }
    
    private boolean doConstTest(Constant c, int opcode) {
        if (c instanceof ConstantObject) {
            Assert.that((opcode == OPC.IF_EQ_O) || (opcode == OPC.IF_NE_O));
            boolean eqTest = opcode == OPC.IF_EQ_O;
            ConstantObject co = (ConstantObject)c;
            boolean eqZero = (co.getValue() == null);
            return eqZero == eqTest;
        } else {
            return ConstantEvaluator.doConstTest(c.getValue(), opcode);
        }
    }
    
    private boolean doConstTest(Constant c1, Constant c2, int opcode) {
        if (c1 instanceof ConstantObject) {
            Assert.that((opcode == OPC.IF_CMPEQ_O) || (opcode == OPC.IF_CMPNE_O) , "opcode = " + opcode);
            boolean eqTest = (opcode == OPC.IF_CMPEQ_O);
            ConstantObject co1 = (ConstantObject)c1;
            ConstantObject co2 = (ConstantObject)c2;
            boolean eqObjects = (co1.getValue() == co2.getValue());
            return eqObjects == eqTest;
        } else {
            return ConstantEvaluator.doConstTest(c1.getValue(), c2.getValue(), opcode);
        }
    }

/*if[DEBUG_CODE_ENABLED]*/
    private boolean sameValue(StackProducer val1, StackProducer val2) {
        if (val1.isIdempotent(_ir_, method) && val2.isIdempotent(_ir_, method)) {
            if (val1 == val2) {
                return true;
            } else if (val1 instanceof LoadLocal &&
                       val2 instanceof LoadLocal) {
                LoadLocal l1 = (LoadLocal)val1;
                LoadLocal l2 = (LoadLocal)val2;
                return l1.getLocal() == l2.getLocal();
            } else if (val1 instanceof GetField &&
                       val2 instanceof GetField) {
                GetField l1 = (GetField)val1;
                GetField l2 = (GetField)val2;
                return l1.getField().getFullyQualifiedName().equals(l2.getField().getFullyQualifiedName()) &&
                        sameValue(l1.getObject(), l2.getObject());
            } else if (val1 instanceof GetStatic &&
                       val2 instanceof GetStatic) {
                GetStatic l1 = (GetStatic)val1;
                GetStatic l2 = (GetStatic)val2;
                return l1.getField().getFullyQualifiedName().equals(l2.getField().getFullyQualifiedName());
            } else if (val1 instanceof ArrayLength &&
                       val2 instanceof ArrayLength) {
                ArrayLength l1 = (ArrayLength)val1;
                ArrayLength l2 = (ArrayLength)val2;
                return sameValue(l1.getArray(), l2.getArray());
            }
        }
        return false;
    }
/*end[DEBUG_CODE_ENABLED]*/

    /**
     * Call when we try to back up an instruction during optimization. Sometimes we try to back up past the 
     * first instruction!
     */
    void fixupNextInstruction() {
        if (nextInstruction == null) {
            nextInstruction = _ir_.getHead();
        }
    }
    
    /**
	 * {@inheritDoc}
	 */
    public void doIfCompare(IfCompare insn) {
        if (!Arg.get(Arg.OPTIMIZE_BYTECODE_CONTROL).getBool()) {
            return;
        }
        //TargetedInstruction ti = insn.getTarget().getTargetedInstruction();
                
        if (isBranchToNext(insn)) {
            if (trace) {
                Tracer.traceln("Replacing " + insn + " with pop + pop");
            }
            nextInstruction = insn.getPrevious(); // yes, backup and recheck.
			remover.insertBefore(new Pop(insn.getLeft()), insn);
			remover.replace(new Pop(insn.getRight()), insn);
            fixupNextInstruction();
            // if we end up popping a constant, or idempotent value, a later pass will kill it.
        } else if (insn.getLeft()  instanceof Constant &&
                   insn.getRight() instanceof Constant) {
            if (trace) {
                Tracer.traceln("Constant branch. Replacing " + insn + " with goto or fallthrough");
            }
            Constant c1 = (Constant)insn.getLeft();
            Constant c2 = (Constant)insn.getRight();
            if (c1.getTag() == c2.getTag()) {
                boolean doBranch = doConstTest(c1, c2, insn.getOpcode());
                remover.remove(c1);
                remover.remove(c2);
                nextInstruction = insn.getPrevious(); // yes, backup and recheck.
                if (doBranch) {
                    // test succeeded, make branch unconditional
                    remover.replace(new Branch(insn.getTarget()), insn);
                } else {
                    // test failed - fall through:
                    remover.remove(insn);
                }
                fixupNextInstruction();
            }
/*if[DEBUG_CODE_ENABLED]*/
        } else if (sameValue(insn.getLeft(), insn.getRight())) {
            // could optimize this test!
            System.out.println("********** If test has same value! left: " + insn.getLeft() + ", right: " + insn.getRight());
/*end[DEBUG_CODE_ENABLED]*/
        } else if (insn.getLeft()  instanceof Constant && ((Constant)insn.getLeft()).isDefaultValue()) {
            // turn into doIf
          //  System.out.println("********** If test has zero constant for left: " + insn);
        } else if (insn.getRight()  instanceof Constant && ((Constant)insn.getRight()).isDefaultValue()) {
            // turn into doIf
           // System.out.println("********** If test has has zero constant for right: " + insn);
        } else {
            // look to eliminate stack manipulations by swapping the operands:
            StoreLocal store = isStoreLoadStore(insn, insn.getLeft(), insn.getRight());
            if (store != null) {
                if (trace) {
                    Tracer.traceln("Simplified if_cmp operands. Replacing " + insn);
                }
                StackProducer newRight = store.getValue();
                remover.remove(store);
                remover.remove(insn.getRight());
                Instruction newIf = new IfCompare(insn.getLeft(),
                        newRight,
                        insn.getSwappedOpcode(),
                        insn.getTarget());
                newIf.setBytecodeOffset(insn.getBytecodeOffset());
                remover.replace(newIf, insn);
                fixupNextInstruction();
            }
        }
	}
    
    /**
	 * {@inheritDoc}
	 */
    public void doIf(If insn) {
        if (!Arg.get(Arg.OPTIMIZE_BYTECODE_CONTROL).getBool()) {
            return;
        }
        
        //TargetedInstruction ti = insn.getTarget().getTargetedInstruction();
        StackProducer producer = insn.getValue();
        
        if (isBranchToNext(insn)) {
            if (trace) {
                Tracer.traceln("Replacing " + insn + " with pop");
            }
            nextInstruction = insn.getPrevious(); // yes, backup and recheck.
            // if we end up popping a constant or other idempotent value, recheck will kill it.
            remover.replace(new Pop(producer), insn);
            fixupNextInstruction();
        } else if (producer instanceof Constant) {
            if (trace) {
                Tracer.traceln("Constant branch. Replacing " + insn + " with goto or fallthrough");
            }
            Constant c = (Constant)producer;
            boolean doBranch = doConstTest(c, insn.getOpcode());
            remover.remove(c);
            nextInstruction = producer.getPrevious(); // yes, backup and recheck.
            if (doBranch) {
                // test succeeded, make branch unconditional
                remover.replace(new Branch(insn.getTarget()), insn);
            } else {
                // test failed - fall through:
                remover.remove(insn);
            }
            fixupNextInstruction();
        } else {
            // @TODO:
            
            // if the point of this IF is to convert an expresion to a booelan, and the only 
            // use if the boolean is as an expression for another IF, then don't bother 
            // creating the booelan.
            // 
            // useful for cleaning up inlines of functions that compute booleans, 
            // such as Modifier.isAbstract(int mod) { return (mod & ABSTRACT) != 0; }
            
            // template match:
            // IF EQ_I GOTO A
            // CONST 1
            // STORE T1
            // GOTO B
            // A: PHI 
            // CONST 0
            // STORE T1
            // B: PHI
            // LOAD T1
            // IF EQ_I

        }
    }
           
    /**
	 * {@inheritDoc}
	 */
    public void doBranch(Branch insn) {
        if (!Arg.get(Arg.OPTIMIZE_BYTECODE_CONTROL).getBool()) {
            return;
        }
        
        if (isBranchToNext(insn)) {
            if (trace) {
                Tracer.traceln("Deleting goto next instruction");
            }
            nextInstruction = insn.getPrevious(); // yes, backup and recheck.
            remover.remove(insn);
            fixupNextInstruction();
        }
    }
  
    /**
     * Template matcher or store B; load A; load B. 
     * There are several cases where we can remove the Store B;Load B part.
     * This occurs  in arithmetic operations or comparisons, when the second operand (B)
     * causes the stack to be flushed (is an invoke, getStatic, etc.)
     * template test:
               eval A
               store A
               eval B
               store B
               load A
               load B
               OPERATION
             ===> replace with
               eval A
               store A
               eval B
               load A
               OPERATION
     */
    StoreLocal isStoreLoadStore(Instruction insn, Instruction leftInsn, Instruction rightI) {
        if (rightI instanceof LoadLocal) {
            
            LoadLocal rightInsn = (LoadLocal)rightI;
            Local rightLocal = rightInsn.getLocal();
            Instruction leftInsnPrev = leftInsn.getPrevious();
            
            if (insn.getPrevious() == rightInsn
                    && rightInsn.getPrevious() == leftInsn
                    && leftInsnPrev != null
                    && leftInsnPrev instanceof StoreLocal
                    && localVarUsage.numLocalReads(rightLocal) == 1
                    && localVarUsage.numLocalWrites(rightLocal) == 1) {
                StoreLocal storeInsn = (StoreLocal)leftInsnPrev;
                if (storeInsn.getLocal() == rightLocal) {
                    return storeInsn;
                }
            }
        }
        return null;
    }
    
    /**
     * Try to remove producer and it's closure. If idempotent, then can remove, otherwise pop.
     */
    private void removeProducer(StackProducer producer, String msg) {
        if (producer.isIdempotent(_ir_, method)) {
            if (trace) {
                Tracer.traceln(msg + " Removing " + producer);
            }
            remover.removeClosure(producer);
        } else {
             if (trace) {
                Tracer.traceln(msg + " Popping " + producer);
            }
            remover.insertBefore(new Pop(producer), producer.getNext());
        }
    }
    
    /**
     * Replace the given binary operation with the new value (often a constant).
     */
    private void replaceBinaryOp(StackProducer insn, StackProducer left, StackProducer right, StackProducer newValue) {
        nextInstruction = insn.getNext();
        removeProducer(left, "    ");
        removeProducer(right, "    ");
        newValue.setBytecodeOffset(insn.getBytecodeOffset());
        remover.replace(newValue, insn);
        OperandReplacer.replace(newValue, insn, nextInstruction);
    }
    
    /**
     * Replace the given unary operation with the new value (often a constant).
     */
    private void replaceUnaryOp(StackProducer insn, StackProducer operand, StackProducer newValue) {
        nextInstruction = insn.getNext();
        removeProducer(operand, "    ");
        newValue.setBytecodeOffset(insn.getBytecodeOffset());
        remover.replace(newValue, insn);
        OperandReplacer.replace(newValue, insn, nextInstruction);
    }
    
    /**
     * Optimize arithmetic operations. javac doesn't do many of the obvious things, leaving it for the JIT to clean up.
     */
	public void doArithmeticOp(ArithmeticOp insn) {
        
        // look for optimizations when at least one operand is constant:
        if (insn.getLeft() instanceof Constant ||
            insn.getRight() instanceof Constant) {
            
            /*
             * Check for (const OP const)
             * If so, delete (const OP const), leaving resulting const on the stack.
             *
             * eg. "100 + 1" => 101
             * (Yes, this really happens. Can happen more after inlining.)
             */
            if (insn.getLeft() instanceof Constant &&
                insn.getRight() instanceof Constant) {

                Constant left = (Constant) insn.getLeft();
                Constant right = (Constant) insn.getRight();
                Object constantVal = ConstantEvaluator.evaluateBinary(left.getValue(), right.getValue(), insn.getOpcode());

                if (constantVal != null) {
                    if (trace) {
                        Tracer.traceln("Constant operands for " + insn + ". Replacing with constant value.");
                    }
                    replaceBinaryOp(insn, insn.getLeft(), insn.getRight(), Constant.create(constantVal));
                    return;
                }
            }

            /*
             * Check for (X OP const) being an identity function.
             * If so, delete "OP const", leaving X on the stack.
             *
             * eg. "X + 0" => X
             */
            int identity = insn.isIdentity();
            if (identity != 0) {
                StackProducer survivingValue;
                StackProducer unneededValue;
                if (trace) {
                    Tracer.traceln(insn + " is an identity function. Removing operation.");
                }
                if (identity < 0) {
                    survivingValue = insn.getLeft();
                    unneededValue = insn.getRight();
                } else {
                    survivingValue = insn.getRight();
                    unneededValue = insn.getLeft();
                }
                // we no longer need this value, so pop or delete.
                removeProducer(unneededValue, "    ");
                nextInstruction = insn.getNext();
                OperandReplacer.replace(survivingValue, insn, nextInstruction);
                remover.remove(insn);
                return;
            }

            /*
             * Check for (X OP const) being an constant function.
             * If so, replace "(X OP const)" with the constant.
             *
             * eg. "X * 0" => 0
             */
            Object constantVal = insn.isConstantResult();
            if (constantVal != null) {
                if (trace) {
                    Tracer.traceln(insn + " is a constant function. Replacing with constant value.");
                }
                if (constantVal != null) {
                    replaceBinaryOp(insn, insn.getLeft(), insn.getRight(), Constant.create(constantVal));
                    return;
                }
            }

            /*
             * Check for X MUL K, or K MUL X, where k is power-of-2 constant
             */
            int reducableMUL = insn.isReducableMul();
            if (reducableMUL != 0) {
                StackProducer survivingValue;
                StackProducer unneededValue;
                if (trace) {
                    Tracer.traceln(insn + " by a constant power-of-2. Replacing with shift.");
                }
                if (reducableMUL < 0) {
                    survivingValue = insn.getLeft();
                    unneededValue = insn.getRight();
                } else {
                    survivingValue = insn.getRight();
                    unneededValue = insn.getLeft();
                }

                StackProducer newValue;
                StackProducer shiftConstant = Constant.create(new Integer(Math.abs(reducableMUL)));
                if (insn.getOpcode() == OPC.MUL_I) {
                    newValue = new ArithmeticOp(survivingValue, shiftConstant, OPC.SHL_I);
                } else {
                    newValue = new ArithmeticOp(survivingValue, shiftConstant, OPC.SHL_L);
                }

                removeProducer(unneededValue, "    ");
                nextInstruction = insn.getNext();
                newValue.setBytecodeOffset(insn.getBytecodeOffset());
                remover.replace(newValue, insn);
                OperandReplacer.replace(newValue, insn, nextInstruction);
                remover.insertBefore(shiftConstant, newValue);
                return;
            }

            /** 
             * why no op DIV C => op SHR optimization? Because that doesn't handle case were op is negative
             * without adding more code, which is what we're trying to avoid. We could add an optimized native
             * "method" that only handles division by constant powers of two. 
             */
            /*
             * Check for (X AND_I 0xFFFF). Comes up when people work with unsigned shorts. Can replace with i2c instruction.
             */
            int reducableAND = insn.isReducableAnd();
            if (reducableAND != 0) {
                StackProducer survivingValue;
                StackProducer unneededValue;
                if (trace) {
                    Tracer.traceln(insn + " is creating an unsigned short. Use i2c instead.");
                }
                if (reducableAND < 0) {
                    survivingValue = insn.getLeft();
                    unneededValue = insn.getRight();
                } else {
                    survivingValue = insn.getRight();
                    unneededValue = insn.getLeft();
                }

                StackProducer newValue = new ConversionOp(Klass.CHAR, survivingValue, OPC.I2C);
                removeProducer(unneededValue, "    ");
                nextInstruction = insn.getNext();
                newValue.setBytecodeOffset(insn.getBytecodeOffset());
                remover.replace(newValue, insn);
                OperandReplacer.replace(newValue, insn, nextInstruction);
                return;
            }
        }
        
        if (insn.isCommutative()) {
            // look to eliminate stack manipulations by swapping the operands:
            StoreLocal store = isStoreLoadStore(insn, insn.getLeft(), insn.getRight());
            if (store != null) {
                if (trace) {
                    Tracer.traceln("Simplified operands. Replacing " + insn);
                }
                StackProducer newRight = store.getValue();
                remover.remove(store);
                remover.remove(insn.getRight());
                StackProducer newInsn = new ArithmeticOp(insn.getLeft(),
                        newRight,
                        insn.getOpcode());
                newInsn.setBytecodeOffset(insn.getBytecodeOffset());
                remover.replace(newInsn, insn);
                OperandReplacer.replace(newInsn, insn, newInsn);
                fixupNextInstruction();
                return;
            }
        }
    }

    /**
	 * {@inheritDoc}
	 */
	public void doConversionOp(ConversionOp insn) {
       StackProducer value = insn.getValue();
        if (value instanceof Constant) {
            if (trace) {
                Tracer.traceln("Constant operands for " + insn + ". Replacing with constant value.");
            }
            Constant c  = (Constant)insn.getValue();
            Object constantVal = ConstantEvaluator.evaluateUnary(c.getValue(), insn.getOpcode());

            if (constantVal != null) {
                replaceUnaryOp(insn, insn.getValue(), Constant.create(constantVal));
            }
        } else if (value instanceof ConversionOp) {
            if (((ConversionOp) value).getOpcode() == insn.getOpcode()) {
                if (trace) {
                    Tracer.traceln("Redundant conversion operation " + insn + ". Should remove!");
                }
                // TODO: Actually replace.
            }
        }
	}
    
    /**
	 * {@inheritDoc}
	 */
	public void doNegationOp(NegationOp insn) {
        StackProducer value = insn.getValue();

        if (value instanceof Constant) {
            if (trace) {
                Tracer.traceln("Constant operands for " + insn + ". Replacing with constant value.");
            }
            Constant c  = (Constant)insn.getValue();
            Object constantVal = ConstantEvaluator.evaluateUnary(c.getValue(), insn.getOpcode());

             if (constantVal != null) {
                replaceUnaryOp(insn, insn.getValue(), Constant.create(constantVal));
            }
        } else if (value instanceof NegationOp) {
            if (trace) {
                Tracer.traceln("Redundant negation operation " + insn + ". Should remove!");
            }
        // TODO: Actually replace.
        }
    }
    
    /**
	 * {@inheritDoc}
	 */
    public void doCheckCast(CheckCast insn) {
        /*
         * Remove un-needed check casts.
         */
        if (insn.getObject() instanceof ConstantObject) {
            ConstantObject c = (ConstantObject)insn.getObject();
            Object constValue = c.getConstantObject();
            Klass klass = (Klass)insn.getConstantObject();
            if (trace) {
                Tracer.traceln("Constant operands for " + insn + ". Replacing with constant value.");
            }
            if (constValue == null ||     // null can always be cast to any class. The preprocessor sometimes generates this case.
                c.isInstanceOf(klass)) {
                nextInstruction = insn.getNext();
                remover.remove(insn);
                OperandReplacer.replace(c, insn, nextInstruction);
            }
        }
	}
    
    /**
	 * {@inheritDoc}
	 */
    public void doInstanceOf(InstanceOf insn) {
        if (insn.getObject() instanceof ConstantObject) {
            ConstantObject c = (ConstantObject)insn.getObject();
            Object constValue = c.getConstantObject();
            Klass klass = (Klass)insn.getConstantObject();
            if (trace) {
                Tracer.traceln("Constant operands for " + insn + ". Replacing with constant value.");
            }
            
            int result = 0;
            // null is NOT an instanceof any class (unlike in checkcast).
            if (constValue != null && c.isInstanceOf(klass)) {
                result = 1;
            }
            replaceUnaryOp(insn, insn.getObject(), Constant.create(new Integer(result)));
            /*nextInstruction = insn.getNext();
            remover.remove(c);
            Constant c2 = Constant.create(new Integer(result));
            remover.replace(c2, insn);
            OperandReplacer.replace(c2, insn, nextInstruction);    */  
        }
	}
    
    public void doPutField(PutField insn) {
        /*
         * Java code often inserts conversion bytecodes before putfields, becuase java put fields are not typed.
         * But squawk putfields are typed, and don't need conversions.
         */
        if (false &&
                insn.getValue() instanceof ConversionOp) {
            ConversionOp valConverter = (ConversionOp)insn.getValue();
            int opcode = valConverter.getOpcode();
            if ((opcode == OPC.I2B ||
                    opcode == OPC.I2S ||
                    opcode == OPC.I2C)
                    && (valConverter.getTo() == insn.getMutationType())) {
//System.out.println("&&& Conversion " + valConverter + " before " + insn);
//System.out.println("!!! Can replace it");
            }
        }
        
        /*
         * Look for constructors that set the value of one of the new object's instance field's to it's
         * default value (null, zero, etc). We can eliminate the putfield.
         * 
         * Gotchas:
         * - Are we in the right constructor for the particular field?
         * - Is object null? Not in cinostructors that don't change "this"
         * - Are there any other sets of the field in the constructor? Check that there's only one putfield for this field.
         * - Does the constructor call anything that could set the field? Check that there are no calls out (except for superclass?)
         *    - Simplest check is that there are no invokes that preceed the putfield (rely on inlining super constructor.
         */
        if (false &&
                method.isConstructor()
                && insn.getField().getDefiningClass() == method.getDefiningClass()
                && (insn.getValue() instanceof Constant) && !_ir_.changesThis()) {
            Constant c = (Constant)insn.getValue();
            if (c.isDefaultValue()) {
System.out.println("**** Is this a redundant set of field " + insn.getField() + " to " + c);
            }
        }
	}
    
    public void doPutStatic(PutStatic insn) {        
        /*
         * Java code often inserts conversion bytecodes before putstatics, becuase java putstatics are not typed.
         * But squawk putstatics are typed and don't need conversions.
         */
        if (false && 
                insn.getValue() instanceof ConversionOp) {
            ConversionOp valConverter = (ConversionOp)insn.getValue();
            int opcode = valConverter.getOpcode();
            if ((opcode == OPC.I2B ||
                    opcode == OPC.I2S ||
                    opcode == OPC.I2C)
                    && (valConverter.getTo() == insn.getMutationType())) {
//System.out.println("&&& Conversion " + valConverter + " before " + insn);
//System.out.println("!!! Can replace it");
            }
        }
    }
    
    public void doArrayStore(ArrayStore insn) {
        /*
         * Java code often inserts conversion bytecodes before putstatics, becuase java putstatics are not typed.
         * But squawk putstatics are typed and don't need conversions.
         */
        if (false && 
                insn.getValue() instanceof ConversionOp) {
            ConversionOp valConverter = (ConversionOp)insn.getValue();
            int opcode = valConverter.getOpcode();
            if ((opcode == OPC.I2B ||
                    opcode == OPC.I2S ||
                    opcode == OPC.I2C)
                    && (valConverter.getTo() == insn.getMutationType())) {
//System.out.println("&&& Conversion " + valConverter + " before " + insn);
//System.out.println("!!! Can replace it");
            }
        }
	}
    
/***************************************************************************
 *                        NOT CURRENTLY OPTIMIZED                          *
 ***************************************************************************/
    
	public void doArrayLength(ArrayLength insn) {
        if (insn.getArray() instanceof ConstantObject) {
 System.out.print("!!! constant array for " + insn);
        }
        // never see this in the bootstrap suite at least
	}
    
	public void doArrayLoad(ArrayLoad insn) {
        // never see this in the bootstrap suite at least
	}
    
	public void doComparisonOp(ComparisonOp insn) {
        // used for floating point comparisons.
        // integer compares converted to if_cmp.
        // never see this case in boostrap suite:
        /*if (insn.getLeft() instanceof Constant &&
            insn.getRight() instanceof Constant) {
            if (trace) {
                Tracer.traceln("Constant comparision. Could replace " + insn);
            }
        }*/
	}
    
/***************************************************************************
 *                        OPTIMIZATION LOOP                                *
 ***************************************************************************/
    
    /**
     * Verify that all operands of all instructions are themselves found in the IR.
     */
    private void verify() {
        _ir_.verify(true);
    }
    
    private void trace(String msg) {
        if (trace) {
            Translator.trace(method, _ir_, "Optimize (" + msg + " pass " + optPass + ")");
        }   
    }
    
    /** 
     * Do an optimization pass over the code. May be called repeatedly.
     */
    private void optimizePass() {
        Instruction instruction = _ir_.getHead();
        verify();
        
        while (instruction != null) {
            nextInstruction = null;
            instruction.visit(this);
            if (nextInstruction == null) {
                Assert.that(instruction.isInIR(_ir_), instruction + " was removed, but nextInstruction wasn't adjusted.");
                nextInstruction = instruction.getNext();
            }
            instruction = nextInstruction;
        }
        
        lookForDeadStores();
        
        if (remover.hasChanged()) {
            trace("after");
        }
        
        verify();
    }
    
    /**
     * Loop over IR, optimizing until there are no changes.
     * 
     * @return true if the optimizer changed the IR.
     */
    public boolean optimize() {
        boolean changed = false;
        
        trace("before");
        localVarUsage.count(_ir_);
        targetTable.generate(_ir_);
        
        do {
            remover.reset();
            optimizePass();
            optPass++;
            changed = changed | remover.hasChanged();
        } while (remover.hasChanged());

/*if[DEBUG_CODE_ENABLED]*/
        LocalUseCounter dbgCounter = new LocalUseCounter(trace);
        dbgCounter.count(_ir_);
        dbgCounter.verifyEqual(localVarUsage);
        
        TargetTable tableB = new TargetTable();
        tableB.generate(_ir_);
        tableB.verifyEqual(targetTable);
/*end[DEBUG_CODE_ENABLED]*/
        
        return changed;
    }
    
    /** The dead code remover should use the same instruction remover...
     * @return the remover
     */
    public InstructionRemover getRemover() {
        return remover;
    }
    
}

/**
 * Private class to count the number of times a local variable is used.
 */
final class LocalUseCounter {
    SquawkHashtable variableUse;
    boolean trace;
    
    LocalUseCounter(boolean trace) {
        this.trace = trace;
    }

    static final class VariableUse {
        int reads;
        int writes;
        
        /** track all of the store instructions to this local. This will be updated for all instructions added and removed during optimization.*/
        SquawkVector stores;
        
        VariableUse(boolean isParameter) {
            if (isParameter) {
                writes = 1;
            } else {
                writes = 0;
            }
        }
    }
    
    public Enumeration getLocals() {
        return variableUse.keys();
    }
    
    /**
     * Update cached information considering this instruction.
     *  Creates a VariableUse record if necessary.
     *
     * @param instruction the instruction being considered.
     */
    public void addInstruction(LocalVariable instruction) {
        Local local = instruction.getLocal();
        VariableUse useInfo = (VariableUse)variableUse.get(local);
        if (useInfo == null) {
            useInfo = new VariableUse(local.isParameter());
            variableUse.put(local, useInfo);
        }
        
        if (instruction.readsValue()) {
            useInfo.reads++;
        }
        if (instruction.writesValue()) {
            useInfo.writes++;
            
            if (instruction instanceof StoreLocal) {
                if (useInfo.stores == null) {
                    useInfo.stores = new SquawkVector();
                }
                useInfo.stores.addElement(instruction);
            }
        }
    }
    
    /**
     * Given that this instruction is being deleted, 
     * update cached information. This instruction must already
     * have been recorded by addinstruction().
     * This will delete the local variable from the CodeParser if there are no other users of it.
     *
     * @param instruction the instruction being deleted.
     * @param codeParser the CodeParser that records the existence of local variable for this code.
     */
    public void deleteInstruction(LocalVariable instruction, CodeParser codeParser) {
        Local local = instruction.getLocal();
        VariableUse useInfo = (VariableUse)variableUse.get(local);
        
        if (instruction.readsValue()) {
            useInfo.reads--;
        }
        if (instruction.writesValue()) {
            useInfo.writes--;
            if (instruction instanceof StoreLocal) {
                if (useInfo.stores != null) {
                    useInfo.stores.removeElement(instruction);
                }
            }
        }
        
        if (useInfo.writes == 0 && useInfo.reads == 0) {
            codeParser.localVariableDeleted(local);
            variableUse.remove(local);
            if (trace) {
                Tracer.traceln("   Deleted: " + local);
            }
        }
        
        Assert.that(useInfo.writes >= 0 && useInfo.reads >= 0);
    }

    /**
     * Iterate over the IR counting the operand uses.
     */
    void count(IR ir) {
        Instruction instruction = ir.getHead();
        variableUse = new SquawkHashtable();
        while (instruction != null) {
            if (instruction instanceof LocalVariable) {
                addInstruction((LocalVariable) instruction);
            }
            instruction = instruction.getNext();
        }
    }
    
    public int numLocalReads(Local local) {
        VariableUse useInfo = (VariableUse)variableUse.get(local);
        return useInfo.reads;
    }

    public int numLocalWrites(Local local) {
        VariableUse useInfo = (VariableUse)variableUse.get(local);
        return useInfo.writes;
    }
    
    /*
     * If there is only one known store to a variable, return the StoreLocal.
     */
    public StoreLocal lastLocalWrite(Local local) {
        VariableUse useInfo = (VariableUse)variableUse.get(local);
        if (useInfo.writes == 1 && useInfo.stores != null && useInfo.stores.size() == 1) {
            return (StoreLocal)useInfo.stores.firstElement();
        }
        return null;
    }
    
    public SquawkVector localWrites(Local local) {
        VariableUse useInfo = (VariableUse)variableUse.get(local);
        return useInfo.stores;
    }

/*if[DEBUG_CODE_ENABLED]*/
    /**
     * Verify that both LocalUseCounters contain the same variables, with the same counts.
     */
    public void verifyEqual(LocalUseCounter counterB) {
        // check from this first:
        Enumeration e = this.variableUse.keys();
        while (e.hasMoreElements()) {
            Local key = (Local)e.nextElement();
            VariableUse va = (VariableUse)this.variableUse.get(key);
            VariableUse vb = (VariableUse)counterB.variableUse.get(key);
            if (vb == null) {
                Assert.that(key.isParameter() && va.reads == 0 && va.writes == 1, "counterB doesn't have var for "+ key);
            } else {
                Assert.that(va.reads == vb.reads && va.writes == vb.writes, "Counters unequal for "+ key);
            }
        }
        
        e = counterB.variableUse.keys();
        while (e.hasMoreElements()) {
            Local key = (Local)e.nextElement();
            VariableUse vb = (VariableUse)counterB.variableUse.get(key);
            if (this.variableUse.get(key) == null) {
                Assert.that(key.isParameter() && vb.reads == 0 && vb.writes == 1, "this doesn't have var for "+ key);
            }
        }
    }
/*end[DEBUG_CODE_ENABLED]*/
   
}

/**
 * Private class to keep track of instructions that that target other instructions
 */
class TargetTable {
    
    /**
     * A collection of all the TargetingInstructions in this method.
     */ 
    private SquawkVector targetingInstructions;
    
    /**
     * Used to look for a use of a specific target.
     */
    static final class TargetFinder implements TargetVisitor {
        private boolean found = false;
        private final Target searchingFor;
        
        TargetFinder(Target searchingFor) {
            this.searchingFor = searchingFor;
        }
        
        /**
         * {@inheritDoc}
         */
        public void doTarget(TargetingInstruction instruction, Target target) {
            if (target == searchingFor || target.getTargetedInstruction() == searchingFor.getTargetedInstruction()) {
                found = true;
            }
        }
        
        /**
         * @return true an instruction was found that had the same target (or targeted instruction) as the target
         * searchingFor
         */
        public boolean found() {
            return found;
        }
    }
    
    /**
     * Used to potentially remove Phi, when deleting a TargetingInstruction and there are no other
     * instructions targeting the Phi. 
     */
    final class UnusedPhiRemover implements TargetVisitor {
        OptInstructionRemover remover;
        
        UnusedPhiRemover(OptInstructionRemover remover) {
            this.remover = remover;
        }
        
        public void doTarget(TargetingInstruction instruction, Target target) {
            Instruction ti = (Instruction)target.getTargetedInstruction();
            if (ti.isInIR(remover.ir) && (ti instanceof Phi) && !isTargeted(target)) {
                //System.out.println("Found a Phi to kill");
                remover.remove((Phi)ti, true); // allow removal of targetable instruction
            }
        }
    }
    
    /**
     * Update cached information considering this instruction.
     *
     * @param instruction the instruction being considered.
     */
    public void addInstruction(TargetingInstruction instruction) {
        Assert.that(!targetingInstructions.contains(instruction));
        targetingInstructions.addElement(instruction);
    }
    
    /**
     * Given that this instruction is being deleted, 
     * update cached information. This instruction must already
     * have been recorded by addinstruction().
     *
     * @param instruction the instruction being deleted.
     */
    public void deleteInstruction(TargetingInstruction instruction, OptInstructionRemover remover) {
        boolean contained = targetingInstructions.removeElement(instruction);
        Assert.that(contained);
        
        instruction.visit(new UnusedPhiRemover(remover));
    }

    /**
     * Iterate over the IR remembering the targeting instructions.
     */
    void generate(IR ir) {
        Instruction instruction = ir.getHead();
        targetingInstructions = new SquawkVector();
        while (instruction != null) {
            if (instruction instanceof TargetingInstruction) {
                addInstruction((TargetingInstruction) instruction);
            }
            instruction = instruction.getNext();
        }
    }
    
    /**
     * Returns true if the there exists some instruction that targets the same instruction as target t.
     *
     * @param t the target to search for.
     * @return true if the instruction at the target is still targetted.
     */
    public boolean isTargeted(Target t) {
        int len = targetingInstructions.size();
        TargetFinder visitor = new TargetFinder(t);
        for (int i = 0; i < len; i++) {
            TargetingInstruction ti = (TargetingInstruction)targetingInstructions.elementAt(i);
            ti.visit(visitor);
            if (visitor.found) {
                return true;
            }
        }
        return false;
    }

/*if[DEBUG_CODE_ENABLED]*/
    /**
     * Verify that both TargetTables contain the same instructions.
     */
    public void verifyEqual(TargetTable tableB) {
        int lenA = targetingInstructions.size();
        Assert.that(lenA == tableB.targetingInstructions.size());
        for (int i = 0; i < lenA; i++) {
            Assert.that(tableB.targetingInstructions.contains(this.targetingInstructions.elementAt(i)));
        }
    }
/*end[DEBUG_CODE_ENABLED]*/
    
}

/**
 * Version of InstructionRemover that also updates cached variable use state.
 */
class OptInstructionRemover extends InstructionRemover implements OperandVisitor {
	/**
     * Keep around reference to CodeParser.
     */
    private final CodeParser codeParser;
    
    /**
     * Cached information of the static read and write counts of local variables.
     */
    private final LocalUseCounter localVarUsage;
    
    private final TargetTable targetTable;
    
	public OptInstructionRemover(IR ir, CodeParser codeParser, LocalUseCounter localVarUsage, TargetTable targetTable) {
        super(ir);
        this.codeParser = codeParser;
        this.localVarUsage = localVarUsage;
        this.targetTable = targetTable;
	}
	
    /** 
     * Update cached information to reflect new instruction insn.
     *
     * @param insn the new instruction
     */
    private void addInstruction(Instruction insn) {
        if (insn instanceof LocalVariable) {
            localVarUsage.addInstruction((LocalVariable)insn);
        } else if (insn instanceof TargetingInstruction) {
            targetTable.addInstruction((TargetingInstruction)insn);
        }
    }
    
    /** 
     * Update cached information to reflect deleting old instruction insn.
     *
     * @param insn the instruction that was deleted.
     */
    private void deleteInstruction(Instruction insn) {
        if (insn instanceof LocalVariable) {
            localVarUsage.deleteInstruction((LocalVariable)insn, codeParser);
        } else if (insn instanceof TargetingInstruction) {
            targetTable.deleteInstruction((TargetingInstruction)insn, this);
        }
    }
    
    /**
	 * {@inheritDoc}
	 */
    public boolean remove(Instruction insn, boolean evenTargetable) {
        if (super.remove(insn, evenTargetable)) {
            deleteInstruction(insn);
            return true;
        }
        
        Assert.shouldNotReachHere();
        return false;
    }
    
    /**
	 * {@inheritDoc}
	 */
	public boolean replace(Instruction newInsn, Instruction oldInsn) {
		if (super.replace(newInsn, oldInsn)) {
            addInstruction(newInsn);
            deleteInstruction(oldInsn);
            return true;
		}
        Assert.shouldNotReachHere();
        return false;
	}
	
    /**
	 * {@inheritDoc}
	 */
	public void insertBefore(Instruction insn, Instruction pos) {
		super.insertBefore(insn, pos);
        addInstruction(insn);
	}
    
    /**
     * Remove an instruction and all of its operands from the IR, and update cached information
     *
     * @param insn the instruction to remove
     */
    public boolean removeClosure(Instruction insn) {
        if (remove(insn)) {
            insn.visit(this);
            return true;
        }
        return false;
    }
	
    /**
     * Remove the operand from the ir
     *
     * @param   instruction  the instruction to which the operand belongs
     * @param   operand      the operand to process
     * @return  null
     */
    public StackProducer doOperand(Instruction instruction, StackProducer operand) {
        operand.visit(this);
        remove(operand);
        return operand;
    }
}
