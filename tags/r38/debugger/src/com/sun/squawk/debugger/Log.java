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

package com.sun.squawk.debugger;

import java.io.*;
import javax.microedition.io.*;

import com.sun.squawk.*;

/**
 * This class provides a (very basic) line-based logging facility.
 */
public class Log {

    /**
     * Disables logging.
     */
    public static final int NONE = 0;

    /**
     * Informational log level.
     */
    public static final int INFO = 1;

    /**
     * More verbose log level.
     */
    public static final int VERBOSE = 2;

    /**
     * Log level used to debug problems (very verbose).
     */
    public static final int DEBUG = 3;

    /**
     * The current logging level.
     */
    public static final int level = configLevel();

    /**
     * Where messages are logged.
     */
    public static final PrintStream out = configOut(level);

    /**
     * Configures the logging level based on the value of the <code>"squawk.debugger.log.level"</code>
     * system property.
     *
     * @return the logging level
     */
    private static int configLevel() {
        int level = NONE;
        String prop = System.getProperty("squawk.debugger.log.level");
        if (prop != null) {
            if (prop.equals("none")) {
                level = NONE;
            } else if (prop.equals("info")) {
                level = INFO;
            } else if (prop.equals("verbose")) {
                level = VERBOSE;
            } else if (prop.equals("debug")) {
/*if[DEBUG_CODE_ENABLED]*/
                level = DEBUG;
/*else[DEBUG_CODE_ENABLED]*/
//              System.err.println("NOTE: Log level \"debug\" only supported in debug builds. Setting log level to \"verbose\"");
//              level = VERBOSE;
/*end[DEBUG_CODE_ENABLED]*/
            } else {
                System.err.println("logging disabled - invalid log level in squawk.debugger.log.level system property: " + prop);
            }
        }
        return level;
    }

    /**
     * Configures the logging stream based on the value of the <code>"squawk.debugger.log.url"</code>
     * system property.
     *
     * @return the logging stream
     */
    private static PrintStream configOut(int level) {
        PrintStream out = System.out;
        if (level != NONE) {
            System.err.println("logging level: " + Log.level);
            String prop = System.getProperty("squawk.debugger.log.url");
            if (prop != null) {
                try {
                    out = new PrintStream(Connector.openOutputStream(prop));
                    System.err.println("logging to " + prop);
                } catch (IOException e) {
                    System.err.println("logging to System.out - exception while opening log stream: " + prop);
                    e.printStackTrace();
                }
            } else {
                System.err.println("logging to System.out");
            }
        }
        return out;
    }

    public static boolean info() {
        return level >= INFO;
    }

    public static boolean verbose() {
        return level >= VERBOSE;
    }

    /**
     * The debug level of logging is only enabled in debug builds.
     * In non-debug builds, this is a constant method, easily inlinable,
     * allowing dead-code elimination in the callers 
     * (once byte-code optimizer is enabled).
     */
/*if[DEBUG_CODE_ENABLED]*/
    public static boolean debug() {
        return level >= DEBUG;
    }
/*else[DEBUG_CODE_ENABLED]*/
//    public static boolean debug() {
//        return false;
//    }
/*end[DEBUG_CODE_ENABLED]*/

    /**
     * Logs a message as a line sent to the current logging stream.
     *
     * @param msg    the message to log
     */
    public static void log(String msg) {
        if (out != null) {
            msg = "[Thread " + Thread.currentThread().getName() + "] " + msg;
            out.println(msg);
            out.flush();
        }
    }
}
