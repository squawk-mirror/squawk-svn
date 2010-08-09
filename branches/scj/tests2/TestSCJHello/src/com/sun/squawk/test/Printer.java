package com.sun.squawk.test;

import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.realtime.RealtimeThread;
import javax.safetycritical.ManagedMemory;

public class Printer implements Runnable {

    private String msg;
    private final int depth;
    private int curDepth;
    private long privateSize;
    private long counter;

    Printer(String msg, int depth, long privateSize) {
        this.msg = msg;
        this.depth = depth;
        this.curDepth = depth;
        this.privateSize = privateSize;
        this.counter = 0;
    }

    public void run() {
        ManagedMemory mm = (ManagedMemory) RealtimeThread.getCurrentMemoryArea();
        if (curDepth < depth) {
            AbsoluteTime now = Clock.getRealtimeClock().getTime();
            System.out.println("[" + counter++ + "] " + msg + " @ private memory level "
                    + (depth - curDepth) + " - " + now.getMilliseconds() + ":"
                    + now.getNanoseconds());
        }
        if (--curDepth > 0)
            mm.enterPrivateMemory(privateSize, this);
        curDepth++;
    }
}
