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

import com.sun.squawk.translator.ir.instr.*;
import com.sun.squawk.translator.Translator;
import com.sun.squawk.util.SquawkHashtable;
import com.sun.squawk.util.SquawkVector;
import com.sun.squawk.util.Assert;
import com.sun.squawk.*;

/**
 * This class is used to inline methods.
 */
public class CodeCopier implements InstructionVisitor {
	/**
	 * Classfile of the caller.
	 */
    private com.sun.squawk.translator.ClassFile classFile;
    
    /**
     * Caller.
     */
    private IR ir;
    
    /**
     * Instruction immediately following the invocation being inlined.
     * Such an instruction must always exist, since control flow is not
     * allowed to fall off the edge of the code in Java.
     */
    private Instruction pos;
    
    /**
     * The invocation being inlined.
     */
    private Invoke inv;
    
    /**
     * A mapping from callee instructions to their new copies in the caller.
     */
    private SquawkHashtable copiedInsns;
    
    /**
     * A mapping from callee locals to the new corresponding caller locals.
     */
    private SquawkHashtable copiedLocals;
    
    /**
     * A mapping from callee instructions to arrays of copied targets that
     * should target the copy of that callee instruction.
     */
    private SquawkHashtable targets;
    
    private int nextLocal;
    
    private int firstLocal;
    
    /**
     * Derived local variable types of the caller at the call site.
     */
    private Klass callSiteLocals[];
    
    /**
     * StackProducer corresponding to the value returned by the callee.  It
     * should be null in the case of a void-valued callee.
     */
    private StackProducer lastInsn;
    
    /**
     * Targets pointing to the end of the inlined callee, where there is
     * an instruction to push the value on the stack that would have been
     * returned by the callee.
     */
    private Target returnTargets[];
    
    /**
     * Number of elements in the returnTargets array.  These elements are found
     * in slots 0 through numReturnTargets-1, inclusive.
     */
    private int numReturnTargets;
    
    /**
     * Local variable to which values returned by the callee should be stored.
     * Code at the end of the inlined callee will load this local onto the
     * stack.
     */
    private Local returnLocal;
    
    /**
     * Local variables corresponding to callee parameters.
     */
    private Local parmLocals[];
    
    /**
     * The caller's frame
     */
    Frame frame;
    
    /**
     * Keep track of the head of this inlining.
     */
    private InlinedInvoke inlinedInvoke;
    
    /**
     * Creates a new {@link CodeCopier} for inlining a callee into a caller.
     *
     * @param classFile       classfile of the caller
     * @param ir              code of the caller
     * @param insertBefore    caller instruction to insert callee code immediately before
     * @param inv             invocation being inlined
     * @param callSiteLocals  derived local types in the caller at the invocation instruction
     * @param frame         frame of the caller
     */
    public CodeCopier(com.sun.squawk.translator.ClassFile classFile, IR ir, Instruction insertBefore, Invoke inv, Klass callSiteLocals[], Frame frame) {
        this.classFile = classFile;
        this.ir = ir;
        this.pos = insertBefore;
        this.inv = inv;
        this.frame = frame;
        copiedInsns = new SquawkHashtable();
        copiedLocals = new SquawkHashtable();
        targets = new SquawkHashtable();
        this.callSiteLocals = callSiteLocals;
        firstLocal = callSiteLocals.length;
        nextLocal = firstLocal + 1;
        returnTargets = new Target[10];
        numReturnTargets = 0;
        returnLocal = null;
        
        if (inv.getMethod().getReturnType() != Klass.VOID) {
            returnLocal = new Local(Frame.getLocalTypeFor(inv.getMethod().getReturnType()), firstLocal, false);
        }
    }
    
    public void addExceptionHandler(IRExceptionHandler handler) {
    	IRExceptionHandler newHandler = new IRExceptionHandler(getCopy(handler.getTarget(), false));
    	newHandler.setEntry(getCopy(handler.getEntry()));
    	newHandler.setExit(getCopy(handler.getExit()));
    	ir.addExceptionHandler(newHandler);
    }
    
