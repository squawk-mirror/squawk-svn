package com.sun.squawk.test;

import javax.safetycritical.Mission;

public class MyMission extends Mission {

    public int turn;

    protected void initialize() {
        System.err.print("[HelloWorld] Mission ");
        System.err.print(turn);
        System.err.println(" initializes ...");

        new MyPEH(Config.priority, Config.periodHello, Config.storage, Config.initPrivateSize,
                new Printer("Hello")).register();
        new MyPEH(Config.priority, Config.periodWord, Config.storage, Config.initPrivateSize,
                new Printer("World")).register();
        new AsyncHappeningHandler(Config.priority, Config.aperiod, Config.storage,
                Config.initPrivateSize).register();
        // new SyncHappeningHandler().register();
    }

    public long missionMemorySize() {
        return Config.missionMemSize;
    }

    protected void cleanUp() {
        System.err.println("[HelloWorld] Mission " + turn + " cleanUp ... ");
    }
}
