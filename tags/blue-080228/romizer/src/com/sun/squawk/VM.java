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

package com.sun.squawk;

import java.io.*;
import java.util.*;

import com.sun.squawk.util.Assert;
import com.sun.squawk.vm.Native;

/**
 * The VM used when running the romizer.
 *
 */
public class VM {

    /*
     * Create the dummy isolate for romizing.
     */
    private static Isolate currentIsolate;

    /**
     * Flag to show if the extend bytecode can be executed.
     */
    static boolean extendsEnabled;
    
    
     /**
     * Flag to show if should be a bigEndian system
     */
    private static boolean bigEndian;

    /*=======================================================================*\
     *                           Romizer support                             *
    \*=======================================================================*/

    /**
     * The system-dependent path-separator character. This character is used to
     * separate filenames in a sequence of files given as a <em>path list</em>.
     * On UNIX systems, this character is <code>':'</code>; on Windows
     * systems it is <code>';'</code>.
     *
     * @return  the system-dependent path-separator character
     */
    public static char getPathSeparatorChar() {
        return File.pathSeparatorChar;
    }

    private static int nextIsolateID;
    static int allocateIsolateID() {
        return nextIsolateID++;
    }

    /**
     * The system-dependent default name-separator character.  This field is
     * initialized to contain the first character of the value of the system
     * property <code>file.separator</code>.  On UNIX systems the value of this
     * field is <code>'/'</code>; on Microsoft Windows systems it is <code>'\'</code>.
     *
     * @return <code>'/'</code> or <code>'\'</code>
     * @see     java.lang.System#getProperty(java.lang.String)
     */
    public static char getFileSeparatorChar() {
        return File.separatorChar;
    }

    protected static boolean isVerbose;
    
    public static boolean isVerbose() {
        return isVerbose;
    }
    
    public static void setVerbose(boolean verbose) {
        isVerbose = verbose;
    }

    public static boolean isVeryVerbose() {
        return false;
    }

    /**
     * Determines if the Squawk system is being run in a hosted environment
     * such as the romizer or mapper application.
     *
     * @return true if the Squawk system is being run in a hosted environment
     */
    public static boolean isHosted() {
        return true;
    }

    
    /**
     * On a hosted system , this calls System.setProperty(), otherwise calls Isolate.currentIsolate().setProperty()
     * @param name property name
     * @param value property value
     */
    public static void setProperty(String name, String value) {
        System.setProperty(name, value);
    }
    
    /**
     * Get the endianess.
     *
     * @return true if the system is big endian
     */
    public static boolean isBigEndian() {
        return bigEndian;
    }
    
    /**
     * Set the endianess.
     *
     * @param value new value of isBigEndian
     */
    public static void setIsBigEndian(boolean value) {
        bigEndian = value;
    }

    /**
     * Assert a condition.
     *
     * @param b a boolean value that must be true.
     */
    public static void assume(boolean b) {
        Assert.that(b);
    }

    /**
     * Get the isolate of the currently executing thread.
     *
     * @return the isolate
     */
    public static Isolate getCurrentIsolate() {
        return currentIsolate;
    }

    static void registerIsolate(Isolate isolate) {
    }
    
    /**
     * Set the isolate of the currently executing thread.
     *
     * @param isolate the isolate
     */
    static void setCurrentIsolate(Isolate isolate) {
        currentIsolate = isolate;
    }

    /*=======================================================================*\
     *                              Symbols stripping                        *
    \*=======================================================================*/

    static abstract class Matcher {

        public static final int PRECEDENCE_PACKAGE = 0;
        public static final int PRECEDENCE_CLASS = 1;
        public static final int PRECEDENCE_MEMBER = 2;
        
        public static final int KEEP = 0;
        public static final int STRIP = 1;
        public static final int INTERNAL = 2;

        final int action;
        final int precedence;

        Matcher(int action, int precedence) {
            this.action = action;
            this.precedence = precedence;
        }
        abstract boolean matches(String s);

        /**
         * Determines if this matcher represents a pattern that is more specific
         * than a matcher that matched some input also matched by this matcher.
         *
         * @param m  a matcher that precedes this one in the properties file
         *           and has also matched the same input this matcher just matched
         */
        abstract boolean moreSpecificThan(Matcher m);
    }

