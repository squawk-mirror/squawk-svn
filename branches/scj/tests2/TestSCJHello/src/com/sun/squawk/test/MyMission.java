package com.sun.squawk.test;

import javax.realtime.PriorityParameters;
import javax.safetycritical.ManagedThread;
import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;

public class MyMission extends Mission {

    int turn;
    ManagedThread hello;
    ManagedThread world;

    static long B = 1;
    static long KB = 1024 * B;
    static long MB = 1024 * KB;

    PriorityParameters priority = new PriorityParameters(42);
    StorageParameters storage = new StorageParameters(1 * MB, -1, 5 * KB);
    long privateSize = 5 * KB;
    long initSize = 50 * KB;
    int privateDepth = 10;

    protected void initialize() {
        System.out.println("[SCJ Hello] mission " + turn + " initialize ... ");
        hello = new ManagedThread(priority, storage, initSize, new Printer("Hello", privateDepth,
                privateSize));
        world = new ManagedThread(priority, storage, initSize, new Printer("World", privateDepth,
                privateSize));
        hello.register();
        world.register();
    }

    public long missionMemorySize() {
        return 1 * MB;
    }

    protected void cleanUp() {
        System.out.println("[SCJ Hello] mission " + turn + " clean up ... ");
    }
}
