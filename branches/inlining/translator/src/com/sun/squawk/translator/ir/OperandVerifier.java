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
import com.sun.squawk.translator.ir.instr.LocalVariable;
import com.sun.squawk.util.Assert;
import com.sun.squawk.util.SquawkHashtable;
import com.sun.squawk.*;

/**
 * This debugging class asserts that every operand of every instruction
 * is in the IR, and that every producer is the operand to only one instruction.
 */
public class OperandVerifier implements OperandVisitor, StackMerge.ProducerVisitor {
    private final IR ir;
    
    private SquawkHashtable producerToConsumerMap;
    
    public OperandVerifier(IR ir, boolean checkForDuplicates) {
        this.ir = ir;
        if (checkForDuplicates) {
            this.producerToConsumerMap = new SquawkHashtable();
        }
    }
    
    /**
     * @{iniheritdoc}
     */
    public boolean visit(StackProducer producer) {
        if (producer instanceof StackMerge) {
            ((StackMerge)producer).visitProducers(this);
        } else {
        Assert.that(ir.findInstruction(producer), producer + ", operand of stackmerge is not in IR");
        }
        return true;
    }
    
    /**
     * @{iniheritdoc}
     */
    public StackProducer doOperand(Instruction insn, StackProducer operand) {
        if (producerToConsumerMap != null) {
            Instruction oldConsumer = (Instruction)producerToConsumerMap.get(operand);
            Assert.that(operand.isDuped() || oldConsumer == null, operand + " is already being consumed by " + oldConsumer + " so can't be operand of " + insn);
            producerToConsumerMap.put(operand, insn);
        }
        
        if (operand instanceof StackMerge) {
            ((StackMerge)operand).visitProducers(this);
        } else {
            if (!ir.findInstruction(operand)) {
                Assert.that(ir.findInstruction(operand), operand + ", operand of " + insn + " is not in IR");
                if (operand instanceof LocalVariable) {
                    System.out.println("     on local variable: " + ((LocalVariable)operand).getLocal());
                }
            }
        }
        return operand;
    }
    
    /**
     * Assert.that every operand of every instruction in <code>ir</code>
     * are themselves in instructions in <code>ir</code>. Also checks that all targets listed in the IR
     * refer to instructions still in the IR.
     * This check is disabled if DEBUG_CODE_ENABLED is false.
     *
     * @param ir the complete ir for a method to check.
     */
    public static void verify(IR ir, boolean checkForDuplicates) {
/*if[DEBUG_CODE_ENABLED]*/
        Instruction curr = ir.getHead();
        OperandVisitor visitor = new OperandVerifier(ir, checkForDuplicates);
        
        try {
            while (curr != null) {
                curr.visit(visitor);
                curr = curr.getNext();
            }
        } catch (RuntimeException e) {
            System.out.println("********** OperandVerifier error in:");
            new InstructionTracer(ir).traceAll();
            throw e;
        }
/*end[DEBUG_CODE_ENABLED]*/
    }
}
