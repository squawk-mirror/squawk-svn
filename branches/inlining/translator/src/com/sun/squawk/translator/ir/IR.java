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

import java.util.*;
import com.sun.squawk.util.Assert;
import com.sun.squawk.translator.*;
import com.sun.squawk.translator.ci.CodeParser;
import com.sun.squawk.util.SquawkVector;    // Version without synchronization
import com.sun.squawk.translator.ir.instr.*;
import com.sun.squawk.*;

/**
 * This class encapsultates the intermediate representation for a single
 * method. The instructions in the IR are stored in a sequence.<p>
 *
 */
public final class IR {

    /**
     * A constant empty array of targets.
     */
    private final static Target[] NO_TARGETS = {};

    /**
     * The first instruction in the IR.
     */
    private Instruction head;

    /**
     * The last instruction in the IR.
     */
    private Instruction tail;

    /**
     * The ordered set of {@link ExceptionHandler} instances comprising the
     * exception handler table for this IR.
     */
    private SquawkVector exceptionHandlers;

    /**
     * The targets (i.e. all basic block entries except the first) in the method.
     */
    private Target[] targets = NO_TARGETS;

    /**
     * Creates a new <code>IR</code>.
     */
    public IR() {}

    /*---------------------------------------------------------------------------*\
     *                            Instruction list                               *
    \*---------------------------------------------------------------------------*/

