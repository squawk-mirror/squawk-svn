package com.sun.squawk.test;

import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;

public class MySafelet implements Safelet {

    public MissionSequencer getSequencer() {
        System.out.println("[SCJ Hello] Safelet getSequencer ... ");        
        return new MyMissionSequencer();
    }

    public void setUp() {
        System.out.println("[SCJ Hello] Safelet set up ... ");
    }

    public void tearDown() {
        System.out.println("[SCJ Hello] Safelet tear down ... ");
    }
}