    /**
     * Insert an instruction into the caller's code.  If this instruction
     * references some constant object, add this object to the caller's
     * constant object table.
     *
     * @param insn  instruction to insert
     */
    private void insert(Instruction insn) {
        insn.setBytecodeOffset(inv.getBytecodeOffset());
        ir.insertBefore(insn, pos);
    }
    
    /**
     * Insert an instruction as a replacement for an instruction from the
     * callee.  Any targets which were copied from the callee that target
     * the original instruction will be made to target the new instruction.
     * If the new instruction references some constant object, this object
     * will be added to the caller's constant object table.  If the new
     * instruction is a {@link StackProducer}, then the original one should
     * be as well, and the spill local will be copied over if one exists.
     *
     * @param insn  instruction to insert
     * @param orig  callee instruction being copied
     */
    private void insert(Instruction insn, Instruction orig) {
        copiedInsns.put(orig, insn);
        SquawkVector targetList = (SquawkVector)targets.get(orig);
        if (targetList != null) {
            int len = targetList.size();
            for (int i = 0; i < len; i++) {
                Target t = (Target)targetList.elementAt(i);
                t.setTargetedInstruction((TargetedInstruction)insn);
            }
            targets.remove(orig);
        }
        if (insn instanceof StackProducer) {
            StackProducer newSP = (StackProducer)insn;
            StackProducer oldSP = (StackProducer)orig;
            if (oldSP.isSpilt()) {
                newSP.spill(getCopy(oldSP.getSpillLocal()));
            }
        }
        insert(insn);
    }
    
    private Try getCopy(Try insn) {
    	return (Try)copiedInsns.get(insn);
    }
    
    private TryEnd getCopy(TryEnd insn) {
    	return (TryEnd)copiedInsns.get(insn);
    }
    
     private InlinedInvoke getCopy(InlinedInvoke insn) {
    	return (InlinedInvoke)copiedInsns.get(insn);
    }
    
    /**
     * Get the copy of the given callee instruction that has been
     * inserted into the caller.
     *
     * @param insn  callee instruction
     * @return      copy of callee instruction in caller
     */
    private StackProducer getCopy(StackProducer insn) {
    	if (!copiedInsns.containsKey(insn)) {
    		if (insn instanceof StackMerge) {
    			return copyStackMerge((StackMerge)insn);
    		}
    	}
    	return (StackProducer)copiedInsns.get(insn);
    }
    
    /**
     * Copies the given callee local to a local in the caller that won't
     * interfere with the rest of the code already in the caller.
     *
     * @param local  callee local
     * @return       corresponding caller local
     */
    private Local getCopy(Local local) {
        if (copiedLocals.containsKey(local)) {
            return (Local)copiedLocals.get(local);
        } else {
            Local copiedLocal = new Local(local.getType(), nextLocal++, false);
            copiedLocals.put(local, copiedLocal);
            return copiedLocal;
        }
    }
    
    SquawkHashtable originalTargets = new SquawkHashtable();
    
    /**
     * Copies a callee {@link Target} to the caller.
     *
     * @param target  callee target
     * @param add     whether to update the targeted instruction when that instruction is inserted
     * @return        corresponding caller target
     */
    private Target getCopy(Target target, boolean add) {
        Klass stack[] = new Klass[target.getStack().length];
        System.arraycopy(target.getStack(), 0, stack, 0, stack.length);
        Klass locals[] = new Klass[callSiteLocals.length + target.getLocals().length + 1];
        System.arraycopy(callSiteLocals, 0, locals, 0, callSiteLocals.length);
        locals[callSiteLocals.length] = inv.getMethod().getReturnType();
        System.arraycopy(target.getLocals(), 0, locals, callSiteLocals.length + 1, target.getLocals().length);
        Target newTarget = new Target(0, stack, locals);
        TargetedInstruction targetInsn = target.getTargetedInstruction();
        if (copiedInsns.containsKey(targetInsn)) {
        	if (target.isCatchTarget()) {
        		newTarget.setTargetedCatchInstruction((Catch)copiedInsns.get(targetInsn));
        	} else {
            	newTarget.setTargetedInstruction((TargetedInstruction)copiedInsns.get(targetInsn));
            }
        } else if (add) {
            SquawkVector targetList = (SquawkVector)targets.get(targetInsn);
            if (targetList == null) {
                targetList = new SquawkVector();
                targets.put(targetInsn, targetList);
            }
            if (targetList.size() > 80000) {
                throw new RuntimeException("targetList.size(): " + targetList.size() + " for targeted instruction: " + targetInsn);
            }
            targetList.addElement(newTarget);
        }
        if (target.isBackwardBranchTarget()) {
        	newTarget.markAsBackwardBranchTarget();
        }
        return newTarget;
    }
    
