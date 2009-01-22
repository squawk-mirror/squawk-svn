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

package com.sun.squawk.translator.ir.instr;

import com.sun.squawk.translator.ir.*;
import com.sun.squawk.vm.OPC;
import com.sun.squawk.Method;

/**
 * An instance of <code>ArithmeticOp</code> represents an binary arithmetic
 * operation that pops two values off the operand stack and pushes the
 * result of the operation.
 *
 */
public final class ArithmeticOp extends StackProducer {

    /**
     * The left operand of the operation.
     */
    private StackProducer left;

    /**
     * The left operand of the operation.
     */
    private StackProducer right;

    /**
     * The Squawk opcode corresponding to this operation.
     */
    private final int opcode;

    /**
     * Creates a <code>ArithmeticOp</code> instance representing a binary
     * arithmetic operation.
     *
     * @param   left    the left operand of the operation
     * @param   right   the right operand of the operation
     * @param   opcode  the Squawk opcode corresponding to the operation
     */
    public ArithmeticOp(StackProducer left, StackProducer right, int opcode) {
        super(left.getType());
        this.left   = left;
        this.right  = right;
        this.opcode = opcode;
    }

    /**
     * Gets the left operand of this arithmetic operation.
     *
     * @return the left operand of this arithmetic operation
     */
    public StackProducer getLeft() {
        return left;
    }

    /**
     * Gets the right operand of this arithmetic operation.
     *
     * @return the right operand of this arithmetic operation
     */
    public StackProducer getRight() {
        return right;
    }

