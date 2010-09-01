package javax.realtime;

/**
 * Note: IT IS NOT CLEAR WHICH PACKAGE THIS LIVES IN IF THIS DOES NOT APPEAR I
 * AN RTSJ EXTENSION PACKAGE THEN THIS AND ManagedInterruptHappenings SHOULD BE
 * MERGED.
 */
// @SCJAllowed(LEVEL_1)
public abstract class InterruptHappening extends Happening {

    public InterruptHappening() {
        super();
    }

    protected InterruptHappening(int id) {
        super(id);
    }

    public InterruptHappening(int id, String name) {
        super(id, name);
    }

    public InterruptHappening(String name) {
        super(name);
    }

    /**
   *
   */
    // @SCJAllowed(LEVEL_1)
    protected void process() {
    }

    /**
   *
   */
    // @SCJAllowed(LEVEL_1)
    public final int getPriority() {
        return 1;
    }

    public void trigger() {
        process();
    }

    protected final boolean isAsync() {
        return true;
    }
}
