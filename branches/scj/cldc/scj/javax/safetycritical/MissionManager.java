package javax.safetycritical;

import javax.realtime.RealtimeThread;
import javax.safetycritical.util.Utils;

import com.sun.squawk.BackingStore;
import com.sun.squawk.GC;

/**
 * This interface marked those objects that are managed by some mission and
 * provides a means to obtain the manager for that mission.
 */
public class MissionManager {

    public volatile int phase = INACTIVE;
    public final static int INACTIVE = -1;
    public final static int INITIALIZATION = 0;
    public final static int EXECUTION = 1;
    public final static int CLEANUP = 2;

    private Mission mission;

    private volatile boolean termintionPending = false;
    private volatile boolean sequenceTerminationPending = false;

    // the head of the schedulable list
    private ManagedSchedulable head = null;

    MissionManager(Mission mission) {
        this.mission = mission;
    }

    public void addScheduble(ManagedSchedulable so) {
        if (head == null)
            head = so;
        else {
            so.setNext(head);
            head = so;
        }
    }

    public Mission getMission() {
        return mission;
    }

    public void go() {
        phase = INITIALIZATION;
        mission.initialize();
        phase = EXECUTION;

        if (Utils.DEBUG)
            BackingStore.printCurrentBSStats();

        for (ManagedSchedulable so = head; so != null; so = so.getNext())
            so.start();
        try {
            for (ManagedSchedulable so = head; so != null; so = so.getNext())
                so.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (ManagedSchedulable so = head; so != null; so = so.getNext())
            so.cleanUp();

        phase = EXECUTION;
        mission.cleanUp();
        phase = INACTIVE;
    }

    void requestTermination() {
        if (!termintionPending) {
            termintionPending = true;
            for (ManagedSchedulable so = head; so != null; so = so.getNext())
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
