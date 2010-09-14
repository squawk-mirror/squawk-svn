package com.sun.squawk.test;

import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;

public class MySequencer extends MissionSequencer {

    private int counter = 0;

    private MyMission mission = new MyMission();

    public MySequencer() {
        super(Config.priority, Config.storage);
    }

    protected Mission getNextMission() {
        if (counter++ < Config.nMissions) {
            System.out.println("[SCJ Hello] Safelet getNextMission ... ");
            mission.turn = counter - 1;
            return mission;
        }
        return null;
    }
}
