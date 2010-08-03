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
 * Native is a "stub" version of the runtime's Native class used by native library <i>?declarations</i>.
 * It is the interface that all Library interface declarations should extend.
 *
 * Import annotations can be used in Library declaration interfaces, but they will be stripped
 * by the preprocessor after code generation.
 */
public class Native {

    // TODO: Fix these
    public static final int POINTER_SIZE = 4;
    public static final int LONG_SIZE = 4;
    public static final int WCHAR_SIZE = 2;

    private Native() {}

    public static Library loadLibrary(String name,
                                      Class interfaceClass) {
        return null;
    }

}
