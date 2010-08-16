package com.sun.squawk.test;

import javax.realtime.RealtimeThread;
import javax.safetycritical.ManagedMemory;
import javax.safetycritical.Mission;

public class GarbageGenerator implements Runnable {
    private String msg;
    private final int depth;
    private int curDepth;
    private long privateSize;
    private long counter;
    private int iterations;

    GarbageGenerator(String msg, int depth, long privateSize, int iter) {
        this.msg = msg;
        this.depth = depth;
        this.curDepth = depth;
        this.privateSize = privateSize;
        this.counter = 0;
        this.iterations = iter;
    }

    public void run() {
        ManagedMemory mm = (ManagedMemory) RealtimeThread.getCurrentMemoryArea();
        if (curDepth < depth) {
//            Object[] array = new Object[1000];
//            array[0] = new Object();
            long start = System.currentTimeMillis();

            for (int i = 0; i < 1000000; i++)
                new Object();
//            for (int i = 0; i < 10000000; i++) {
//                array[(i + 1) % 1000] = array[i % 1000];
//            }
            System.err.println("Handling takes " + (System.currentTimeMillis() - start));

            if (--iterations == 0) {
                System.out.println("[" + msg + "] requests termination ...");
                Mission.getCurrentMission().requestTermination();
            }
        }
        if (--curDepth > 0)
            mm.enterPrivateMemory(privateSize, this);
        curDepth++;
    }

}
