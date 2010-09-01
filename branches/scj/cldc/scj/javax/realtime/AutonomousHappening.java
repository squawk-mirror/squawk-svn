package javax.realtime;

/**
 * Autonomous happenings are those that when triggered automatically fire the
 * attached asynchronous events. In SCJ, all happenings are managed, hence there
 * are no visible constructors for the class.
 */
// @SCJAllowedLEVEL_1)
public class AutonomousHappening extends EventHappening {

    /**
     * Creates a Happening in the current memory area with a system assigned
     * name and id.
     */
    // @SCJAllowedLEVEL_1)
    protected AutonomousHappening() {
        super();
    }

    /**
     * Creates a Happening in the current memory area with the specified id and
     * a system-assigned name.
     */
    // @SCJAllowedLEVEL_1)
    protected AutonomousHappening(int id) {
        super(id);
    }

    /**
     * Creates a Happening in the current memory area with the name and id
     * given.
     */
    // @SCJAllowedLEVEL_1)
    protected AutonomousHappening(int id, String name) {
        super(id, name);
    }

    /**
     * Creates a Happening in the current memory area with the name name and a
     * system-assigned id.
     */
    // @SCJAllowedLEVEL_1)
    protected AutonomousHappening(String name) {
        super(name);
    }
}
