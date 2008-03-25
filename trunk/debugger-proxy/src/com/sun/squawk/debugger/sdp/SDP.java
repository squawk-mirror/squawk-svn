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

package com.sun.squawk.debugger.sdp;

import java.io.*;

import com.sun.squawk.debugger.*;
import com.sun.squawk.debugger.DataType.*;
import com.sun.squawk.debugger.EventManager.*;
import com.sun.squawk.debugger.EventRequest.*;
import com.sun.squawk.debugger.EventRequestModifier.*;
import com.sun.squawk.util.*;
import java.util.*;
import com.sun.squawk.*;

/**
 * A SDB (Squawk Debugger Proxy) instance intercedes for a SDA
 * (Squawk Debug Agent) when it's connected to a JPDA compliant debugger.
 *
 */
public class SDP {

    /**
     * The object managing proxy types and reference type identifiers.
     */
    private ProxyTypeManager ptm;

    /**
     * The URL of the channel to the VM running the application to be debugged.
     */
    private String vm_url = "socket://localhost:2800";

    /**
     * Seconds to wait before retrying to connect to a Squawk VM.
     */
    private int retry = 5;

    /**
     * The URL of the channel to the debugger client.
     */
    private String debugger_url = "serversocket://:2900";
    
    /**
     * This is a unique name of the proxy. Typically it is the port number in the debugger_url,
     * so the default name is "2900";
     */
    private String proxyName;

    /**
     * The connection to the JPDA compliant debugger (e.g. jdb).
     */
    private JDBListener jdb;

    /**
     * The connection to the Squawk Debug Agent running in the Squawk VM.
     */
    private SDAListener sda;

    /**
     * The manager of JDWP events generated by the SDA and event
     * requests sent from jdb.
     */
    SDPEventManager eventManager;

    ProxyTypeManager getPTM() {
        return ptm;
    }

    private ThreadProxiesManager tpm;

	private boolean singleSession = false;
    
    /**
     * Act as a JDWP packet sniffer between a JVM and a debugger.
     */
    private boolean sniffOnly = false;
    
    /**
     * If false, the proxy will try to continue executing in the face of certain errors.
     */
    boolean quitOnError = true;
    
    /**
     * @see getSDP()
     */
     private static Hashtable sdpTable = new Hashtable();
     
    /**
     * Return the active SDP instance named proxyNameThe current SDP instance. 
     * Used when running SDP embedded in the same Java process as controller, as in Solarium
     * 
     * @return the SDP instance named proxyName, or null if no active proxy by the name.
     */
     public static SDP getSDP(String proxyName) {
         return (SDP)sdpTable.get(proxyName);
     }
     
    /**
     * Return the unique name of the proxy. Typically it is the port number in the debugger_url,
     * so the default name is "2900";
     * 
     * @return the proxy name
     */
     public String getProxyName() {
         return proxyName;
     }
    
    /**
     * set to true if sdp.quit() was called.
     */
    private boolean quitSDP;
    

    ThreadProxiesManager getTPM() {
        return tpm;
    }

    static class ThreadProxiesManager {

        /**
         * VM thread status mirrors of live threads.
         */
        private Map liveThreads = new HashMap();
        
        /**
         * Also keep track of zombie threads debugger might ask about them.
         */
        private Map zombieThreads = new HashMap();

        synchronized void suspendAllThreads() {
            for (Iterator i = liveThreads.values().iterator(); i.hasNext(); ) {
                ProxyThread pt = (ProxyThread)i.next();
                pt.setSuspendCount(pt.getSuspendCount() + 1);
            }
        }

        synchronized ProxyThread getRunningThread() {
            for (Iterator i = liveThreads.values().iterator(); i.hasNext(); ) {
                ProxyThread thread = (ProxyThread) i.next();
                if (thread.getStatus() == JDWP.ThreadStatus_RUNNING && !thread.isSuspended()) {
                    return thread;
                }
            }
            return null;
        }
        
        /** 
         * To report bootstrap classes, we need a thread to claim repsonisibility. Choose any.
         * Tends to be the isolate's main thread, when called at startup.
         */
        synchronized ProxyThread getSomeThread() {
            for (Iterator i = liveThreads.values().iterator(); i.hasNext(); ) {
                return (ProxyThread) i.next();
            }
            return null;
        }

        /**
         * Gets all the mirrors.
         */
        synchronized Collection getThreads() {
            Assert.that(!liveThreads.isEmpty());
            return new ArrayList(liveThreads.values());
        }

