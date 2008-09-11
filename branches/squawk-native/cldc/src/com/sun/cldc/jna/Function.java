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

import com.sun.squawk.Address;
import com.sun.squawk.NativeUnsafe;
import com.sun.squawk.VM;
import com.sun.squawk.vm.ChannelConstants;

/**
 * A pointer to a native function that can be called from Java.
 * 
 * <h3>Differences from JNA</h3>
 * <ul>
 * <li> Function is NOT a subclass of Pointer
 * <li> Invocation is via calls to one of the predfined "call()" methods, not using the generic invoke() method.
 * <li> Throws RuntimeExceptions instead of UnsatisfiedLinkErrors. Are link errors really "unrecoverable"? Platform independant code might want to work around missing functions.
 * <li> Calling conventions unimplemented
 * <li> no finalization in cldc, need to call dispose() explicitly (could add a shutdownhook though).
 * <li> no parseVersion();
 * <li> no getFile()
 * </ul>
 */
public final class Function {
    private final static boolean DEBUG = false;

    private final Address funcAddr;
    private final String name; // for debugging/tracing

    /**
     * Create a new function pointer with the given name to the given address
     * 
     * @param name the native name of the function
     * @param funcAddr the address of the native function
     */
    Function(String name, Address funcAddr) {
        this.funcAddr = funcAddr;
        this.name = name;
    }

    /**
     * Dynamically look up a native function by name.
     * 
     * Look up the symbol in the specified library
     * 
     * @param lib the runtime library to look in
     * @param funcName
     * @return an object that can be used to call the named function
     * @throws RuntimeException if there is no function by that name.
     */
    public static Function getFunction(NativeLibrary lib, String funcName) {
        Pointer name0 = Pointer.createStringBuffer(funcName);
        int result = VM.execSyncIO(ChannelConstants.DLSYM, 0, name0.address().toUWord().toInt(), 0, 0, 0, 0, null, null);
        name0.free();
        if (DEBUG) {
            VM.print("Function Lookup for ");
            VM.print(funcName);
            VM.print(" = ");
            VM.printAddress(Address.fromPrimitive(result));
            VM.println();
        }
        if (result == 0) {
            throw new RuntimeException("Can't find native symbol " + funcName);
        }
        return new Function(funcName, Address.fromPrimitive(result));
    }
    
    /**
     * Dynamically look up a native function by name.
     * 
     * Look up the symbol in the default list of loaded libraries.
     * 
     * @param funcName
     * @return an object that can be used to call the named function
     * @throws RuntimeException if there is no function by that name.
     */
    public static Function getFunction(String funcName) {
        return getFunction(NativeLibrary.getDefaultInstance(), funcName);      
    }

     /**
     * Dynamically look up a native function by name in the named library.
     * 
     * @param libraryName 
     * @param funcName
     * @return an object that can be used to call the named function
     * @throws RuntimeException if there is no function by that name.
     */
    public static Function getFunction(String libraryName, String funcName) {
        return getFunction(NativeLibrary.getInstance(libraryName), funcName);      
    }
    
    /**
     * @return the function name
     */
    public String getName() {
        return name;
    }
    
    public String toString() {
        return "Function(" + name + ", " + funcAddr.toUWord().toInt() + ")";
    }

    /**
     * Call a function pointer with no arguments
     * @return return value
     */
    public int call0() {
        if (DEBUG) {
            VM.print(name);
            VM.println(".call0");
        }
        return NativeUnsafe.call0(funcAddr);
    }

    /**
     * Call a function pointer with one arguments
     */
    public int call1(int i1) {
        if (DEBUG) {
            VM.print(name);
            VM.println(".call1");
        }
        return NativeUnsafe.call1(funcAddr, i1);
    }

    /**
     * Call a function pointer with two arguments
     */
    public int call2(int i1, int i2) {
        if (DEBUG) {
            VM.print(name);
            VM.println(".call2");
        }
        return NativeUnsafe.call2(funcAddr, i1, i2);
    }

