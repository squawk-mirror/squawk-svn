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
 *      This is a CLDC/JNA Interface class definition
 *      generated by com.sun.cldc.jna.JNAGen
 *      from the CLDC/JNA Interface class declaration in ./cldc-native-declarations/src/com/sun/squawk/platform/posix/natives/Select.java
 */
package com.sun.squawk.platform.posix.natives;

import com.sun.cldc.jna.*;

/**
 * java wrapper around #include <sys/select.h>
 *
 */
/*@Includes({"<sys/select.h>"})*/
public interface Select extends Library {

    Select INSTANCE = (Select) Native.loadLibrary("RTLD",
            Select.class);
    /**
     * The maximum number of file descriptors that a fd_set object can hold information about.
     */
    public static final int FD_SETSIZE = INSTANCE.initConstInt(0);
    /**
     * The maximum number of file descriptors that a fd_set object can hold information about.
     */
    public static final int fd_set_SIZEOF = INSTANCE.initConstInt(0);

    /**
     * Select() examines the I/O descriptor sets whose addresses are passed in readfds, writefds,
     * and errorfds to see if some of their descriptors are ready for reading, are ready for writ-
     * ing, or have an exceptional condition pending, respectively.
     *
     * On return, select() replaces the given descriptor sets
     * with subsets consisting of those descriptors that are ready for the requested operation.
     *
     * @param nfds The first nfds descriptors are checked in each set
     * @param readfds
     * @param writefds
     * @param errorfds
     * @param timeout if timout is nill, wait forever, if a pointer to a zero'd timeval, then does NOT wait.
     * @return the total number of ready descriptors in all the sets
     */
//    int select(int nfds, Pointer readfds,
//            Pointer writefds,
//            Pointer errorfds,
//            Pointer timeout);

    /**
     * removes fd from fdset
     *
     * @param fd
     * @param fd_set
     */
    /*@NativeName("sysFD_CLR")*/ // wrapper for macro FD_CLR
    void FD_CLR(int fd, Pointer fd_set);

    /**
     * includes a particular descriptor fd in fdset.
     *
     * @param fd
     * @param fd_set
     */
    /*@NativeName("sysFD_SET")*/ // wrapper for macro FD_SET
    void FD_SET(int fd, Pointer fd_set);

    /**
     * is non-zero if fd is a member of fd_set, zero otherwise.
     * @param fd
     * @param fd_set
     * @return
     */
   /*@NativeName("sysFD_ISSET")*/ // wrapper for macro FD_ISSET
    boolean FD_ISSET(int fd, Pointer fd_set);
}