        /**
         * Gets a thread mirror based on an object identifier.
         * Note that the thread may not be alive.
         *
         * @param id   the ID of the thread to retrieve
         * @return the thread corresponding to <code>id</code>
         * @throws SDWPException if there is no thread corresponding to id
         */
        synchronized ProxyThread getThread(ObjectID id) throws SDWPException {
            ProxyThread thread = (ProxyThread) liveThreads.get(id);
            if (thread == null) {
                // debugger could be asking about now dead thread. Check zombie list:
                thread = (ProxyThread) zombieThreads.get(id);
                if (thread == null) {
                    throw new SDWPException(JDWP.Error_INVALID_THREAD, "object ID does not denote a Thread instance: " + id);
                }
            }
            return thread;
        }

        /**
         * Updates the thread mirrors based on a packet from the VM.
         */
        synchronized void updateThreads(PacketInputStream in) throws IOException {
            int count = in.readInt("threads");
            Map nowLive = new HashMap(count);
            
            for (int i = 0; i != count; ++i) {
                ObjectID id = in.readObjectID("thread");
                int status = in.readInt("status");
                int suspendCount = in.readInt("suspendCount");
                String name = in.readString("name");

                ProxyThread thread = (ProxyThread)liveThreads.get(id);
                if (thread == null) {
                    thread = new ProxyThread(id, name, status, suspendCount);
                    liveThreads.put(id, thread); // add new threads to global list;
                } else {
                    thread.setName(name);
                    thread.setStatus(status);
                    thread.setSuspendCount(suspendCount);
                }
                nowLive.put(id, thread);
            }
            
            // notice dead threads
            Set liveThreadKeys = liveThreads.keySet();
            for (Iterator i = liveThreadKeys.iterator(); i.hasNext(); ) {
                ObjectID id = (ObjectID) i.next();
                if (nowLive.get(id) == null) {
                    ProxyThread deadThread = (ProxyThread)liveThreads.get(id);
                    deadThread.setStatus(JDWP.ThreadStatus_ZOMBIE);
                    deadThread.setSuspendCount(0);
                    i.remove();
                    Assert.always(liveThreads.get(id) == null);
                    zombieThreads.put(id, deadThread);
                }
            }
        }

        synchronized void updateThread(ObjectID id, String name, int status, int suspendCount) {
            Assert.always(zombieThreads.get(id) == null); // zombies shouldn't come back to life.'

            ProxyThread thread = (ProxyThread)liveThreads.get(id);
            if (thread == null) {
                thread = new ProxyThread(id, name, status, suspendCount);
                liveThreads.put(id, thread);
            } else {
                thread.setName(name);
                thread.setStatus(status);
                thread.setSuspendCount(suspendCount);
            }
            
            if (status == JDWP.ThreadStatus_ZOMBIE) {
                liveThreads.remove(id);
                zombieThreads.put(id, thread);
            }
        }
    }


    /**
     * Parses the command line arguments to configure this debugger proxy.
     *
     * @param args  the command line arguments
     * @return      true if there were no errors in the arguments and this debugger proxy is now configured
     */
    private boolean parseArgs(String args[]) {
        String logLevel = "none";
        String logURL = null;
        String classPath = null;

        for (int argc = 0; argc != args.length; ++argc) {
            String arg = args[argc];
            try {
                if (arg.startsWith("-cp:")) {
                    classPath = ArgsUtilities.toPlatformPath(arg.substring("-cp:".length()), true);
                } else if (arg.startsWith("-log:")) {
                    logLevel = arg.substring("-log:".length());
                } else if (arg.startsWith("-logFile:")) {
                    logURL = "file://" + arg.substring("-logFile:".length());
                } else if (arg.startsWith("-replay:")) {
                    debugger_url = arg.substring("-replay:".length());
                } else if (arg.startsWith("-l:")) {
                    debugger_url = "serversocket://:" + arg.substring("-l:".length());
                } else if (arg.startsWith("-vm:")) {
                    vm_url = arg.substring("-vm:".length());
                } else if (arg.startsWith("-debugger:")) {
                    debugger_url = arg.substring("-debugger:".length());
                } else if (arg.startsWith("-retry:")) {
                    try {
                        retry = Integer.parseInt(arg.substring("-retry:".length()));
                    } catch (NumberFormatException e) {
                        usage("argument to '-retry' must be an integer");
                        return false;
                    }
                } else if (arg.startsWith("-quitOnError:")) {
                    String boolStr = arg.substring("-quitOnError:".length());
                    quitOnError = Boolean.parseBoolean(boolStr);
                } else if (arg.equals("-h")) {
                    usage(null);
                    return false;
                } else if (arg.equals("-singlesession")) {
                	singleSession = true;
                } else if (arg.equals("-sniffer")) {
                	sniffOnly = true;
                } else {
                    usage("Unknown option: " + arg);
                    return false;
                }
            } catch (NumberFormatException e) {
                System.err.println("Badly formatted option: " + arg);
                return false;
            }
        }

        if (classPath == null) {
            System.err.println("A path for the Squawk classes must be specified using the -cp option. For example:");
            System.err.println("    -cp:j2me/j2meclasses:debugger/j2meclasses:samples/j2meclasses");
            return false;
        }
        ProxySupport.initializeTranslator(classPath);

        System.setProperty("squawk.debugger.log.level", logLevel);
        if (logURL != null) {
            System.setProperty("squawk.debugger.log.url", logURL);
        }
        
        if (debugger_url.indexOf("serversocket://:") == 0) {
            proxyName = debugger_url.substring("serversocket://:".length());
        } else {
            proxyName = debugger_url;
        }
        
        return true;
    }