    /**
     * Call a function pointer with three arguments
     */
    public int call3(int i1, int i2, int i3) {
        if (DEBUG) {
            VM.print(name);
            VM.println(".call3");
        }
        return NativeUnsafe.call3(funcAddr, i1, i2, i3);
    }

    /**
     * Call a function pointer with four arguments
     */
    public int call4(int i1, int i2, int i3, int i4) {
        if (DEBUG) {
            VM.print(name);
            VM.println(".call4");
        }
        return NativeUnsafe.call4(funcAddr, i1, i2, i3, i4);
    }

    /**
     * Call a function pointer with five arguments
     */
    public int call5(int i1, int i2, int i3, int i4, int i5) {
        if (DEBUG) {
            VM.print(name);
            VM.println(".call5");
        }
        return NativeUnsafe.call5(funcAddr, i1, i2, i3, i4, i5);
    }

    /**
     * Call a function pointer with one arguments
     */
    public int call1(Pointer p1) {
        return call1(p1.address().toUWord().toPrimitive());
    }

    /**
     * Call a function pointer with two arguments
     */
    public int call2(Pointer p1, Pointer p2) {
        return call2(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive());
    }

    /**
     * Call a function pointer with two arguments
     */
    public int call2(Pointer p1, int i2) {
        return call2(p1.address().toUWord().toPrimitive(), i2);
    }

    /**
     * Call a function pointer with two arguments
     */
    public int call2(int i1, Pointer p2) {
        return call2(i1, p2.address().toUWord().toPrimitive());
    }

    /**
     * Call a function pointer with three arguments
     */
    public int call3(int i1, Pointer p2, int i3) {
        return call3(i1, p2.address().toUWord().toPrimitive(), i3);
    }
    
    public int call3(int i1, Pointer p2, Pointer p3) {
        return call3(i1, p2.address().toUWord().toPrimitive(), p3.address().toUWord().toPrimitive());
    }

    /**
     * Call a function pointer with five arguments
     */
    public int call5(int i1, Pointer p2, Pointer p3, Pointer p4, Pointer p5) {
        return call5(i1,
                p2.address().toUWord().toPrimitive(),
                p3.address().toUWord().toPrimitive(),
                p4.address().toUWord().toPrimitive(),
                p5.address().toUWord().toPrimitive());
    }
 
    public int call5(int i1, int i2, int i3, Pointer p4, int i5) {
        return call5(i1, i2, i3,
                p4.address().toUWord().toPrimitive(),
                i5);
    }

    public int call5(int i1, int i2, int i3, Pointer p4, Pointer p5) {
        return call5(i1, i2, i3,
                p4.address().toUWord().toPrimitive(),
                p5.address().toUWord().toPrimitive());
    }
    
    /**
     * Standard conversion function that creates an structure instance of type <code>klass</code> from a C address <code>ptr</code>.
     * If <code>addr0</code> is not NULL, create a new Structure object and copy the data
     *  from the C struct to the Structure object.
     * 
     * @param klass 
     * @param ptr the raw native address of the C struct
     * @return null, or a Structure containing the data from C struct
     */
    public static Structure returnStruct(Class klass, int ptr) {
        Address addr = Address.fromPrimitive(ptr);
        if (addr.isZero()) {
            return null;
        } else {
            Structure result;
            try {
                result = (Structure) klass.newInstance();
                result.useMemory(new Pointer(addr, result.size()));
                result.read();
                return result;
            } catch (InstantiationException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Standard conversion function that creates java string from a C char* <code>ptr</code>.
     * 
     * @param ptr the raw native address of the C struct
     * @return null, or Java String containing the string in Java format
     */
    public static String returnString(int ptr) {
        Address result = Address.fromPrimitive(ptr);
        return Pointer.NativeUnsafeGetString(result);
    }
}
