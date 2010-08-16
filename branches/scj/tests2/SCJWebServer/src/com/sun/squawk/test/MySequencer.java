package com.sun.squawk.test;

import javax.realtime.PriorityParameters;
import javax.safetycritical.Mission;
import javax.safetycritical.SingleMissionSequencer;
import javax.safetycritical.StorageParameters;

public class MySequencer extends SingleMissionSequencer {

    private boolean first = true;

    protected Mission getNextMission() {
        if (first) {
            first = false;
            return new MyMission();
        }
        return null;
    }

    public MySequencer(PriorityParameters priority, StorageParameters storage) {
        super(priority, storage, null);
    }
}
