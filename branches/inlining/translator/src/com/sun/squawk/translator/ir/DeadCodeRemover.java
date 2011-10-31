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
import com.sun.squawk.translator.Translator;
import com.sun.squawk.util.SquawkHashtable;
import com.sun.squawk.util.SquawkVector;
import com.sun.squawk.util.Tracer;
import com.sun.squawk.util.Assert;
import com.sun.squawk.*;
import java.util.Enumeration;

/**
 * This is responsible for detecting and removing all unreachable code in a method. The basic idea 
 * is to walk over the instructions in the method (both the main body and the exception handlers), 
 * normally going from one instruction to the next, but visiting all of the targets of instructions that 
 * branch. We stop walking when we get to a return or throw instruction.
 *
 * All of the visited instructions are collected into the reachable table. Additionally, all of the instructions
 * are collected into the allCode table for safe keeping.
 *
 * We then walk over the allCode table, deleting instructions from the IR that are not in the reachable table.
 */
public class DeadCodeRemover {
    /**
     * SquawkHashtable of all reachable instructions. This is searched frequently, hence the SquawkHashtable.
     */
    private final SquawkHashtable reachable;
    
    /**
     * A SquawkVector of all of the original instructions in the method.
     * This is kept as a separate collection from, the IR's instruction list, because we are removing instructions 
     * from the IR as we go.
     */
    private final SquawkVector allCode;
    
    /**
     * A TargetVisitor that checks if a given target is still reachable.
     */
    private final FindReachableInstructions finder;
    
    /**
     * The IR of the method we are pruning.
     */
    private final IR ir;
    
    /**
     * The method we are pruning.
     */
    private final Method method;
    
    /**
     * The remover to be used to delete unreachable instructions.
     */
    private final InstructionRemover remover;
    
    /**
     * private tracing flag.
     */
    private final boolean trace;
    
    private final SquawkVector deadProducers;
    
    /**
     * Removes unreachable instructions from a method.
     *
     * @param ir the code to check
     * @param method the <code>Method</code> object of the code to check.
     * @param remover the <code>InstructionRemover</code> to use to remove the dead code.
     */
    public DeadCodeRemover(IR ir, Method method, InstructionRemover remover) {
        reachable = new SquawkHashtable();
        allCode = new SquawkVector();
        finder = new FindReachableInstructions();
        deadProducers = new SquawkVector();
        this.ir = ir;
        this.method = method;
        this.remover = remover;
        this.trace = Translator.TRACING_ENABLED && Translator.isTracing("optimize", method);
    }
    
    /**
     * Utility to delete the instruction, and do appropriate tracing and testing.
     *
     * @param insn the instruction to remove
     */
    private void remove(Instruction insn) {
        boolean removed = remover.remove(insn, true); // allow removal of targetable instructions
        if (insn instanceof StackProducer) {
            deadProducers.addElement(insn);
        }
        Assert.that(removed);
        if (trace) {
            Tracer.traceln("Unreachable so deleted: " + insn);
        }
    }
    
    /**
     * Actually remove the unreachable instructions.
     *
     * @return true if any instructions were removed.
     */
    public boolean removeUnreachableCode() {
        remover.reset();
        
        // Fill in reachable and allCode tables:
        addCode(ir.getHead());
        findReachable(ir.getHead());
        for (Enumeration e = ir.getExceptionHandlers(); e != null && e.hasMoreElements(); ) {
            IRExceptionHandler handler = (IRExceptionHandler)e.nextElement();
            findReachable(handler.getCatch());
        }
        
        // Now delete unreachable:
        int len = allCode.size();
        for (int i = 0; i < len; i++) {
            Instruction insn = (Instruction)allCode.elementAt(i);
            if (insn.isInIR(ir) && !reachable.containsKey(insn)) {
                if (insn instanceof Try) {
                    // note that Try instructions are never targets, but an enclosed Phi may be,
                    // so leave in, attempt cleanup on TryEnd
                } else if (insn instanceof TryEnd) {
                    // check for empty try block, delete only then
                   /*
                    // if we remove the try block, then we need to remove the IRExceptionHandler too.
                    Instruction prev = insn.getPrevious();
                    if (prev instanceof Try) {
                        remove(insn);
                        remove(prev);
                    }*/
                } else if (insn instanceof Position) {
                    if (insn != ir.getTail()) {
                        remove(insn);
                    }
                } else if (insn instanceof InlinedEnd) {
                    InlinedEnd end = (InlinedEnd)insn;
                    if (!reachable.containsKey(end.getInvoke())) {
                        remove(insn);
                    }
                } else {
                    remove(insn);
                }
            }
        }
        
        if (remover.hasChanged()) {
            // cleanup frame info - some stack producers are now gone...
            if (!deadProducers.isEmpty()) {
                CleanUpStackMerges cleaner = new CleanUpStackMerges();
                Instruction insn = ir.getHead();
                while (insn != null) {
                    insn.visit(cleaner);
                    insn = insn.getNext();
                }
            }
            
            if (trace) {
                Translator.trace(method, ir, "After dead code optimizations");
            }
            ir.verify(true);
        }
        
        return remover.hasChanged();
    }
    
    class CleanUpStackMerges implements OperandVisitor {

        public StackProducer doOperand(Instruction instruction, StackProducer operand) {
            if (operand instanceof StackMerge) {
                StackMerge sm = (StackMerge) operand;
                for (int i = 0; i < deadProducers.size(); i++) {
                    StackProducer deadProducer = (StackProducer)deadProducers.elementAt(i);
                    if (sm.contains(deadProducer)) {
                        sm.removeProducer(deadProducer);
                    }
                }
            }
            return operand;
        }
    }
    
    /** 
     * Find reachable code from instructions that target other instructions.
     */
     class FindReachableInstructions implements TargetVisitor {
         
        
        public void doTarget(TargetingInstruction instruction, Target target) {
            findReachable((Instruction)(target.getTargetedInstruction()));
        }
    }
    
    /**
     * Adds all instructions reachable from the given one to the hashtable.
     *
     * @param insn       root instruction to look for reachable instructions from
     * @param reachable  hashtable to which to add all reachable instructions as keys
     */
    private void findReachable(Instruction insn) {
        while (insn != null && !reachable.containsKey(insn)) {
    		reachable.put(insn, insn);
    		if (insn instanceof TargetingInstruction) {
                TargetingInstruction ti = (TargetingInstruction)insn;
                
                // recursively visit the instructions of all of the targets of this TargetingInstruction: 
                ti.visit(finder);
                
    			if (ti.isUnconditional()) {
                    // unconditional goto, do not visit next instruction.
    				break;
    			}
            } else if (insn instanceof Return || insn instanceof Throw) {
                // do not visit next instruction.
                break;
            }
            insn = insn.getNext();
    	}
    }
    
    /**
     * Used to populate the allCode table.
     *
     * @param insn the first instruction in a list of instructions.
     */
    private void addCode(Instruction insn) {
        while (insn != null) {
            Assert.that(!allCode.contains(insn), insn + " is already in allCode");
            allCode.addElement(insn);
            insn = insn.getNext();
    	}
    }
    
}