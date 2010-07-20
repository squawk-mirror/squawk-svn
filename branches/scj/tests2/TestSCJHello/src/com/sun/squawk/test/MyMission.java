package com.sun.squawk.test;

import javax.realtime.PriorityParameters;
import javax.safetycritical.ManagedThread;
import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;

public class MyMission extends Mission {

    int turn;
    ManagedThread hello;
    ManagedThread world;

    PriorityParameters priority = new PriorityParameters(42);
    StorageParameters storage = new StorageParameters(1000000, 5000, 5000);
    long initAreaSize = 5000;

    protected void initialize() {
        System.out.print("[SCJ Hello] Mission initialize ... ");
        System.out.print(turn);
        System.out.println(" turn");

        hello = new ManagedThread(priority, storage, initAreaSize, new Printer("Hello"));
        world = new ManagedThread(priority, storage, initAreaSize, new Printer("World"));
        hello.register();
        world.register();
    }

    public long missionMemorySize() {
        return 2000000;
    }

    protected void cleanUp() {
        System.out.print("[SCJ Hello] Mission clean up ... ");
        System.out.print(turn);
        System.out.println(" turn");
    }
}
