package javax.realtime;

//@SCJAllowed(LEVEL_1)
public class AsyncEvent {

    /**
     * fire this event, i.e., releases the execution of all handlers that were
     * added to this event.
     * 
     * @memory Does not allocate memory. Does not allow this to escape local
     *         variables.
     */
    // @SCJAllowed(LEVEL_1)
    public void fire() {
    }
}
