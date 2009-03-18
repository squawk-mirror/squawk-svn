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

package com.sun.squawk.platform.posix.vxworks.natives;

import com.sun.squawk.platform.posix.natives.*;
import com.sun.cldc.jna.*;
import com.sun.cldc.jna.ptr.*;
import com.sun.squawk.Address;

public class LibCImpl extends com.sun.squawk.platform.posix.natives.LibCImpl {
    private final static boolean DEBUG = true;

    /*----------------------------- defines -----------------------------*/
//
//    public final static int EPERM = 1;
//    public final static int ENOENT = 2;
//    public final static int ESRCH = 3;
//    public final static int EINTR = 4;
//    public final static int EIO = 5;
//    public final static int ENXIO = 6;
//    public final static int E2BIG = 7;
//    public final static int ENOEXEC = 8;
//    public final static int EBADF = 9;
//    public final static int ECHILD = 10;
//    public final static int EDEADLK = 33;
//    public final static int ENOMEM = 12;
//    public final static int EACCES = 13;
//    public final static int EFAULT = 14;
//    public final static int EBUSY = 16;
//    public final static int EEXIST = 17;
//    public final static int EXDEV = 18;
//    public final static int ENODEV = 19;
//    public final static int ENOTDIR = 20;
//    public final static int EISDIR = 21;
//    public final static int EINVAL = 22;
//    public final static int ENFILE = 23;
//    public final static int EMFILE = 24;
//    public final static int ENOTTY = 25;
//    public final static int ETXTBSY = 63;
//    public final static int EFBIG = 27;
//    public final static int ENOSPC = 28;
//    public final static int ESPIPE = 29;
//    public final static int EROFS = 30;
//    public final static int EMLINK = 31;
//    public final static int EPIPE = 32;
//    public final static int EDOM = 37;
//    public final static int ERANGE = 38;
//    public final static int EAGAIN = 11;
//    public final static int EWOULDBLOCK = 70;
//    public final static int EINPROGRESS = 68;
//    public final static int EALREADY = 69;
//    public final static int ENOTSOCK = 50;
//    public final static int EDESTADDRREQ = 40;
//    public final static int EMSGSIZE = 36;
//    public final static int EPROTOTYPE = 41;
//    public final static int ENOPROTOOPT = 42;
//    public final static int EPROTONOSUPPORT = 43;
//    public final static int ENOTSUP = 35;
//    public final static int EAFNOSUPPORT = 47;
//    public final static int EADDRINUSE = 48;
//    public final static int EADDRNOTAVAIL = 49;
//    public final static int ENETDOWN = 62;
//    public final static int ENETUNREACH = 51;
//    public final static int ENETRESET = 52;
//    public final static int ECONNABORTED = 53;
//    public final static int ECONNRESET = 54;
//    public final static int ENOBUFS = 55;
//    public final static int EISCONN = 56;
//    public final static int ENOTCONN = 57;
//    //#define	ESHUTDOWN	58		/* Can't send after socket shutdown */
////#define	ETOOMANYREFS	59		/* Too many references: can't splice */
//    public final static int ETIMEDOUT = 60;
//    public final static int ECONNREFUSED = 61;
//    public final static int ELOOP = 64;
//    public final static int ENAMETOOLONG = 26;
//    public final static int EHOSTUNREACH = 65;
//    //#define	ENOTBLK		66		/* Block device required */
//    //#define	EHOSTDOWN	67		/* Host is down */
//    public final static int ENOTEMPTY = 15;
//    public final static int EDQUOT = 83;
//    public final static int ENOLCK = 34;
//    public final static int ENOSYS = 71;
//    public final static int EOVERFLOW = 85;
//    public final static int ECANCELED = 72;
//    public final static int EIDRM = 84;
//    public final static int ENOMSG = 80;
//    //#define EFPOS		81		/* File positioning error *
//    public final static int EILSEQ = 82;
//    public final static int EBADMSG = 77;
//    public final static int EMULTIHOP = 86;
//    public final static int ENODATA = 78;
//    public final static int ENOLINK = 87;
//    public final static int ENOSR = 74;
//    public final static int ENOSTR = 75;
//    public final static int EPROTO = 76;
//    public final static int ETIME = 79;
//    public final static int F_DUPFD = 0;
//    public final static int F_GETFD = 1;
//    public final static int F_SETFD = 2;
//    public final static int F_GETFL = 3;
//    public final static int F_SETFL = 4;
//    public final static int O_RDONLY = 0;
//    public final static int O_WRONLY = 1;
//    public final static int O_RDWR = 2;
//    public final static int O_ACCMODE = 3;
//    public final static int O_NONBLOCK = 4;
//    public final static int O_APPEND = 8;
//    public final static int O_SYNC = 128;
//    public final static int O_CREAT = 512;
//    public final static int O_TRUNC = 1024;
//    public final static int O_EXCL = 2048;
//    public final static int S_IFBLK = 24576;
//    public final static int S_IFCHR = 8192;
//    public final static int S_IFDIR = 16384;
//    public final static int S_IFIFO = 4096;
//    public final static int S_IFLNK = 40960;
//    public final static int S_IFMT = 61440;
//    public final static int S_IFREG = 32768;
//    public final static int S_IFSOCK = 49152;
//    public final static int S_IRGRP = 32;
//    public final static int S_IROTH = 4;
//    public final static int S_IRUSR = 256;
//    public final static int S_IRWXG = 56;
//    public final static int S_IRWXO = 7;
//    public final static int S_IRWXU = 448;
//    public final static int S_ISGID = 1024;
//    public final static int S_ISUID = 2048;
//    public final static int S_ISVTX = 512;
//    public final static int S_IWGRP = 16;
//    public final static int S_IWOTH = 2;
//    public final static int S_IWUSR = 128;
//    public final static int S_IXGRP = 8;
//    public final static int S_IXOTH = 1;
//    public final static int S_IXUSR = 64;
//    public final static int SEEK_SET = 0;
//    public final static int SEEK_CUR = 1;
//    public final static int SEEK_END = 2;

