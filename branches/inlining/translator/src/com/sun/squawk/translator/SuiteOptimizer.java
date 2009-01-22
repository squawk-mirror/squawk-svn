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

package com.sun.squawk.translator;

import java.util.Enumeration;
import com.sun.squawk.util.Assert;
import com.sun.squawk.translator.ir.*;
import com.sun.squawk.translator.ir.instr.*;
import com.sun.squawk.util.Tracer;
import com.sun.squawk.*;

/**
 * The SuiteOptimizer is responsible for 
 * bytecode optimization and inlining for all of the methods of a suite.
 */
public class SuiteOptimizer {
    
    Translator translator;
    
    /** Creates a new instance of SuiteOptimizer */
    public SuiteOptimizer(Translator translator) {
        this.translator = translator;
    }
    
    /**
     * Given a <code>Method</code>, look up the <ocde>Code</code>
     * object.
     *
     * @param method the method to lookup
     * @return the <code>Code</code> for the method.
     */
    private Code getMethodCode(Method method) {
        ClassFile cf = translator.lookupClassFile(method.getDefiningClass());
        if (cf == null) {
            return null;
        }
        
        if (method.isStatic()) {
            int len = cf.getStaticMethodCount();
            for (int i = 0; i < len; i++) {
                Code c = cf.getStaticMethod(i);
                if (c != null && method.equals(c.getMethod())) {
                    return c;
                }
            }
        } else {
            int len = cf.getVirtualMethodCount();
            for (int i = 0; i < len; i++) {
                Code c = cf.getVirtualMethod(i);
                if (c != null && method.equals(c.getMethod())) {
                    return c;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns true if the caller can tell statically which actual method
     * will be called.
     *
     * @param m the callee
     * @aparm invokeSuper pass true if the caller is using an InvokeSuper instruction.
     * @return true if the method might be inlinable.
     */
    private boolean isMethodResolvable(Method m, MethodDB.Entry mw, boolean invokeSuper) {
        if (m.isConstructor()) {
            Translator.numConstructors++;
            return true;
        }
        
        // synchronized methods are OK to inline, because synchronization is explicit in bytecodes, not hidden in invoke.
        
        if (m.isStatic() || m.isFinal() || m.isPrivate()) {
            Translator.numSimple++;
            return true;
        }
        
        if (m.getDefiningClass().isFinal()) {
            Translator.numClassFinal++;
            return true;
        }
        
        if (invokeSuper) {
            Translator.numSuper++;
            return true;
        }
        
        if (!m.isAbstract() && Arg.get(Arg.INLINE_NEVER_OVERRIDDEN).getBool() && translator.methodDB.isNeverOverriden(mw)) {
            Translator.numEffectivelyFinal++;
            return true;
        }
        
if (!m.isAbstract() && translator.methodDB.isNeverOverriden(mw)) {
    Tracer.traceln("Can't inline " + m + " because it's not final, even though it's not overridden.");
}
        
        return false;
    }
    
    /**
     * Returns true if the callee method can plausibly be inlined,
     * without looking at the IR of the callee.
     *
     * @param m the callee
     * @aparm invokeSuper pass true if the caller is using an InvokeSuper instruction.
     * @return true if the method might be inlinable.
     */
    private boolean isMethodInlinable(Method m, MethodDB.Entry mw, boolean invokeSuper) {
        if (m.isNotInlined() || m.isNative()) {
            return false;
        }
        return isMethodResolvable(m, mw, invokeSuper);
    }
    
    /**
     * Do the actual inlining of the callee into the caller.
     *
     * @param classFile the ClassFile that defines <code>code</code>
     * @param callerCode the caller's <code>Code</code> object that conatins a call to be inlined.
     * @param callerMethod the Method corresponding to <code>code</code>.
     * @param calleeCode the callee's <code>Code</code> object to be inlined.
     * @param insertBefore the instruction before the call to inline
     * @param inv the invoke instruction to be inlined
     */
    private void doInlining(ClassFile classFile, Code callerCode, Method callerMethod, Code calleeCode,
                            Instruction insertBefore, Invoke inv) {
        
        IR ir = callerCode.getIR();
        Frame frame = callerCode.getFrame();
        ir.verify(true);
        
        /*
         * compute new maxstack = max(maxStack, inlinedMaxStack)
         * where inlinedMaxStack = parameterSize + callee.maxStack
         */
        int oldSize = frame.getMaxStack();
        int returnSize = 0;
        Klass returnType = inv.getMethod().getReturnType();
        if (returnType != null) {
            if (returnType.isDoubleWord()) {
                returnSize = 2;
            } else {
                returnSize = 1;
            }
        }
        int enterExitSize = Math.max(inv.getMethod().getParametersSize() , returnSize);
        frame.growMaxStack(enterExitSize + calleeCode.getFrame().getMaxStack());
        if (false && oldSize != frame.getMaxStack()) {
System.out.println("Inlining " + inv.getMethod() + " into " + callerMethod);
System.out.println("    Old Max: " + oldSize);
System.out.println("    Param size: " + inv.getMethod().getParametersSize() );
System.out.println("    returnSize: " + returnSize);
System.out.println("    callee max: " + calleeCode.getFrame().getMaxStack());
System.out.println("    new max: " + frame.getMaxStack());
        }

        
        Klass callSiteLocals[] = frame.getLocals(inv);
        CodeCopier copier = new CodeCopier(classFile, ir, insertBefore, inv, callSiteLocals, frame);
        copier.insertInit();
        Instruction curr = calleeCode.getIR().getHead();
        while (curr != null) {
            curr.visit(copier);
            curr = curr.getNext();
        }
        copier.insertExit();
        
        for (Enumeration e = calleeCode.getIR().getExceptionHandlers(); e != null && e.hasMoreElements(); ) {
            IRExceptionHandler handler = (IRExceptionHandler)e.nextElement();
            copier.addExceptionHandler(handler);
        }
        
        StackProducer newOperand = (StackProducer)copier.getLast();
        if (newOperand != null) {
            if (inv.isSpilt()) {
                newOperand.spill(inv.getSpillLocal());
            }
            if (inv.isDuped()) {
                newOperand.setDuped(frame);
            }
            OperandReplacer.replace(newOperand, inv, inv);
        }

        ir.remove(inv);
        ir.verify(true);
    }
    
    /**
     * Static methods may require a CLINIT instruction, which in general is not inlinable.
     * So only allow inlining static methods when we don't need a CLINIT. This includes:<br>
     * - The callee's class has no static initializer.<br>
     * - The callee will immediately execute an instruction (such as GetStatic), that will
     *   call CLINIT when needed.<br>
     * - The caller is an improper subclass of the callee's class.<br>
     *
     * @param caller the method calling <code>calleeMethod</code>
     * @param calleeMethod the method being called
     * @param calleeIR the ir of <code>calleeMethod</code>
     * @return true if the call could be inlined.
     */ 
    private boolean isStaticInlinable(Method caller, Method calleeMethod, IR calleeIR) {
        Assert.that(calleeMethod.isStatic());
        Assert.that(!calleeMethod.isConstructor());
        
        /* some comments say we can count on Klas being initialized.... (but we manage to inline these anyway?
        if (calleeMethod.getDefiningClass() == Klass.CLASS) {
            return true;
        }*/
        if (calleeMethod.isAllowInlined()) {
            return true; // ok, skip static initialization
        } else if (!calleeMethod.requiresClassClinit() ||
             calleeIR.willDoInitCheck() ||
             calleeMethod.getDefiningClass().isAssignableFrom(caller.getDefiningClass())
             ) {
            return true;
        }
       
        return false;
    }
    
    /**
     * Instance methods require a null-check if we can't prove receiver is not null, or 
     * the callee does some other action with implicit nullcheck.<br>
     * - The callee will immediately execute an instruction (such as GetStatic), that will
     *   do a nullcheck when needed. <br>
     * - The caller is a non-static method and the callee's receiver is the caller's "this", which can't be null.
     *   since we don't even try to inline within methods that modify "this".<br>
     *
     * @param caller the method calling <code>calleeMethod</code>
     * @param calleeMethod the method being called
     * @param calleeIR the ir of <code>calleeMethod</code>
     * @param rcvr the receiver of the callee
     * @return true if the call could be inlined.
     *
     * @todo could check for previous getfield or invokevirtual has succeeded...
     */ 
    private boolean isInstanceInlinable(Method caller, Method calleeMethod, IR calleeIR, StackProducer rcvr) {
        Assert.that(!calleeMethod.isStatic());
        if (calleeMethod.isAllowInlined()) {
            return true; // ok, skip a null check.
        } else if (calleeIR.willDoInitCheck()) {
            return true;
        } else if ((caller.isConstructor() || !caller.isStatic()) && (rcvr instanceof LoadLocal)) {
            LoadLocal receiver = (LoadLocal)rcvr;
            if (receiver.isParam0()) {
                return true;
            }
        }
        
        // NOT INLINABLE:
        if (Translator.TRACING_ENABLED && Translator.isTracing("inlining", caller)) {
            if (getInlineLimit(calleeMethod) >= calleeIR.optimizedIRLength()) {
                if (rcvr instanceof LoadLocal) {
                    Tracer.traceln("Can't inline " + calleeMethod + " in " + caller + " because recvr is " + rcvr + " of " + ((LoadLocal)rcvr).getLocal());
                } else {
                    Tracer.traceln("Can't inline " + calleeMethod + " in " + caller + " because recvr is " + rcvr);
                }
            }
        }
        return false;
    }
    
    /**
     * Only inline a constructor call with is a super call of the enclosing constructor. Do not inline constructor calls
     * after a "new" instruction.
     *
     * Only try to inline super constructors. Do not inline constructor after "new".
     *   1) Caller & callee must be constructors
     *   2) Don't atempt inlining once we see a "new" instruction in caller (this is a little conservative)
     *   3) defining class of callee must the superclass of the defining class of caller.
     *
     * @param callerMethod the method calling <code>calleeMethod</code>
     * @param calleeMethod the method being called
     * @param couldBeSuperConstructor false if there was a preceeding "new" instruction
     * @return true if the call could be inlined.
     */ 
    private boolean isConstructorInlinable(Method callerMethod, Method calleeMethod, boolean couldBeSuperConstructor) {
        Assert.that(calleeMethod.isConstructor());
        Klass callerKlass = callerMethod.getDefiningClass();
        Klass calleeKlass = calleeMethod.getDefiningClass();
        if (callerMethod.isConstructor() && couldBeSuperConstructor
                && ((callerKlass == calleeKlass) || (callerKlass.getSuperclass() == calleeKlass))) {
            return true;
        }
        return false;
    }
    
    /**
     * Figure out how large this method can be to allow inlining.
     * Make allowances for parameters and return values. The callee will have nodes for reading parameters,
     * which we may be able to remove after inlining, and we may be able to move the result value closer
     * to where it needs to go.
     */
    private int getInlineLimit(Method calleeMethod) {
        int inlineLimit = Arg.get(Arg.INLINE_METHOD_LIMIT).getInt() + calleeMethod.getParameterTypes().length;
        if (!calleeMethod.isStatic()) {
            inlineLimit++; // allowance for "this
        }
        if (calleeMethod.getReturnType() != Klass.VOID || calleeMethod.isConstructor()) {
            inlineLimit++; // allowance for return value
        }
        return inlineLimit;
    }
    
    /**
     * Identify well known functions called with constant objects. Maybe we can evaluate them now.
     * ex: 
     *     "This is some string".length();
     * OK, this doesn't happen in any interesting cases in the bootstrap suite.
     */
/*if[EXCLUDE]*/
    private void checkForFunctionOnConstants(Method callerMethod, Invoke inv) {
        Method calleeMethod = inv.getMethod();
        if (calleeMethod.getReturnType() != Klass.VOID
            && calleeMethod.getFullyQualifiedName().startsWith("java.lang.")) {
            StackProducer[] params = inv.getParameters();
            boolean allParamsAreConstant = params.length > 0;
            for (int i = 0; i < params.length; i++) {
                if (!(params[i] instanceof Constant)) {
                    allParamsAreConstant = false;
                    break;
                }
            }
            
            if (allParamsAreConstant) {
                System.out.println("Constant call to " + calleeMethod.getFullyQualifiedName() + " in "  + callerMethod.getFullyQualifiedName());
            }
        }
    }
/*end[EXCLUDE]*/
    
     /**
     * After inlining, dead code elimination, and byecode optimizations, record the remaining calls to methods..
     *
     * @param code the caller's <code>Code</code> object to search for references
     * @param callerMethod the Method corresponding to <code>code</code>.
     */
    private void recordCalls(Code code, Method callerMethod, MethodDB.Entry callerEntry) {
        IR ir = code.getIR();
        Instruction insn = ir.getHead();
        while (insn != null) {
            if (insn instanceof Invoke) {
                Invoke inv = (Invoke)insn;
                translator.methodDB.recordMethodCall(callerEntry, inv.getMethod());
            }
            insn = insn.getNext();
        }
    }
    
    /**
     * Inline calls from <code>code</code> to other methods.
     *
     * @param classFile the ClassFile that defines <code>code</code>
     * @param code the caller's <code>Code</code> object that may contain calls to be inlined.
     * @param callerMethod the Method corresponding to <code>code</code>.
     */
    private void tryInlining(ClassFile classFile, Code code, Method callerMethod, MethodDB.Entry callerEntry) {
        boolean didInlining = false;
        
        /*
         * Don't inline construcor call after 'new', because there will be no indication to the squawk
         * verifier that the new object was ever initialized. Only inline super constructor calls. 
         */
        boolean couldBeSuperConstructor = callerMethod.isConstructor();
        final boolean trace = Translator.TRACING_ENABLED && Translator.isTracing("inlining", callerMethod);

        IR ir = code.getIR();
        Instruction insn = ir.getHead();
        while (insn != null) {
            if (insn instanceof New) {
                couldBeSuperConstructor = false; // a super constructor call happens early on. If we see a "new", then any calls to a constructor can't be super calls.
                insn = insn.getNext();
            } else if (insn instanceof Invoke) {
                Invoke inv = (Invoke)insn;
                boolean inlineThisCall = false;
                insn = insn.getNext();
                Method calleeMethod = inv.getMethod();
                MethodDB.Entry calleeEntry = translator.methodDB.lookupMethodEntry(calleeMethod);
               
                if (isMethodInlinable(calleeMethod, calleeEntry, (inv instanceof InvokeSuper))) {
                    Code calleeCode = getMethodCode(calleeMethod);
                    Assert.that(!calleeMethod.isAbstract());
                    
                    if (calleeCode != null) {
                        IR calleeIR = calleeCode.getIR();
                        int inlineLimit = getInlineLimit(calleeMethod);
                        inlineThisCall = true; // assume we will, then prove otherwise
                        String failMsg = null;

                        // make sure that callee has done it's inlining first, and been optimized, before checking for size limits, 
                        // and before checking ir properties.
                        optimizeMethod(translator.lookupClassFile(calleeMethod.getDefiningClass()), calleeCode, calleeMethod, calleeEntry);
                        
                        // do size check first:
                        if (!calleeMethod.isForceInlined() &&
                            ((!calleeIR.hasBeenOptimized() && calleeIR.size(false) > inlineLimit) ||
                             (calleeIR.size(false) > inlineLimit))) {
                            Translator.numTooLarge++;
                            inlineThisCall = false;
                        } else if (!calleeIR.hasBeenOptimized()) {
                            Translator.numRecursive++;
                            inlineThisCall = false;
                            failMsg = "Can't inline (IR not optimized) ";
                        } else if (calleeIR.getNonEmptyStackOnReturn()) {
                            // don't try to inline a method that leaves data on stack (besides return value). We would have to pop that data.
                            Translator.numNonEmptyStackOnReturn++;
                            inlineThisCall = false;
                        } else if (calleeMethod.isConstructor()) {
                            if (!isConstructorInlinable(callerMethod, calleeMethod, couldBeSuperConstructor)) {
                                Translator.numCantInlineConstructor++;
                                inlineThisCall = false;
                                failMsg = "Can't inline constructor ";
                            }
                        } else if (calleeMethod.isStatic()) {
                            if (!isStaticInlinable(callerMethod, calleeMethod, calleeIR)) {
                                Translator.numCantInlineStatic++;
                                inlineThisCall = false;
                                failMsg = "Can't inline static ";
                            }
                        } else {
                            Assert.that(!calleeMethod.isStatic());
                            if (!isInstanceInlinable(callerMethod, calleeMethod, calleeIR, inv.getParameters()[0])) {
                                Translator.numCantInlineInstance++;
                                inlineThisCall = false;
                                failMsg = "Can't inline virtual ";
                            }
                        }
                        
                        if (inlineThisCall) {
                            if (trace) {
                                Tracer.traceln("Inlining from " + Klass.toString(calleeMethod, false) + " into " + Klass.toString(callerMethod, false));
                            }
                            
                            // If we are inlining a call to a super constructor, let verifier know. 
                            if (isJavaLangObjectInit(calleeMethod) || calleeIR.getInlinedSuperConstructor()) {
                                ir.setInlinedSuperConstructor();
                            }
                            
                            calleeEntry.setWasInlined();
                            doInlining(classFile, code, callerMethod, calleeCode, insn, inv);
                        } else {
                            if (trace && failMsg != null) {
                                Tracer.traceln(failMsg + Klass.toString(calleeMethod, false) + " into " + Klass.toString(callerMethod, false));
                            }
                        }
                    } else {
                        // NO IR available, do template based inlining...
                        if (Arg.get(Arg.INLINE_OBJECT_CONSTRUCTOR).getBool() && couldBeSuperConstructor && isJavaLangObjectInit(calleeMethod)) {
                            // we know that java.lang.Object.<init>() is empty, so remove calls to it from within other constructors.
                            ir.remove(inv);
                            InlinedInvoke iinv = new InlinedInvoke(calleeMethod);
                            ir.insertBefore(iinv, insn);
                            ir.insertBefore(new InlinedEnd(calleeMethod, iinv), insn);
                            OperandReplacer.replace(inv.getParameters()[0], inv, insn);   
                            ir.setInlinedSuperConstructor();
                            inlineThisCall = true;
                        } else {
                            Translator.numNoCode++;
                            inlineThisCall = false;
                        }
                    }
                    
/*if[EXCLUDE]*/
                    if (!inlineThisCall) {
                        checkForFunctionOnConstants(callerMethod, inv);
                    }
/*end[EXCLUDE]*/
                    
                    didInlining = didInlining | inlineThisCall;
                }
                
                Translator.numCalls++;
                if (inlineThisCall) {
                    Translator.numCallsInlined++;
                }
                
                if (!inlineThisCall && calleeMethod.isForceInlined() && translator.getSuite().isBootstrap()) {
                    Tracer.traceln("WARNING: Didn't inline " + Klass.toString(calleeMethod, false) + " into " + Klass.toString(callerMethod, false) + " even though ForceInlined pragma was used");
                }
            } else {
                insn = insn.getNext();
            }
        }
        
        if (trace && didInlining) {
            Tracer.traceln("After Inlining INTO " + Klass.toString(callerMethod, false) + ":");
            translator.trace(callerMethod, ir);
        }
    }

    /**
     * Optimize the ir of the method specified by <code>code</code> and <code>method</code>, optionally 
     * inlining into this method.
     *
     * @param classFile the ClassFile that defines <code>code</code>
     * @param code      the <code>Code</code> object to be optimized
     * @param method    the Method corresponding to <code>code</code>
     */
    private void optimizeMethod(ClassFile classFile, Code code, Method method, MethodDB.Entry entry) {
        if (code == null || code.doingInlining) {
            return;
        }
        code.doingInlining = true;
        IR ir = code.getIR();
        Translator.beforeSize += ir.size(false);
        
        try {
            if (translator.shouldDoInlining()) {
                if (method.isStatic() || !ir.changesThis()) {
                    // inlining will try to analyze virtual calls assumming that "this" is not null
                    // so don't inline if assumption may be false.
                    tryInlining(classFile, code, method, entry);
                }
            }
            
            IROptimizer optimizer = new IROptimizer(ir, method, code.getCodeParser());
            InstructionRemover remover = optimizer.getRemover();
            boolean changed = false;
            
            // Try both general IR optimizations and dead code elimination until there are no changes:
            // SHOWER, RINSE, REPEAT
            // do first pass unconditionally, then repeat phases as needed.
            if (Arg.get(Arg.OPTIMIZE_BYTECODE).getBool()) {
                optimizer.optimize(); // this repeats until no more changes...
            }
            if (Arg.get(Arg.OPTIMIZE_DEADCODE).getBool()) {
                DeadCodeRemover dcr = new DeadCodeRemover(ir, method, remover);
                changed = dcr.removeUnreachableCode();
            }
            
            while (changed) {
                 if (Arg.get(Arg.OPTIMIZE_BYTECODE).getBool()) {
                    changed = optimizer.optimize(); // this repeats until no more changes...
                }
                if (changed && Arg.get(Arg.OPTIMIZE_DEADCODE).getBool()) {
                    DeadCodeRemover dcr = new DeadCodeRemover(ir, method, remover);
                    changed = dcr.removeUnreachableCode();
                }
            }
            
            recordCalls(code, method, entry);
            // Set IR properties after all inlining and optimization
            ir.setProperties(method);
            
            // verify that java.lang.Object.<init> is empty:
            if (Arg.get(Arg.INLINE_OBJECT_CONSTRUCTOR).getBool() && isJavaLangObjectInit(method)) {
                verifyDefaultConstructorIsEmpty(ir);
            }
            Translator.afterSize += ir.size(false);
        } catch (Error ex) {
            System.err.println("Error optimizing " + method);
            translator.trace(method, ir);
            throw ex;
        }
    }
    
    /**
     * Optimize suite by inlining and other bytecode optimizations.
     */
    public void optimizeSuite(Suite suite) {
        for (int cno = 0; cno < suite.getClassCount(); cno++) {
            Klass klass = suite.getKlass(cno);
            if (klass != null && !klass.isSynthetic()) {
                ClassFile classFile = translator.lookupClassFile(klass);
                if (classFile == null) {
                    continue;
                }
                
                for (int i = 0; i < classFile.getStaticMethodCount(); i++) {
                    Method method = klass.getMethod(i, true);
                    MethodDB.Entry entry = translator.methodDB.lookupMethodEntry(method);
                    optimizeMethod(classFile, classFile.getStaticMethod(i), method, entry);
                }
                for (int i = 0; i < classFile.getVirtualMethodCount(); i++) {
                    Method method = klass.getMethod(i, false);
                    MethodDB.Entry entry = translator.methodDB.lookupMethodEntry(method);
                    optimizeMethod(classFile, classFile.getVirtualMethod(i), method, entry);
                }
            }
        }
    }
    
    /**
     * Returns true if m is the constructor java.lang.Object.<init>().
     */
    private boolean isJavaLangObjectInit(Method m) {
        return m.isConstructor() && (m.getDefiningClass() == Klass.OBJECT);
    }
 
    /**
     * When shouldInlineObjectConstructor() is true, we assume that the constructor java.lang.Object.<init>()
     * is empty, so calls to it can be eliminated. Veryify that this is true.
     */
    private void verifyDefaultConstructorIsEmpty(IR ir) {
        Assert.always(ir.size(false) == 2, "java.lang.Object.<init> is not empty as expected.");
        Instruction insn = ir.getHead().skipPosition();
        Assert.always(insn instanceof LoadLocal, "java.lang.Object.<init> is not as expected: " + insn);
        insn = insn.getNext().skipPosition();
        Assert.always(insn instanceof Return, "java.lang.Object.<init> is not as expected." + insn);
    }
}
