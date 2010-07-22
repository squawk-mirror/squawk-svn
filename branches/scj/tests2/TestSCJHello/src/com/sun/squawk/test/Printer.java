package com.sun.squawk.test;

import javax.realtime.RealtimeThread;
import javax.safetycritical.ManagedMemory;

public class Printer implements Runnable {

    private String msg;
    private final int depth;
    private int curDepth;
    private long privateSize;

    Printer(String msg, int depth, long privateSize) {
        this.msg = msg;
        this.depth = depth;
        this.curDepth = depth;
        this.privateSize = privateSize;
    }

    public void run() {
        ManagedMemory mm = (ManagedMemory) RealtimeThread.getCurrentMemoryArea();
        System.out.println(msg + " from nested private memory level " + (depth - curDepth));
        if (--curDepth > 0)
            mm.enterPrivateMemory(privateSize, this);
    }
}
