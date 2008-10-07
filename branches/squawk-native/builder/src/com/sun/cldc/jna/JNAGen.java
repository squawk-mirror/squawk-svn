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

package com.sun.cldc.jna;

import com.sun.squawk.builder.gen.*;
import com.sun.squawk.builder.Build;
import com.sun.squawk.builder.BuildException;
import com.sun.squawk.builder.Command;
import com.sun.squawk.builder.util.FileSet;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;
import com.sun.squawk.builder.gen.Generator.*;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;

class JNAGenException extends Exception {
    JNAGenException(String msg) {
            super(msg);
    }
}

/**
 * A tool that reads Java interfaces and classes that describe C functions, structures, and constants
 * to import from C. JNAGen then writes C code that will generate Java classes that defines Java access to the 
 * C functions, structures, and constants.
 * 
 * This is part of CLDC Java Native Access
 * 
 * @see com.sun.cldc.jna
 */
public class JNAGen extends Command {
    public final static int TAB = 4; // number of spaces to indent generated Java code by, per-level
    
    File baseDir;
    String classpath;
    
    PrintWriter out;
    
    public JNAGen(Build env, File baseDir, String classpath) {
        super(env, "JNAGen");
        this.baseDir = baseDir;
        this.classpath = classpath;
    } 
    
    void indent(int n) {
        for (int i = 0; i < n; i++) {
            out.print(' ');
        }
    }
    
    /**
     * Print a line with "indent" containing a "printf" of the String "line"
     * @param indent spaces to indent in the "printf"
     * @param line the text to print
     */
    void metaPrint(int indent, String line) {
        indent(4);
        out.print("fprintf(out, \"");
        indent(indent * TAB);
        out.println(line + "\");");
    }
    
        /**
     * Print a line with "indent" containing a "printf" of the String "line"
     * @param indent spaces to indent in the "printf"
     * @param line the text to print
     */
    void metaPrintln(int indent, String line) {
        indent(4);
        out.print("fprintf(out, \"");
        indent(indent * TAB);
        out.println(line + "\\n\");");
    }
    
    /**
     * Print a line with "indent" containing a "printf" of the String "line", with additional printf arguments.
     * @param indent spaces to indent before the "printf"
     * @param line the text to print
     */
    void metaPrint(int indent, String line, String[] printfArgs) {
            indent(4);
            out.print("fprintf(out, \"");
            indent(indent * TAB);
            out.print(line + "\"");
            for (int i = 0; i < printfArgs.length; i++) {
                out.print(", ");
                out.print(printfArgs[i]);
            }
            out.println(");");
    }
    
