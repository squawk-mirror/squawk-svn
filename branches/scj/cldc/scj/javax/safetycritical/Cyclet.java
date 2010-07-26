package javax.safetycritical;

import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;

/**
 * TBD: Does the JSR302 expert group approve of the following revision?
 * 
 * A safety-critical application consists of one or more missions, executed
 * concurrently or in sequence. Every Level-0 safety-critical application is
 * represented by Cyclet or a subclass of Cyclet which identifies the outer-most
 * MissionSequencer. This outer-most MissionSequencer takes responsibility for
 * running the sequence of Missions that comprise this safety-critical
 * application.
 * <p>
 * The mechanism used to identify the Safelet to a particular SCJ environment is
 * implementation defined.
 * <p>
 * Given class c of type Cyclet or a subclass of Cyclet that represents a
 * particular SCJ application, the SCJ infrastructure invokes in sequence
 * c.setUp() followed by c.getSequencer(). For the MissionSequencer q returned
 * from s.getSequencer(), the SCJ infrastructure arranges for an independent
 * thread to begin executing the code for that sequencer and then waits for that
 * thread to terminate its execution. Upon termination of the MissionSequencer's
 * thread, the SCJ infrastructure invokes s.tearDown().
 */
// @SCJAllowed
public class Cyclet implements Safelet {

    /**
     * Construct a Cyclet.
     */
    // @SCJAllowed
    public Cyclet() {
    }

    /**
     * The default implementation of getSequencer() returns a
     * SingleMissionSequencer() which runs the Level0Mission represented by
     * getPrimordialMission() exactly once. The default sequencer runs at
     * "normal" priority and uses a conservatively large value for
     * StorageParameters.
     * 
     * @return the MissionSequencer that oversees execution of Missions for this
     *         application.
     */
    // @SCJAllowed
    // @SCJRestricted({INITIALIZATION})
    public MissionSequencer getSequencer() {
        int priority = PriorityScheduler.instance().getNormPriority();
        StorageParameters sp = new StorageParameters(10000L, 10000L, 10000L);
        return new SingleMissionSequencer(new PriorityParameters(priority), sp,
                getPrimordialMission());
    }

    /**
     * Code to execute before the sequencer starts. The default implementation
     * does nothing.
     */
    // @SCJAllowed
    public void setUp() {
        // do nothing
    }

    /**
     * Code to execute after the sequencer ends. The default implementation does
     * nothing.
     */
    // @SCJAllowed
    public void tearDown() {
        // do nothing
    }

    /**
     * At configuration time, the developer has the option of specifying a
     * primordial mission, which is identified by a fully qualified class name.
     * Configuration parameters might be specified as options to the SCJ
     * compiler or linker, or might be specified on the command line. If the
     * primordial mission is specified, this method returns a reference to an
     * instance of the primordial mission, allocated in ImmortalMemory. If no
     * primordial mission was specified, this method returns null.
     * <p>
     * In the case that this method returns a non-null result, the returned
     * object is allocated at the time of the first invocation of this method.
     * This method will be called following an invocation of setUp().
     */
    public static Level0Mission getPrimordialMission() {
        // vendor-specific implementation not shown.
        return null;
    }

    /**
     * A previous revision declared this to be an abstract instance method
     * taking an array of PeriodicEventHandlers as its argument. That earlier
     * design did not generalize to the situation under which a Level0
     * application consists of a sequence of Missions, each of which needs a
     * distinct cyclic scheduler. This newer design generalizes to sequences of
     * Level-0 missions, and also makes more effective use of the revised design
     * under which Missions reside in the same scope as their
     * ManagedSchedulables, so a Mission can easily find all of its
     * ManagedSchedulables.
     * 
     * @return the schedule to be used for scheduling the Level0Mission
     *         identified by argument m. The cyclic schedule is typically
     *         generated by vendor-specific tools. The returned object is
     *         expected to reside within the MissionMemory of Level0Mission m.
     */
    // @SCJAllowed
    public static CyclicSchedule getSchedule(Level0Mission m) {
        return m.getSchedule();
    }
}