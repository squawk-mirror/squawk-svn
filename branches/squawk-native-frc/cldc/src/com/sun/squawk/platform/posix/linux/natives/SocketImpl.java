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

/* **** HAND_GENERATED FILE
 */

package com.sun.squawk.platform.posix.linux.natives;

import com.sun.cldc.jna.*;
import com.sun.cldc.jna.ptr.*;

public class SocketImpl extends com.sun.squawk.platform.posix.natives.SocketImpl {

    /*----------------------------- defines -----------------------------*/

    private final static int[] intConstants = {
        /* public final static int AF_INET = */2,
        /* public final static int SOCK_STREAM =  */ 1,
        /* public final static int SOCK_DGRAM =  */ 2,
        /* public final static int SOCK_RAW =  */ 3,
        /* public final static int INADDR_ANY =  */ 0,
        /* public final static int INET_ADDRSTRLEN =  */ 16,
        /* public final static int SOL_SOCKET =  */ 65535,
        /* public final static int SO_DEBUG =  */ 1,
        /* public final static int SO_ACCEPTCONN =  */ 2,
        /* public final static int SO_REUSEADDR =  */ 4,
        /* public final static int SO_KEEPALIVE =  */ 8,
        /* public final static int SO_DONTROUTE =  */ 16,
        /* public final static int SO_BROADCAST =  */ 32,
        /* public final static int SO_OOBINLINE =  */ 256
    };
    
    private static boolean[] intConstantCheck;

    public int initConstInt(int index) {
        if (Native.DEBUG) {
             intConstantCheck = Native.doInitCheck(intConstantCheck, intConstants.length, index);
        }
        return intConstants[index];
    }

    /*----------------------------- methods -----------------------------*/
//    private final Function getsockoptPtr;
//
//    public int getsockopt(int arg0, int arg1, int arg2, ByReference arg3, IntByReference arg4) {
//        Pointer var3 = arg3.getPointer();
//        Pointer var4 = arg4.getPointer();
//        int result0 = getsockoptPtr.call5(arg0, arg1, arg2, var3, var4);
//        int result = (int)result0;
//        return result;
//    }
//
//    private final Function bindPtr;
//
//    public int bind(int arg0, sockaddr_in arg1) {
//        arg1.allocateMemory();
//        arg1.write();
//        Pointer var1 = arg1.getPointer();
//        int result0 = bindPtr.call2(arg0, var1);
//        int result = (int)result0;
//        arg1.read();
//        arg1.freeMemory();
//        return result;
//    }
//
//    private final Function listenPtr;
//
//    public int listen(int arg0, int arg1) {
//        int result0 = listenPtr.call2(arg0, arg1);
//        int result = (int)result0;
//        return result;
//    }
//
//    private final Function shutdownPtr;
//
//    public int shutdown(int arg0, int arg1) {
//        int result0 = shutdownPtr.call2(arg0, arg1);
//        int result = (int)result0;
//        return result;
//    }
//
//    private final Function inet_ntopPtr;
//
//    public String inet_ntop(int arg0, IntByReference arg1, Pointer arg2, int arg3) {
//        Pointer var1 = arg1.getPointer();
//        int result0 = inet_ntopPtr.call4(arg0, var1, arg2, arg3);
//        String result = Function.returnString(result0);
//        return result;
//    }
//
//    private final Function socketPtr;
//
//    public int socket(int arg0, int arg1, int arg2) {
//        int result0 = socketPtr.call3(arg0, arg1, arg2);
//        int result = (int)result0;
//        return result;
//    }
//
//    private final Function acceptPtr;
//
//    public int accept(int arg0, sockaddr_in arg1, IntByReference arg2) {
//        arg1.allocateMemory();
//        arg1.write();
//        Pointer var1 = arg1.getPointer();
//        Pointer var2 = arg2.getPointer();
//        int result0 = acceptPtr.call3(arg0, var1, var2);
//        int result = (int)result0;
//        arg1.read();
//        arg1.freeMemory();
//        return result;
//    }
//
//    private final Function connectPtr;
//
//    public int connect(int arg0, sockaddr_in arg1, int arg2) {
//        arg1.allocateMemory();
//        arg1.write();
//        Pointer var1 = arg1.getPointer();
//        int result0 = connectPtr.call3(arg0, var1, arg2);
//        int result = (int)result0;
//        arg1.read();
//        arg1.freeMemory();
//        return result;
//    }
//
//    private final Function setsockoptPtr;
//
//    public int setsockopt(int arg0, int arg1, int arg2, ByReference arg3, int arg4) {
//        Pointer var3 = arg3.getPointer();
//        int result0 = setsockoptPtr.call5(arg0, arg1, arg2, var3, arg4);
//        int result = (int)result0;
//        return result;
//    }
//
//    private final Function inet_ptonPtr;
//
//    public boolean inet_pton(String arg0, IntByReference arg1) {
//        Pointer var0 = Pointer.createStringBuffer(arg0);
//        Pointer var1 = arg1.getPointer();
//        int result0 = inet_ptonPtr.call2(var0, var1);
//        boolean result = (result0 == 0) ? false : true;
//        var0.free();
//        return result;
//    }
//
    public SocketImpl() {
//        NativeLibrary jnaNativeLibrary = Native.getLibraryLoading();
//        getsockoptPtr = jnaNativeLibrary.getFunction("getsockopt");
//        bindPtr = jnaNativeLibrary.getFunction("bind");
//        listenPtr = jnaNativeLibrary.getFunction("listen");
//        shutdownPtr = jnaNativeLibrary.getFunction("shutdown");
//        inet_ntopPtr = jnaNativeLibrary.getFunction("inet_ntop");
//        socketPtr = jnaNativeLibrary.getFunction("socket");
//        acceptPtr = jnaNativeLibrary.getFunction("accept");
//        connectPtr = jnaNativeLibrary.getFunction("connect");
//        setsockoptPtr = jnaNativeLibrary.getFunction("setsockopt");
//        inet_ptonPtr = jnaNativeLibrary.getFunction("inet_pton");
    }
    

}


