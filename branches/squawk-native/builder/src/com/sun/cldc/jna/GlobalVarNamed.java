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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The GlobalVarNamed annotation can be applied to method declarations in a 
 * Library to indicate that the body of the method should be a getter or setter 
 * or a C global variable with the specified name.
 * 
 * When applied to a method with no parameters and a non-void return type, 
 * a getter method will be generated for the C variable with
 * the specified name.
 * 
 * When applied to a method with one parameters and a void return type, 
 * a setter method will be generated for the C variable with
 * the specified name.
 * 
 * All other cases will result in an error at code generation time.
 * 
 * Example: Accessors for the C variable "errno"
 * 
 *    @GlobalVarNamed("errno")
 *    public int getLastError();
 * 
 *    @GlobalVarNamed("errno")
 *    public void setLastError(int value);
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalVarNamed {

    String value();
}