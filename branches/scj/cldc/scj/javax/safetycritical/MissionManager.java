package javax.safetycritical;

import javax.realtime.Happening;

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

    // ManagedSchedulables, Happenings, belonging to current mission
    private ManagedThread threads = null;
    private ManagedEventHandler eventHandlers = null;
    private SimpleLinkedList happenings = new SimpleLinkedList();

    MissionManager(Mission mission) {
        this.mission = mission;
    }

    void regManagedEventHandler(ManagedEventHandler handler) {
        if (eventHandlers == null)
            eventHandlers = handler;
        else {
            handler.next = eventHandlers;
            eventHandlers = handler;
        }
    }

    void regManagedThread(ManagedThread thread) {
        if (threads == null)
            threads = thread;
        else {
            thread.next = threads;
            threads = thread;
        }
    }

    public void regHappening(Happening hap) {
        happenings.addLast(hap);
    }

    Mission getMission() {
        return mission;
    }

    void startMission() {
        phase = INITIALIZATION;
        doInitialization();
        phase = EXECUTION;
        doExecution();
        phase = CLEANUP;
        doCleanup();
        phase = INACTIVE;
    }

    private void doInitialization() {
        mission.initialize();
    }

    private void doExecution() {
        ManagedEventHandler h = null;
        ManagedThread t = null;

        // start all
        for (h = eventHandlers; h != null; h = h.next)
            h.start();
        for (t = threads; t != null; t = t.next)
            t.start();

        // wait for all
        try {
            for (h = eventHandlers; h != null; h = h.next)
                h.join();
            for (t = threads; t != null; t = t.next)
                t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // clean up all
        for (h = eventHandlers; h != null; h = h.next)
            h.cleanUp();
        for (t = threads; t != null; t = t.next)
            t.cleanUp();
    }

    private void doCleanup() {
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
        while (happenings.size() > 0)
            ((Happening) happenings.removeFirst()).unRegister();
    }

    void requestTermination() {
        if (!termintionPending) {
            termintionPending = true;
            for (ManagedEventHandler handler = eventHandlers; handler != null; handler = handler.next)
                handler.stop();
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
