package com.sun.squawk.test;

import javax.safetycritical.Mission;

public class MyMission extends Mission {

    public int turn;

    protected void initialize() {
        System.out.print("[HelloWorld] Mission ");
        System.out.print(turn);
        System.out.println(" initializes ...");

        new MyPEH(Config.priority, Config.periodHello, Config.storage, Config.initPrivateSize,
                new Printer("Hello")).register();
        new MyPEH(Config.priority, Config.periodWord, Config.storage, Config.initPrivateSize,
                new Printer("World")).register();
        new AsyncHapHandler(Config.priority, Config.aperiod, Config.storage,
                Config.initPrivateSize).register();
        // new SyncHappeningHandler().register();
        
        System.out.print("[HelloWorld] Mission ");
        System.out.print(turn);
        System.out.println(" initialization finished ...");
    }

    public long missionMemorySize() {
        return Config.missionMemSize;
    }

    protected void cleanUp() {
        System.out.println("[HelloWorld] Mission " + turn + " cleanUp ... ");
    }
}
