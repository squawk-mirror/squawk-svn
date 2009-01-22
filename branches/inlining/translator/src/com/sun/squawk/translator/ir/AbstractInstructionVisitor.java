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

abstract public class AbstractInstructionVisitor implements InstructionVisitor {
	/**
	 * {@inheritDoc}
	 */
	public void doArithmeticOp(ArithmeticOp insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doArrayLength(ArrayLength insn) {
	
	}

	/**
	 * {@inheritDoc}
	 */
	public void doArrayLoad(ArrayLoad insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doArrayStore(ArrayStore insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doBranch(Branch insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doCheckCast(CheckCast insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doConversionOp(ConversionOp insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doComparisonOp(ComparisonOp insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doTry(Try insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doTryEnd(TryEnd insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doIf(If insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doIfCompare(IfCompare insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doIncDecLocal(IncDecLocal insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doInstanceOf(InstanceOf insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doFindSlot(FindSlot insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doInvokeSlot(InvokeSlot insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doInvokeStatic(InvokeStatic insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doInvokeSuper(InvokeSuper insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doInvokeVirtual(InvokeVirtual insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doConstant(Constant insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doCatch(Catch insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doGetField(GetField insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doGetStatic(GetStatic insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doLoadLocal(LoadLocal insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doLookupSwitch(LookupSwitch insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doMonitorEnter(MonitorEnter insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doMonitorExit(MonitorExit insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doNegationOp(NegationOp insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doNewArray(NewArray insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doNewDimension(NewDimension insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doNew(New insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doPhi(Phi insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doPop(Pop insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doPosition(Position insn) {
	
	}
    
    /**
     * {@inheritDoc}
     */
    public void doInlinedInvoke(InlinedInvoke instruction) {

    }
    
    /**
     * {@inheritDoc}
     */
    public void doInlinedEnd(InlinedEnd instruction) {

    }
	
	/**
	 * {@inheritDoc}
	 */
	public void doReturn(Return insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doPutField(PutField insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doPutStatic(PutStatic insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doStoreLocal(StoreLocal insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doStackMerge(StackMerge insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doStackOp(StackOp insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doTableSwitch(TableSwitch insn) {
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void doThrow(Throw insn) {
	
	}
}