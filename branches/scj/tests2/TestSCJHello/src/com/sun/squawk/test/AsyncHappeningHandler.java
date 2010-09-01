package com.sun.squawk.test;

import javax.realtime.AperiodicParameters;
import javax.realtime.AsyncEvent;
import javax.realtime.PriorityParameters;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;
import javax.safetycritical.AperiodicEvent;
import javax.safetycritical.AperiodicEventHandler;
import javax.safetycritical.ManagedAutonomousHappening;
import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;

public class AsyncHappeningHandler extends AperiodicEventHandler {

    static class AsyncHappening extends ManagedAutonomousHappening {
        public AsyncHappening(AperiodicEventHandler handler) {
            super(Config.SIGQUIT);
            AsyncEvent event = new AperiodicEvent(handler);
            attach(event);
        }
    }

    public AsyncHappeningHandler(PriorityParameters priority, AperiodicParameters aperiod,
            StorageParameters storage, long initMemSize) {
        super(priority, aperiod, storage, initMemSize);
        new AsyncHappening(this).register();
    }

    public void handleAsyncEvent() {
        Printer.silence();
        RelativeTime oneSec = new RelativeTime(1000, 0);
        System.err.println("\n[HelloWorld] Prepare to shutdown ...");
        for (int i = 0; i < 3; i++) {
            System.err.print("[HelloWorld] Count down -----------------------> ");
            System.err.print(3 - i);
            System.err.println(" <---------------------");
            try {
                RealtimeThread.sleep(oneSec);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Mission.getCurrentMission().requestSequenceTermination();
    }
}
