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

import com.sun.squawk.translator.ir.instr.TargetingInstruction;
import com.sun.squawk.translator.ir.instr.TargetedInstruction;
import com.sun.squawk.util.Assert;
import com.sun.squawk.*;


/**
 * This debugging class asserts that every targeted instruction of every targeting instruction is
 * conatined in the IR.
 */
public class TargetVerifier implements TargetVisitor {
    private final IR ir;
    
    public TargetVerifier(IR ir) {
        this.ir = ir;
    }
    
    /**
     * @{iniheritdoc}
     */
    public void doTarget(TargetingInstruction insn, Target target) {
        Instruction ti = (Instruction)target.getTargetedInstruction();
        Assert.that(ti != null && ir.findInstruction(ti), ti + ", target of " + insn + " is not in IR");
    }
    
    /**
     * Assert.that every operand of every instruction in <code>ir</code>
     * are themselves in instructions in <code>ir</code>. Also checks that all targets listed in the IR
     * refer to instructions still in the IR.
     * This check is disabled if DEBUG_CODE_ENABLED is false.
     *
     * @param import the complete if for a method to check.
     */
    public static void verify(IR ir) {
/*if[DEBUG_CODE_ENABLED]*/
        try {
            Instruction curr = ir.getHead();
            TargetVerifier visitor = new TargetVerifier(ir);
            while (curr != null) {
                if (curr instanceof TargetingInstruction) {
                    ((TargetingInstruction)curr).visit(visitor);
                }
                curr = curr.getNext();
            }

        } catch (RuntimeException e) {
            System.out.println("********** TargetVerifier error in:");
            new InstructionTracer(ir).traceAll();
            throw e;
        }
/*end[DEBUG_CODE_ENABLED]*/
    }
}
