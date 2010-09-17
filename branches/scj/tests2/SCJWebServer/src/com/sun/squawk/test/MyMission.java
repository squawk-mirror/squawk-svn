package com.sun.squawk.test;

import java.io.IOException;

import javax.safetycritical.Mission;

import org.sunspotworld.demo.WebServer;

public class MyMission extends Mission {

    WebServer server;

    // Runs in immortal memory
    public MyMission() {
        try {
            server = new WebServer(8080);
        } catch (IOException e) {
            System.err.println("Creating server ");
            e.printStackTrace();
        }
    }

    protected void initialize() {
        try {
            server.initialize();
//            new AsyncHapHandler(Config.priority, Config.aperiod, Config.storage,
//                    Config.initPrivateSize).register();
        } catch (IOException e) {
            System.err.println("Server initialization ");
            e.printStackTrace();
        }
    }

    public long missionMemorySize() {
        return Config.missionMemSize;
    }
}
