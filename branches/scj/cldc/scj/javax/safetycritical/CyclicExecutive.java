package javax.safetycritical;

import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.safetycritical.util.Utils;

/**
 * TBD: An earlier version of CyclicExecutive extended Mission. In the current
 * design, CyclicExecutive produces a MissionSequencer which has the ability to
 * run a sequence of Missions. There's been some back and forth on this. Many of
 * our earlier design choices were based on the assumption that a Level0 Safelet
 * consists of only one Mission, but we subsequently reversed that choice
 * without fixing the relevant libraries. I understand a fundamental desire that
 * "simple things be simple". But there's some question in my mind as to what is
 * simple. The current draft document pursues option 2.
 * 
 * Option 1: CyclicExecutive extends Level0Mission and implements Safelet, with
 * the following consequences:
 * 
 * a. The application developer extends CyclicExecutive b. We need a variant of
 * CyclicExecutive that doesn't extend Level0Mission, because some Level0
 * applications are going to be sequences of Missions rather than a single
 * mission. c. CyclicExecutive can define a default getSequencer method which
 * returns a SingleMissionSequencer with a "normal" priority and a
 * "reasonably conservative" StorageParameters object, with the single mission
 * represented by "this" CyclicExecutive. d. The user overrides the initialize()
 * and getSchedule() methods, and optionally, the cleanup method. e. I don't
 * like the name CyclicExecutive for this. I'd rather call it CyclicApplication
 * as it is both a Safelet and a Mission.
 * 
 * Option 2: Cyclet is a concrete class that implements Safelet, but does not
 * extend Level0Mission, with the following consequences:
 * 
 * a. Configuraton of the SCJ run-time specifies both the name of the Cyclet
 * subclass (or Cyclet itself) and an optional name of the primordial mission.
 * Infrastructure invokes in sequence the setUp(), getSequencer(),
 * "sequencer.run()", and tearDown(). b. The default implementation of
 * getSequencer returns a SingleMissionSequencer with a normal priority and a
 * reasonably conservative StorageParameters object, representing the single
 * mission that is obtained by invoking the static method of Cyclet that is
 * declared as:
 * 
 * public static Level0Mission getPrimordialMission();
 * 
 * The vendor is required to implement this method in a vendor-specific way. It
 * could, for example, obtain this mission from a command-line argument, or from
 * a configuration choice specified at build time. c. The application developer
 * extends Level0Mission and overrides the initialize() and getSchedule()
 * methods.
 */

// @SCJAllowed
public abstract class CyclicExecutive implements Safelet {
    /**
     * Constructor for a Cyclic Executive. Level 0 Applications need to extend
     * CyclicExecutive and define a getSchedule() method. Level 1 and Level 2
     * applications should not extend CyclicExecutive, but rather should
     * implement Safelet more directly.
     * 
     * @param storage
     */
    // @SCJAllowed
    public CyclicExecutive(StorageParameters storage) {
        Utils.unimplemented();
    }

    /**
     * TBD: Does the JSR302 expert group approve of the following revision?
     * 
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
     * @return the schedule to be used by for the Level0Mission identified by
     *         argument m. The cyclic schedule is typically generated by
     *         vendor-specific tools. The returned object is expected to reside
     *         within the MissionMemory of Level0Mission m.
     */
    // @SCJAllowed
    public static CyclicSchedule getSchedule(Level0Mission m) {
        Utils.unimplemented();
        return null;
    }

    /**
     * Under normal circumstances, this is invoked from SCJ infrastructure code
     * with ImmortalMemory as the current allocation area.
     * 
     * @return the sequencer to be used for the Level 0 application. By default
     *         this is a SingleMissionSequencer, although this method can be
     *         overridden by the application if an alternative sequencer is
     *         desired.
     */
    // @SCJAllowed
    public MissionSequencer getSequencer() {
        Utils.unimplemented();
        return null;
    }

    private Mission getMission() {
        Utils.unimplemented();
        return null;
    }
}