      /* private Target getCopy(Target target, boolean add) {
        Klass stack[] = new Klass[target.getStack().length];
        System.arraycopy(target.getStack(), 0, stack, 0, stack.length);
        Klass locals[] = new Klass[callSiteLocals.length + target.getLocals().length + 1];
        System.arraycopy(callSiteLocals, 0, locals, 0, callSiteLocals.length);
        locals[callSiteLocals.length] = inv.getMethod().getReturnType();
        System.arraycopy(target.getLocals(), 0, locals, callSiteLocals.length + 1, target.getLocals().length);
        Target newTarget = new Target(0, stack, locals);
        TargetedInstruction targetInsn = target.getTargetedInstruction();
        if (copiedInsns.containsKey(targetInsn)) {
        	if (target.isCatchTarget()) {
        		newTarget.setTargetedCatchInstruction((Catch)copiedInsns.get(targetInsn));
        	} else {
            	newTarget.setTargetedInstruction((TargetedInstruction)copiedInsns.get(targetInsn));
            }
        } else if (add) {
            if (targets.containsKey(targetInsn)) {
                Target targetList[] = (Target[])targets.get(targetInsn);
                for (int i = 0; i < targetList.length; i++) {
                    if (targetList[i] == null) {
                        targetList[i] = newTarget;
                    } else if (i == targetList.length - 1) {
                        if (targetList.length > 80000) {
                            System.out.println("Weird! There are " + targetList.length + " targets to " + targetInsn);
                        }
                        Target tmp[] = new Target[targetList.length * 2];
                        System.arraycopy(targetList, 0, tmp, 0, targetList.length);
                        targetList = tmp;
                        targets.put(targetInsn, targetList);
                    }
                }
            } else {
                Target targetList[] = new Target[10];
                targetList[0] = newTarget;
                targets.put(targetInsn, targetList);
            }
        }
        if (target.isBackwardBranchTarget()) {
        	newTarget.markAsBackwardBranchTarget();
        }
        return newTarget;
    }*/
    
    private Target getCopy(Target target) {
    	return getCopy(target, true);
    }
    
    /**
     * Returns a new target pointing to instructions in the caller 
     * corresponding to the callee's method return.
     *
     * @return   new return target
     */
    private Target makeReturnTarget() {
        Klass stack[] = new Klass[0];
        Klass locals[] = new Klass[callSiteLocals.length + 1];
        System.arraycopy(callSiteLocals, 0, locals, 0, callSiteLocals.length);
        locals[callSiteLocals.length] = inv.getMethod().getReturnType();
        Target target = new Target(0, stack, locals);
        //ir.addTarget(target);
        while (numReturnTargets >= returnTargets.length) {
            Target tmp[] = new Target[returnTargets.length * 2];
            System.arraycopy(returnTargets, 0, tmp, 0, returnTargets.length);
            returnTargets = tmp;
        }
        returnTargets[numReturnTargets++] = target;
        return target;
    }
    /*
     private Target makeReturnTarget() {
        Klass stack[] = new Klass[1];
        stack[0] = inv.getMethod().getReturnType();
        Klass locals[] = new Klass[callSiteLocals.length + 1];
        System.arraycopy(callSiteLocals, 0, locals, 0, callSiteLocals.length);
        locals[callSiteLocals.length] = inv.getMethod().getReturnType();
        Target target = new Target(0, stack, locals);
        while (numReturnTargets >= returnTargets.length) {
            Target tmp[] = new Target[returnTargets.length * 2];
            System.arraycopy(returnTargets, 0, tmp, 0, returnTargets.length);
            returnTargets = tmp;
        }
        returnTargets[numReturnTargets++] = target;
        return target;
    }*/
    
