package javax.safetycritical;

import javax.realtime.AutonomousHappening;

/**
   *
   */
// @SCJAllowed(LEVEL_1)
public class ManagedAutonomousHappening extends AutonomousHappening {

    /**
     * Creates a Happening in the current memory area with a system assigned
     * name and id.
     */
    // @SCJAllowed(LEVEL_1)
    public ManagedAutonomousHappening() {
        super();
    }

    /**
     * Creates a Happening in the current memory area with the specified id and
     * a system-assigned name.
     */
    // @SCJAllowed(LEVEL_1)
    public ManagedAutonomousHappening(int id) {
        super(id);
    }

    /**
     * Creates a Happening in the current memory area with the name and id
     * given.
     */
    // @SCJAllowed(LEVEL_1)
    public ManagedAutonomousHappening(int id, String name) {
        super(id, name);
    }

    /**
     * Creates a Happening in the current memory area with the name name and a
     * system-assigned id.
     */
    // @SCJAllowed(LEVEL_1)
    public ManagedAutonomousHappening(String name) {
        super(name);

    }

    // // @SCJAllowed(LEVEL_1)
    // // @SCJRestricted(phase = INITIALIZATION)
    // public final void attach(AsyncEvent ae) {
    // }
    //
    // // @SCJAllowed(INFRASTRUCTURE)
    // public final void detach(AsyncEvent ae) {
    // }

}