    /**
     * Returns the number of IR nodes (not including exception handlers?)
     *
     * @param includePseudo if true, count all IR nodes, otehrwise only nodes that generate bytecode.
     * @return  number of nodes.
     */
    public int size(boolean includePseudo) {
        int count = 0;
        for (Instruction i = head; i != null; i = i.next) {
            if (includePseudo || !(i instanceof PseudoInstruction)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Searches for an instruction in the list of instructions.
     *
     * @param   instruction  the instruction to search for
     * @return  true if <code>instruction</code> is found
     */
    public boolean findInstruction(Instruction instruction) {
        for (Instruction i = head; i != null; i = i.next) {
            if (instruction == i) {
                return true;
            }
        }
        return false;
    }

    /**
     * Append an instruction to the end of the instruction sequence represented
     * by this IR. The instruction sequence must not already contain
     * <code>instruction</code>.
     *
     * @param instruction  the instruction to append
     */
    public void append(Instruction instruction) {
        if (head == null) {
            Assert.that(tail == null);
            head = tail = instruction;
        } else {
            Assert.that(tail != null);
            Assert.that(!findInstruction(instruction), "instruction cannot be in IR twice");
            tail.next = instruction;
            instruction.previous = tail;
            instruction.next = null;
            tail = instruction;
        }
//Assert.that(findInstruction(instruction));
//Assert.that(findInstruction(head));
//Assert.that(findInstruction(tail));
    }

    /**
     * Prepend an instruction to the start of the instruction sequence
     * represented by this IR. The instruction sequence must not already contain
     * <code>instruction</code>.
     *
     * @param instruction  the instruction to prepend
     */
    public void prepend(Instruction instruction) {
        if (head == null) {
            Assert.that(tail == null);
            head = tail = instruction;
        } else {
            Assert.that(tail != null);
            Assert.that(!findInstruction(instruction), "instruction cannot be in IR twice");
            instruction.next = head;
            head.previous = instruction;
            head = instruction;
        }
        tail = instruction;
    }

    /**
     * Insert an instruction into the instruction sequence represented by this
     * IR. The instruction sequence must not already contain
     * <code>instruction</code> but must contain <code>pos</code>.
     *
     * @param instruction  the instruction to be inserted
     * @param pos          the instruction that <code>instruction</code> will be
     *                     inserted after
     */
    public void insertAfter(Instruction instruction, Instruction pos) {
        Assert.that(!findInstruction(instruction));
        Assert.that(findInstruction(pos));
        if (pos == tail) {
            tail = instruction;
        } else {
            pos.next.previous = instruction;
        }
        instruction.next = pos.next;
        instruction.previous = pos;
        pos.next = instruction;
    }

    /**
     * Insert an instruction into the instruction sequence represented by this
     * IR. The instruction sequence must not already contain
     * <code>instruction</code> but must contain <code>pos</code>.
     *
     * @param instruction the instruction to be inserted
     * @param pos         the instruction that <code>instruction</code> will be
     *                    inserted before
     */
    public void insertBefore(Instruction instruction, Instruction pos) {
        Assert.that(!findInstruction(instruction));
        Assert.that(findInstruction(pos));
        if (pos == head) {
            head = instruction;
        } else {
            pos.previous.next = instruction;
        }
        instruction.previous = pos.previous;
        instruction.next = pos;
        pos.previous = instruction;
    }

    /**
     * Remove an instruction from the instruction sequence represented by this
     * IR. The instruction sequence must already contain
     * <code>instruction</code>.
     *
     * @param instruction  the instruction to be removed
     */
    public void remove(Instruction instruction) {
        Assert.that(findInstruction(instruction));
        if (instruction == head) {
            head = instruction.next;
        }
        if (tail == instruction) {
            tail = instruction.previous;
        }
        if (instruction.previous != null) {
            instruction.previous.next = instruction.next;
        }
        if (instruction.next != null) {
            instruction.next.previous = instruction.previous;
        }
        instruction.next = instruction.previous = null;
//Assert.that(!findInstruction(instruction));
//Assert.that(findInstruction(head));
//Assert.that(findInstruction(tail));
    }

    /**
     * Gets the first instruction in the IR.
     *
     * @return the first instruction in the IR
     */
    public Instruction getHead() {
        return head;
    }

    /**
     * Gets the last instruction in the IR.
     *
     * @return the last instruction in the IR
     */
    public Instruction getTail() {
        return tail;
    }
    
    /**
     * Calculate number of instructions in IR.
     */
    public int size() {
        Instruction instr = head;
        int n = 0;
        while (instr != null) {
            instr = instr.getNext();
            n++;
        }
        return n;
    }

    /*---------------------------------------------------------------------------*\
     *                                Targets                                    *
    \*---------------------------------------------------------------------------*/

    /**
     * Sets the targets of the method.
     *
     * @param targets the targets of the method
     */
    public void setTargets(Target[] targets) {
        this.targets = targets;
    }

    /**
     * Gets the targets of the method.
     *
     * @return the targets of the method
     */
    public Target[] getTargets() {
        return targets;
    }

    /*---------------------------------------------------------------------------*\
     *                             Exception handlers                            *
    \*---------------------------------------------------------------------------*/

    /**
     * Adds an exception handler to the exception handler table for this IR.
     *
     * @param handler  the exception handler to add
     */
    void addExceptionHandler(IRExceptionHandler handler) {
        if (exceptionHandlers == null) {
            exceptionHandlers = new SquawkVector();
        }
        exceptionHandlers.addElement(handler);
    }

    /**
     * Gets an enumeration over the exception handlers of this IR.
     *
     * @return an enumeration over the exception handlers of this IR or
     *         <code>null</code> if this IR has no exception handlers
     */
    public Enumeration getExceptionHandlers() {
        if (exceptionHandlers == null) {
            return null;
        }
        return exceptionHandlers.elements();
    }

    /*---------------------------------------------------------------------------*\
     *                          Instruction iteration                            *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets an enumeration over the sequence of instructions in this IR.
     * The returned enumeration is invalidated if the list of instructions is
     * modified in anyway.
     *
     * @param   reverse  if true, the returned enumerator traverses the
     *                   instructions in reveres order
     * @return  an enumeration over the instructions in this IR
     */
    public Enumeration getInstructions(final boolean reverse) {
        return new Enumeration() {
            Instruction instruction = reverse ? tail : head;
            public boolean hasMoreElements() {
                return instruction != null;
            }
            public Object nextElement() {
                if (instruction == null) {
                    throw new NoSuchElementException();
                }
                Instruction result = instruction;
                instruction = reverse ? instruction.previous : instruction.next;
                return result;
            }
        };
    }


    /*---------------------------------------------------------------------------*\
     *                         Bytecode transformation                           *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets the transformed method represented by this IR. This can only be
     * called once the bytecode transformation has completed.
     *
     * @param method     the method owning this code
     * @param codeParser the code parser for the method
     * @param locals     the types of the locals (but not the parameters)
     * @param clearedSlots the number of local variables (after the first one) that need clearing
     * @param maxStack   the maximum number of stack words required
     * @param classFile  the class file
     * @return           the method body represented by this IR
     */
    public MethodBody getMethodBody(
                                     Method     method,
                                     CodeParser codeParser,
                                     Klass[]    locals,
                                     int        clearedSlots,
                                     int        maxStack,
                                     ClassFile  classFile
                                   ) {
        /*
         * Emit the bytecodes.
         */
        InstructionEmitter emitter = new InstructionEmitter(this, classFile, method, clearedSlots);
        emitter.emit();
        byte[] code = emitter.getCode();
        byte[] typeMap = null;
/*if[TYPEMAP]*/
        typeMap = emitter.getTypeMap();
/*end[TYPEMAP]*/

        /*
         * Build exception handler table
         */
        ExceptionHandler[] exceptionTable = null;
        if (exceptionHandlers != null && !exceptionHandlers.isEmpty()) {
            int lth = exceptionHandlers.size();
            exceptionTable = new ExceptionHandler[lth];
            for (int i = 0 ; i < lth ; i++) {
                IRExceptionHandler irHandler = (IRExceptionHandler)exceptionHandlers.elementAt(i);
                exceptionTable[i] = new ExceptionHandler(irHandler.getEntry().getBytecodeOffset(),
                                                         irHandler.getExit().getBytecodeOffset(),
                                                         irHandler.getCatch().getExceptionBytecodeOffset(),
                                                         irHandler.getCatch().getType()
                                                         );
            }
        }

        /*
         * If the target method needs a CLASS_CLINIT then maxStack must be at least 1.
         */
        if (maxStack == 0 && method.requiresClassClinit()) {
            maxStack++;
        }

        /*
         * Create the method body
         */
        MethodBody body = new MethodBody(
                                          method,
                                          maxStack,
                                          locals,
                                          exceptionTable,
                                          codeParser.getLineNumberTable(code),
                                          codeParser.getLocalVariableTable(),
                                          code,
                                          typeMap,
                                          Translator.REVERSE_PARAMETERS,
                                          inlinedSuperConstructor
                                        );

        return body;
    }
    

    /*---------------------------------------------------------------------------*\
     *                             IR Properties                                 *
    \*---------------------------------------------------------------------------*/

    /**
     * True IFF this is a constructor that inlined java.lang.Object.<init>. Used by squawk verifier.
     */
    private boolean inlinedSuperConstructor;

    /**
     * True IFF this is a static method that certainly calls CLinit, or an instance method that does a nullcheck .
     */
    private boolean willDoInitCheck;
    
   /**
     * The number if non-pseudo IR nodes.
     */
    private int optimizedIRLength = -1;
    
    /**
     * Does the method return leaving data on the operand stack? LEgal, but harder to inline.
     */
    private boolean nonEmptyStackOnReturn = false;
    
    /**
     * Does this intance method change the value of "this".
     */
    private boolean changesThis = false;
    
    /**
     * True IFF this is a static method that certainly calls CLinit via a GetStatic, PutStatic
     * (and perhaps InvokeStatic). Set after inlining and optimizing.
     *
     * WARNING: The point of this is to allow inlining of static methods that need clinit called,
     * by reasoning that the GetStatic (etc) will call it if needed. But we can NOT eliminate generally
     * a required CLINIT at static method entry, becuase the GetStatic/PutStatics will be transformed
     * into THIS_GETSTATIC (etc), which might assume the the class was initialized.
     *
     * in a method body
     */
    public boolean willDoInitCheck() {
        Assert.that(optimizedIRLength >= 0);
        return willDoInitCheck;
    }
    
    /**
     * Set the property.  Set after inlining and optimizing.
     */
    private void setWillDoInitCheck(boolean value) {
        willDoInitCheck = value;
    }

    /**
     * True IFF this is a constructor that inlined java.lang.Object.<init>. Used by squawk verifier.
     */
    public boolean getInlinedSuperConstructor() {
        Assert.that(optimizedIRLength >= 0);
        return inlinedSuperConstructor;
    }
    
    /**
     * Set true IFF this is a constructor that inlined java.lang.Object.<init>. Used by squawk verifier.
     */
    public void setInlinedSuperConstructor() {
        Assert.that(!inlinedSuperConstructor);
        inlinedSuperConstructor = true;
    }
    
    /**
     * Return the number of non-pseudo nodes. This is a cache of size(false).
     */
    public int optimizedIRLength() {
        Assert.that(optimizedIRLength >= 0);
        Assert.that(optimizedIRLength == size(false));
        return optimizedIRLength;
    }
    
    /** 
     * @return true if the IR has been inlined into (if possible) and optimized, and properties set.
     */
    public boolean hasBeenOptimized() {
        return (optimizedIRLength >= 0);
    }
    
    /**
     * Called when we notice that a method returns leaving items (besides the return value) on the stack.
     */
    public void setNonEmptyStackOnReturn() {
        nonEmptyStackOnReturn = true;
    }
    
    /*
     * Does this method return leaving items (besides the return value) on the stack.
     *
     * @return true if so.
     */
    public boolean getNonEmptyStackOnReturn() {
        return nonEmptyStackOnReturn;
    }
    
    /*
     * Does this method set the "this" variable.
     *
     * @return true if so.
     */
    public boolean changesThis() {
        return changesThis;
    }
    
    /*
     * Set changesThis. Called by IRBuilder.
     */
    public void setChangesThis() {
        changesThis = true;
    }
            
    /**
     * Analyze <code>ir</code> after inlining and optimization.
     * If the IR begins with a getStatic, PutStatic, or InvokeStatic of the same class as the method's class
     * then we can assume that the method doesn't need a CLINIT when it's inlined.
     * Which means that it may be possible to inline <code>method</code>. 
     * Note that instance field getters also imply that the class is initialized, or that 
     * a NullPointerException will occur. <p><p>
     * WARNING: Can't use this to omit the CLINIT of a *non-inlined* method!
     *
     * Note that only iterates over a subset of the IR.
     */
    private void analyzeWillDoInitCheckStatic(Method method) {
        Assert.that(method.isStatic());
        Assert.that(!willDoInitCheck);
        Instruction instruction = getHead();
        
        while (instruction != null) {
            if (instruction instanceof StoreLocal ||
                instruction instanceof LoadLocal ||
                instruction instanceof PseudoInstruction || // inlines, positions, even try is OK
                instruction instanceof Constant) {
                // does NOT change willDoInitCheck
           } else if (instruction.doesImplicitClinitOf() == method.getDefiningClass()) {
                setWillDoInitCheck(true);
                break;
            } else {
                break; // failed!
            }
            instruction = instruction.getNext();
        }
    }
    
    /**
     * Analyze <code>ir</code> after inlining and optimization.
     * If the IR begins with a getField, PutField, or InvokeVirtual on "this"
     * then we can assume that the method doesn't need a nullcheck when it's inlined.
     * Which means that it may be possible to inline <code>method</code>.
     *
     * Note that only iterates over a subset of the IR.
     */
    private void analyzeWillDoInitCheckInstance(Method method) {
        Assert.that(!method.isStatic());
        Assert.that(!willDoInitCheck);

        if (changesThis()) {
            // if "this" is changed, then we can't really tell what is going on.
            return;
        }
        
        Instruction instruction = getHead();
        while (instruction != null) {
            if (instruction instanceof StoreLocal ||
                instruction instanceof LoadLocal ||
                instruction instanceof PseudoInstruction ||// inlines, positions, even try is OK
                instruction instanceof Constant) {
                // does NOT change willDoInitCheck
            } else {
                StackProducer nullCheckedObject = instruction.doesImplicitNullCheckOn();
                if ((nullCheckedObject  instanceof LoadLocal) &&
                    ((LoadLocal)nullCheckedObject).isParam0()) {
                        setWillDoInitCheck(true);
                    }
                break;
            }
            instruction = instruction.getNext();
        }
    }
    
    public void setProperties(Method method) {
        // If this method needs a CLINIT (eg. its a static of a class with a CLINIT)
        // Check if one of the first instructions implicitly calls CLININT anyway.
        if (method.isStatic()) {
            if (method.requiresClassClinit()) {
                analyzeWillDoInitCheckStatic(method);
            }
        } else {
            analyzeWillDoInitCheckInstance(method);
        }
        
        int numNodes = 0;
        for (Instruction insn = head; insn != null; insn = insn.next) {
            if (!(insn instanceof PseudoInstruction)) {
                numNodes++;
            }
        }
        
        optimizedIRLength = numNodes;
    }
    
    public void verify(boolean allowDuplicates) {
        OperandVerifier.verify(this, allowDuplicates);
        TargetVerifier.verify(this);
    }

}
