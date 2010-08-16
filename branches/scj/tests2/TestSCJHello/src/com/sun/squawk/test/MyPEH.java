package com.sun.squawk.test;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

public class MyPEH extends PeriodicEventHandler {

    private Runnable logic;

    public MyPEH(PriorityParameters priority, PeriodicParameters period, StorageParameters storage,
            long initMemSize, Runnable logic) {
        super(priority, period, storage, initMemSize);
        this.logic = logic;
    }

    public void handleAsyncEvent() {
        logic.run();
    }
}
