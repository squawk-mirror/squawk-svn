package com.sun.squawk.test;

import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;

public class MySafelet implements Safelet {

    public MissionSequencer getSequencer() {
        System.err.println("[HelloWorld] Safelet getSequencer ... ");
        return new MyMissionSequencer();
    }

    public void setUp() {
        System.err.println("[HelloWorld] Safelet setUp ... ");
    }

    public void tearDown() {
        System.err.println("[HelloWorld] Safelet tearDown ... ");
    }
}
