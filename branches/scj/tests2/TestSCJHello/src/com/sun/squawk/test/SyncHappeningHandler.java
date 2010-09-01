package com.sun.squawk.test;

import javax.safetycritical.ManagedInterruptHappening;

public class SyncHappeningHandler extends ManagedInterruptHappening {

    public SyncHappeningHandler() {
        super(Config.SIGQUIT);
    }

    protected void process() {
        Printer.silence();
    }
}
