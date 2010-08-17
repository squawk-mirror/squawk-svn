package org.sunspotworld.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;
import javax.realtime.RealtimeThread;
import javax.safetycritical.ManagedMemory;
import javax.safetycritical.PeriodicEventHandler;

import com.sun.squawk.BackingStore;
import com.sun.squawk.test.Config;

public class WorkerThread extends PeriodicEventHandler {

    private static int workerCounter = 0;
    WebServer server;
    SynchronizedSocket notifier;
    HTTPSession session = new HTTPSession();
    WorkerThread next;

    WorkerThread(WebServer server, SynchronizedSocket notifier) {
        super(Config.priority, Config.period, Config.storage, Config.initPrivateSize, "Worker-"
                + workerCounter++);
        this.server = server;
        this.notifier = notifier;
    }

    public void handleAsyncEvent() {
        ManagedMemory mm = (ManagedMemory) RealtimeThread.getCurrentMemoryArea();
        while (true) {
            mm.enterPrivateMemory(Config.privateSize, session);
            // BackingStore.printCurrentBSStats();
        }
    }

    class HTTPSession implements Runnable {

        public void run() {
            debug(getName() + " is running ...");

            StreamConnection conn = null;
            try {
                conn = notifier.acceptAndOpen(getName());
            } catch (IOException e) {
                debug("Error in accept:");
                e.printStackTrace();
            }

            InputStream is;
            OutputStream os;
            server.openConnections++;
            try {
                long start = System.currentTimeMillis();
                // debug(getName() + " handles request starting @ " +
                // System.currentTimeMillis());

                is = conn.openInputStream();
                os = conn.openOutputStream();
                server.server.handleRequest(is, os);
                os.write(0);
                os.flush();

                // debug(getName() + " handles request finished @ " +
                // System.currentTimeMillis());
                debug(getName() + ": service takes " + (System.currentTimeMillis() - start));
                is.close();
                os.close();
            } catch (IOException e) {
                debug("Closing connection!");
                e.printStackTrace();
            }
            try {
                conn.close();
                server.openConnections--;
            } catch (IOException ioe) {
                debug("Error in closing connection!");
                ioe.printStackTrace();
            }

            if (Config.DEBUG)
                BackingStore.printBSTree(true);
        }
    }

    private static void debug(String s) {
        System.err.println(s);
    }
}