    /**
     * Print a line with "indent" containing a "printf" of the String "line", with additional printf arguments.
     * @param indent spaces to indent before the "printf"
     * @param line the text to print
     */
    void metaPrintln(int indent, String line, String[] printfArgs) {
            indent(4);
            out.print("fprintf(out, \"");
            indent(indent * TAB);
            out.print(line + "\\n\"");
            for (int i = 0; i < printfArgs.length; i++) {
                out.print(", ");
                out.print(printfArgs[i]);
            }
            out.println(");");
    }
    
    
    void metaPrintln(int indent, String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            metaPrintln(indent, lines[i]);
        }
    }
    
    /**
     * Prints code to generate the the standard Squawk VM copyright message from C.
     * 
     * This is the viral nature of the GPL enshrined in C!
     * 
     * @param out  where to print the message
     */
    final void printCopyright(InterfaceDecl interfaceDecl) {
        out.println("static FILE* out;");
        out.println("");
        out.println("void printCopyright() {");
        metaPrintln(0, Generator.COPYRIGHT_LINES);
        metaPrintln(0, "");
        metaPrintln(0, "/* **** GENERATED FILE -- DO NOT EDIT ****");
        metaPrintln(0, " *      generated by " + this.getClass().getName());
        metaPrintln(0, " *      from the CLDC/JNA Interface class " + interfaceDecl.interfaceClass.getName());
        metaPrintln(0, " */");
        metaPrintln(0, "");
        out.println("}\n");
    }
    
    private void printIncludes(InterfaceDecl interfaceDecl) {
        out.println("#include <stddef.h>");
        out.println("#include <stdlib.h>");
        out.println("#include <stdio.h>");
        out.println("#include <errno.h>");

        for (String include : interfaceDecl.includes) {
            out.println("#include " + include);
        }
        out.println("");
    }
    
    private void printPackages(InterfaceDecl interfaceDecl) {
        metaPrintln(0, "package " + interfaceDecl.interfaceClass.getPackage().getName() + ";");
        metaPrintln(0, "");
        metaPrintln(0, "import com.sun.cldc.jna.*;");
        metaPrintln(0, "");
        out.println();
    }
    
    /* only supprto top-level library defn */
    private void printLibraryDefinition(InterfaceDecl interfaceDecl) {        
        if (interfaceDecl.library == null) {
            out.println("    /* used default library */");
        } else {
            metaPrintln(1, "public final static NativeLibrary NATIVE_LIBRARY = new NativeLibrary(\\\"" + interfaceDecl.library + "\\\");");
        }
        metaPrintln(0, "");
        out.println();
    }
    
    /*------------------------- DEFINES --------------------------*/
    
    private void printADefine(Field f, String format, int level) {
        String[] args = new String[1];
        args[0] = f.getName();
        metaPrintln(level, "public final static " + f.getType().getSimpleName() + " " + f.getName() + " = " + format + ";", args);
    }
        
    private void printDefines(InterfaceDecl interfaceDecl, int level) throws JNAGenException {
        if (interfaceDecl.defines.size() == 0) {
            return;
        }
        
        metaPrintln(level, "/*----------------------------- defines -----------------------------*/");
        metaPrintln(0, "");
        
        // Field[] fields = interfaceClass.getDeclaredFields();
        for (Field f : interfaceDecl.defines) {
            if (f.getType().equals(Integer.TYPE)) {
                printADefine(f, "%d", level);
            } else if (f.getType().equals(Long.TYPE)) {
                printADefine(f, "%dll", level);
            } else if (f.getType().equals(String.class)) {
                printADefine(f, "\"%s\"", level);
            } else {
                throw new JNAGenException("JNAGEn cannot handle fields of type " + f.getType() + " for field " + f);
            }
        }
        metaPrintln(0, "");
        out.println();
    }
   
    /*------------------------- GLOBAL VARIABLES --------------------------*/
    private static String getPtrName(String nativeName) {
        return nativeName + "Ptr";
    }

    private void printVarPtr(InterfaceDecl interfaceDecl, String nativeName, int varSize, int level) {
        metaPrint(level, "public static VarPointer " + getPtrName(nativeName) + " = VarPointer.getVarPointer(");
        if (interfaceDecl.library != null) {
            metaPrint(0, "NATIVE_LIBRARY, ");
        }
        metaPrintln(0, "\\\"" + nativeName + "\\\", " + varSize + ");");
        metaPrintln(0, "");
    }
    
    /**
     * Return a string containing the Java code to get a value of type "valtype" from the
     * pointer "ptrName" using offset.
     * @param valType
     * @param ptrName
     * @param offset
     * @return a Java expression that gets the value
     */
    private String getValue(Class valType, String ptrName, int offset) throws JNAGenException {
        if (valType.equals(Integer.TYPE)) {
            return ptrName + ".getInt(" + offset + ")";
        } else if (valType.equals(Long.TYPE)) {
            return ptrName + ".getLong(" + offset + ")";
        } else if (valType.equals(String.class)) {
            return ptrName + ".getString(" + offset + ")";
        } else if (valType.equals(Pointer.class)) {
            return ptrName + ".getPointer(" + offset + ")";
        } else {
            throw new JNAGenException("JNAGEn cannot get values of type " + valType);
        }
    }
    
    /**
     * Return a string containing the Java code to get a value of type "valtype" from the
     * pointer "ptrName" using offset.
     * 
     * @param valtype Class of the value
     * @param ptrName name of the pointer
     * @param valName the name of the value to set
     * @param offset
     * @return a Java expression that sets the value
     */
    private String setValue(Class valType, String ptrName, String valName, int offset) throws JNAGenException {
        if (valType.equals(Integer.TYPE)) {
            return ptrName + ".setInt(" + offset + ", " + valName + ")";
        } else if (valType.equals(Long.TYPE)) {
            return ptrName + ".setLong(" + offset + ", " + valName + ")";
        } else if (valType.equals(String.class)) {
            return ptrName + ".setString(" + offset + ", " + valName + ")";
        } else if (valType.equals(Pointer.class)) {
            return ptrName + ".setPointer(" + offset + ", " + valName + ")";
        } else {
            throw new JNAGenException("JNAGEn cannot get values of type " + valType);
        }
    }

    private void printGetter(Method m, String nativeName, int level) throws JNAGenException {
        if (m.getParameterTypes().length != 0 ||
            m.getReturnType() == Void.class) {
            throw new JNAGenException("JNAGEn cannot handle setters of the form " + m);
        }
        
        printMethodDecl(m, level);
        metaPrintln(level + 1, "return " + getValue(m.getReturnType(), getPtrName(nativeName), 0) + ";");
        metaPrintln(level, "}");
        metaPrintln(0, "");
    }

    private void printSetter(Method m, String nativeName, int level) throws JNAGenException {
        if (m.getParameterTypes().length != 1 ||
            m.getReturnType() != Void.class) {
            throw new JNAGenException("JNAGEn cannot handle setters of the form " + m);
        }
        
        printMethodDecl(m, level);
        metaPrintln(level + 1, setValue(m.getParameterTypes()[0], getPtrName(nativeName), "arg0", 0) + ";");
        metaPrintln(level, "}");
        metaPrintln(0, "");
    }
    
    /*
     * Given that a getter and/or a setter is declared, rteurn the type of the value
     */
    private Class getVariableType(Method getter, Method setter) throws JNAGenException {
        if (getter != null) {
            return getter.getReturnType();
        } else if (setter != null) {
            return setter.getParameterTypes()[0];
        } else {
            throw new JNAGenException("getter or setter expected.");
        }
    }
    
    private int getVariableSize(Class c) throws JNAGenException {
          if (c.equals(Integer.TYPE)) {
            return 4;
        } else if (c.equals(Long.TYPE)) {
            return 8;
        } else if (c.equals(String.class)) {
            return 1024;
        } else if (c.equals(Pointer.class)) {
            return 4;
        } else {
            throw new JNAGenException("JNAGEn cannot get values of type " + c);
        }
    }

    private void printVars(InterfaceDecl interfaceDecl, int level) throws JNAGenException {
        if (interfaceDecl.globals.size() == 0) {
            return;
        }

        metaPrintln(level, "/*----------------------------- variables -----------------------------*/");
        metaPrintln(0, "");
        
        for (String name : interfaceDecl.globals) {
            Method getter = interfaceDecl.getters.get(name);
            Method setter = interfaceDecl.setters.get(name);
            Method protoMethod = (getter != null) ? getter : setter;
            Class varType = getVariableType(getter, setter);
            int size = getVariableSize(varType);

            printVarPtr(interfaceDecl, name, size, level);
            if (getter != null) {
                printGetter(getter, name, level);
            }
            if (setter != null) {
                printSetter(setter, name, level);
            }
        }
        metaPrintln(0, "");
        out.println();
    }
    
    /*------------------------- METHODS --------------------------*/
    
    private static String getPtrName(Member m) {
        return m.getName() + "Ptr";
    }
        
    private void printFunctionPtr(InterfaceDecl interfaceDecl, Method m, int level) {
        metaPrint(level, "private final static Function " + getPtrName(m) + " = Function.getFunction(");
        if (interfaceDecl.library != null) {
            metaPrint(0, "NATIVE_LIBRARY, ");
        }
        metaPrintln(0, "\\\"" + m.getName() + "\\\");");
        metaPrintln(level, "");
    }
    
    private static byte[] dummyByteArray = new byte[0];
    
    private String createNativeParam(Class type, int i, int level) throws JNAGenException {
        if (type.equals(String.class)) {
            metaPrintln(level + 1, "Pointer var" + i + " = Pointer.createStringBuffer(arg" + i + ");");
        } else if (Structure.class.isAssignableFrom(type)) {
            metaPrintln(level + 1, "arg" + i + ".allocateMemory();");
            metaPrintln(level + 1, "arg" + i + ".write();");
            metaPrintln(level + 1, "Pointer var" + i + " = arg" + i + ".getPointer();");
        } else if (type.isPrimitive()) {
            return "arg" + i;
        } else {
            throw new JNAGenException("Can't translate arguments of type " + type);
        }
        return "var" + i;
    }
    
    private void cleanupNativeParam(Class type, int i, int level) throws JNAGenException {
        if (type.equals(String.class)) {
            metaPrintln(level + 1, "arg" + i + ".free();");
        } else if (Structure.class.isAssignableFrom(type)) {
            metaPrintln(level + 1, "arg" + i + ".read();");
            metaPrintln(level + 1, "arg" + i + ".freeMemory();");
        } else if (type.isPrimitive()) {
        } else {
            throw new JNAGenException("Can't translate arguments of type " + type);
        }
    }
    
    private void convertReturn(Class type, int level) throws JNAGenException {
        if (type.equals(String.class)) {
            metaPrintln(level + 1, type.getSimpleName() + " result = Function.returnString(result0);");
        } else if (Structure.class.isAssignableFrom(type)) {
            metaPrintln(level + 1, type.getSimpleName() + " result = " + type.getSimpleName() + "();");
            metaPrintln(level + 1, "Pointer rp = new Pointer(result0, result.size());");
            metaPrintln(level + 1, "result.useMemory(rp);");
            metaPrintln(level + 1, "result.read();");
        } else if (type.isPrimitive() && !type.equals(Long.TYPE) && !type.equals(Double.TYPE)) {
            metaPrintln(level + 1, type.getSimpleName() + " result = (" +  type.getSimpleName() + ")result0;");
        } else {
            throw new JNAGenException("Can't translate return values of type " + type);
        }
    }
    /***
     * Given a method, print code to do any translation of Java->C parameters, and return an array of strings naming the native parameter variables.
     * @param parameterTypes
     * @return  array of strings naming the native parameter variables.
     */
    private String[] createNativeParams(Method m, int level) throws JNAGenException {
        Class[] parameterTypes = m.getParameterTypes();
        String[] result = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            result[i] = createNativeParam(parameterTypes[i], i, level);
        }
        return result;
    }
    
    private void cleanUpNativeParams(Method m, String[] nativeParams, int level) throws JNAGenException {
        Class[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            cleanupNativeParam(parameterTypes[i], i, level);
        }
    }

    private void printMethodDecl(Method m, int level) {
        metaPrint(level, "public " + m.getReturnType().getName() + " " + m.getName() + "(");
        Class[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            String comma = (i != 0 ? ", " : "");
            metaPrint(0, comma + parameterTypes[i].getSimpleName() + " arg" + i);
        }
        metaPrintln(0, ") {");
    }
     
