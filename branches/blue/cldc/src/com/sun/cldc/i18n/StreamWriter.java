/*
 * Copyright 1999-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.cldc.i18n;

import java.io.*;

/**
 * General prototype for character converting stream writers.
 *
 * @version 1.0 11/16/99
 */
public abstract class StreamWriter extends Writer {

    /** Output stream to write to */
    public OutputStream out;

    /**
     * Open the writer
     */
    public Writer open(OutputStream out, String enc)
        throws UnsupportedEncodingException {

        this.out = out;
        return this;
    }

    /**
     * Flush the writer and the output stream.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void flush() throws IOException {
        out.flush();   
    }

    /**
     * Close the writer and the output stream.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void close() throws IOException {
        out.close();      
    }

    /**
     * Get the size in bytes of an array of chars
     */
    public abstract int sizeOf(char[] array, int offset, int length);

}