    /**
     * Prints a usage message to the console.
     *
     * @param errMsg  an optional error message to print first
     */
    private void usage(String errMsg) {
        PrintStream out = System.out;
        if (errMsg != null) {
            out.println(errMsg);
        }
        out.println("Usage: SDP [-options] ");
        out.println("where options include:");
        out.println();
        out.println("    -debugger:<url> The URL of the channel that debug proxy will");
        out.println("                    listen on for a connection from a JPDA debugger.");
        out.println("                    Only specify -debugger OR -l, not both.");
        out.println("                    (default is " + debugger_url + ").");
        out.println("    -retry:<n>      retry to connect to VM every 'n' seconds (default is " + retry +")");
        out.println("    -l:<port>       local port number that the debug proxy will");
        out.println("                    listen on for a connection from a JPDA debugger.");
        out.println("                    (default is 2900)");
        out.println("    -vm:<url>       The URL of the channel to the VM running the");
        out.println("                    application to be debugged.");
        out.println("                    (default is " + vm_url + ").");
        out.println("    -log:<level>    sets logging level to 'none', 'info', 'verbose' or 'debug'");
        out.println("    -logFile:<file> where messages should be logged (default is stdout),");
        out.println("    -cp:<path>      a list of paths separated by '" + File.pathSeparator + "' where the");
        out.println("                    debug proxy can find class files.");
        out.println("                    (default is '.')");
        out.println("    -singlesession  allow only one connection from a debugger before exiting");
        out.println("    -sniffer        act as a JDWP packet sniffer between a JVM and a debugger,");
        out.println("                    logging packets as specified by -log and -logfile options.");
        out.println("    -quitOnError:<b> If false, the proxy will try to ignore certain proxy errors.");
        out.println("                    (default is true).");
        out.println("    -h              shows this usage message");
        out.println();
    }

    /**
     * Quit this proxy
     */
    public void quit() {
        quitSDP = true;
        if (sda != null) {
            sda.quit();
        }
        if (jdb != null) {
            jdb.quit();
        }
    }
    