    /**
     * Gets the Squawk opcode corresponding this arithmetic operation.
     *
     * @return the Squawk opcode corresponding this arithmetic operation
     */
    public int getOpcode() {
        return opcode;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doArithmeticOp(this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean mayCauseGC(boolean isStatic) {
        return opcode == OPC.DIV_I || opcode == OPC.DIV_L || opcode == OPC.REM_I || opcode == OPC.REM_L;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        left  = visitor.doOperand(this, left);
        right = visitor.doOperand(this, right);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isIdempotent(IR ir, Method caller) {
         if (opcode == OPC.DIV_I || opcode == OPC.DIV_L || opcode == OPC.REM_I || opcode == OPC.REM_L) {
             // might cause divide by zero exception
             return false;
         } else {
             return left.isIdempotent(ir, caller) && right.isIdempotent(ir, caller);
         }
    }
    
    /**
	 * Return true if we can swap the operands A & B. Allows
     * optimizations that reduce munging the stack.
	 */
    public boolean isCommutative() {
        switch (opcode) {
            case OPC.ADD_I:
            case OPC.ADD_L:
            case OPC.MUL_I:
            case OPC.MUL_L:
            case OPC.AND_I:
            case OPC.AND_L:
            case OPC.OR_I:
            case OPC.OR_L:
            case OPC.XOR_I:
            case OPC.XOR_L:
                return true;
            default:
                return false;
        }
    }
    
    boolean isConstantValue(StackProducer operand, Object val) {
        return (operand instanceof Constant) && ((Constant)operand).getValue().equals(val);
    }
    
    /**
     * Compare both left then right operands to the contant <code>val></code>.
     * @param val the constant to check for
     * @return -1 if the left operand equals the constant, 1 if the right if operand equals the constant, or zero if neother do.
     */
    int isOpConstantValue(Object val) {
        if (isConstantValue(getLeft(), val)) {
            return 1;
        } else if (isConstantValue(getRight(), val)) {
            return -1;
        }
        return 0;
    }
    
    private static final Integer INT_ZERO = new Integer(0);
    private static final Integer INT_ONE = new Integer(1);
    private static final Integer INT_MONE = new Integer(-1);
    private static final Integer INT_FFFF = new Integer(0xFFFF);
    private static final Long LONG_ZERO = new Long(0);
    private static final Long LONG_ONE = new Long(1);
    private static final Long LONG_MONE = new Long(-1);
    private static final Long LONG_FFFFFFFF = new Long(0xFFFFFFFF);
    
    /**
     * determine if operation is a simple identitfy function on one of it's operands
     *
     * @return -1 if operation is identify function on LEFT operand<br>
     *            1 if operation is identify function on RIGHT operand<br>
     *            0 otherwise
     */
    public int isIdentity() {
        switch (opcode) {
            case OPC.ADD_I:
                return isOpConstantValue(INT_ZERO);
            case OPC.ADD_L:
                return isOpConstantValue(LONG_ZERO);
            case OPC.MUL_I:
                return isOpConstantValue(INT_ONE);
            case OPC.MUL_L:
                return isOpConstantValue(LONG_ONE);
            case OPC.AND_I:
                return isOpConstantValue(INT_MONE);
            case OPC.AND_L:
                return isOpConstantValue(LONG_MONE);
            case OPC.OR_I:
                return isOpConstantValue(INT_ZERO);
            case OPC.OR_L:
                return isOpConstantValue(LONG_ZERO);
            case OPC.XOR_I:
                return isOpConstantValue(INT_ZERO);
            case OPC.XOR_L:
                return isOpConstantValue(LONG_ZERO);
            case OPC.DIV_I:
                 if (isConstantValue(getRight(), INT_ONE)) {
                    return -1;
                }
                return 0;
            case OPC.DIV_L:
                 if (isConstantValue(getRight(), LONG_ONE)) {
                    return -1;
                }
                return 0;
            case OPC.SHL_I:
            case OPC.SHR_I:
            case OPC.USHR_I:
                if (isConstantValue(getRight(), INT_ZERO)) {
                    return -1;
                }
                return 0;
            case OPC.SHL_L:
            case OPC.SHR_L:
            case OPC.USHR_L:
                if (isConstantValue(getRight(), LONG_ZERO)) {
                    return -1; 
                }
                return 0;
            default:
                return 0;
        }
    }
    
    private static final Integer[] powersOf2Int = {
        new Integer(1),
        new Integer(2),
        new Integer(4),
        new Integer(8),
        new Integer(16),
        new Integer(32),
        new Integer(64)
    };
    
    private static final Long[] powersOf2Long = {
        new Long(1),
        new Long(2),
        new Long(4),
        new Long(8),
        new Long(16),
        new Long(32),
        new Long(64)
    };
    
    /**
     * if a mul can be turned into a shift left, then will return a non-zero number encoding the number 
     * to shift and the operand of the mul to be shifted.
     */
    public int isReducableMul() {
        switch (opcode) {
            case OPC.MUL_I: {
                for (int i = 1; i < powersOf2Int.length; i++) {
                   int dir = isOpConstantValue(powersOf2Int[i]);
                   if (dir != 0) {
                       return dir * i;
                   }
                }
                return 0;
            }
            case OPC.MUL_L: {
                for (int i = 1; i < powersOf2Long.length; i++) {
                   int dir = isOpConstantValue(powersOf2Long[i]);
                   if (dir != 0) {
                       return dir * i;
                   }
                }
                return 0;
            }
        default:
                return 0;
        }
    }
    
    /**
     * A op AND 0xFFFF (or the reverse) can be turned into an i2c instruction.
     * 
     * Note that the similar AND_L case can't be handled easily because AND_L leaves 64 bits 
     * on the stack, but l2i leaves only 32 bits.
     *
     * @return -1 if operation is identify function on LEFT operand<br>
     *            1 if operation is identify function on RIGHT operand<br>
     *            0 otherwise
     */
    public int isReducableAnd() {
        if (opcode == OPC.AND_I) {
            return isOpConstantValue(INT_FFFF);
        /* } if (opcode == OPC.AND_L) {
            return isOpConstantValue(LONG_FFFFFFFF);
         * */
        } else {
            return 0;
        }
    }
    
    /**
     * determine if operation is a simple constant function (because one of it's operands is
     * a constant).
     *
     * @return an object representing the constant value, or null
     */
    public Object isConstantResult() {
        switch (opcode) {
            case OPC.MUL_I:
                if (isOpConstantValue(INT_ZERO) != 0) {
                    return INT_ZERO;
                }
                return null;
            case OPC.MUL_L:
                if (isOpConstantValue(LONG_ZERO) != 0) {
                    return LONG_ZERO;
                }
                return null;
            case OPC.AND_I:
                if (isOpConstantValue(INT_ZERO) != 0) {
                    return INT_ZERO;
                }
                return null;
            case OPC.AND_L:
                if (isOpConstantValue(LONG_ZERO) != 0) {
                    return LONG_ZERO;
                }
                return null;
            case OPC.OR_I:
                if (isOpConstantValue(INT_MONE) != 0) {
                    return INT_MONE;
                }
                return null;
            case OPC.OR_L:
                if (isOpConstantValue(LONG_MONE) != 0) {
                    return LONG_MONE;
                }
                return null;
            case OPC.REM_I:
                 if (isConstantValue(getRight(), INT_ONE)) {
                    return INT_ZERO;
                }
                return null;
            case OPC.REM_L:
                 if (isConstantValue(getRight(), LONG_ONE)) {
                    return LONG_ZERO;
                }
                return null;
            case OPC.SHL_I:
            case OPC.SHR_I:
            case OPC.USHR_I:
                if (isConstantValue(getLeft(), INT_ZERO)) {
                    return INT_ZERO;
                }
                return null;
            case OPC.SHL_L:
            case OPC.SHR_L:
            case OPC.USHR_L:
                if (isConstantValue(getLeft(), LONG_ZERO)) {
                    return LONG_ZERO;
                }
                return null;
            default:
                return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {        
        return super.toString() + " = " + com.sun.squawk.vm.Mnemonics.getMnemonic(opcode);
    }
    
}