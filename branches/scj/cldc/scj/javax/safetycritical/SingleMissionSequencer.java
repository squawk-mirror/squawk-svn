package javax.safetycritical;

import javax.realtime.PriorityParameters;

//@SCJAllowed
public class SingleMissionSequencer extends MissionSequencer {
    Mission mission;

    // @SCJAllowed
    // @SCJRestricted(INITIALIZATION)
    public SingleMissionSequencer(PriorityParameters priority,
            StorageParameters storage, Mission mission) {
        super(priority, storage);
        this.mission = mission;
    }

    // @SCJAllowed
    protected Mission getNextMission() {
        Mission m = mission;
        mission = null;
        return m;
    }
}
