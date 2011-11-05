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
 *      from the CLDC/JNA Interface class com.sun.squawk.platform.posix.natives.LibC
 */

package com.sun.squawk.platform.posix.linux.natives;

import com.sun.cldc.jna.*;
import com.sun.cldc.jna.ptr.*;

public final class LibCImpl extends com.sun.squawk.platform.posix.natives.LibCImpl {

    /*----------------------------- defines -----------------------------*/

    private final static int[] intConstants = {
    /* public final static int EPERM = */1,
    /* public final static int ENOENT = */2,
    /* public final static int ESRCH = */3,
    /* public final static int EINTR = */4,
    /* public final static int EIO = */5,
    /* public final static int ENXIO = */6,
    /* public final static int E2BIG = */7,
    /* public final static int ENOEXEC = */8,
    /* public final static int EBADF = */9,
    /* public final static int ECHILD = */10,
    /* public final static int EDEADLK = */35,
    /* public final static int ENOMEM = */12,
    /* public final static int EACCES = */13,
    /* public final static int EFAULT = */14,
    /* public final static int EBUSY = */16,
    /* public final static int EEXIST = */17,
    /* public final static int EXDEV = */18,
    /* public final static int ENODEV = */19,
    /* public final static int ENOTDIR = */20,
    /* public final static int EISDIR = */21,
    /* public final static int EINVAL = */22,
    /* public final static int ENFILE = */23,
    /* public final static int EMFILE = */24,
    /* public final static int ENOTTY = */25,
    /* public final static int ETXTBSY = */26,
    /* public final static int EFBIG = */27,
    /* public final static int ENOSPC = */28,
    /* public final static int ESPIPE = */29,
    /* public final static int EROFS = */30,
    /* public final static int EMLINK = */31,
    /* public final static int EPIPE = */32,
    /* public final static int EDOM = */33,
    /* public final static int ERANGE = */34,
    /* public final static int EAGAIN = */11,
    /* public final static int EWOULDBLOCK = */11,
    // ---------- errnos diverge completely from solaris here --------
    /* public final static int EINPROGRESS = */115,
    /* public final static int EALREADY = */114,
    /* public final static int ENOTSOCK = */88,
    /* public final static int EDESTADDRREQ = */89,
    /* public final static int EMSGSIZE = */90,
    /* public final static int EPROTOTYPE = */ 91,
    /* public final static int ENOPROTOOPT = */ 92,
    /* public final static int EPROTONOSUPPORT = */ 93,
    /* public final static int ENOTSUP = */ 95,            /* NOT DEFINED FOR LINUX, == EOPNOTSUPP */
    /* public final static int EAFNOSUPPORT = */97,
    /* public final static int EADDRINUSE = */98,
    /* public final static int EADDRNOTAVAIL = */99,
    /* public final static int ENETDOWN = */100,
    /* public final static int ENETUNREACH = */101,
    /* public final static int ENETRESET = */102,
    /* public final static int ECONNABORTED = */103,
    /* public final static int ECONNRESET = */104,
    /* public final static int ENOBUFS = */105,
    /* public final static int EISCONN = */106,
    /* public final static int ENOTCONN = */107,
    /* public final static int ETIMEDOUT = */110,
    /* public final static int ECONNREFUSED = */111,
    /* public final static int ELOOP = */40,
    /* public final static int ENAMETOOLONG = */36,
    /* public final static int EHOSTUNREACH = */113,
    /* public final static int ENOTEMPTY = */39,
    /* public final static int EDQUOT = */122,
    /* public final static int ENOLCK = */37,
    /* public final static int ENOSYS = */38,
    /* public final static int EOVERFLOW = */75,
    /* public final static int ECANCELED = */125,
    /* public final static int EIDRM = */43,
    /* public final static int ENOMSG = */42,
    /* public final static int EILSEQ = */84,
    /* public final static int EBADMSG = */74,
    /* public final static int EMULTIHOP = */72,
    /* public final static int ENODATA = */61,
    /* public final static int ENOLINK = */67,
    /* public final static int ENOSR = */63,
    /* public final static int ENOSTR = */60,
    /* public final static int EPROTO = */71,
    /* public final static int ETIME = */62,
    //---------------------------
    /* public final static int F_DUPFD = */0,
    /* public final static int F_GETFD = */1,
    /* public final static int F_SETFD = */2,
    /* public final static int F_GETFL = */3,
    /* public final static int F_SETFL = */4,
    /* public final static int O_RDONLY = */0,
    /* public final static int O_WRONLY = */1,
    /* public final static int O_RDWR = */2,
    /* public final static int O_ACCMODE = */3,
    /* public final static int O_NONBLOCK = */2048,
    /* public final static int O_APPEND = */1024,
    /* public final static int O_SYNC = */4096,
    /* public final static int O_CREAT = */64,
    /* public final static int O_TRUNC = */512,
    /* public final static int O_EXCL = */128,
    /* public final static int S_IFBLK = */24576,
    /* public final static int S_IFCHR = */8192,
    /* public final static int S_IFDIR = */16384,
    /* public final static int S_IFIFO = */4096,
    /* public final static int S_IFLNK = */40960,
    /* public final static int S_IFMT = */61440,
    /* public final static int S_IFREG = */32768,
    /* public final static int S_IFSOCK = */49152,
    /* public final static int S_IRGRP = */32,
    /* public final static int S_IROTH = */4,
    /* public final static int S_IRUSR = */256,
    /* public final static int S_IRWXG = */56,
    /* public final static int S_IRWXO = */7,
    /* public final static int S_IRWXU = */448,
    /* public final static int S_ISGID = */1024,
    /* public final static int S_ISUID = */2048,
    /* public final static int S_ISVTX = */512,
    /* public final static int S_IWGRP = */16,
    /* public final static int S_IWOTH = */2,
    /* public final static int S_IWUSR = */128,
    /* public final static int S_IXGRP = */8,
    /* public final static int S_IXOTH = */1,
    /* public final static int S_IXUSR = */64,
    /* public final static int SEEK_SET = */0,
    /* public final static int SEEK_CUR = */1,
    /* public final static int SEEK_END = */2,
    /* public final static int EOPNOTSUPP = */ 95
        };