    static class PackageMatcher extends Matcher {
        private final String pkg;
        private final boolean recursive;
        PackageMatcher(String pattern, int action) {
            super(action, PRECEDENCE_PACKAGE);
            if (pattern.endsWith(".**")) {
                pkg = pattern.substring(0, pattern.length() - 3);
                recursive = true;
            } else {
                if (!pattern.endsWith(".*")) {
                    throw new IllegalArgumentException("Package pattern must end with \".*\" or \".**\"");
                }
                pkg = pattern.substring(0, pattern.length() - 2);
                recursive = false;
            }
        }

        boolean moreSpecificThan(Matcher m) {
            if (m instanceof PackageMatcher) {
                if (!recursive) {
                    return true;
                }
                PackageMatcher pm = (PackageMatcher)m;
                if (!pm.recursive) {
                    return false;
                }

                // both recursive so the longer package prefix is stronger
                return pkg.length() >= pm.pkg.length();
            } else {
                return precedence > m.precedence;
            }
        }

        boolean matches(String s) {
            if (recursive) {
                return s.startsWith(pkg);
            } else {
                if (!s.startsWith(pkg)) {
                    return false;
                }

                // Strip package prefix
                s = s.substring(pkg.length());

                // Matches if no more '.'s in class name
                return s.indexOf('.') == -1;
            }
        }
        
        public String toString() {
            return "PackageMatcher [" + pkg + (recursive ? ".*" : "") + ']';
        }
    }

    static class ClassMatcher extends Matcher {
        private final String pattern;
        ClassMatcher(String pattern, int action) {
            super(action, PRECEDENCE_CLASS);
            if (pattern.indexOf('#') != -1 || pattern.indexOf('*') != -1) {
                throw new IllegalArgumentException("Class name must not contain '*' or '#'");
            }
            this.pattern = pattern;
        }

        boolean moreSpecificThan(Matcher m) {
            if (m instanceof ClassMatcher) {
                // The class pattern is identical so take the one later
                // in the properties file (i.e. this one)
                return true;
            } else {
                return precedence > m.precedence;
            }
        }

        public boolean matches(String s) {
            int index = s.indexOf('#');
            if (index != -1) {
                s = s.substring(0, index);
            }
            return pattern.equals(s);
        }
        
        public String toString() {
            return "ClassMatcher [" + pattern + ']';
        }
    }

    static class MemberMatcher extends Matcher {
        private final String pattern;
        MemberMatcher(String pattern, int action) {
            super(action, PRECEDENCE_MEMBER);
            if (pattern.indexOf('*') != -1) {
                throw new IllegalArgumentException("Member name must not contain '*'");
            }
            this.pattern = pattern;
        }

        boolean moreSpecificThan(Matcher m) {
            if (m instanceof MemberMatcher) {
                // take the longer pattern
                return pattern.length() >= ((MemberMatcher)m).pattern.length();
            } else {
                return precedence > m.precedence;
            }
        }

        public boolean matches(String s) {
            if (s.indexOf('#') == -1) {
                return false;
            }
            return s.startsWith(pattern);
        }
        
        public String toString() {
            return "MemberMatcher [" + pattern + ']';
        }
    }

    static List<Matcher> stripMatchers = new ArrayList<Matcher>();

