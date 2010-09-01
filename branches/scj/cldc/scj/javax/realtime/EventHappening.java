package javax.realtime;

/**
 * Event happenings are happenings that can be associated with asynchronous
 * events (via the attach method).
 */
// @SCJAllowed(LEVEL_1)
public abstract class EventHappening extends Happening {

    private AsyncEvent event;

    protected EventHappening() {
        super();
    }

    protected EventHappening(int id) {
        super(id);
    }

    protected EventHappening(int id, String name) {
        super(id, name);
    }

    protected EventHappening(String name) {
        super(name);
    }

    /**
     * Attach the AsyncEvent ae to this Happening. ADD LEVEL CONSTRAINTS????
     * 
     * @throws IllegalStateException
     *             if called from outside the mission initialization phase.
     */
    // @SCJAllowed(SUPPORT)
    public void attach(AsyncEvent ae) {
        event = ae;
    }

    /**
     * Detach the AsyncEvent ae from this Happening.
     * 
     * @throws IllegalStateException
     *             if called from outside the mission initialization phase.
     */
    // @SCJAllowed(SUPPORT)
    public void detach(AsyncEvent ae) {
        if (event == ae)
            event = null;
    }

    public void trigger() {
        if (event != null)
            event.fire();
    }

    protected final boolean isAsync() {
        return true;
    }
}
