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
 * LibraryImport is the interface that all Library interface declarations should extend.
 * 
 * Import annotations can be used in Library declaration interfaces, but they will be stripped
 * by the preprocessor after code generation.
 */
public interface LibraryImport {
    /**
     * The value of any constant set to "IMPORT" will be the value of the C macro with the same name as the constant field's.
     */
    public final static int IMPORT = 0;
    
    /**
     * The value of any constant set to "DEFINED" will be true if there is a defiend C macro with the same name 
     * as the constant field's, otherwise false.
     */
    public final static boolean DEFINED = true;

}