    /**
     * Starts a single debug session between a VM and a debug client. Returns
     * when the session is closed from either end.
     */
    private void go() {

        // Establish the connection to the VM
        sda = new SDAListener(this);
        ptm = new ProxyTypeManager();
        tpm = new ThreadProxiesManager();
        byte[] handshake = "SDWP-Handshake".getBytes();
        while (true) {
            try {
                System.out.println("Trying to connect to VM on " + vm_url);
                long now = System.currentTimeMillis();
                sda.open(vm_url, handshake, true, false);
                System.out.println("Established connection to VM (handshake took " + (System.currentTimeMillis() - now) + "ms)");
                ptm.setVM(sda);
                break;
            } catch (IOException e) {
                if (quitSDP) {
                    return;
                }
                System.out.println("Failed to establish connection with VM: " + e.getMessage() + " - trying again in " + retry + " seconds...");

                // Sleep and try again
                try {
                    Thread.sleep(retry * 1000);
                } catch (InterruptedException ie) {
                }
            }
        }

        // Establish connection from debugger
        jdb = new JDBListener(this);
        handshake = "JDWP-Handshake".getBytes();
        try {
            System.out.println("Waiting for connection from debugger on " + debugger_url);
            jdb.open(debugger_url, handshake, false, true);
            System.out.println("Established connection with debugger");
        } catch (IOException e) {
            System.out.println("Failed to establish connection with JDWP debugger: " + e.getMessage());
            sda.quit();
            return;
        }

        // The connection with the VM may have been lost by the time the debugger connects
        if (sda.hasQuit()) {
            jdb.quit();
            return;
        }

        eventManager = new SDPEventManager(new MatcherImpl());

        //
        // Two threads are required here so that we can listen to
        // the debugger and the VM at the same time. Each thread
        // knows how to talk to the other so that information can flow
        // over the proxy.
        //

        jdb.bindProxyPeer(sda);

        Thread sdaThread = new Thread(sda, "SDAListener");
        Thread jdbThread = new Thread(jdb, "JDBListener");

        sdaThread.start();

        // Wait until an event has been passed to the debugger before receiving
        // commands from the debugger
        sda.waitForEvent();
        if (!sda.hasQuit()) {
            jdbThread.start();
        }

        // At this point we have successfully connected the debugger
        // through this proxy to the Squawk VM. We can now sit back and
        // wait for packets to start flowing.
        try {
            sdaThread.join();
            jdbThread.join();
        } catch (InterruptedException ex) {
        }

        if (Log.info()) {
            Log.log("Completed shutdown");
        }
        System.out.println("Debug session completed.");
        System.out.println();
    }

    public static void main(String args[]) throws IOException {
        SDP sdp = new SDP();
        try {

            Thread.currentThread().setName("SDP");

            if (!sdp.parseArgs(args)) {
                System.exit(1);
            }

            sdpTable.put(sdp.getProxyName(), sdp);
            do {
                if (sdp.sniffOnly) {
                    sdp.goSniff();
                } else {
                    sdp.go();
                }
            } while (!sdp.singleSession);
        } finally {
            sdpTable.remove(sdp.getProxyName());
        }
    }
    
    /**
     * Starts a single sniff session between a JVM and a debug client. Returns
     * when the session is closed from either end.
     */
    private void goSniff() {

        // Establish the connection to the VM
        JDWPListener sda = new JDWPSniffer.JVMSniffer();
        
        byte[] handshake = "JDWP-Handshake".getBytes();
        while (true) {
            try {
                System.out.println("Trying to connect to VM on " + vm_url);
                long now = System.currentTimeMillis();
                sda.open(vm_url, handshake, true, false);
                System.out.println("Established connection to VM (handshake took " + (System.currentTimeMillis() - now) + "ms)");
                break;
            } catch (IOException e) {
                System.out.println("Failed to establish connection with VM: " + e.getMessage() + " - trying again in " + retry + " seconds...");

                // Sleep and try again
                try {
                    Thread.sleep(retry * 1000);
                } catch (InterruptedException ie) {
                }
            }
        }

        // Establish connection from debugger
        JDWPListener jdb = new JDWPSniffer.JDBSniffer();
        try {
            System.out.println("Waiting for connection from debugger on " + debugger_url);
            jdb.open(debugger_url, handshake, false, true);
            System.out.println("Established connection with debugger");
        } catch (IOException e) {
            System.out.println("Failed to establish connection with JDWP debugger: " + e.getMessage());
            sda.quit();
            return;
        }

        // The connection with the VM may have been lost by the time the debugger connects
        if (sda.hasQuit()) {
            jdb.quit();
            return;
        }

        jdb.bindProxyPeer(sda);

        Thread jvmThread = new Thread(sda, "JVM");
        Thread jdbThread = new Thread(jdb, "Debugger");

        jvmThread.start();
        jdbThread.start();

        // At this point we have successfully connected the debugger
        // through this proxy to the JVM. We can now sit back and
        // wait for packets to start flowing.
        try {
            jvmThread.join();
            jdbThread.join();
        } catch (InterruptedException ex) {
        }

        if (Log.info()) {
            Log.log("Completed shutdown");
        }
        System.out.println("Debug session completed.");
        System.out.println();
    }

    /*-----------------------------------------------------------------------*\
     *                            SDPEventManager                            *
    \*-----------------------------------------------------------------------*/

    class MatcherImpl implements EventRequestModifier.Matcher {

