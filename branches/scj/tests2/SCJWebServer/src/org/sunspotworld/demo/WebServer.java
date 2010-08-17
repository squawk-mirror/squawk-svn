/*
 * Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * This is a part of the Squawk JVM.
 */
package org.sunspotworld.demo;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnectionNotifier;

import com.sun.squawk.test.Config;

public class WebServer {

    NanoHTTP server;
    public int openConnections;
    private int port;

    public WebServer(int port) throws IOException {
        this.port = port;
        server = new NanoHTTP();
        server.addApplication("/", new AppListServer("iPod Touch"));
        server.addApplication("/about", new AboutServer("iPod Touch"));
        server.addApplication("/stats", new VMStatsServer("iPod Touch"));
        server.addApplication("/files", new FileServer());
    }

    public void initialize() throws IOException {
        SynchronizedSocket notifier = new SynchronizedSocket((StreamConnectionNotifier) Connector
                .open("socket://:" + port));
        for (int i = 0; i < Config.threadPoolSize; i++)
            new WorkerThread(this, notifier).register();
    }
}
