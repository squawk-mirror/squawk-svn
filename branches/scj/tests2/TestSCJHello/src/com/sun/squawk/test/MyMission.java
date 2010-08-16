package com.sun.squawk.test;

import javax.realtime.AsyncEventHandler;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.ManagedSchedulable;
import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;

import com.sun.squawk.VM;

public class MyMission extends Mission {

    public int turn;
    private static long B = 1;
    private static long KB = 1024 * B;
    private static long MB = 1024 * KB;

    private long innerPrivateSize = 5 * MB;
    private long initPrivateSize = 50 * KB;
    private int privateDepth = 2;
    private int iterations = 1;

    protected void initialize() {
        System.out
                .println("[SCJ Hello] mission " + turn + " initialize ... @" + VM.getTimeMillis());

        PriorityParameters priority = new PriorityParameters(Thread.NORM_PRIORITY);

        RelativeTime startHello = new RelativeTime(0, 0);
        RelativeTime startWorld = new RelativeTime(50, 0);
        RelativeTime interval = new RelativeTime(100, 0);
        RelativeTime deadline = null;
        AsyncEventHandler deadlineMisshandler = null;
        PeriodicParameters periodHello = new PeriodicParameters(startHello, interval, deadline,
                deadlineMisshandler);
        PeriodicParameters periodWorld = new PeriodicParameters(startWorld, interval, deadline,
                deadlineMisshandler);

        long totalBS = 6 * MB;
        long nativeStack = -1;
        long javaStack = 5 * KB;
        StorageParameters storage = new StorageParameters(totalBS, nativeStack, javaStack);

//        ManagedSchedulable hello = new MyPEH(priority, periodHello, storage, initPrivateSize,
//                new GarbageGenerator("Hello", privateDepth, innerPrivateSize, iterations));
        ManagedSchedulable world = new MyPEH(priority, periodWorld, storage, initPrivateSize,
                new GarbageGenerator("World", privateDepth, innerPrivateSize, iterations));

//        hello.register();
        world.register();
    }

    public long missionMemorySize() {
        return 1 * MB;
    }

    protected void cleanUp() {
        System.out.println("[SCJ Hello] mission " + turn + " clean up ... ");
    }
}