        /**
         * {@inheritDoc}
         */
        public boolean matches(ClassMatch modifier, Debugger.Event event) {
            String name = ((ProxyType)event.object).getName();
            boolean result = false;
            switch (modifier.matchKind) {
                case ClassMatch.EQUALS:      result = name.equals(modifier.pattern);        break;
                case ClassMatch.STARTS_WITH: result = name.startsWith(modifier.pattern);    break;
                case ClassMatch.ENDS_WITH:   result = name.endsWith(modifier.pattern);      break;
                case ClassMatch.CONTAINS:    result = name.indexOf(modifier.pattern) != -1; break;
                default: Assert.shouldNotReachHere();
            }
            return modifier.exclude ^ result;
        }

        /**
         * {@inheritDoc}
         */
        public boolean matches(ClassOnly modifier, Debugger.Event event) {
            ProxyType type = (ProxyType)event.object;
            try {
                return ptm.lookup(modifier.clazz, true).getKlass().isAssignableFrom(type.getKlass());
            } catch (SDWPException e) {
                System.err.println("Class ID in ClassOnly modifier is invalid: " + e);
                return false;
            }
        }

        /**
         * {@inheritDoc}
         */
        public boolean matches(ExceptionOnly modifier, Debugger.Event event) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public boolean matches(LocationOnly modifier, Debugger.Event event) {
            return false;
        }
    }

    class SDPEventManager extends EventManager {

        /**
         * Event IDs originating from the SDA will be even and events from the SDP will be odd.
         */
        private int nextEventID = 1;

        SDPEventManager(EventRequestModifier.Matcher matcher) {
            super(matcher);
        }
        
        /** 
         * @param id an EventRequest id
         * @retun true if this id is for a proxy handled event.
         */
        public boolean isMyEventRequestID(int id) {
            return (id & 1) == 1;
        }
        
        /**
         * Calculate the next valid proxy Event request id.
         * @return id
         */
        private int getNextEventRequestID() {
            int id = nextEventID += 2;
            Assert.that(isMyEventRequestID(id));
            return id;
        }

        public int registerEventRequest(PacketInputStream in) throws IOException, SDWPException {
            int kind = in.readByte("eventKind");
            EventRequest request;
            
            switch (kind) {
                case JDWP.EventKind_CLASS_PREPARE:
                    request = new ClassPrepare(getNextEventRequestID(), in, kind);
                    break;
                case JDWP.EventKind_BREAKPOINT:
                case JDWP.EventKind_SINGLE_STEP:
                case JDWP.EventKind_VM_INIT:
                case JDWP.EventKind_THREAD_START:
                case JDWP.EventKind_THREAD_END:
                case JDWP.EventKind_VM_DEATH:
                case JDWP.EventKind_EXCEPTION:
                    return -1;
                case JDWP.EventKind_FRAME_POP:
                case JDWP.EventKind_USER_DEFINED:
                case JDWP.EventKind_CLASS_UNLOAD:
                case JDWP.EventKind_CLASS_LOAD:
                case JDWP.EventKind_FIELD_ACCESS:
                case JDWP.EventKind_FIELD_MODIFICATION:
                case JDWP.EventKind_EXCEPTION_CATCH:
                case JDWP.EventKind_METHOD_ENTRY:
                case JDWP.EventKind_METHOD_EXIT:
                    request = new Unsupported(getNextEventRequestID(), in, kind);
                    break;
                default:
                    throw new SDWPException(JDWP.Error_INVALID_EVENT_TYPE, "event kind = " + kind);
            }
            if (request.suspendPolicy != JDWP.SuspendPolicy_NONE) {
                // forget about handling suspension remotely, just send off.
                return -1;
            }
            register(request);
            return request.id;
        }

        public void send(Debugger.Event event, MatchedRequests mr) throws IOException, SDWPException {

            Assert.that(mr.suspendPolicy == JDWP.SuspendPolicy_NONE);
//            // do thread suspension:
//            if (mr.suspendPolicy != JDWP.SuspendPolicy_NONE) {
//                Thread thread = event.getThread();
//                suspendThreads(mr.suspendPolicy == JDWP.SuspendPolicy_ALL ? null : thread);
//            }

            CommandPacket command = new CommandPacket(JDWP.Event_COMMAND_SET, JDWP.Event_Composite_COMMAND, false);
            PacketOutputStream out = command.getOutputStream();
            out.writeByte(mr.suspendPolicy, "suspendPolicy");
            out.writeInt(mr.requests.size(), "events");
            for (Enumeration e = mr.requests.elements(); e.hasMoreElements(); ) {
                SDPEventRequest request = (SDPEventRequest)e.nextElement();
                out.writeByte(request.kind, "eventKind");
                out.writeInt(request.id, "requestID");
                request.write(out, event);
                if (Log.info()) {
                    Log.log("Added notification: " + request);
                }
            }

            jdb.sendCommand(command);
        }

    } // SDPEventManager

