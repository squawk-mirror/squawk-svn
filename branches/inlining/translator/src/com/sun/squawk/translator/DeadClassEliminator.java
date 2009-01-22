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

import com.sun.squawk.Klass;
import com.sun.squawk.Suite;
import com.sun.squawk.VM;
import com.sun.squawk.util.*;
import com.sun.squawk.Method;
import com.sun.squawk.Field;
import com.sun.squawk.Modifier;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import com.sun.squawk.translator.ir.ReferenceRecordingVisitor;
import com.sun.squawk.translator.ir.IR;
import com.sun.squawk.translator.ir.Instruction;

/**
 * Detect and remove unused classes.
 *
 * After calling computeClassesUsed(), the translator can use isMarked() to determine unused classes.
 */
public class DeadClassEliminator {
    
    private Translator translator;
    private Hashtable referencedClasses = new Hashtable();    
    
    public boolean isMarked(Klass klass) {
        return referencedClasses.get(klass) != null;
    }
    
    /**
     * Mark this ref, and refs to superclass, interfaces, and component type (if an array).
     *
     * @return true if this is was unmarked
     */
    public boolean markClass(Klass klass) {
        if (klass != null && !isMarked(klass)) {
            referencedClasses.put(klass, klass);
            return true;
        }
        return false;
    }
    
    /** Creates a new instance of DeadClassEliminator */
    public DeadClassEliminator(Translator translator) {
        this.translator = translator;
    }
    
    /*---------------------------------------------------------------------------*\
     *                         Track unused classes                              *
    \*---------------------------------------------------------------------------*/
    
    /**
     * Is this a class that might be called by the system through some basic mechanism,
     * such as "main", called by interpreter, etc.
     *
     * @param klass the klass
     * @return true if this is a main method, an methdo of the class Object
     */
    private static boolean isBasicRoot(Klass klass) {
        
        if (klass.getSystemID() >= 0) {             // system klasses are basic.
            return true;
        }
        
        if (klass.isSynthetic()) {
            return true;
        }
        
        if (klass.hasMain()) {
            return true;
        }
        
        // @TODO: This is probably too general!
    /*    if (klass.hasDefaultConstructor()) {
            return true;
        } */
        
        if (klass.getInternalName().equals("Lcom.sun.squawk.VM;")) {
            // this has interpreter invoked methods.
            return true;
        }
        
        return false;
    }
    
    
    /**
     * Given a SquawkVector of Strings, return a sorted array of those strings.
     */
    private String[] sortStringVector(SquawkVector v) {
        String[] tmp = new String[v.size()];
        v.copyInto(tmp);
        Arrays.sort(tmp, new Comparer() {
            public int compare(Object a, Object b) {
                String astr = (String)a;
                String bstr = (String)b;
                
                return astr.compareTo(bstr);
            }
        });
        return tmp;
    }
    
    /**
     * Given a SquawkVector of Strings, print it sorted
     *
     * @param v a SquawkVector of Strings
     * @param prefix string to print before element.
     */
    private void printVectorSorted(SquawkVector v, String prefix) {
        String[] results = sortStringVector(v);
        for (int i = 0; i < results.length; i++) {
            Tracer.trace(prefix);
            Tracer.traceln(results[i]);
        }
    }
    
    
    /**
     * Given the method's access, the defining class' acess, and the suite type,
     * determine the final accessibility of the method outside of this suite.
     *
     * Note that we are talking about the accessibility of a particular method,
     * not all of the methods that override a super method. There are cases where
     * a super method is not accessibole, but an override is. A latter check for overriding
     * will mark the super method as used.
     *
     * @param klass the klass
     * @return true if an external suite could possibly access this klass.
     */
    public boolean isExternallyVisible(Klass klass) {
        int modifiers = klass.getModifiers();
        int suiteType = translator.getSuiteType();
        
        boolean sealedPackages = (suiteType == Suite.LIBRARY) || (suiteType == Suite.APPLICATION);
        
        if (/*translator.getSuite().isBootstrap() &&*/ VM.stripSymbols(klass) && !VM.isInternal(klass)) {
            // if the symbol is stripped, and it wasn't marked as "internal" in the library.proprties file,
            // then there is no way that this is externally visible.
            return false;
        } else {
            // It's declared externally visible, but is it really?
            Assert.that(Modifier.isPackagePrivate(modifiers) || Modifier.isProtected(modifiers) || Modifier.isPublic(modifiers));
            
            switch (suiteType) {
                case Suite.APPLICATION:
                    // no possible child suite:
                    return false;
                    
                case Suite.LIBRARY:
                    // what can we do here
                    return true;
                default:
                    // extendable and debuggable suites leave all symbols externally visible.
                    return true;
            }
        }
    }
    
    /**
     * Use a mark stack to avoid deep recursion.
     */
    SquawkVector markStack;
    
    void shallowMark(Klass klass) {
        if (klass != null && !isMarked(klass)) {
            markStack.addElement(klass);
        }
    }
    
