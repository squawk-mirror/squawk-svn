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

import com.sun.squawk.translator.ir.instr.PseudoInstruction;
import com.sun.squawk.translator.ir.instr.Position;
import com.sun.squawk.translator.ir.instr.StackProducer;
import com.sun.squawk.Method;
import com.sun.squawk.Klass;

/**
 * This is the root of the instruction hierarchy.
 *
 */
public abstract class Instruction {

    /**
     * The value of an undefined offset.
     */
    final static int OFFSETNOTDEFINED = -99999;

    /**
     * Offset in bytes to the start of the instruction in the method. This field
     * starts life as the offset into the Java bytecodes and is used later as the
     * offset into the Squawk bytecodes.
     */
    private int offset = OFFSETNOTDEFINED;

    /**
     * Next instruction in a linked list of instructions. This field is
     * directly manipulated by an {@link IR} instance.
     */
    Instruction next;

    /**
     * Previous instruction in a linked list of instructions. This field is
     * directly manipulated by an {@link IR} instance.
     */
    Instruction previous;

    /**
     * Creates an instruction.
     */
    protected Instruction() {
    }

    /**
     * Gets the instruction immediately after this one in an
     * instruction sequence. The value <code>null</code> will be returned if
     * this is the last instruction in the sequence.
     *
     * @return  the instruction immediately after this one in an
     *          instruction sequence
     */
    public final Instruction getNext() {
        return next;
    }

    /**
     * Gets the instruction immediately before this one in an
     * instruction sequence. The value <code>null</code> will be returned if
     * this is the first instruction in the sequence.
     *
     * @return  the instruction immediately before this one in an
     *          instruction sequence
     */
    public final Instruction getPrevious() {
        return previous;
    }

    /**
     * Does this instruction think it is linked into the IR?
     *
     * @return true if the instruction is linked.
     */
    public final boolean isInIR(IR ir) {
        return next != null || previous != null || this == ir.getHead();
    }

        
    /**
     * Given an instruction, skip instructions until the first non-pseudo instruction is found.
     *
     * @return the first instruction in the IR
     */
    public Instruction skipPseudo() {
        Instruction insn = this;
        while (insn != null && insn instanceof PseudoInstruction) {
            insn = insn.getNext();
        }
        return insn;
    }

   /**
     * Given an instruction, skip instructions until the first non-skipPosition instruction is found.
     *
     * @return the first instruction in the IR
     */
    public Instruction skipPosition() {
        Instruction insn = this;
        while (insn != null && insn instanceof Position) {
            insn = insn.getNext();
        }
        return insn;
    }

    /**
     * Sets the offset in bytes from the start of the machine code sequence.
     *
     * @param offset the machine code offset
     */
    public final void setBytecodeOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Gets the offset in bytes from the start of the mechine code sequence.
     *
     * @return  the machine code offset
     */
    public final int getBytecodeOffset() {
        return offset;
    }

    /**
     * Gets the size of the machine code sequence.
     *
     * @return the machine code size
     */
    public int getSize() {
        return 0;
    }

    /**
     * Calculate the size of the machine code sequence.
     *
     * @return the machine code size
     */
    public int calculateSize() {
        return getSize();
    }

    /**
     * Get a constant object used by the instruction. This object will be placed into
     * the object table of the enclosing class.
     *
     * @return the object or null if there is none
     */
    public Object getConstantObject() {
        return null;
    }

    /**
     * Determines if this instruction will (potentially) result in a call to
     * Java code or is a backward branch.
     *
     * @return  true if this instruction will (potentially) result in a call
     *          to Java code or is a backward branch.
     */
    public boolean constrainsStack() {
        return false;
    }

    /**
     * Determines if execution of this instruction may cause a garbage collection.
     * This will be true if the instruction is an invoke or any other instruction
     * that results in a call to Java code as any call may require the stack to be
     * extended which in turn requires allocation of memory. It will also be true
     * of instructions which may result in an exception being raised.
     *
     * @param isStatic true if the instruction is in a static method
     * @return true if this instruction may cause a garbage collection
     */
    public boolean mayCauseGC(boolean isStatic) {
        return constrainsStack();
    }

    /**
     * Entry point for an InstructionVisitor.
     *
     * @param visitor  the InstructionVisitor object
     */
    public abstract void visit(InstructionVisitor visitor);

    /**
     * Entry point for an OperandVisitor. The operands must be visited in the
     * order that they were pushed onto the stack.
     *
     * @param visitor  the OperandVisitor object
     */
    public void visit(OperandVisitor visitor) {}
    
    /**
     * An instruction is idempotent if it and it's inputs have no side effects beyond
     * leaving a result on the operand stack. If analysis shows that there is no need
     * for the result, an idempotent instruction (and it's operands recursively) may
     * be eliminated.
     *
     * @return true if the instruction and it's operands have no side effects.
     */
    public boolean isIdempotent(IR ir, Method caller) {
        return false;
    }
    
    /**
     *
     * @return the StackProducer that will be implcitly nullchecked, or null
     */
    public StackProducer doesImplicitNullCheckOn() {
        return null;
    }
    
    /**
     *
     * @return the klass that will be implcitly initialized, or null
     */
    public Klass doesImplicitClinitOf() {
        return null;
    }
    
    public String toString() {
        int offset = getBytecodeOffset();
        String shortClassName = getClass().getName();
        int i = shortClassName.lastIndexOf('.');
        shortClassName = shortClassName.substring(i+1);
        
        return offset + ":" + shortClassName;
    }

}
