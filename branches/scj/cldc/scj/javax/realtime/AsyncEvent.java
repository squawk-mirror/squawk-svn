package javax.realtime;

//@SCJAllowed(LEVEL_1)
public class AsyncEvent {

    /*
     * TODO: Multiple handlers per event should be supported. Currently only
     * support one for simplicity.
     */
    private AsyncEventHandler handler;

    protected void setHandler(AsyncEventHandler handler) {
        this.handler = handler;
    }

    // @SCJAllowed(LEVEL_1)
    public void fire() {
        if (handler != null)
            handler.release();
    }
}
