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

package com.sun.squawk;

import java.io.*;
import java.util.*;

import javax.microedition.io.Connector;

import com.sun.squawk.util.Tracer;
import com.sun.squawk.util.ArgsUtilities;
import com.sun.squawk.util.StringTokenizer;

/**
 * The Java application manager is the master isolate used to coordinate application execution.
 *
 */
public class JavaApplicationManager {
    
    /**
     * Purely static class should not be instantiated.
     */
    private JavaApplicationManager() {}
    
    /**
     * The class path to use when loading through the translator instance (if any).
     */
    private static String classPath;

    /**
     * The suite to which the leaf suite will be bound (if any).
     */
    private static String parentSuiteURI;

    /**
     * Command line option to enable display of execution statistics before exiting.
     */
    private static boolean displayExecutionStatistics;

/*if[DEBUG_CODE_ENABLED]*/
    /**
     * Specifies if the application is to be serialized, deserialized and then restarted when it
     * stops by hibernating itself.
     */
    private static boolean testoms;
/*else[DEBUG_CODE_ENABLED]*/
//    private static final boolean testoms = false;
/*end[DEBUG_CODE_ENABLED]*/
    
    /**
     * Specify the MIDlet- property to extract to determine which MIDlet should be run from a suite.
     */
    private static int midletPropertyNum;

    /**
     * Specify the Midlet class name directly.
     */
     private static String testMIDletClass;
     
    /**
     * new isolate's properties.
     */
    private static Hashtable newProps;

    /**
     * Main routine.
     *
     * @param args the command line argument array
     */
    public static void main(String[] args) throws Exception {
        // If no name is specified for MIDlet, assume MIDlet-1
        midletPropertyNum = 1;
        
        /*
         * Process any switches.
         */
        if (args.length != 0) {
            args = processVMOptions(args);
        }

        String mainClassName = null;
        String[] javaArgs = null;
        if (testMIDletClass != null) {
            mainClassName = "com.sun.squawk.imp.MIDletMainWrapper";
            javaArgs = new String[] {"-name", testMIDletClass};
        } else if (args.length > 0) {
            /*
             * Split out the class name from the other arguments.
             */
            mainClassName = args[0].replace('/', '.');
            javaArgs = new String[args.length - 1];
            for (int i = 0 ; i < javaArgs.length ; i++) {
                javaArgs[i] = args[i+1];
            }
        } // else use midletPropertyNum

        /*
         * Get the start time.
         */
        long startTime = System.currentTimeMillis();

        /*
         * Create the application isolate and run it.
         */
        Isolate isolate;
        if (mainClassName != null) {
            // create raw isolate
            isolate = new Isolate(newProps, mainClassName, javaArgs, classPath, parentSuiteURI);
        } else {
             // create midlet
            isolate = new Isolate(newProps, midletPropertyNum, classPath, parentSuiteURI);
        }
        
        /*
         * Start the isolate and wait for it to complete.
         */
        isolate.start();
        isolate.join();

        /*
         * If the isolate was hibernated then save it and restart it.
         */
        while (isolate.isHibernated() && testoms) {
            try {
                String url = "file://" + isolate.getMainClassName() + ".isolate";
                DataOutputStream dos = Connector.openDataOutputStream(url);
                isolate.save(dos, url, VM.isBigEndian());
                System.out.println("Saved isolate to " + url);
                dos.close();

                DataInputStream dis = Connector.openDataInputStream(url);
                /*isolate = */Isolate.load(dis, url);
                dis.close();

                isolate.unhibernate();
                isolate.join();

            } catch (java.io.IOException ioe) {
                System.err.println("I/O error while trying to save or re-load isolate: ");
                ioe.printStackTrace();
                break;
            }
        }

        /*
         * Get the exit status.
         */
        int exitCode = isolate.getExitCode();

        /*
         * Show execution statistics if requested
         */
        if (displayExecutionStatistics) {
            long endTime = System.currentTimeMillis();
            System.out.println();
            System.out.println("=============================");
            System.out.println("Squawk VM exiting with code "+exitCode);
            if (GC.getPartialCollectionCount() > 0) {
                System.out.println(""+GC.getPartialCollectionCount()+" partial collections");
            }
            if (GC.getFullCollectionCount() > 0) {
                System.out.println(""+GC.getFullCollectionCount()+" full collections");
            }
            GC.getCollector().dumpTimings(System.out);
            System.out.println("Execution time was "+(endTime-startTime)+" ms");
            System.out.println("=============================");
            System.out.println();
        }

        /*
         * Stop the VM.
         */
        VM.stopVM(exitCode);
    }

