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

package com.sun.squawk.debugger;

/**
 * A SDWPException is thrown when there is an error parsing or handling a JDWP/SDWP request.
 */
public class SDWPException extends Exception {

    private final int error;

    public SDWPException() {
        super("Generic SDWPException");
        this.error = JDWP.Error_INTERNAL;
    }

    public SDWPException(String msg) {
        super(msg);
        this.error = JDWP.Error_INTERNAL;
    }

    public SDWPException(int error, String msg) {
        super(msg);
        this.error = error;
    }

    public int getError() {
        return error;
    }

}
