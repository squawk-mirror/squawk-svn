
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

package com.sun.squawk.platform.windows;

import com.sun.squawk.VM;
import com.sun.squawk.VMThread;
import com.sun.cldc.jna.Pointer;
import com.sun.squawk.platform.GCFSockets;
import com.sun.cldc.jna.ptr.IntByReference;
import com.sun.squawk.platform.windows.natives.*;
import com.sun.squawk.platform.windows.natives.LibC.*;
import com.sun.squawk.util.Assert;
import java.io.IOException;

/**
 * POSIX implementation of GCFSockets that calls the BSD socket API.
 */
public class GCFSocketsImpl implements GCFSockets {

    /** Read errno, try to clean up fd, and create exception. */
    private static IOException newError(int fd, String msg)  {
        int err_code = LibCUtil.errno();
        VM.print(msg);
        VM.print(": errno: ");
        VM.print(err_code);
        VM.println();
        Socket.INSTANCE.shutdown(fd, 2);
        LibC.INSTANCE.close(fd);
        return new IOException(" errno: " + err_code + " on fd: " + fd + " during " + msg);
    }

    private void set_blocking_flags(int fd, boolean is_blocking) throws IOException{
        int flags = LibC.INSTANCE.fcntl(fd, LibC.F_GETFL, 0);
        if (flags >= 0) {
            if (is_blocking == true) {
                flags &= ~LibC.O_NONBLOCK;
            } else {
                flags |= LibC.O_NONBLOCK;
            }
            int res = LibC.INSTANCE.fcntl(fd, LibC.F_SETFL, flags);
            if (res != -1) {
                return;
            }
        }
        throw newError(fd, "set_blocking_flags");
    }

    /**
     * @inheritDoc
     */
    public int open(String hostname, int port, int mode) throws IOException {
        // init_sockets(); win32 only
        int fd = -1;

        fd = Socket.INSTANCE.socket(Socket.AF_INET, Socket.SOCK_STREAM, 0);
//System.err.println("Socket.socket fd: " + fd);
        if (fd < 0) {
            throw newError(fd, "socket create");
        }

        set_blocking_flags(fd, /*is_blocking*/ false);

        NetDB.hostent phostent;
        // hostname is always NUL terminated. See socket/Protocol.java for detail.
        phostent = NetDB.INSTANCE.gethostbyname(hostname);
        if (phostent == null) {
            throw newError(fd, "gethostbyname (herrono = " + NetDB.INSTANCE.h_errno() + ")");
        }

        Socket.sockaddr_in destination_sin = new Socket.sockaddr_in();
        destination_sin.sin_family = Socket.AF_INET;
        destination_sin.sin_port = Inet.htons((short) port);
        destination_sin.sin_addr = phostent.h_addr_list[0];

//System.err.println("   addr  " + inet_ntop(destination_sin.sin_addr).toString());
//System.err.println("connect: hostname: " + hostname + " port: " + port + " mode: " + mode);

        if (Socket.INSTANCE.connect(fd, destination_sin, destination_sin.size()) < 0) {
            int err_code = LibCUtil.errno();
            if (err_code == LibC.EINPROGRESS || err_code == LibC.EWOULDBLOCK) {
                // When the socket is ready for connect, it becomes *writable*
                // (according to BSD socket spec of select())
                VMThread.getSystemEvents().waitForWriteEvent(fd);
            } else {
                throw newError(fd, "connect");
            }
        }

        return fd;
    }

    /**
     * Takes an IPv4 Internet address and returns string representing the address
     * in `.' notation
     *
     * @param in the opaque bytes of an IPv4 "struct in_addr"
     * @return String
     */
    public static String inet_ntop(int in) {
        Pointer charBuf = new Pointer(Socket.INET_ADDRSTRLEN);
        IntByReference addrBuf = new IntByReference(in); // the addr is passed by value (to handle IPv6)
        String result = Socket.INSTANCE.inet_ntop(Socket.AF_INET, addrBuf, charBuf, Socket.INET_ADDRSTRLEN);
        addrBuf.free();
        charBuf.free();
        return result;
    }

    /**
     * Opens a server TCP connection to clients.
     * Creates, binds, and listens
     *
     * @param port local TCP port to listen on
     * @param backlog listen backlog.
     *
     * @return a native handle to the network connection.
     * @throws IOException
     */
    public int openServer(int port, int backlog) throws IOException {
        int fd = -1;

        fd = Socket.INSTANCE.socket(Socket.AF_INET, Socket.SOCK_STREAM, 0);
        if (fd < 0) {
            throw newError(fd, "socket create");
        }

        set_blocking_flags(fd, /*is_blocking*/ false);

        IntByReference option_val = new IntByReference(1);
        if (Socket.INSTANCE.setsockopt(fd, Socket.SOL_SOCKET, Socket.SO_REUSEADDR, option_val, 4) < 0) {
            throw newError(fd, "setSockOpt");
        }
        option_val.free();

        Socket.sockaddr_in local_sin = new Socket.sockaddr_in();
        local_sin.sin_family = Socket.AF_INET;
        local_sin.sin_port = Inet.htons((short) port);
        local_sin.sin_addr = Socket.INADDR_ANY;
        if (Socket.INSTANCE.bind(fd, local_sin) < 0) {
            throw newError(fd, "bind");
        }

       if (Socket.INSTANCE.listen(fd, backlog) < 0) {
            throw newError(fd, "listen");
        }

        return fd;
    }

