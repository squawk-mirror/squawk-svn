package org.sunspotworld.demo;

import java.io.IOException;

import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class SynchronizedSocket {
    StreamConnectionNotifier notifier;

    SynchronizedSocket(StreamConnectionNotifier notifier) {
        this.notifier = notifier;
    }

    synchronized StreamConnection acceptAndOpen(String who) throws IOException {
//        System.err.print(who);
//        System.err.println(" is listenning ...");
        StreamConnection ret = notifier.acceptAndOpen();
//        System.err.print(who);
//        System.err.println(" gets a request ...");
        return ret;
    }
}
