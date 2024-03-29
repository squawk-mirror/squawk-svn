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

/* This is a HAND-GENERATED INTERFACE to  <sys/ioctl.h>
 */

package com.sun.squawk.platform.posix.solaris.natives;

import com.sun.cldc.jna.*;
import com.sun.cldc.jna.ptr.*;

public class IoctlImpl extends com.sun.squawk.platform.posix.natives.IoctlImpl {

    /*----------------------------- defines -----------------------------*/

    /*
     * Ioctl's have the command encoded in the lower word, and the size of
     * any in or out parameters in the upper word.  The high 3 bits of the
     * upper word are used to encode the in/out status of the parameter.
     */

    /**
     * WARNING: This is different on different systems
     * SOLARIS: 0xFF
     * MAC OS X: 0x1FFF
     *  LINUX, not used???
     */
    public final static int IOCPARM_MASK = 0xff;		/* parameter length, at most 13 bits */

    public final static int IOC_VOID = 0x20000000;
    /* copy parameters out */
    public final static int IOC_OUT = 0x40000000;
    /* copy parameters in */
    public final static int IOC_IN = 0x80000000;
    /* copy paramters in and out */
    public final static int IOC_INOUT = (IOC_IN | IOC_OUT);
    /* mask for IN/OUT/VOID */
    public final static int IOC_DIRMASK = 0xe0000000;

    private static int _IOC(int inout, char group, int num, int len) {
        return (inout | ((len & IOCPARM_MASK) << 16) | (((int) group) << 8) | (num));
    }

    private static  int _IO(char g, int n) {
        return _IOC(IOC_VOID, (g), (n), 0);
    }

    private static  int _IOR(char g, int n) {
        return _IOC(IOC_OUT, (g), (n), 4);
    }

    private static  int _IOW(char g, int n) {
        return _IOC(IOC_IN, (g), (n), 4);
    }
    /* this should be _IORW, but stdio got there first */

    private static  int _IOWR(char g, int n) {
        return _IOC(IOC_INOUT, (g), (n), 4);
    }

    private final static int[] intConstants = {
        /* public final static int FIOCLEX = */_IO('f', 1),
        /* public final static int FIONCLEX =  */ _IO('f', 2),
        /* public final static int FIONREAD =  */ _IOR('f', 127),
        /* public final static int FIONBIO =  */ _IOW('f', 126),
        /* public final static int FIOASYNC =  */ _IOW('f', 125),
        /* public final static int FIOSETOWN =  */ _IOR('f', 123),
        /* public final static int FIOGETOWN =  */ _IOR('f', 122)
    };

    private static boolean[] intConstantCheck;

    public int initConstInt(int index) {
        if (Native.DEBUG) {
             intConstantCheck = Native.doInitCheck(intConstantCheck, intConstants.length, index);
        }
        return intConstants[index];
    }

    /*----------------------------- methods -----------------------------*/

}