    /**
     * Accept client connections on server socket fd.
     * Blocks until a client connects.
     *
     * @param fd open server socket. See {@link #openServer}.
     *
     * @return a native handle to the network connection.
     * @throws IOException
     */
    public int accept(int fd) throws IOException {
        VMThread.getSystemEvents().waitForReadEvent(fd);

        Socket.sockaddr_in remote_sin = new Socket.sockaddr_in();
        IntByReference address_len = new IntByReference(4);
        int newSocket = Socket.INSTANCE.accept(fd, remote_sin, address_len);
        if (newSocket < 0) {
            throw newError(fd, "accept");
        }
        address_len.free();
        set_blocking_flags(newSocket, /*is_blocking*/ false);
        // we could read info about client from remote_sin, but don't need to.

        return newSocket;
    }

    /**
     * @inheritDoc
     */
    public int readBuf(int fd, byte b[], int offset, int length) throws IOException {
        byte[] buf = b;
        if (offset != 0) {
            buf = new byte[length];
            System.arraycopy(b, offset, buf, 0, length);
        }
        int result = LibC.INSTANCE.read(fd, buf, length); // We rely on open0() for setting the socket to non-blocking

        if (result == 0) {
            // If remote side has shut down the connection gracefully, and all
            // data has been received, recv() will complete immediately with
            // zero bytes received.
            //
            // This is true for Win32/CE and Linux
            result = -1;
        } else if (result < 0) {
            int err_code = LibCUtil.errno();
            if (err_code == LibC.EWOULDBLOCK) {
                VMThread.getSystemEvents().waitForReadEvent(fd);
                result = LibC.INSTANCE.read(fd, buf, length); // We rely on open0() for setting the socket to non-blocking
            }
            LibCUtil.errCheckNeg(result);
        }

        return result;
    }

    public int readByte(int fd) throws IOException {
        int result = -1;
        byte[] b = new byte[1];
        int n = readBuf(fd, b, 0, 1);

        if (n == 1) {
            result = b[0]; // do not sign-extend

            Assert.that(0 <= result && result <= 255, "no sign extension");
        } else if (n == 0) {
            // If remote side has shut down the connection gracefully, and all
            // data has been received, recv() will complete immediately with
            // zero bytes received.
            //
            // This is true for Win32/CE and Linux
            result = -1;
        }

        return result;
    }

    /**
     * @inheritDoc
     */
    public int writeBuf(int fd, byte buffer[], int off, int len) throws IOException {
        int result = 0;
                byte[] buf = buffer;
        if (off != 0) {
            buf = new byte[len];
            System.arraycopy(buffer, off, buf, 0, len);
        }

        result = LibC.INSTANCE.write(fd, buf, len);// We rely on open0() for setting the socket to non-blocking

        if (result < 0) {
            int err_code = LibCUtil.errno();
            if (err_code == LibC.EWOULDBLOCK) {
                VMThread.getSystemEvents().waitForWriteEvent(fd);
                result = LibC.INSTANCE.write(fd, buf, len); // We rely on open0() for setting the socket to non-blocking
            }
            LibCUtil.errCheckNeg(result);
        }

        return result;
    }

    /**
     * @inheritDoc
     */
    public int writeByte(int fd, int b) throws IOException {
        byte[] buf = new byte[1];
        int result = writeBuf(fd, buf, 0, 1);
        return result;
    }

    /**
     * @inheritDoc
     */
    public int available(int fd) throws IOException {
        Pointer buf = new Pointer(4);
        int err = Ioctl.INSTANCE.ioctl(fd, Ioctl.FIONREAD, buf.address().toUWord().toPrimitive());
        int result = buf.getInt(0);
        buf.free();
        LibCUtil.errCheckNeg(err);
//        System.err.println("available0(" + fd + ") = " + result);
        return result;

    }

    /**
     * @inheritDoc
     */
    public void close(int fd) throws IOException {
        // NOTE: this would block the VM. A real implementation should
        // make this a async native method.
        Socket.INSTANCE.shutdown(fd, 2);
        LibC.INSTANCE.close(fd);
    }

    /**
     * set a socket option
     *
     * @param socket socket descriptor
     * @param option_name
     * @param option_value new value
     * @throws IOException on error
     */
    public void setSockOpt(int socket, int option_name, int option_value) throws IOException {
        IntByReference value = new IntByReference(option_value);
        int err = Socket.INSTANCE.setsockopt(socket, Socket.SOL_SOCKET, option_name, value, 4);
        value.free();
        LibCUtil.errCheckNeg(err);
    }

    /**
     * get a socket option
     *
     * @param socket socket descriptor
     * @param option_name
     * @throws IOException on error
     */
    public int getSockOpt(int socket, int option_name) throws IOException {
        IntByReference value = new IntByReference(0);
        IntByReference opt_len = new IntByReference(0);

        int err = Socket.INSTANCE.getsockopt(socket, Socket.SOL_SOCKET, option_name, value, opt_len);
        int result = value.getValue();
        value.free();
        Assert.that(opt_len.getValue() == 4);
        opt_len.free();
        LibCUtil.errCheckNeg(err);
        return result;
    }

}