    /**
     * Gets the {@link StackProducer} corresponding to the callee's return value.
     *
     * @return callee return value
     */
    public StackProducer getLast() {
        return lastInsn;
    }
    
    Object[] inlinedParams;
    
    /**
     * Inserts setup code for the inlined callee.
     */
    public void insertInit() {
        Klass params[] = inv.getMethod().getParameterTypes();
        inlinedInvoke = new InlinedInvoke(inv.getMethod());
        insert(inlinedInvoke);
        
        if (!inv.getMethod().isStatic() || inv.getMethod().isConstructor()) {
        	Klass tmp[] = new Klass[params.length + 1];
        	System.arraycopy(params, 0, tmp, 1, params.length);
        	tmp[0] = inv.getMethod().getDefiningClass();
        	params = tmp;
        }
        StackProducer producers[] = inv.getParameters();
        int numParms = 0;
        for (int i = 0; i < params.length; i++) {
        	if (params[i].isPrimitive() && params[i].isDoubleWord()) {
        		numParms += 2;
        	} else {
        		numParms++;
        	}
        }
        parmLocals = new Local[numParms];
        int index = 0;
        for (int i = 0; i < params.length; i++) {
            int paramNum = index;
            int origNum = i;
            if (!Translator.REVERSE_PARAMETERS) {
                paramNum = parmLocals.length - 1 - index;
                origNum = params.length - 1 - i;
            }
            // Create the Local representing the local in the original callee for the parameter (which didn't exist.
            // getCopy() will create a temporary varaible that we will use in this inlining.
            // TODO: This is not stricly needed, since we will never look in the copied locals for these parameters, we always look directly in 
            // parmLocals...
            parmLocals[paramNum] = getCopy(new Local(Frame.getLocalTypeFor(params[origNum]), origNum, false));
            
            // mark the producer as spilled. IRTransform will try to optimize away....
            //producers[origNum].spill(parmLocals[paramNum]);
            // transformer will generate store if needed.
            insert(new StoreLocal(parmLocals[paramNum], producers[origNum]));
            if (params[i].isPrimitive() && params[i].isDoubleWord()) {
            	index += 2;
            } else {
            	index++;
            }
        }
    }
    
    /**
     * Inserts exit code for the inlined callee.
     */
    public void insertExit() {
        boolean hasReturnValue = (inv.getMethod().getReturnType() != Klass.VOID);
        if (numReturnTargets > 0) {
            Phi phi = new Phi(returnTargets[0]);
            for (int i = 1; i < numReturnTargets; i++) {
                returnTargets[i].setTargetedInstruction(phi);
            }
            insert(phi);
            
            if (!hasReturnValue) {
                lastInsn = null;
            } else {
                LoadLocal ll = new LoadLocal(inv.getMethod().getReturnType(), returnLocal);
                insert(ll);
                lastInsn = ll;
            }
        } else if (hasReturnValue) {
            Klass retType = inv.getMethod().getReturnType();
            if (retType.isPrimitive()) {
                if (retType == Klass.LONG) {
                    insert(lastInsn = Constant.create(new Long(0)));
/*if[FLOATS]*/
                } else if (retType == Klass.DOUBLE) {
                    insert(lastInsn = Constant.create(new Double(0)));
                } else if (retType == Klass.FLOAT) {
                    insert(lastInsn = Constant.create(new Float(0)));
/*end[FLOATS]*/
                } else {
                    insert(lastInsn = Constant.create(new Integer(0)));
                }
            } else {
                insert(lastInsn = Constant.create(null));
            }
        }
        insert(new InlinedEnd(inv.getMethod(), inlinedInvoke));
    }
    
