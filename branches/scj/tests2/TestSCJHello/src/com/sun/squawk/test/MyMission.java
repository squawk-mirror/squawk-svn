package com.sun.squawk.test;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.ManagedSchedulable;
import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;

import com.sun.squawk.VM;

public class MyMission extends Mission {

    int turn;
    static long B = 1;
    static long KB = 1024 * B;
    static long MB = 1024 * KB;

    PriorityParameters priority = new PriorityParameters(42);
    PeriodicParameters period = new PeriodicParameters(null, new RelativeTime(100, 0), null,
            null);
    StorageParameters storage = new StorageParameters(1 * MB, -1, 5 * KB);
    long privateSize = 5 * KB;
    long initSize = 50 * KB;
    int privateDepth = 2;

    protected void initialize() {
        System.out.println("[SCJ Hello] mission " + turn + " initialize ... @" + VM.getTimeMillis());
        // ManagedThread hello = new ManagedThread(priority, storage, initSize,
        // new Printer("Hello", privateDepth,
        // privateSize));
        // ManagedThread world = new ManagedThread(priority, storage, initSize,
        // new Printer("World", privateDepth,
        // privateSize));
        ManagedSchedulable hello = new MyPEH(priority, period, storage, initSize, new Printer(
                "Hello", privateDepth, privateSize));
        ManagedSchedulable world = new MyPEH(priority, period, storage, initSize, new Printer(
                "World", privateDepth, privateSize));
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
