package com.sun.squawk.test;

import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.realtime.RealtimeThread;
import javax.safetycritical.ManagedMemory;
import javax.safetycritical.Mission;

public class Printer implements Runnable {

    private String msg;
    private int depth = 0;
    private int iters = 0;
    private static volatile boolean allowed = true;

    Printer(String msg) {
        this.msg = msg;
    }

    public void run() {
        try {
            if (checkForTermination())
                return;
            if (allowed)
                doPrint();
            ManagedMemory mm = (ManagedMemory) RealtimeThread.getCurrentMemoryArea();
            if (++depth < Config.privateDepth)
                mm.enterPrivateMemory(Config.privateSize, this);
            depth--;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private boolean checkForTermination() {
        if (depth == 0 && iters++ >= Config.iterations) {
            System.err.println("[HelloWorld] All iterations finished. Requests mission termination ...");
            Mission.getCurrentMission().requestTermination();
            return true;
        }
        return false;
    }

    private void doPrint() {
        AbsoluteTime now = Clock.getRealtimeClock().getTime();
        System.err.print("[");
        System.err.print(iters);
        System.err.print("] \t");
        System.err.print(msg);
        System.err.print(" @ PM Level ");
        System.err.print(depth);
        System.err.print(" \t - ");
        System.err.print(now.getMilliseconds());
        System.err.print(":");
        System.err.println(now.getNanoseconds());
    }

    public static void silence() {
        allowed = false;
        System.err.println("[HelloWorld] Printers silenced!");
    }
}
