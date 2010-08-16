package com.sun.squawk.test;

import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.realtime.RealtimeThread;
import javax.safetycritical.ManagedMemory;
import javax.safetycritical.Mission;

public class Printer implements Runnable {

    private String msg;
    private final int depth;
    private int curDepth;
    private long privateSize;
    private long counter;
    private int iterations;

    Printer(String msg, int depth, long privateSize, int iter) {
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
            AbsoluteTime now = Clock.getRealtimeClock().getTime();
            System.out.println("[" + counter++ + "] " + msg + " @ private memory level "
                    + (depth - curDepth) + " - " + mm + " - " + now.getMilliseconds() + ":"
                    + now.getNanoseconds());
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