    /**
     * Resets the settings used to determine what symbols are to be retained/stripped
     * when stripping a suite in library mode (i.e. -strip:l) or extendable library
     * mode (i.e. -strip:e). An element that is <code>private</code> will always be stripped
     * in these modes and an element that is package <code>private</code> will always be
     * stripped in library mode.
     * <p>
     * The key of each property in the file specifies a pattern that will be used to match
     * a class, field or method that may be stripped. The value for a given keep must be
     * "keep", "strip", or "internal". 
     *
     *  keep: Keep the symbols that match the pattern. 
     *
     *  strip: Remove the symbols that match the pattern. 
     *         If the underlying method is not called within the suite, it me be removed.
     *
     *  internal: Remove the symbols that match the pattern, but keep the matching methods
     *            even if not called within the suite. This is used when multiple suites are compiled togther.
     *            The child suites can call methods even though symbols were stripped.
     * 
     * There are 3 different types of patterns that can be specified:
     *
     * 1. A package pattern ends with ".*" or ".**". The former is used to match an
     *    element in a package and the latter extends the match to include any sub-package.
     *    For example:
     *
     *    java.**=keep
     *    javax.**=keep
     *    com.sun.**=strip
     *    com.sun.squawk.*=keep
     *
     *    This will keep all the symbols in any package starting with "java." or "javax.".
     *    All symbols in a package starting with "com.sun." will also be stripped <i>except</i>
     *    for symbols in the "com.sun.squawk" package.
     *
     *    This also show the precedence between patterns. A more specific pattern takes
     *    precedence over a more general pattern. If two patterns matching some given input
     *    are identical, then the one occurring lower in the properties file has higher precedence.
     *
     *  2. A class pattern is a fully qualified class name. For example:
     *
     *    com.sun.squawk.Isolate=keep
     *
     *  3. A field or method pattern is a fully qualified class name joined to a field or method
     *     name by a '#' and (optionally) suffixed by parameter types for a method. For example:
     *
     *    com.sun.squawk.Isolate#isTckTest=strip
     *    com.sun.squawk.Isolate#clearErr(java.lang.String)=keep
     *    com.sun.squawk.Isolate#removeThread(com.sun.squawk.VMThread,boolean)=strip
     *
     * A member pattern takes precedence over a class pattern which in turn takes precedence
     * over a package pattern.
     *
     * @param path  the properties file with the settings
     */
    static void resetSymbolsStripping(final File path) {
        stripMatchers.clear();
        boolean enableDynamicClassloading = Boolean.parseBoolean(Romizer.getBuildProperty("ENABLE_DYNAMIC_CLASSLOADING"));
        try {
            FileInputStream fis = new FileInputStream(path);
            Properties properties = new Properties();
            properties.load(fis);
            fis.close();
            System.out.println("Loaded suite stripping settings from " + path);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String k = (String) entry.getKey();
                String v = (String) entry.getValue();

                int action;
                if ("keep".equalsIgnoreCase(v)) {
                    action = Matcher.KEEP;
                } else if ("strip".equalsIgnoreCase(v)) {
                    action = Matcher.STRIP;
                } else if ("internal".equalsIgnoreCase(v)) {
                    if (enableDynamicClassloading) {
                        action = Matcher.INTERNAL;
                    } else {
                        action = Matcher.STRIP;
                    }
                } else {
                    throw new IllegalArgumentException("value for property " + k + " in " + path + " must be 'keep', 'strip', or 'internal'");
                }

                if (k.endsWith("*")) {
                    stripMatchers.add(new PackageMatcher(k, action));
                } else if (k.indexOf('#') != -1) {
                    stripMatchers.add(new MemberMatcher(k, action));
                } else {
                    stripMatchers.add(new ClassMatcher(k, action));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading properties from " + path + ": " + e);
            stripMatchers.clear();
        }
    }

    /**
     * Determines the symbol's matcher
     *
     * @param s   a class, field or method identifier
     * @return    the most specific Matcher for the symbol, or null if there is no matcher.
     */
    private static Matcher getMatcher(String s) {
        Matcher current = null;
        for (Matcher m : stripMatchers) {
            if (m.matches(s) && (current == null || m.moreSpecificThan(current))) {
                current = m;
            }
        }
        return current;
    }
    
    /**
     * Determines if the symbols for a class, field or method should be stripped.
     *
     * @param s   a class, field or method identifier
     */
    private static boolean strip(String s) {
        Matcher current = getMatcher(s);
        return current != null && (current.action != Matcher.KEEP);
    }

    /**
     * Determines if all the symbolic information for a class should be stripped. This
     * is used during the bootstrap process by the romizer to strip certain classes
     * based on their names.
     *
     * @param klass         the class to consider
     * @return true if the class symbols should be stripped
     */
    public static boolean stripSymbols(Klass klass) {
        boolean result = strip(klass.getName());
        if (result & !isInternal(klass)) {
            klass.updateModifiers(Modifier.SUITE_PRIVATE);
        }
        return result;
    }

    /**
     * Create a string for this Member that will be used or matching.
     *
     * @param member the memeber to name
     * @return a string suitable or matchings
     */
    private static String getSymbolString(Member member) {
        String s = member.getDefiningClass().getName() + "#" + member.getName();
        if (member instanceof Method) {
            Method method = (Method)member;
            Klass[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 0) {
                s += "()";
            } else {
                StringBuffer buf = new StringBuffer(15);
                buf.append('(');
                for (int i = 0 ; i < parameterTypes.length ; i++) {
                    buf.append(parameterTypes[i].getInternalName());
                    if (i != parameterTypes.length - 1) {
                        buf.append(',');
                    }
                }
                buf.append(')');
                s += buf.toString();
            }
        }
        return s;
    }
    
    /**
     * Determines if all the symbolic information for a field or method should be stripped. This
     * is used during the bootstrap process by the romizer to strip certain fields and methods
     * based on their names.
     *
     * @param member        the method or field to consider
     * @return true if the class symbols should be stripped
     */
    public static boolean stripSymbols(Member member) {
        return strip(getSymbolString(member));
    }
    
    /**
     * Determines if the field or method is internal, so should be retained (even if symbol gets stripped)
     *
     * @param member        the method or field to consider
     * @return true if the class symbols should be stripped
     */
    public static boolean isInternal(Member member) {
        String s = getSymbolString(member);
        Matcher current = getMatcher(s);
        return current != null && (current.action == Matcher.INTERNAL);
    }
    
    /**
     * Determines if the klass is internal, so should be retained (even if symbol gets stripped)
     *
     * @param klass        the klass to consider
     * @return true if the class symbols should be stripped
     */
    public static boolean isInternal(Klass klass) {
        Matcher current = getMatcher(klass.getName());
        return current != null && (current.action == Matcher.INTERNAL);
    }
    
    /**
     * Support routine to get the object representing the class of a given object.
     * This takes into account whether or not the VM is running in hosted mode or
     * not. The returned object can only be used for identity comparisons.
     * @param object
     * @return Class
     */
    public static Object getClass(Object object) {
        return object.getClass();
    }

    /**
     * Support routine to test whether a given object is an array.
     * This takes into account whether or not the VM is running in hosted mode or
     * not.
     * @param object
     * @return true if object is an array
     */
    public static boolean isArray(Object object) {
        return object.getClass().isArray();
    }



    /*=======================================================================*\
     *                              Native methods                           *
    \*=======================================================================*/

    /**
     * Zero a block of memory.
     *
     * @param      start        the start address of the memory area
     * @param      end          the end address of the memory area
     */
    static void zeroWords(Address start, Address end) {
        // Not needed for the romizer.
    }

    /**
     * Determines if the VM was built with memory access type checking enabled.
     *
     * @return true
     */
    public static boolean usingTypeMap() {
        return /*VAL*/true/*TYPEMAP*/;
    }


    /*=======================================================================*\
     *                              Symbols dumping                          *
    \*=======================================================================*/


    public static final int STREAM_STDOUT = 0;
    public static final int STREAM_STDERR = 1;
    static final int STREAM_SYMBOLS = 2;
    static final int STREAM_HEAPTRACE = 3;

    static int stream = STREAM_STDOUT;
    static final PrintStream Streams[] = new PrintStream[4];

    /**
     * Sets the stream for the VM.print... methods to one of the STREAM_... constants.
     *
     * @param stream  the stream to use for the print... methods
     * @return old stream
     */
    public static int setStream(int stream) {
        Assert.always(stream >= STREAM_STDOUT && stream <= STREAM_HEAPTRACE, "invalid stream specifier");
        int old = VM.stream;
        VM.stream = stream;
        return old;
    }

    /**
     * Print an error message.
     *
     * @param msg the message
     */
    public static void println(String msg) {
        PrintStream out = Streams[stream];
        out.println(msg);
        out.flush();
    }

    public static void println(boolean x) {
        PrintStream out = Streams[stream];
        out.println(x);
        out.flush();
    }

    public static void println() {
        PrintStream out = Streams[stream];
        out.println();
        out.flush();
    }

    public static void print(String s) {
        PrintStream out = Streams[stream];
        out.print(s);
        out.flush();
    }

    public static void print(int i) {
        PrintStream out = Streams[stream];
        out.print(i);
        out.flush();
    }

    public static void print(char ch) {
        PrintStream out = Streams[stream];
        out.print(ch);
        out.flush();
    }

    static void printAddress(Address val) {
        printUWord(val.toUWord());
    }

    public static void printAddress(Object val) {
        PrintStream out = Streams[stream];
        out.print(val);
        out.flush();
    }

    public static void printUWord(UWord val) {
        PrintStream out = Streams[stream];
        out.print(val);
        out.flush();
    }

    public static void printOffset(Offset val) {
        PrintStream out = Streams[stream];
        out.print(val);
        out.flush();
    }

    /**
     * Stop fatally.
     */
    public static void fatalVMError() {
        throw new Error();
    }

    /*=======================================================================*\
     *                             Object graph copying                      *
    \*=======================================================================*/

    /**
     * Make a copy of the object graph rooted at a given object.
     *
     * @param object    the root of the object graph to copy
     * @return the ObjectMemorySerializer.ControlBlock instance that contains the serialized object graph and
     *                  its metadata. This will be null if there was insufficient memory
     *                  to do the serialization.
     */
    static ObjectMemorySerializer.ControlBlock copyObjectGraph(Object object) {
        assume(object instanceof Suite);
        return ObjectGraphSerializer.serialize(object);
    }

    /**
     * Copy memory from one array to another.
     *
     * @param      src          the source array.
     * @param      srcPos       start position in the source array.
     * @param      dst          the destination array.
     * @param      dstPos       start position in the destination data.
     * @param      length       the number of bytes to be copied.
     * @param      nvmDst       the destination buffer is in NVM
     *
     * @vm2c proxy
     */
    static void copyBytes(Object src, int srcPos, Object dst, int dstPos, int length, boolean nvmDst) {
    }

   /**
     * VM-private version of System.arraycopy for arrays of primitives that does little error checking.
     */
    public static void arraycopyPrimitive0(Object src, int src_position, Object dst, int dst_position, 
                                           int totalLength, int dataSize) {
        System.arraycopy(src, src_position, dst, dst_position, totalLength);
    }
    
    /**
     * VM-private version of System.arraycopy for arrays of objects that does little error checking.
     */
    public static void arraycopyObject0(Object src, int src_position, Object dst, int dst_position, 
                                           int length) {
        System.arraycopy(src, src_position, dst, dst_position, length);
    }
    
    /*=======================================================================*\
     *                          Native method lookup                         *
    \*=======================================================================*/

    /**
     * Hashtable to translate names into enumerations.
     */
    private static Hashtable<String, Integer> methodTable = new Hashtable<String, Integer>();

    /**
     * Hashtable of unused native methods.
     */
    private static Hashtable<String, String> unused = new Hashtable<String, String>();

    /**
     * Initializer.
     */
    static {
        Streams[STREAM_STDOUT] = System.out;
        Streams[STREAM_STDERR] = System.err;

        try {
            Class<?> clazz = com.sun.squawk.vm.Native.class;
            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            for (int i = 0 ; i < fields.length ; i++) {
                java.lang.reflect.Field field = fields[i];
                if (field.getType() == Integer.TYPE) {
                    String name = field.getName().replace('_', '.').replace('$', '.');
                    int number = field.getInt(null);
                    methodTable.put(name, new Integer(number));
                    unused.put(name, name);
                }
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
    }

    /**
     * Determines if a given native method can be linked to by classes dynamically
     * loaded into the Squawk VM.
     *
     * @param name   the fully qualified name of a native method
     * @return true if the method can be linked to
     */
    static boolean isLinkableNativeMethod(String name) {
        String table = Native.LINKABLE_NATIVE_METHODS;
        String last = null;
        int id = 0;
        int start = 0;
        int end = table.indexOf(' ');
        while (end != -1) {
            int sharedSubstringLength = table.charAt(start++) - '0';
            String entryName = table.substring(start, end);

            // Prepend prefix shared with previous entry (if any)
            if (sharedSubstringLength != 0) {
                Assert.that(last != null);
                entryName = last.substring(0, sharedSubstringLength) + entryName;
            }

            if (entryName.equals(name)) {
                return true;
            }

            start = end + 1;
            end = table.indexOf(' ', start);
            last = entryName;
            id++;
        }
        return false;
    }

    /**
     * Gets the identifier for a registered native method.
     *
     * @param name   the fully qualified name of the native method
     * @return the identifier for the method or -1 if the method has not been registered
     */
    public static int lookupNative(String name) {
        Integer id = methodTable.get(name);
        if (id != null) {
            unused.remove(name);
            return id.intValue();
        }
        return -1;
    }

    /**
     * Print all the symbols in a form that will go into a properties file.
     *
     * @param out stream to print to
     */
    public static void printNatives(PrintStream out) {
    	for (Map.Entry<String, Integer> entry : methodTable.entrySet()) {
            out.println("NATIVE." + entry.getKey() + ".NAME=" + entry.getValue());
        }


/* Uncomment to get list of apparently unused native methods */
/*
        Enumeration keys = unused.keys();
        while (keys.hasMoreElements()) {
            System.err.println("Warning: Unused native method "+keys.nextElement());
        }
*/
    }

    private VM() {
    }
    
}
