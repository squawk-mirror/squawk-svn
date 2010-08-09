package javax.safetycritical;

import javax.safetycritical.util.SCJLinkedList;

/**
 * This interface marked those objects that are managed by some mission and
 * provides a means to obtain the manager for that mission.
 */
public class MissionManager {

    public volatile int phase;

    public final static int INACTIVE = -1;
    public final static int INITIALIZATION = 0;
    public final static int EXECUTION = 1;
    public final static int CLEANUP = 2;

    Mission mission;

    // TODO: re-implement the linked list
    SCJLinkedList sos = new SCJLinkedList();

    MissionManager(Mission mission) {
        System.out.println("[SCJ] MissionManager.<init>");
        this.mission = mission;
        phase = INACTIVE;
    }

    public void addScheduble(ManagedSchedulable so) {
        sos.add(so);
    }

    public Mission getMission() {
        return mission;
    }

    public void go() {
        phase = INITIALIZATION;
        mission.initialize();
        phase = EXECUTION;
        for (int i = 0; i < sos.size(); i++)
            ((ManagedSchedulable) sos.get(i)).start();
        try {
            for (int i = 0; i < sos.size(); i++)
                ((ManagedSchedulable) sos.get(i)).join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        phase = EXECUTION;
        mission.cleanUp();
        phase = INACTIVE;
    }
}