    private static boolean[] intConstantCheck;

    /**
     * Get the constant value for constant # index for this platform.
     * @param index
     * @return
     */
    public int initConstInt(int index) {
        if (Native.DEBUG) {
             intConstantCheck = Native.doInitCheck(intConstantCheck, intConstants.length, index);
        }
        return intConstants[index];
    }

    /*----------------------------- methods -----------------------------*/
    
    private final static int _STAT_VER = 1; /* thank you POSIX */

    /**
     * Implements stat by calling __xstat function
     */
    public int stat(String arg0, stat arg1) {
        Pointer var0 = Pointer.createStringBuffer(arg0);
        arg1.allocateMemory();
        arg1.write();
        Pointer var1 = arg1.getPointer();
        int result0 = statPtr.call3(_STAT_VER, var0, var1);
        int result = (int) result0;
        var0.free();
        arg1.read();
        arg1.freeMemory();
        return result;
    }

    /**
     *  Implements fstat by calling __fxstat function
     */
    public int fstat(int arg0, stat arg1) {
        arg1.allocateMemory();
        arg1.write();
        Pointer var1 = arg1.getPointer();
        int result0 = fstatPtr.call3(_STAT_VER, arg0, var1);
        int result = (int) result0;
        arg1.read();
        arg1.freeMemory();
        return result;
    }
    
    public String realName(String nominalName) {
        if (nominalName.equals("stat")) {
            return "__xstat";
        } else if (nominalName.equals("fstat")) {
            return "__fxstat";
        } else {
            return nominalName;
        }
    }

    /*----------------------------- variables -----------------------------*/

}