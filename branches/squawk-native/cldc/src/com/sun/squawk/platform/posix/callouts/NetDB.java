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

package com.sun.squawk.platform.posix.callouts;

import com.sun.cldc.jna.*;

/**
 *
 * java wrapper around #include <netdb.h>
 */
public class NetDB {
    
    /** pure static class */
    private NetDB() {}
    
    private static final VarPointer h_errnoPtr = VarPointer.getVarPointer("h_errno", 4);
        
    private static final Function gethostbynamePtr = Function.getFunction("gethostbyname");

    /**
     * The gethostbyname() function returns a HostEnt structure describing an internet host referenced by name.
     *
     * @param name the host name
     * @return the address of struct hostent, or null on error
     */
    public static HostEnt gethostbyname(String name) {
        Pointer name0 = Pointer.createStringBuffer(name);
        Structure result = Function.returnStruct(HostEnt.class,
                                                 gethostbynamePtr.call1(name0));
        name0.free();
        return (HostEnt)result;
    }
    
    /** Authoritative Answer Host not found. 
     * @see #h_errno() */
    public final static int HOST_NOT_FOUND = 1; 
    /** Non-Authoritative Host not found, or SERVERFAIL. 
     * @see #h_errno() */
    public final static int TRY_AGAIN = 2; 
    /** Non recoverable errors, FORMERR, REFUSED, NOTIMP. 
     * @see #h_errno() */
    public final static int NO_RECOVERY = 3; 
    /** Valid name, no data record of requested type.
     * @see #h_errno() */
    public final static int NO_DATA = 4; 

    /** Return error code for last call to gethostbyname() or gethostbyaddr().
     * @return one of the error codes defined in this class.
     */
    public static int h_errno() {
        return h_errnoPtr.getInt(0);
    }
    
    /** C STRUCTURE HostEnt
            struct  hostent {
                     char    *h_name;         official name of host 
                     char    **h_aliases;     alias list 
                     int     h_addrtype;      host address type 
                     int     h_length;        length of address 
                     char    **h_addr_list;   list of addresses from name server 
             };
             #define h_addr  h_addr_list[0]  address, for backward compatibility 
    */
    public static class HostEnt extends Structure {
        public String h_name;          /* official name of host */

        public int h_addrtype;         /* host address type */

        public int h_length;           /* length of address */

        public int[] h_addr_list;      /* list of addresses from name server */
        
        public void read() {
            final int MAX_ADDRS = 16;
            Pointer p = getPointer();
            h_name = p.getPointer(0, 1024).getString(0);
            h_addrtype = p.getInt(8);
            h_length = p.getInt(12);
            if (h_length != 4) {
                System.err.println("WARNING: Unexpected h_length value");
            }
            Pointer adrlist = p.getPointer(16, MAX_ADDRS * 4);
          
//            System.out.println("in read(). Buffer: " + p);
//            System.out.println("    name: " + h_name);
//            System.out.println("    h_addrtype: " + h_addrtype);
//            System.out.println("    adrlist: " + adrlist);
            
            Pointer[] addrPtrs = new Pointer[MAX_ADDRS];

            int count = 0;
            for (int i = 0; i < MAX_ADDRS; i++) {
                Pointer addrPtr = adrlist.getPointer(i * 4, h_length);
                if (addrPtr == null) {
                    break;
                }
                addrPtrs[i] = addrPtr;
                count++;
            }
//            System.out.println("    adrlist count: " + count);

            h_addr_list = new int[count];
            for (int i = 0; i < count; i++) {
                int addr = addrPtrs[i].getInt(0);
//                System.err.println("    addr  " + addr);
                h_addr_list[i] = addr;
            }
        }

        public void write() {
        }

        public int size() { return 5 * 4; }
    
    } /* HostEnt */

   /*if[DEBUG_CODE_ENABLED]*/
    public static void main(String[] args) {
        String[] hosts = {"localhost",
            "www.sun.com",
            "127.0.0.1",
            "www.google.com", // has multiple addrs
            "adfadfadf.adfadf",
            ""
        };

        for (int i = 0; i < hosts.length; i++) {
            System.err.println("Trying lookup of " + hosts[i]);
            HostEnt hostent = gethostbyname(hosts[i]);
           // System.err.println("result: " + hostent);
            if (hostent == null) {
                System.err.println(" lookup error  " + h_errno());
            } else {
                int len = hostent.h_addr_list.length;
                for (int j = 0; j < len; j++) {
                    int addr = hostent.h_addr_list[j];
                    String addrstr = Socket.inet_ntop(addr);
                    System.err.println("   addr  " + addrstr);
                }

            }

        }
    }
   /*end[DEBUG_CODE_ENABLED]*/
}
