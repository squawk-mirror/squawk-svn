package com.sun.squawk.test;

import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;

public class MyMissionSequencer extends MissionSequencer {

    private int counter = 0;

    private MyMission mission = new MyMission();

    public MyMissionSequencer() {
        super(null, null);
    }

    protected Mission getNextMission() {
        if (counter++ < 3) {
            System.out.println("[SCJ Hello] Safelet get next mission ... ");
            mission.turn = counter - 1;
            return mission;
        }
        return null;
    }

}
