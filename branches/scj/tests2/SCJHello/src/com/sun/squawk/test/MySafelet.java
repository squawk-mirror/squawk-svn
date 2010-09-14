package com.sun.squawk.test;

import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;

import com.sun.squawk.BackingStore;

public class MySafelet implements Safelet {

    public MissionSequencer getSequencer() {
        System.out.println("[HelloWorld] Safelet getSequencer ... ");
        return new MySequencer();
    }

    public void setUp() {
        System.out.println("[HelloWorld] Safelet setUp ... ");
        // This is NOT public API. Use it ONLY when you don't want to make your
        // screen full of illegal assignment warning and really want to see some
        // useful messages instead during the run.
        BackingStore.disableScopeCheck();
    }

    public void tearDown() {
        System.out.println("[HelloWorld] Safelet tearDown ... ");
    }
}