    /**
     * Process any VM command line options.
     *
     * @param args the arguments as supplied by the VM.startup code
     * @return the arguments needed by the main() routine of the isolate
     */
    private static String[] processVMOptions(String[] args) {
        int offset = 0;
        while (offset != args.length) {
            String arg = args[offset];
            if (arg.charAt(0) == '-') {
                processVMOption(arg);
            } else {
                break;
            }
             offset++;
        }
        String[] javaArgs = new String[args.length - offset];
        for (int i = 0 ; i < javaArgs.length ; i++) {
            javaArgs[i] = args[offset++];
        }
        return javaArgs;
    }

    /**
     * Shows the version information.
     *
     * @param out  the print stream to use
     */
    private static void showVersion(PrintStream out) {
        out.println((Klass.SQUAWK_64 ? "64" : "32") + " bit squawk:");
        out.println("    debug code " + (Klass.DEBUG_CODE_ENABLED ? "enabled" : "disabled"));
        out.println("    assertions " + (Klass.ASSERTIONS_ENABLED ? "enabled" : "disabled"));
        out.println("    tracing " + (Klass.TRACING_ENABLED ? "enabled" : "disabled"));
        boolean floatSupported = "${build.properties:FLOATS}".equals("true");
        if (floatSupported) {
            out.println("    floating point supported");
        } else {
            out.println("    no floating point support");
        }
        out.println("    bootstrap suite: ");
        StringTokenizer st = new StringTokenizer(VM.getCurrentIsolate().getBootstrapSuite().getConfiguration(), ",");
        while (st.hasMoreTokens()) {
            out.println("        " + st.nextToken().trim());
        }
        VM.printConfiguration();
    }

    /**
     * Shows the classes in the image.
     *
     * @param out  the print stream to use
     * @param packagesOnly if true, only a listing of the packages in the image is shown
     */
    private static void showImageContents(PrintStream out, boolean packagesOnly) {
        Suite bootstrapSuite = VM.getCurrentIsolate().getBootstrapSuite();
        if (packagesOnly) {
            out.println("Packages in image:");
            Hashtable packages = new Hashtable();
            int count = bootstrapSuite.getClassCount();
            for (int i = 0; i != count; ++i) {
                Klass klass = bootstrapSuite.getKlass(i);
                if (klass != null && !klass.isSynthetic()) {
                    String className = klass.getInternalName();
                    int index = className.lastIndexOf('.');
                    if (index != -1) {
                        String packageName = className.substring(0, className.lastIndexOf('.'));
                        if (packages.get(packageName) == null) {
                            out.println("  " + packageName);
                            packages.put(packageName, packageName);
                        }
                    }
                }
            }
        } else {
            out.println("Classes in image:");
            int count = bootstrapSuite.getClassCount();
            for (int i = 0; i != count; ++i) {
                Klass klass = bootstrapSuite.getKlass(i);
                if (klass != null && !klass.isSynthetic()) {
                    out.println("  " + klass.getName());
                }
            }
        }
    }