    private final static int[] intConstants = {
/*public final static int EPERM =*/ 1,
/*public final static int ENOENT =*/ 2,
/*public final static int ESRCH =*/ 3,
/*public final static int EINTR =*/ 4,
/*public final static int EIO =*/ 5,
/*public final static int ENXIO =*/ 6,
/*public final static int E2BIG =*/ 7,
/*public final static int ENOEXEC =*/ 8,
/*public final static int EBADF =*/ 9,
/*public final static int ECHILD =*/ 10,
/*public final static int EDEADLK =*/ 33,
/*public final static int ENOMEM =*/ 12,
/*public final static int EACCES =*/ 13,
/*public final static int EFAULT =*/ 14,
/*public final static int EBUSY =*/ 16,
/*public final static int EEXIST =*/ 17,
/*public final static int EXDEV =*/ 18,
/*public final static int ENODEV =*/ 19,
/*public final static int ENOTDIR =*/ 20,
/*public final static int EISDIR =*/ 21,
/*public final static int EINVAL =*/ 22,
/*public final static int ENFILE =*/ 23,
/*public final static int EMFILE =*/ 24,
/*public final static int ENOTTY =*/ 25,
/*public final static int ETXTBSY =*/ 63,
/*public final static int EFBIG =*/ 27,
/*public final static int ENOSPC =*/ 28,
/*public final static int ESPIPE =*/ 29,
/*public final static int EROFS =*/ 30,
/*public final static int EMLINK =*/ 31,
/*public final static int EPIPE =*/ 32,
/*public final static int EDOM =*/ 37,
/*public final static int ERANGE =*/ 38,
/*public final static int EAGAIN =*/ 11,
/*public final static int EWOULDBLOCK =*/ 70,
/*public final static int EINPROGRESS =*/ 68,
/*public final static int EALREADY =*/ 69,
/*public final static int ENOTSOCK =*/ 50,
/*public final static int EDESTADDRREQ =*/ 40,
/*public final static int EMSGSIZE =*/ 36,
/*public final static int EPROTOTYPE =*/ 41,
/*public final static int ENOPROTOOPT =*/ 42,
/*public final static int EPROTONOSUPPORT =*/ 43,
/*public final static int ENOTSUP =*/ 35,
/*public final static int EAFNOSUPPORT =*/ 47,
/*public final static int EADDRINUSE =*/ 48,
/*public final static int EADDRNOTAVAIL =*/ 49,
/*public final static int ENETDOWN =*/ 62,
/*public final static int ENETUNREACH =*/ 51,
/*public final static int ENETRESET =*/ 52,
/*public final static int ECONNABORTED =*/ 53,
/*public final static int ECONNRESET =*/ 54,
/*public final static int ENOBUFS =*/ 55,
/*public final static int EISCONN =*/ 56,
/*public final static int ENOTCONN =*/ 57,
    //#define	ESHUTDOWN	58		/* Can't send after socket shutdown */
//#define	ETOOMANYREFS	59		/* Too many references: can't splice */
/*public final static int ETIMEDOUT =*/ 60,
/*public final static int ECONNREFUSED =*/ 61,
/*public final static int ELOOP =*/ 64,
/*public final static int ENAMETOOLONG =*/ 26,
/*public final static int EHOSTUNREACH =*/ 65,
    //#define	ENOTBLK		66		/* Block device required */
    //#define	EHOSTDOWN	67		/* Host is down */
/*public final static int ENOTEMPTY =*/ 15,
/*public final static int EDQUOT =*/ 83,
/*public final static int ENOLCK =*/ 34,
/*public final static int ENOSYS =*/ 71,
/*public final static int EOVERFLOW =*/ 85,
/*public final static int ECANCELED =*/ 72,
/*public final static int EIDRM =*/ 84,
/*public final static int ENOMSG =*/ 80,
    //#define EFPOS		81		/* File positioning error *
/*public final static int EILSEQ =*/ 82,
/*public final static int EBADMSG =*/ 77,
/*public final static int EMULTIHOP =*/ 86,
/*public final static int ENODATA =*/ 78,
/*public final static int ENOLINK =*/ 87,
/*public final static int ENOSR =*/ 74,
/*public final static int ENOSTR =*/ 75,
/*public final static int EPROTO =*/ 76,
/*public final static int ETIME =*/ 79,
/*public final static int F_DUPFD =*/ 0,
/*public final static int F_GETFD =*/ 1,
/*public final static int F_SETFD =*/ 2,
/*public final static int F_GETFL =*/ 3,
/*public final static int F_SETFL =*/ 4,
/*public final static int O_RDONLY =*/ 0,
/*public final static int O_WRONLY =*/ 1,
/*public final static int O_RDWR =*/ 2,
/*public final static int O_ACCMODE =*/ 3,
/*public final static int O_NONBLOCK =*/ 0x4000,
/*public final static int O_APPEND =*/ 8,
/*public final static int O_SYNC =*/  0x2000,
/*public final static int O_CREAT =*/ 0x0200,
/*public final static int O_TRUNC =*/ 0x0400,
/*public final static int O_EXCL =*/ 0x0800,
/*public final static int S_IFBLK =*/ 24576,
/*public final static int S_IFCHR =*/ 8192,
/*public final static int S_IFDIR =*/ 16384,
/*public final static int S_IFIFO =*/ 4096,
/*public final static int S_IFLNK =*/ 40960,
/*public final static int S_IFMT =*/ 61440,
/*public final static int S_IFREG =*/ 32768,
/*public final static int S_IFSOCK =*/ 49152,
/*public final static int S_IRGRP =*/ 32,
/*public final static int S_IROTH =*/ 4,
/*public final static int S_IRUSR =*/ 256,
/*public final static int S_IRWXG =*/ 56,
/*public final static int S_IRWXO =*/ 7,
/*public final static int S_IRWXU =*/ 448,
/*public final static int S_ISGID =*/ 1024,
/*public final static int S_ISUID =*/ 2048,
/*public final static int S_ISVTX =*/ 512,
/*public final static int S_IWGRP =*/ 16,
/*public final static int S_IWOTH =*/ 2,
/*public final static int S_IWUSR =*/ 128,
/*public final static int S_IXGRP =*/ 8,
/*public final static int S_IXOTH =*/ 1,
/*public final static int S_IXUSR =*/ 64,
/*public final static int SEEK_SET =*/ 0,
/*public final static int SEEK_CUR =*/ 1,
/*public final static int SEEK_END =*/ 2,
/*public final static int EOPNOTSUPP =*/ 45};

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

    /**
     * @TODO: This is a little messy - errno really IS a symbol in vxworks, but the headers say call __errno() and deref that!
     */
//    private final Function errnoPtr;
//
//    public int errno() {
//        int result0 = errnoPtr.call0();
//        Pointer p = new Pointer(result0);
//        int result = p.getInt(0);
//        return result;
//    }

     private final Pointer errnoPtr;

    public int errno() {
        return errnoPtr.getInt(0);
    }
    
    public LibCImpl() {
        NativeLibrary jnaNativeLibrary = Native.getLibraryLoading();
       // errnoPtr = jnaNativeLibrary.getFunction("__errno");
                errnoPtr = jnaNativeLibrary.getGlobalVariableAddress("errno", 4);

    }
    

}