    abstract class SDPEventRequest extends EventRequest {
        protected SDPEventRequest(int kind, int suspendPolicy) {
            super(kind, suspendPolicy);
        }
        protected SDPEventRequest(int id, PacketInputStream in, int kind) throws SDWPException, IOException {
            super(id, in, kind);
        }
        protected EventRequestModifier readModifier(PacketInputStream in, int kind) throws SDWPException, IOException {
            int modKind = in.readByte("modKind");
            EventRequestModifier modifier;
            switch (modKind) {
                case JDWP.EventRequest_MOD_COUNT:          modifier = new Count(in);                    break;
                case JDWP.EventRequest_MOD_CLASS_ONLY:     modifier = new ClassOnly(in, kind);          break;
                case JDWP.EventRequest_MOD_CLASS_MATCH:    modifier = new ClassMatch(in, kind, false);  break;
                case JDWP.EventRequest_MOD_CLASS_EXCLUDE:  modifier = new ClassMatch(in, kind, true);   break;
                case JDWP.EventRequest_MOD_THREAD_ONLY:    modifier = new ThreadOnly(in, kind);         break;
                default: throw new SDWPException(JDWP.Error_NOT_IMPLEMENTED, "Unimplemented modkind " + modKind);
            }
            return modifier;
        }

        abstract void write(PacketOutputStream out, Debugger.Event event) throws IOException, SDWPException;
    }

    /**
     * This class encapsulates a request for notification of a <code>JDWP.EventKind.CLASS_PREPARE</code> event.
     */
    class ClassPrepare extends SDPEventRequest {

        /**
         * @see EventRequest#EventRequest(int, int)
         */
        public ClassPrepare(int suspendPolicy) {
            super(JDWP.EventKind_CLASS_PREPARE, suspendPolicy);
        }

        /**
         * @see EventRequest#EventRequest(PacketInputStream, EventManager.VMAgent, int)
         */
        public ClassPrepare(int id, PacketInputStream in, int kind) throws SDWPException, IOException {
            super(id, in, kind);
        }

        /**
         * {@inheritDoc}
         */
        public void write(PacketOutputStream out, Debugger.Event event) throws IOException {
            out.writeObjectID((ObjectID)event.getThreadID(), "thread");
            Assert.that(event.object instanceof ProxyType);
            ProxyType type = (ProxyType)event.object;

            out.writeByte(JDWP.getTypeTag(type.getKlass()), "refTypeTag");
            out.writeReferenceTypeID(type.getID(), "typeID");
            String sig = type.getSignature();
            out.writeString(sig, "signature");

            // All classes are initialized from a debugger clients perspective as
            // the default values will be returned for the statics of uninitialized
            // classes
            out.writeInt(JDWP.ClassStatus_VERIFIED_PREPARED_INITIALIZED, "status");
        }
    }

    /**
     * This class encapsulates a request for notification of an event kind that is not applicable to Squawk.
     * These events are registered so that a VenetRequest.Set command from a debugger client is
     * successful. It also means that the debugger can later clear the event.
     */
    class Unsupported extends SDPEventRequest {
        public Unsupported(int id, PacketInputStream in, int kind) throws SDWPException, IOException {
            super(id, in, kind);
        }
        public void write(PacketOutputStream out, Debugger.Event event) throws IOException {
            Assert.shouldNotReachHere();
        }
        public boolean matchKind(int eventKind) {
            return false;
        }
    }

    public void suspendThreads(Thread thread) {
        try {
            if (thread == null) {

                tpm.suspendAllThreads();
                // Asynchronous command
                CommandPacket command = new CommandPacket(JDWP.VirtualMachine_COMMAND_SET, JDWP.VirtualMachine_Suspend_COMMAND, false);
                sda.sendCommand(command);
            } else {
                ProxyThread pt = (ProxyThread)thread;
                pt.setSuspendCount(pt.getSuspendCount() + 1);

                // Asynchronous command
                CommandPacket command = new CommandPacket(JDWP.ThreadReference_COMMAND_SET, JDWP.ThreadReference_Suspend_COMMAND, false);
                command.getOutputStream().writeObjectID(pt.id, "thread");
                sda.sendCommand(command);
            }
        } catch (IOException e) {
            System.err.println("Error sending suspend command to VM: ");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error sending suspend command to VM: ");
            e.printStackTrace();
        }
    }
}