    /**
     * Process a VM command line option.
     *
     * @param arg the argument
     */
    private static void processVMOption(String arg) {
        if (arg.startsWith("-cp:")) {
            // Fix up the class path with respect to the system dependant separator characters
            classPath = ArgsUtilities.toPlatformPath(arg.substring("-cp:".length()), true);
        } else if (arg.startsWith("-suite:")) {
            parentSuiteURI = "file://" + arg.substring(7) + Suite.FILE_EXTENSION;
/*if[FLASH_MEMORY]*/
        } else if (arg.startsWith("-spotsuite:")) {
            parentSuiteURI = arg.substring(1);
/*end[FLASH_MEMORY]*/
        } else if (arg.equals("-egc")) {
            GC.setExcessiveGC(true);
        } else if (arg.equals("-nogc")) {
            VM.allowUserGC(false);
/*if[EXCLUDE]*/
        } else if (arg.equals("-imageclasses")) {
            showImageContents(System.err, false);
            VM.stopVM(0);
        } else if (arg.equals("-imagepackages")) {
            showImageContents(System.err, true);
            VM.stopVM(0);
/*end[EXCLUDE]*/
        } else if (arg.startsWith("-isolateinit:")) {
            String initializer = arg.substring(13);
            VM.setIsolateInitializerClassName(initializer);
        } else if (arg.startsWith("-MIDlet-")) {
            try {
                midletPropertyNum = Integer.parseInt(arg.substring("-MIDlet-".length()));
            } catch (NumberFormatException ex) {
                usage("Bad value for -MIDlet- "+arg);
                VM.stopVM(0);
            }
        } else if (arg.startsWith("-testMIDlet:")) {
            testMIDletClass = arg.substring(12);
        } else if (arg.equals("-version")) {
            showVersion(System.err);
            VM.stopVM(0);
        } else if (arg.equals("-verbose")) {
            if (!VM.isVerbose()) {
                VM.setVerboseLevel(1);
            }
        } else if (arg.equals("-veryverbose")) {
            if (!VM.isVeryVerbose()) {
                VM.setVerboseLevel(2);
            }
/*if[DEBUG_CODE_ENABLED]*/
        } else if (arg.equals("-testoms")) {
            testoms = true;
/*end[DEBUG_CODE_ENABLED]*/
        } else if (arg.startsWith("-trace")) {
            if (arg.startsWith("-tracefilter:")) {
                Tracer.setFilter(arg.substring("-tracefilter:".length()));
            } else {
                String feature = arg.substring("-trace".length());
                Tracer.enableFeature(feature);
                if (arg.equals("-traceconverting")) {
                    Tracer.enableFeature("loading"); // -traceconverting subsumes -traceloading
                }
            } 
        } else if (arg.equals("-stats")) {
            displayExecutionStatistics = true;
        } else if (arg.startsWith("-sampleStatData:")) {
            String url = arg.substring("-sampleStatData:".length());
            try {
                System.out.println("Sending samples of statictics to " + url);
                final DataOutputStream dos = Connector.openDataOutputStream(url);;
                new Thread(new Runnable() {
                    public void run() {
                        VM.Stats stats = new VM.Stats();
                        while (true) {
                            stats.sendStatData(dos);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }).start();
            } catch (IOException ex) {
                ex.printStackTrace();
                System.err.println("Ignoring -sampleStatData option");
            }
        } else if (arg.startsWith("-D")) {
            if (newProps == null) {
                newProps = new Hashtable();
            }
            String propAndValue = arg.substring("-D".length());
            int seperator = propAndValue.indexOf('=');
            String prop = propAndValue.substring(0, seperator);
            String val = propAndValue.substring(seperator+1);
            newProps.put(prop, val);
            // System properties are not "global global"
        } else if (arg.equals("-h")) {
            usage("");
            VM.stopVM(0);
        } else if (!GC.getCollector().processCommandLineOption(arg)) {
            usage("Unrecognised option: "+arg);
            VM.stopVM(0);
        }
    }

     /**
     * Print a usage message and exit.
     *
     * @param msg error message
     */
    private static void usage(String msg) {
        PrintStream out = System.out;
        out.println();
        if (msg.length() > 0) {
            out.println("** " + msg + " **\n");
        }
        out.print(
                "Usage: squawk [-options] class [args...] | [-MIDlet-x]\n" +
                "\n" +
                "if there is no class specified, then try MIDlet-1 property to find a MIDlet\n" +
                "where options include:\n" +
                "    -cp:<directories and jar/zip files separated by ':' (Unix) or ';' (Windows)>\n" +
                "                          paths where classes, suites and sources can be found\n" +
                
                "    -suite:<name>         suite name (without \"" + Suite.FILE_EXTENSION + "\") to load\n" +
/*if[FLASH_MEMORY]*/
                "    -spotsuite:<name>     suite name (without \"" + Suite.FILE_EXTENSION + "\") to load\n" +
/*end[FLASH_MEMORY]*/
/*if[EXCLUDE]*/
                "    -imageclasses         show the classes in the boot image and exit\n" +
                "    -imagepackages        show the packages in the boot image and exit\n" +
/*end[EXCLUDE]*/
                "    -isolateinit:<class>  class whose main will be invoked on Isolate start, single arg \"true\" if first Isolate being initialized\n" +
                "    -MIDlet-x             which MIDlet-x property to use from " + Suite.PROPERTIES_MANIFEST_RESOURCE_NAME + "\n" +
                "    -testMIDlet:<class>   specify MIDlet class name directly\n" +
                "    -sampleStatData:url   poll VM.Stats every 500ms and send samples to url\n" +
                "    -version              print product version and exit\n" +
                "    -verbose              report when a class is loaded\n" +
                "    -veryverbose          report when a class is initialized or looked up and\n" +
                "                          various other output\n"
/*if[DEBUG_CODE_ENABLED]*/
              + "    -testoms              continually serialize, deserialize and restart the application if it hibernates itself\n"
/*end[DEBUG_CODE_ENABLED]*/
            );

        if (Klass.TRACING_ENABLED) {
        out.print(
                "    -traceoms             trace object memory serialization\n" +
                "    -traceswapper         trace endianess swapping\n"
                );
        }
        TranslatorInterface t = VM.getCurrentIsolate().getTranslator();
        if (t == null) {
            t = VM.getCurrentIsolate().getDefaultTranslator();
        }
        if (t != null) {
            t.printTraceFlags(out);
        }
        GC.getCollector().usage(out);
        out.print(
                "    -egc                  enable excessive garbage collection\n" +
                "    -nogc                 disable application calls to Runtime.gc()\n" +
                "    -stats                display execution statistics before exiting\n" +
                "    -D<name>=<value>      set a system property\n" +
                "    -h                    display this help message\n" +
                "    -X                    display help on native VM options\n"
                );
        VM.stopVM(0);
    }

}
