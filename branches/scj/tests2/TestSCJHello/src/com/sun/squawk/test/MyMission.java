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
    StorageParameters storage = new StorageParameters(50 * KB, -1, 5 * KB);
    long initAreaSize = 5 * KB;

    protected void initialize() {
        System.out.println("[SCJ Hello] mission " + turn + " initialize ... ");
        hello = new ManagedThread(priority, storage, initAreaSize, new Printer("Hello", 3,
                initAreaSize));
        world = new ManagedThread(priority, storage, initAreaSize, new Printer("World", 3,
                initAreaSize));
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
