package javax.safetycritical;

import javax.realtime.AsyncEventHandler;
import javax.realtime.MemoryArea;
import javax.realtime.PriorityParameters;
import javax.realtime.RealtimeThread;

/**
 * A MissionSequencer runs a sequence of independent Missions interleaved with
 * repeated execution of certain Missions.
 */
// @SCJAllowed
public abstract class MissionSequencer extends AsyncEventHandler {

    private MissionMemory memory;

    private RealtimeThread thread;

    private Runnable runner = new InMissionMemoryRunner();

    private volatile boolean terminationRequestReceived = false;

    /**
     * Construct a MissionSequencer to run at the priority and with the memory
     * resources specified by its parameters.
     * 
     * @throws IllegalStateException
     *             if invoked at an inappropriate time. The only appropriate
     *             times for instantiation of a new MissionSequencer are (a)
     *             during execution of Safelet.getSequencer() by SCJ
     *             infrastructure during startup of an SCJ application, or (b)
     *             during execution of Mission.initialize() by SCJ
     *             infrastructure during initialization of a new Mission in a
     *             LevelTwo configuration of the SCJ run-time environment.
     */
    // @SCJAllowed
    // @SCJRestricted( { INITIALIZATION })
    public MissionSequencer(PriorityParameters priority, StorageParameters storage) {
        MemoryArea area = RealtimeThread.getCurrentMemoryArea();
        thread = new RealtimeThread(priority, storage, area, this);
        thread.start();
    }

    class InMissionMemoryRunner implements Runnable {

        public void run() {
            Mission mission = getNextMission();
            if (mission == null) {
                terminationRequestReceived = true;
            } else {
                MissionManager manager = new MissionManager(mission);
                mission.setManager(manager);
                memory.shrink(mission.missionMemorySize());
                memory.setManager(manager);
                manager.startMission();
                terminationRequestReceived = mission.sequenceTerminationPending();
                // The manager instance will be gone soon along with the mission
                // memory. So nulling the pointer.
                mission.setManager(null);
            }
        }
    }

    /**
     * This method is declared final because the implementation is provided by
     * the vendor of the SCJ implementation and shall not be overridden. This
     * method performs all of the activities that correspond to sequencing of
     * Missions by this MissionSequencer.
     */
    // @SCJAllowed
    public final synchronized void handleAsyncEvent() {
        memory = new MissionMemory(0);
        do {
            memory.reinitialize();
            memory.enter(runner);
        } while (!terminationRequestReceived);
        memory.destroy();
        cancel();
    }

    /**
     * Try to finish the work of this mission sequencer soon by invoking the
     * currently running Mission's requestTermination method. Upon completion of
     * the currently running Mission, this MissionSequencer shall return from
     * its eventHandler method without invoking getNextMission and without
     * starting any additional missions.
     * <p>
     * Note that requestSequenceTermination does not force the sequence to
     * terminate because the currently running Mission must voluntarily
     * relinquish its resources.
     * <p>
     * TBD: shouldn't we also have a sequenceTerminationPending() method? We
     * need something like this in order to implement
     * Mission.sequenceTerminationPending().
     */
    // @SCJAllowed(LEVEL_2)
    public final void requestSequenceTermination() {
        terminationRequestReceived = true;
        Mission.getCurrentMission().requestTermination();
    }

    /**
     * This method is only intended to be invoked at Level 2. At Levels 0 and 1,
     * the infrastructure automatically starts the mission sequencer.
     * <p>
     * At level 2, the initial mission sequencer is started by infrastructure
     * code, but application code is required to explicitly start inner-nested
     * MissionSequencers. They may do so from any Schedulable context, including
     * the enclosing MissionSequencer's event handling thread.
     * <p>
     * Kelvin recommends against exposing this API. It seems assymetrical. All
     * other managed event handlers are implicitly started when the mission
     * initialization completes. That is the right time to do it... Leaving this
     * in the programmer's hands significantly complicates the execution model.
     * What if the user starts the sequencer inside the mission initialization
     * code. is that valid? what if the mission sequencer has never been started
     * when it comes time to shut down the surrounding mission? this would add
     * complexity to the mission termination code.
     */
    // @SCJAllowed(LEVEL_2)
    public final synchronized void start() {
        release_protected();
    }

    public final void join() throws InterruptedException {
        thread.join();
    }

    /**
     * This method is called by infrastructure to select the initial Mission to
     * execute, and subsequently, each time one Mission terminates, to determine
     * the next Mission to execute.
     * <p>
     * Prior to each invocation of getNextMission() by infrastructure,
     * infrastructure instantiates and enters a very large MissionMemory
     * allocation area. The typical behavior is for getNextMission() to return a
     * Mission object that resides in this MissionMemory area.
     * 
     * @return the next Mission to run, or null if no further Missions are to
     *         run under the control of this MissionSequencer.
     */
    // @SCJAllowed
    protected abstract Mission getNextMission();
}
