package javax.safetycritical;

import javax.realtime.AsyncEventHandler;
import javax.realtime.PriorityParameters;
import javax.realtime.ReleaseParameters;

//@SCJAllowed
public abstract class ManagedEventHandler extends AsyncEventHandler implements
        ManagedSchedulable {
    /**
     * Constructor to create an event handler.
     * <p>
     * Does not perform memory allocation. Does not allow this to escape local
     * scope. Builds links from this to priority, parameters, and name so those
     * three arguments must reside in scopes that enclose this.
     * <p>
     * 
     * @param priority
     *            specifies the priority parameters for this periodic event
     *            handler. Must not be null.
     * 
     * @param release
     *            specifies the periodic release parameters, in particular the
     *            start time and period. Note that a relative start time is not
     *            relative to NOW but relative to the point in time when
     *            initialization is finished and the timers are started. This
     *            argument must not be null.
     *            <p>
     * @param scp
     *            The scp parameter describes the organization of memory
     *            dedicated to execution of the underlying thread. (added by MS)
     *            <p>
     * @param memSize
     *            the size in bytes of the private scoped memory area to be used
     *            for the execution of this event handler. 0 for an empty memory
     *            area. Must not be negative.
     *            <p>
     * @throws IllegalArgumentException
     *             if priority parameters are null or if memSize is negative.
     */

    ManagedEventHandler(PriorityParameters priority, ReleaseParameters release,
            StorageParameters scp, long memSize, String name) {
    }

    /**
     * Application developers override this method with code to be executed when
     * this event handler's execution is disabled (upon termination of the
     * enclosing mission).
     * 
     */
    // @SCJAllowed
    protected void cleanUp() {
    }

    /**
     * Application developers override this method with code to be executed
     * whenever the event(s) to which this event handler is bound is fired.
     */
    // @SCJAllowed
    public abstract void handleAsyncEvent();

    /**
     * @return the name of this event handler.
     */
    // @SCJAllowed
    public String getName() {
        return null;
    }

    /**
     * @see javax.safetycritical.ManagedSchedulable#register()
     */
    // @SCJAllowed
    public void register() {
    }
}