    private void scanMethod(ClassFile classFile, Code code, Method m) {
        if (code != null && m != null 
                && (!Arg.get(Arg.DEAD_METHOD_ELIMINATION).getBool() ||
                    translator.dme.isMarkedUsed(m))) {
            shallowMark(m.getReturnType());
            Klass[] parameters = m.getParameterTypes();
            for (int j = 0; j < parameters.length; j++) {
                shallowMark(parameters[j]);
            }
            ClassReferenceRecordingVisitor visitor = new ClassReferenceRecordingVisitor(this);
            IR ir = code.getIR();
            for (Instruction instruction = ir.getHead() ; instruction != null ; instruction = instruction.getNext()) {
                instruction.visit(visitor);
            }
            
            
        }
    }
    
    /**
     * Mark the class, and scan methods for references to other classes.
     * Push new references onto the markStack.
     */
    public void scanClassMethods(Klass klass) {
        ClassFile classFile = translator.lookupClassFile(klass);
        if (classFile == null) {
            return;
        }
        
        for (int i = 0; i < classFile.getStaticMethodCount(); i++) {
            scanMethod(classFile, classFile.getStaticMethod(i), klass.getMethod(i, true));
        }
        for (int i = 0; i < classFile.getVirtualMethodCount(); i++) {
            scanMethod(classFile, classFile.getVirtualMethod(i), klass.getMethod(i, false));
        }
    }
    
    /**
     * Mark the class, and scan methods for references to other classes.
     * Push new references onto the markStack.
     */
    public void scanClassDeep(Klass klass) {
        if (markClass(klass)) {
            shallowMark(klass.getSuperclass());
            shallowMark(klass.getComponentType());
            Klass[] interfaces = klass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                shallowMark(interfaces[i]);
            }
            if (!klass.isSynthetic()) {
                scanClassMethods(klass);
            }
        }
    }
    
    public void computeClassesUsed() {
        boolean trace = (Translator.TRACING_ENABLED && Tracer.isTracing("DCE")) || VM.isVeryVerbose();
        //Enumeration e;
        
        SquawkVector foundClasses = new SquawkVector(); // used for tracing
        markStack = new SquawkVector(); // stack of classes to be marked
        Suite suite = translator.getSuite();
        
        // Preserve methods that might be called autmatically by system, beyond the powers of analysis:
        foundClasses.removeAllElements();
        for (int cno = 0; cno < suite.getClassCount(); cno++) {
            Klass klass = suite.getKlass(cno);
            if (klass != null) {
                if (isBasicRoot(klass)) {
                    if (trace) {
                        foundClasses.addElement(klass.toString());
                    }
                    scanClassDeep(klass);
                }
            }
        }
        if (trace && foundClasses.size() != 0) {
            Tracer.traceln("[translator DCE: ==== System roots:  " + foundClasses.size() + " =====");
            printVectorSorted(foundClasses, "System root: ");
        }
        
        // Preserve all externally visible classes
        foundClasses.removeAllElements();
        for (int cno = 0; cno < suite.getClassCount(); cno++) {
            Klass klass = suite.getKlass(cno);
            if (klass != null) {
                if (isExternallyVisible(klass) && !isMarked(klass)) {
                    if (trace) {
                        foundClasses.addElement(klass.toString());
                    }
                    scanClassDeep(klass);
                }
            }
        }
        if (trace && foundClasses.size() != 0) {
            Tracer.traceln("[translator DCE: ==== Visible roots:  " + foundClasses.size() + " =====");
            printVectorSorted(foundClasses, "Visible root: ");
        }
        
        // Now mark all
        int len;
        while ((len = markStack.size()) > 0) {
            Klass klass = (Klass)markStack.lastElement();
            markStack.removeElementAt(len - 1);
            if (markClass(klass)) {
                shallowMark(klass.getSuperclass());
                shallowMark(klass.getComponentType());
                Klass[] interfaces = klass.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    shallowMark(interfaces[i]);
                }
                if (!klass.isSynthetic()) {
                    scanClassMethods(klass);
                }
            }
        }
        
        // report unused classes:
        foundClasses.removeAllElements();
        for (int cno = 0; cno < suite.getClassCount(); cno++) {
            Klass klass = suite.getKlass(cno);
            if (klass != null) {
                if (!isMarked(klass)) {
                    if (trace) {
                        foundClasses.addElement(klass.toString());
                    }
                    // TODO: Actually remove class!
  //suite.removeClass(klass);
                }
            }
        }
        if (trace || VM.isVeryVerbose()) {
            if (foundClasses.size() != 0) {
                Tracer.traceln("[translator DCE: ==== Unused classes:  " + foundClasses.size() + " (used classes: " + referencedClasses.size() + ") =====");
                Tracer.traceln("[translator DCE: CLASSES NOT ACTUALLY REMOVED (to be written) =====");
                printVectorSorted(foundClasses, "    ");
            }
        }
    }
    
}

class ExampleDeadClass {
    ExampleDeadClass(int b) {}
    
    
}

class ClassReferenceRecordingVisitor extends ReferenceRecordingVisitor {
    
    private DeadClassEliminator dce;
    
    ClassReferenceRecordingVisitor(DeadClassEliminator dce) {
        this.dce = dce;
    }
    
    protected void recordKlass(Klass klass) {
        dce.shallowMark(klass);
    }
    
    protected void recordMethod(Method method) {
        dce.shallowMark(method.getDefiningClass());
    }
    
    protected void recordField(Field field) {
        dce.shallowMark(field.getDefiningClass());
        dce.shallowMark(field.getType());
    }
    
}