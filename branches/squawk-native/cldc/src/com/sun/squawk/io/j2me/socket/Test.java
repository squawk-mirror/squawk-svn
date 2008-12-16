//if[!FLASH_MEMORY]
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

package com.sun.squawk.io.j2me.socket;

import com.sun.squawk.VM;
import java.io.*;
import javax.microedition.io.*;

/**
 *
 * Simple test case
 */
public class Test {

    /**
     * test code
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        try {
            System.err.println("creating twiddler");
            Thread twiddler = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        VM.print('$');
                        Thread.yield();
                    }
                }
            }, "Twiddler Thread");
            twiddler.setPriority(Thread.MIN_PRIORITY);
            VM.setAsDaemonThread(twiddler);
            twiddler.start();

            StreamConnection c = (StreamConnection)Connector.open("socket://www.sun.com:80");
            DataOutputStream out = c.openDataOutputStream();
            DataInputStream in = c.openDataInputStream();

            byte[] data = "GET /index.html HTTP/1.1\r\nHost: www.sun.com\r\n\r\n".getBytes();
            out.write(data, 0, data.length);

            StringBuffer strbuf = new StringBuffer();

            int b = 0;
            while ((b = in.read(data, 0, data.length)) != -1) {
                for (int i = 0; i < b; i++) {
                    System.err.print((char) data[i]);
                    //strbuf.append((char)data[i]);
                }
            }
        //System.err.println("Read: " + strbuf.toString());

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private Test() {
    }

}