    /**
     * {@inheritDoc}
     */
    public void doArithmeticOp(ArithmeticOp insn) {
        insert(new ArithmeticOp(getCopy(insn.getLeft()), getCopy(insn.getRight()), insn.getOpcode()), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doArrayLength(ArrayLength insn) {
        insert(new ArrayLength(getCopy(insn.getArray())), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doArrayLoad(ArrayLoad insn) {
        insert(new ArrayLoad(insn.getType(), getCopy(insn.getArray()), getCopy(insn.getIndex())), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doArrayStore(ArrayStore insn) {
        insert(new ArrayStore(insn.getComponentType(), getCopy(insn.getArray()), getCopy(insn.getIndex()), getCopy(insn.getValue())), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doBranch(Branch insn) {
        insert(new Branch(getCopy(insn.getTarget())), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doCheckCast(CheckCast insn) {
        insert(new CheckCast(insn.getType(), getCopy(insn.getObject())), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doConversionOp(ConversionOp insn) {
        insert(new ConversionOp(insn.getTo(), getCopy(insn.getValue()), insn.getOpcode()), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doComparisonOp(ComparisonOp insn) {
        insert(new ComparisonOp(getCopy(insn.getLeft()), getCopy(insn.getRight()), insn.getOpcode()), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doTry(Try insn) {
        insert(new Try(), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doTryEnd(TryEnd insn) {
        insert(new TryEnd(), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doIf(If insn) {
        insert(new If(getCopy(insn.getValue()), insn.getOpcode(), getCopy(insn.getTarget())), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doIfCompare(IfCompare insn) {
        insert(new IfCompare(getCopy(insn.getLeft()), getCopy(insn.getRight()), insn.getOpcode(), getCopy(insn.getTarget())), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doIncDecLocal(IncDecLocal insn) {
        insert(new IncDecLocal(getCopy(insn.getLocal()), insn.isIncrement()), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doInstanceOf(InstanceOf insn) {
        insert(new InstanceOf(insn.getCheckType(), getCopy(insn.getObject())), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doFindSlot(FindSlot insn) {
        insert(new FindSlot(insn.getMethod(), getCopy(insn.getReceiver())), insn);
    }
    
    private StackProducer[] duplicateParameters(Invoke insn) {
        StackProducer origParams[] = insn.getParameters();
        StackProducer params[] = new StackProducer[origParams.length];
        for (int i = 0; i < origParams.length; i++) {
            params[i] = getCopy(origParams[i]);
        }
        return params;
    }
    
    /**
     * {@inheritDoc}
     */
    public void doInvokeSlot(InvokeSlot insn) {
        insert(new InvokeSlot(insn.getMethod(), duplicateParameters(insn)), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doInvokeStatic(InvokeStatic insn) {
        insert(new InvokeStatic(insn.getMethod(), duplicateParameters(insn)), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doInvokeSuper(InvokeSuper insn) {
        insert(new InvokeSuper(insn.getMethod(), duplicateParameters(insn)), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doInvokeVirtual(InvokeVirtual insn) {
        insert(new InvokeVirtual(insn.getMethod(), duplicateParameters(insn)), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doConstant(Constant insn) {
        insert(Constant.create(insn.getValue()), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doCatch(Catch insn) {
        insert(new Catch(insn.getType(), getCopy(insn.getTarget(), false)), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doGetField(GetField insn) {
        insert(new GetField(insn.getField(), getCopy(insn.getObject())), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doGetStatic(GetStatic insn) {
        insert(new GetStatic(insn.getField()), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doLoadLocal(LoadLocal insn) {
        Local local = insn.getLocal();
        if (local.isParameter()) {
            int index = local.getJavacIndex();
            Assert.that(parmLocals[index] != null);
            local = parmLocals[index];
        } else {
            local = getCopy(local);
        }
        insert(new LoadLocal(insn.getType(), local), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doLookupSwitch(LookupSwitch insn) {
        Target targets[] = insn.getTargets();
        int caseValues[] = insn.getCaseValues();
        LookupSwitch newInsn = new LookupSwitch(getCopy(insn.getKey()), targets.length, getCopy(insn.getDefaultTarget()));
        for (int i = 0; i < targets.length; i++) {
            newInsn.addTarget(i, caseValues[i], getCopy(targets[i]));
        }
        insert(newInsn, insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doMonitorEnter(MonitorEnter insn) {
    	StackProducer object = insn.getObject();
    	if (object != null) {
    		object = getCopy(object);
    	}
        insert(new MonitorEnter(object), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doMonitorExit(MonitorExit insn) {
    	StackProducer object = insn.getObject();
    	if (object != null) {
    		object = getCopy(object);
    	}
        insert(new MonitorExit(object), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doNegationOp(NegationOp insn) {
        insert(new NegationOp(getCopy(insn.getValue()), insn.getOpcode()), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doNewArray(NewArray insn) {
        insert(new NewArray(insn.getType(), getCopy(insn.getLength())), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doNewDimension(NewDimension insn) {
        insert(new NewDimension(getCopy(insn.getArray()), getCopy(insn.getLength())), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doNew(New insn) {
        Klass type = insn.getRuntimeType();
        insert(new New(new com.sun.squawk.translator.ci.UninitializedObjectClass(type.getName(), type)), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doPhi(Phi insn) {
    	insert(new Phi(getCopy(insn.getTarget(), false)), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doPop(Pop insn) {
        insert(new Pop(getCopy(insn.value())), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doPosition(Position insn) {
        // who cares about these
    }
    
   /**
     * {@inheritDoc}
     */
    public void doInlinedInvoke(InlinedInvoke insn) {
        // copy nested inlining info...
        insert(new InlinedInvoke(insn.getMethod()), insn);
    }
    
   /**
     * {@inheritDoc}
     */
    public void doInlinedEnd(InlinedEnd insn) {
        // copy nested inlining info...
        insert(new InlinedEnd(insn.getMethod(), getCopy(insn.getInvoke())), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doReturn(Return insn) {
        if (returnLocal != null) {
            insert(new StoreLocal(returnLocal, getCopy(insn.getValue())), insn);
            insert(new Branch(makeReturnTarget()));
        } else {
            insert(new Branch(makeReturnTarget()), insn);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void doPutField(PutField insn) {
        insert(new PutField(insn.getField(), getCopy(insn.getObject()), getCopy(insn.getValue())), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doPutStatic(PutStatic insn) {
        insert(new PutStatic(insn.getField(), getCopy(insn.getValue())), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doStoreLocal(StoreLocal insn) {
        Local local = insn.getLocal();
        if (local.isParameter()) {
            local = parmLocals[local.getJavacIndex()];
        } else {
            local = getCopy(local);
        }
        insert(new StoreLocal(local, getCopy(insn.getValue())), insn);
    }
    
    private class StackMergeVisitor implements StackMerge.ProducerVisitor {
        private StackMerge newStackMerge;
        
        public StackMergeVisitor(StackMerge newStackMerge) {
            this.newStackMerge = newStackMerge;
        }
    
        public boolean visit(StackProducer producer) {
            newStackMerge.addProducer(getCopy(producer));
            return true;
        }
    }
    
    private StackMerge copyStackMerge(StackMerge insn) {
    	StackMerge newInsn = new StackMerge(insn.getType());
        insn.visitProducers(new StackMergeVisitor(newInsn));
        return newInsn;
    }
    
    /**
     * {@inheritDoc}
     */
    public void doStackMerge(StackMerge insn) {
        insert(copyStackMerge(insn), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doStackOp(StackOp insn) {
        insert(new StackOp(insn.getOpcode()), insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doTableSwitch(TableSwitch insn) {
        int low = insn.getLow();
        Target targets[] = insn.getTargets();
        TableSwitch newInsn = new TableSwitch(getCopy(insn.getKey()), low, insn.getHigh(), getCopy(insn.getDefaultTarget()));
        for (int i = 0; i < targets.length; i++) {
            newInsn.addTarget(low + i, getCopy(targets[i]));
        }
        insert(newInsn, insn);
    }
    
    /**
     * {@inheritDoc}
     */
    public void doThrow(Throw insn) {
        insert(new Throw(getCopy(insn.getThrowable())), insn);
    }
}
