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
import com.sun.squawk.*;

/**
 * An instance of <code>InlinedInvoke</code> is a pseudo instruction that
 * represents an invoke that has been inlined by the translator.
 * This is not only useful for debugging, but conveys extra infromation to the verifier,
 * such as the fact that the inlined code was for a constructor, so the TOS should now be considered 
 * initialized.
 *
 * Instruction is neither a stack producer or consumer.
 */
public class InlinedInvoke extends Instruction implements PseudoInstruction {

    /**
     * The method inlined.
     */
    private final Method method;
    
    
    /**
     * Is this a static method that needs clinit called?
     */
    //private boolean requiresClassClinit;   
     
    /**
     * Creates an instance of <code>InlinedInvoke</code> to represent an inlined call.
     *
     * @param  method      the method inlined
     */
    public InlinedInvoke(Method method) {
        this.method = method;
       // this.requiresClassClinit = method.requiresClassClinit();
    }
    
    /**
     * Return if this inlnined invoke needs a to do a class init.
     */
  /*  public boolean requiresClassClinit() {
        return requiresClassClinit;
    }*/
    
    /**
     * Clear the need to do a class inint for this inline (if, for example, 
     * inlining into another method of the class.
     */
/*    public void clearRequiresClassClinit() {
        requiresClassClinit = false;
    } */


    /**
     * Gets the method inlined by this instruction.
     *
     * @return the method inlined by this instruction
     */
    public Method getMethod() {
        return method;
    }
    
    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doInlinedInvoke(this);
    }
}
