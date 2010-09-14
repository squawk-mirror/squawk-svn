package com.sun.squawk.test;

import javax.safetycritical.ManagedInterruptHappening;

/**
 * Not used
 */
public class SyncHapHandler extends ManagedInterruptHappening {

    public SyncHapHandler() {
        super(Config.SIGQUIT);
    }

    protected void process() {
        Printer.silence();
    }
}
