//if[!AUTOGEN_JNA_NATIVES]
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

/* **** GENERATED FILE -- DO NOT EDIT ****
 *      generated by com.sun.cldc.jna.JNAGen
 *      from the CLDC/JNA Interface class com.sun.squawk.platform.posix.natives.Time
 */

package com.sun.squawk.platform.windows.natives;

import com.sun.cldc.jna.*;
import com.sun.cldc.jna.ptr.*;

public final class TimeImpl implements Time {

    /*----------------------------- defines -----------------------------*/

    public int initConstInt(int index) {
        final int[] dummy = {};
        return dummy[index];
    }
    
    public TimeImpl() {
//        NativeLibrary jnaNativeLibrary = Native.getLibraryLoading();
    }
    
    public static class timevalImpl extends Structure {
    
        protected timevalImpl() {}

        public int size() {
            return 8;
        }

        public void read() {
            Pointer p = getPointer();
            timeval o = (timeval)this;
            o.tv_sec = p.getInt(0);
            o.tv_usec = p.getInt(4);
        }

        public void write() {
            Pointer p = getPointer();
            timeval o = (timeval)this;
            p.setInt(0, (int)o.tv_sec);
            p.setInt(4, (int)o.tv_usec);
        }

    }
    

}


