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

import com.sun.squawk.Method;
import com.sun.squawk.Field;
import com.sun.squawk.Klass;
import com.sun.squawk.translator.ir.instr.*;

/**
 * Class that walks all instructions, and notes the classes, fields, and methods that are referenced by the instructions.
 */
abstract public class ReferenceRecordingVisitor extends AbstractInstructionVisitor {
    
    protected abstract void recordKlass(Klass klass);
    
    protected abstract void recordMethod(Method method);
    
    protected abstract void recordField(Field field);
    
    /**
     * {@inheritDoc}
     */
    public void doCheckCast(CheckCast insn) {
        recordKlass(insn.getType());
    }
    
    /**
     * {@inheritDoc}
     */
    public void doConversionOp(ConversionOp insn) {
        // nothing? should only be dealing with primitive ops.
    }
    
    /**
     * {@inheritDoc}
     */
    public void doInstanceOf(InstanceOf insn) {
        recordKlass(insn.getCheckType());
    }
    
    /**
     * {@inheritDoc}
     */
    public void doInvokeSlot(InvokeSlot insn) {
        recordMethod(insn.getMethod());
    }
    
    /**
     * {@inheritDoc}
     */
    public void doInvokeStatic(InvokeStatic insn) {
        recordMethod(insn.getMethod());
    }
    
    /**
     * {@inheritDoc}
     */
    public void doInvokeSuper(InvokeSuper insn) {
        recordMethod(insn.getMethod());
    }
    
    /**
     * {@inheritDoc}
     */
    public void doInvokeVirtual(InvokeVirtual insn) {
        recordMethod(insn.getMethod());
    }
    
    /**
     * {@inheritDoc}
     */
    public void doCatch(Catch insn) {
        recordKlass(insn.getType());
    }
    
    /**
     * {@inheritDoc}
     */
    public void doGetField(GetField insn) {
        recordField(insn.getField());
    }
    
    /**
     * {@inheritDoc}
     */
    public void doGetStatic(GetStatic insn) {
        recordField(insn.getField());
    }
    
    /**
     * {@inheritDoc}
     */
    public void doNewArray(NewArray insn) {
        recordKlass(insn.getType());
    }
    
    /**
     * {@inheritDoc}
     */
    public void doNewDimension(NewDimension insn) {
        recordKlass(insn.getType());
        
    }
    
    /**
     * {@inheritDoc}
     */
    public void doNew(New insn) {
        recordKlass(insn.getType());
        
    }
    
    /**
     * {@inheritDoc}
     */
    public void doPutField(PutField insn) {
        recordField(insn.getField());
    }
    
    /**
     * {@inheritDoc}
     */
    public void doPutStatic(PutStatic insn) {
        recordField(insn.getField());
    }
    
}