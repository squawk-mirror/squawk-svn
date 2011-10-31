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

import com.sun.squawk.translator.ir.instr.StackProducer;
import com.sun.squawk.translator.ir.instr.StackMerge;
import com.sun.squawk.util.Assert;

/**
 * Class used to replace all occurrences of one stack producer with another in a list if instruction in the IR.
 */
public class OperandReplacer implements OperandVisitor {
    
    
    private StackProducer newOperand, oldOperand;
    
    private boolean onceOnly;
    
    private int count;
    
    public OperandReplacer(StackProducer newOperand, StackProducer oldOperand, boolean onceOnly) {
        this.newOperand = newOperand;
        this.oldOperand = oldOperand;
        this.onceOnly = onceOnly;
        this.count = 0;
    }
    
    /**
     * Analyze the operands of insn, replacing the old operand with the new operand.
     * As special case, also analyze the possible producers in a StackMerge.
     */
    public StackProducer doOperand(Instruction insn, StackProducer operand) {
        if (operand instanceof StackMerge) {
            StackMerge merge = (StackMerge)operand;
            merge.replaceProducer(oldOperand, newOperand);
        }
        if (operand == oldOperand && (!onceOnly || count == 0)) {
            count++;
            return newOperand;
        } else {
            return operand;
        }
    }
    
    /**
     * Utility function to start an OperandReplacer on all instructions in the ir list starting
     * from <code>startingFrom</code>
     *
     * @param newOperand the operand to use instead of oldOperand.
     * @param oldoperand the operand to replace.
     * @param startingFrom the first instruction to begin searching from.
     */
    public static void replace(StackProducer newOperand, StackProducer oldOperand,
                               Instruction startingFrom) {
        Assert.that(startingFrom != null);
        Instruction curr = startingFrom;
        OperandVisitor visitor = new OperandReplacer(newOperand, oldOperand, false);
        while (curr != null) {
            curr.visit(visitor);
            curr = curr.getNext();
        }
        
    }
}