package com.sun.squawk.test;

import javax.realtime.AperiodicParameters;
import javax.realtime.AsyncEvent;
import javax.realtime.PriorityParameters;
import javax.realtime.RealtimeThread;
import javax.safetycritical.AperiodicEvent;
import javax.safetycritical.AperiodicEventHandler;
import javax.safetycritical.ManagedAutonomousHappening;
import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;

import com.sun.squawk.BackingStore;

public class AsyncHapHandler extends AperiodicEventHandler {

    static class AsyncHappening extends ManagedAutonomousHappening {
        public AsyncHappening(AperiodicEventHandler handler) {
            super(Config.SIGQUIT);
            AsyncEvent event = new AperiodicEvent(handler);
            attach(event);
        }
    }

    public AsyncHapHandler(PriorityParameters priority, AperiodicParameters aperiod,
            StorageParameters storage, long initMemSize) {
        super(priority, aperiod, storage, initMemSize);
        new AsyncHappening(this).register();
    }

    public void handleAsyncEvent() {
        Printer.silence();
        BackingStore.printBSTree(true);
        System.out.println("\n[HelloWorld] Prepare to shutdown ...");
        for (int i = Config.countDown; i >= 0; i--) {
            System.out.print("[HelloWorld] Count down -----------------------> ");
            System.out.print(i);
            System.out.println(" <---------------------");
            try {
                RealtimeThread.sleep(Config.rel_1000ms_0ns);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Mission.getCurrentMission().requestSequenceTermination();
    }
}
