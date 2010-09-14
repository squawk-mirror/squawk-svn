package com.sun.squawk.test;

import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;

public class MySafelet implements Safelet {

    public MissionSequencer getSequencer() {
        System.out.println("[HelloWorld] Safelet getSequencer ... ");
        return new MySequencer();
    }

    public void setUp() {
        System.out.println("[HelloWorld] Safelet setUp ... ");
    }

    public void tearDown() {
        System.out.println("[HelloWorld] Safelet tearDown ... ");
    }
}
