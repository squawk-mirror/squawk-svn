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

/**
 * The asynchronous handler for POSIX SIGQUIT. When receiving signal, disable
 * printers, print the backing store tree, and count down to terminate the
 * program.
 */
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
        // This is NOT public API. Used only for debugging or for fun.
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
