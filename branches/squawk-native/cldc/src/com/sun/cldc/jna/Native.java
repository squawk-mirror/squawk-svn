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

/**
 * Generic machinery to support access to native code.
 * 
 * <h3>Differences from JNA</h3>
 * <ul>
 * <li> Throws RuntimeExceptions instead of UnsatisfiedLinkErrors. Are link errors really "unrecoverable"? Platform independant code might want to work around missing functions.
 * <li> Search paths unimplemented
 * <li> Calling conventions unimplemented
 * <li> no finalization in cldc, need to call dispose() explicitly (could add a shutdownhook though).
 * <li> no parseVersion();
 * <li> no getFile()
 * </ul>
 */
public class Native {
    private final static boolean DEBUG = false;
    
    // TODO: Fix these
    public static final int POINTER_SIZE = 4;
    public static final int LONG_SIZE = 4;
    public static final int WCHAR_SIZE = 2;

    public final static String DEFAULT = "RTLD";
            
    private Native() {}
    
    public static Library loadLibrary(String name,
                                      Class interfaceClass) {
        try {

            NativeLibrary nl;
            if (name.equals(DEFAULT)) {
                nl = NativeLibrary.getDefaultInstance();
            } else {
                nl = NativeLibrary.getInstance(name);
            }
            Class implClass = Class.forName(interfaceClass.getName() + "Impl");
            Object implementation = implClass.newInstance();

            return (Library) implementation;
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }
   
    
}