//    private void printGlobalGetter(InterfaceDecl interfaceDecl, Method m, String nativeName, int level) throws JNAGenException {
//            metaPrint(level + 1, "int result0 = ");
//            metaPrint(0, getPtrName(m) + ".call" + numParams + "(");
//            for (int i = 0; i < numParams; i++) {
//                String comma = i != 0 ? ", " : "";
//                metaPrint(0, comma + nativeParams[i]);
//            }
//            metaPrintln(0, ");");
//            convertReturn(m.getReturnType(), level);
//            cleanUpNativeParams(m, nativeParams, level);
//            metaPrintln(level + 1, "return result;");
//
//            metaPrintln(level, "}");
//            metaPrintln(level, "");
//        } catch (JNAGenException ex) {
//            System.err.println(ex.getMessage());
//            System.err.println("While importing method " + m);
//            throw ex;
//        }
//    }
//    
//        private void printGlobalSetter(InterfaceDecl interfaceDecl, Method m, int level) throws JNAGenException {
//        try {
//            printFunctionPtr(interfaceDecl, m, level);
//            printMethodDecl(m, level);
//
//            Class[] parameterTypes = m.getParameterTypes();
//            int numParams = parameterTypes.length;
//            String[] nativeParams = createNativeParams(m, level);
//            metaPrint(level + 1, "int result0 = ");
//            metaPrint(0, getPtrName(m) + ".call" + numParams + "(");
//            for (int i = 0; i < numParams; i++) {
//                String comma = i != 0 ? ", " : "";
//                metaPrint(0, comma + nativeParams[i]);
//            }
//            metaPrintln(0, ");");
//            convertReturn(m.getReturnType(), level);
//            cleanUpNativeParams(m, nativeParams, level);
//            metaPrintln(level + 1, "return result;");
//
//            metaPrintln(level, "}");
//            metaPrintln(level, "");
//        } catch (JNAGenException ex) {
//            System.err.println(ex.getMessage());
//            System.err.println("While importing method " + m);
//            throw ex;
//        }
//    }
        
    private void printMethod(InterfaceDecl interfaceDecl, Method m, int level) throws JNAGenException {
        try {
            printFunctionPtr(interfaceDecl, m, level);
            printMethodDecl(m, level);

            Class[] parameterTypes = m.getParameterTypes();
            int numParams = parameterTypes.length;
            String[] nativeParams = createNativeParams(m, level);
            metaPrint(level + 1, "int result0 = ");
            metaPrint(0, getPtrName(m) + ".call" + numParams + "(");
            for (int i = 0; i < numParams; i++) {
                String comma = i != 0 ? ", " : "";
                metaPrint(0, comma + nativeParams[i]);
            }
            metaPrintln(0, ");");
            convertReturn(m.getReturnType(), level);
            cleanUpNativeParams(m, nativeParams, level);
            metaPrintln(level + 1, "return result;");

            metaPrintln(level, "}");
            metaPrintln(level, "");
        } catch (JNAGenException ex) {
            System.err.println(ex.getMessage());
            System.err.println("While importing method " + m);
            throw ex;
        }
    }
        
    private void printMethods(InterfaceDecl interfaceDecl, int level) throws JNAGenException {   
        if (interfaceDecl.methods.size() == 0) {
            return;
        }
        
        metaPrintln(level, "/*----------------------------- methods -----------------------------*/");
        for (Method m : interfaceDecl.methods) {
            printMethod(interfaceDecl, m, level);
        }
        out.println();
    }
        
    /*------------------------- Structures  --------------------------*/
    
    private static String getNativeTypeName(Class c) {
        // TODO: Allow type name mappings....
        return "struct " + c.getSimpleName();
    }
    
    private static String getNativeTypeName(Field f) {
        return getNativeTypeName(f.getDeclaringClass());
    }
    
    /**
     * @param f field
     * @return a String that C will evaluate as the byte offset of this field
     */
    private static String getOffsetStr(Field f) {
        return "offsetof(" + getNativeTypeName(f) + ", " + f.getName() + ")";
    }
    
    /**
     * @param c  class that describes a C struct
     * @return a String that C will evaluate as the size of a struct in bytes
     */
    private static String getSizeofStr(Class c) {
        return "sizeof(" + getNativeTypeName(c) + ")";
    }
    
    private void printFieldDecl(Field f, int level) {
        metaPrintln(level, "public " + f.getType().getSimpleName() + " " + f.getName() + ";");
    }
    
    private void printFieldReader(Field f, int level) {
        metaPrintln(level + 1, f.getName() + " = p.getInt(%d);", new String[] {getOffsetStr(f)});
    }
    
    private void printFieldWriter(Field f, int level) {
        metaPrintln(level + 1, "p.setInt(%d, " + f.getName() + ");", new String[] {getOffsetStr(f)});
    }
    
    private void printStructSupport(InterfaceDecl interfaceDecl, int level) throws JNAGenException {
        if (interfaceDecl.instanceVars.size() == 0) {
            return;
        }
                        
        for (Field f : interfaceDecl.instanceVars) {
            printFieldDecl(f, level);
        }
        metaPrintln(0, "");
        
        metaPrintln(level, "public int size() {");
        metaPrintln(level + 1, "return %d;", new String[] {getSizeofStr(interfaceDecl.interfaceClass)});
        metaPrintln(level, "}");
        metaPrintln(0, "");
        
        metaPrintln(level, "public void read() {");
        metaPrintln(level + 1,     "Pointer p = getPointer();");
        for (Field f : interfaceDecl.instanceVars) {
            printFieldReader(f, level);
        }
        metaPrintln(level, "}");
        metaPrintln(0, "");
        
        metaPrintln(level, "public void write() {");
        metaPrintln(level + 1,     "Pointer p = getPointer();");
        for (Field f : interfaceDecl.instanceVars) {
            printFieldWriter(f, level);
        }
        metaPrintln(level, "}");
        metaPrintln(0, "");

    }
               
    /*------------------------- CLASSES --------------------------*/

    private void printClassHeader(InterfaceDecl interfaceDecl, int level) {
        if (level == 1) {
            metaPrintln(level - 1, "public class " + interfaceDecl.interfaceClass.getSimpleName() + "Import extends Library {");
        } else {
            metaPrintln(level - 1, "public static class " + interfaceDecl.interfaceClass.getSimpleName() + "Impl extends Structure {");
        }
        metaPrintln(level - 1, "");
        out.println();
    }
    
    private void printClassFooter(InterfaceDecl interfaceDecl, int level) {
        metaPrintln(level - 1, "}");
        metaPrintln(level - 1, "\\n");
        out.println();
    }
    
    private void printInnerClasses(InterfaceDecl interfaceDecl, int level) throws JNAGenException {
        for (Class innerClass : interfaceDecl.interfaceClass.getDeclaredClasses()) {
            InterfaceDecl innerDecl = new InterfaceDecl(innerClass);
            printClass(innerDecl, level + 1);
        }
    }
        
    /**
     * Print all of the code for a class and it's inner classes
     * 
     * @param interfaceDecl
     * @param level 1 stands for top-level class.
     */
    private void printClass(InterfaceDecl interfaceDecl, int level) throws JNAGenException {
        printClassHeader(interfaceDecl, level);

        printDefines(interfaceDecl, level);

        if (level == 1) {
            printLibraryDefinition(interfaceDecl);
        }
        printVars(interfaceDecl, level);
        printMethods(interfaceDecl, level);

        printStructSupport(interfaceDecl, level);
        printInnerClasses(interfaceDecl, level);

        printClassFooter(interfaceDecl, level);
    }
    
        /*------------------------- MAIN --------------------------*/

   private void printMain(InterfaceDecl interfaceDecl) throws JNAGenException {
        out.println("int main(int argc, char *argv[]) {");
        out.println("    if (argc < 2) {");
        out.println("        fprintf(stderr, \"Usage <cmd> out_file_name\\n\");");
        out.println("        exit(1);");
        out.println("    }");
        out.println("");
        out.println("    chmod(argv[1], 0222);"); // make existing file writable (if exists)
        out.println("    out = fopen(argv[1], \"w\");");
        out.println("    if (out == NULL) {");
        out.println("        fprintf(stderr, \"File create failed for %s, errno = %d\\n\", argv[1], errno);");
        out.println("        exit(1);");
        out.println("    }");
        out.println("");
        out.println("    printCopyright();");
        out.println("");
        
        printPackages(interfaceDecl);
        printClass(interfaceDecl, 1);
        
        out.println("    fchmod(fileno(out), 0444);");
        out.println("    fclose(out);");
        out.println("}\n");
    }

    void generate(Class interfaceClass) {
        try {
            InterfaceDecl interfaceDecl = new InterfaceDecl(interfaceClass);

            Generator.printCopyright(this.getClass(), out);
            printIncludes(interfaceDecl);
            printCopyright(interfaceDecl);
            printMain(interfaceDecl);
        } catch (JNAGenException ex) {
            System.err.println("While importing library defined by " + interfaceClass);
        }
    }
    
    URL[] filesToURLs(File[] files) {
        URL[] result = new URL[files.length];
        for (int i = 0; i < files.length; i++) {
            try {
                result[i] = new URL("file:" + files[i].getPath() + "/");
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
    
    private String stripSuffix(String filename) {
        int index = filename.lastIndexOf('.');
        return filename.substring(0, index);
    }
    
    /**
     * Preprocess a given set of Java source files.
     *
     * @param   baseDir    the directory under which the "build" directory exists
     * @param   classDirs  the set of directories that are searched recursively for the classes files to be imported
     * @return the preprocessor output directory
     */
    public File generate(File baseDir, File[] classDirs) {
        // Get the output directory
        final File buildDir = Build.mkdir(baseDir, "build");
        
        System.out.println("    Generating intermediate JNA files in " + buildDir + "...");
        ClassLoader parent = this.getClass().getClassLoader(); // we need to load com.sun.cldc.jn.LibraryImport, which is in the same jar as this class...
        URLClassLoader loader = new URLClassLoader(filesToURLs(classDirs), parent);

        for (int i = 0; i != classDirs.length; ++i) {
            File classDir = classDirs[i];
            String classDirStr = classDir.getPath();
            //System.out.println("    Looking for class files in " + classDir + "...");

            FileSet fs = new FileSet(classDir, Build.JAVA_CLASS_SELECTOR);
            Iterator iterator = fs.list().iterator();
            while (iterator.hasNext()) {
                File inputFile = (File) iterator.next();
                if (inputFile.length() != 0 && inputFile.getName().indexOf('$') < 0) { // only look at outer classes. Inner classes will get sucked up by them
                    try {
                        String inFilePath = inputFile.getPath();
                        String classname = inFilePath.substring(classDirStr.length() + 1).replace(File.separatorChar, '.');
                        classname = stripSuffix(classname);
                        Class interfaceClass = Class.forName(classname, true, loader);

                        String outFileName = inputFile.getParent() + File.separator + stripSuffix(inputFile.getName()) + "Import.c";
                        File outputFile = fs.replaceBaseDir(new File(outFileName), buildDir);
                        Build.mkdir(outputFile.getParentFile());
                        System.out.println("        Creating " + outputFile);

                        out = new PrintWriter(outputFile);
                        generate(interfaceClass);
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (ClassNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } finally {
                        if (out != null) {
                            out.close();
                            out = null;
                        }
                    }
                }
            }

        }
        return buildDir;
    }
    
    @Override
    public void run(String[] args) throws BuildException {
        generate(baseDir, new File[] {new File(baseDir, "classes")});
    }

    @Override
    public void usage(String errMsg) {
        this.defaultUsage(null, errMsg);
    }

}
