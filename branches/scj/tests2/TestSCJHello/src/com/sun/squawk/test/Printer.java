package com.sun.squawk.test;

import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.realtime.RealtimeThread;
import javax.safetycritical.ManagedMemory;
import javax.safetycritical.Mission;

public class Printer implements Runnable {

    private String msg;
    private int curDepth;
    private int iterations;
    private int counter;

    Printer(String msg) {
        this.msg = msg;
    }

    public void run() {
        if (curDepth == 0 && iterations++ == Config.iterations) {
            System.out.println("[" + msg + "] requests termination ...");
            Mission.getCurrentMission().requestTermination();
            return;
        }
        ManagedMemory mm = (ManagedMemory) RealtimeThread.getCurrentMemoryArea();
        if (curDepth < Config.privateDepth) {
            AbsoluteTime now = Clock.getRealtimeClock().getTime();
            System.out.println("[" + counter++ + "] " + msg + " @ private memory level "
                    + (Config.privateDepth - curDepth) + " - " + mm + " - " + now.getMilliseconds()
                    + ":" + now.getNanoseconds());
        }
        if (curDepth++ >= Config.privateDepth)
            mm.enterPrivateMemory(Config.privateSize, this);
        curDepth--;
    }
}
