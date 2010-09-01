package javax.safetycritical;

import javax.realtime.Happening;
import javax.safetycritical.util.Utils;

import com.sun.squawk.BackingStore;
import com.sun.squawk.util.SimpleLinkedList;

/**
 * This interface marked those objects that are managed by some mission and
 * provides a means to obtain the manager for that mission.
 */
public class MissionManager {

    volatile int phase = INACTIVE;
    final static int INACTIVE = -1;
    final static int INITIALIZATION = 0;
    final static int EXECUTION = 1;
    final static int CLEANUP = 2;

    private Mission mission;

    private volatile boolean termintionPending = false;
    private volatile boolean sequenceTerminationPending = false;

    // the head of the schedulable list
    private ManagedSchedulable schedulables = null;
    private SimpleLinkedList happenings = new SimpleLinkedList();

    MissionManager(Mission mission) {
        this.mission = mission;
    }

    void regSchedulable(ManagedSchedulable so) {
        if (schedulables == null)
            schedulables = so;
        else {
            so.setNext(schedulables);
            schedulables = so;
        }
    }

    public void regHappening(Happening hap) {
        happenings.addLast(hap);
    }

    Mission getMission() {
        return mission;
    }

    void go() {
        phase = INITIALIZATION;
        mission.initialize();
        phase = EXECUTION;

        if (Utils.DEBUG)
            BackingStore.printCurrentBSStats();

        for (ManagedSchedulable so = schedulables; so != null; so = so.getNext())
            so.start();
        try {
            for (ManagedSchedulable so = schedulables; so != null; so = so.getNext())
                so.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (ManagedSchedulable so = schedulables; so != null; so = so.getNext())
            so.cleanUp();

        phase = CLEANUP;
        mission.cleanUp();
        /*
         * The SCJ Spec says that users CAN unregister the happenings at
         * mission.cleanUp(). However, they are not mandatory to do that. So the
         * problem is what if there are some happening unregistered after the
         * mission.
         * 
         * Leaving some registered happening in the gap between missions can be
         * dangerous since their handlers might be gone along with the mission.
         * Before everything is clarified, we simply unregister all happenings
         * belongs to current mission.
         * 
         * Actually, this is still a hole: can a happening span missions? If
         * yes, how to ensure the happening will not hold some dead reference?
         * Particularly, ManagedAutonomousHappening can be attached with a
         * AsyncEvent. What if the AsyncEvent object dies with the mission,
         * while the happening is still alive?
         */
        while (happenings.size() > 0) {
            ((Happening) happenings.removeFirst()).unRegister();
        }
        phase = INACTIVE;
    }

    void requestTermination() {
        if (!termintionPending) {
            termintionPending = true;
            for (ManagedSchedulable so = schedulables; so != null; so = so.getNext())
                so.stop();
        }
    }

    void requestSequenceTermination() {
        if (!sequenceTerminationPending) {
            sequenceTerminationPending = true;
            requestTermination();
        }
    }

    boolean terminationPending() {
        return termintionPending;
    }

    boolean sequenceTerminationPending() {
        return sequenceTerminationPending;
    }
}
