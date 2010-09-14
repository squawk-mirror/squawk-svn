package com.sun.squawk.test;

import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.realtime.RealtimeThread;
import javax.safetycritical.ManagedMemory;
import javax.safetycritical.Mission;

public class Printer implements Runnable {

    private String msg;
    private int depth = 0;
    private int iter = 0;
    private static volatile boolean allowed = true;

    Printer(String msg) {
        this.msg = msg;
    }

    /**
     * Enter private memory until the max depth. Print the message at each
     * level.
     */
    public void run() {
        try {
            reschedPoint();
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

    /** Terminate current mission if all iterations have been finished. */
    private boolean checkForTermination() {
        if (depth == 0 && iter++ >= Config.iterations) {
            System.out
                    .println("[HelloWorld] All iterations finished. Request mission termination ...");
            Mission.getCurrentMission().requestTermination();
            return true;
        }
        return false;
    }

    /** Format: [HelloWorld] iter: (i) (Hello/World) @ PM Level (l) - (ms):(ns) */
    private void doPrint() {
        AbsoluteTime now = Clock.getRealtimeClock().getTime();
        System.out.print("[HelloWorld] iter: ");
        System.out.print(iter);
        System.out.print(" \t ");
        System.out.print(msg);
        System.out.print(" @ PM Level ");
        System.out.print(depth);
        System.out.print(" \t - ");
        System.out.print(now.getMilliseconds());
        System.out.print(":");
        System.out.println(now.getNanoseconds());
    }

    /** Disable printers */
    public static void silence() {
        allowed = false;
        System.out.println("[HelloWorld] Printers silenced!");
    }

    /** Manually add some back branches for allowing the VM to poll */
    private static void reschedPoint() {
        for (int i = 0; i < 100; i++)
            ;
    }
}
