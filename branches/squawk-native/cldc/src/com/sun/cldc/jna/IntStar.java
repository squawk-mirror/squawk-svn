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
 * A pointer to four bytes of storage.
 * Can be used for IN/OUT parameters.
 * <b>Must call freeMemory() when done!</b>
 */
public class IntStar extends Structure {

    /** 
     * Allocate the backing memory for a  IN/OUT *int parameter.<p>
     * <b>Must call freeMemory() when done!</b>
     * @param initialValue
     */
    public IntStar(int initialValue) {
        allocateMemory();
        set(initialValue);
    }

    public void read() {
    }

    public void write() {
    }

    public int get() {
        return backingNativeMemory.getInt(0);
    }

    public void set(int value) {
        backingNativeMemory.setInt(0, value);
    }

    public int size() {
        return 4;
    }
    
    private static final Function testIntStar1Ptr = Function.getFunction("testIntStar1");
    private static final Function testIntStar2Ptr = Function.getFunction("testIntStar2");

    private static int testIntStar1Ptr(IntStar out) {
        return testIntStar1Ptr.call1(out.getPointer());
    }

    private static void testIntStar2Ptr(IntStar out) {
        testIntStar2Ptr.call1(out.getPointer());
    }

    public static void main(String[] args) {
        IntStar p = new IntStar(-1);

        int result = testIntStar1Ptr(p);
        System.out.println("testIntStar1Ptr = " + result + ", p = " + p.get());
        testIntStar2Ptr(p);
        System.out.println("testIntStar2Ptr p = " + p.get());
        
        p.freeMemory();
        System.out.println("Done test.");
    }
}
