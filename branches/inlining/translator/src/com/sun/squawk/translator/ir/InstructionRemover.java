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

import com.sun.squawk.translator.Translator;
import com.sun.squawk.translator.ir.instr.*;
import com.sun.squawk.util.Tracer;
import com.sun.squawk.Klass;


/**
 * This class manages adding and removing instructions. It is careful not to remove instuctions that may be the targets of
 * other instructions. This is relaxed when deleting dead code - it's OK to remove instruction that are only targeted by 
 * other dead code (which is what the DeadCodeRemover checks for).
 */
public class InstructionRemover {
	protected IR ir;
	private boolean changed;
    
    private final boolean trace;
	
	public InstructionRemover(IR ir) {
		this.ir = ir;
		changed = false;
        trace = Tracer.isTracing("optimize");
	}
	
    /**
     * @return true if the remover has added or removed any instructions from the ir.
     */
	public final boolean hasChanged() {
		return changed;
	}
    
    /**
     * Reset the remover's internal state as if it hadn't changed anything (but leave the changes).
     */
    public void reset() {
		changed = false;
	}
	
    /**
     * @param insn the instruction to check
     * @return true if insn is not targetable
     */
    private boolean check(Instruction insn) {
        if (insn instanceof TargetedInstruction || insn instanceof Try || insn instanceof TryEnd ) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Remove the given instruction, unless it is a targetable instruction and 
     * evenTargetable is false.
     *
     * @param insn the instruction to remove
     * @param evenTargetable if true, remove even targetable instructions (used for dead code removal).
     * @return true if the instruction was removed.
     */
	public boolean remove(Instruction insn, boolean evenTargetable) {
		if (evenTargetable || check(insn)) {
            if (trace) {
                if (evenTargetable && !check(insn)) {
                    Tracer.traceln("    Removed (T) " + insn);
                } else {
                    Tracer.traceln("    Removed " + insn);
                }
            }
            ir.remove(insn);
			changed = true;
            return true;
		}
        if (trace) {
            Tracer.traceln("    Couldn't remove " + insn);
        }
        return false;
	}
    
    /**
     * Remove the given instruction, unless it is a targetable instruction.
     *
     * @param insn the instruction to remove
     * @return true if the instruction was removed.
     */
    public final boolean remove(Instruction insn) {
        return remove(insn, false);
    }

    /**
     * Replace oldInsn with newInsn.
     *
     * @param newInsn the new instruction to add
     * @param oldInsn the old instruction to remove
     * @return true if the replace occurred
     */
	public boolean replace(Instruction newInsn, Instruction oldInsn) {
		if (check(oldInsn)) {
			ir.insertBefore(newInsn, oldInsn);
            if (trace) {
                Tracer.traceln("    Replaced " + oldInsn + " with " + newInsn);
            }
            ir.remove(oldInsn);
			changed = true;
            return true;
		}
        if (trace) {
            Tracer.traceln("    Couldn't replace " + oldInsn + " with " + newInsn);
        }
        return false;
	}
	
    /**
     * Add a new instruction
     *
     * @param insn the new instruction to add
     * @param pos the existing instruction to add before
     */
	public void insertBefore(Instruction insn, Instruction pos) {
		ir.insertBefore(insn, pos);
		changed = true;
	}
}