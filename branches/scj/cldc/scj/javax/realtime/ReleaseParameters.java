package javax.realtime;

//@SCJAllowed
public abstract class ReleaseParameters {
    // @SCJAllowed
    protected ReleaseParameters() {
    }

    // @SCJAllowed(LEVEL_1)
    protected ReleaseParameters(RelativeTime deadline,
            AsyncEventHandler missHandler) {
    }

    // @SCJAllowed(LEVEL_1)
    public RelativeTime getDeadline() {
        return null;
    }

    // @SCJAllowed(LEVEL_1)
    public AsyncEventHandler getDeadlineMissHandler() {
        return null;
    }

}
