package com.sun.squawk.test;

import javax.safetycritical.Mission;

import com.sun.squawk.VM;

public class MyMission extends Mission {

    public int turn;

    protected void initialize() {
        System.out
                .println("[SCJ Hello] mission " + turn + " initialize ... @" + VM.getTimeMillis());

        new MyPEH(Config.priority, Config.periodHello, Config.storage, Config.initPrivateSize,
                new Printer("Hello")).register();
        new MyPEH(Config.priority, Config.periodWord, Config.storage, Config.initPrivateSize,
                new Printer("World")).register();
    }

    public long missionMemorySize() {
        return Config.missionMemSize;
    }

    protected void cleanUp() {
        System.out.println("[SCJ Hello] mission " + turn + " clean up ... ");
    }
}
