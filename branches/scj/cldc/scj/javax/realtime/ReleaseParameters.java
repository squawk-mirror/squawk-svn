package javax.realtime;

//@SCJAllowed
public abstract class ReleaseParameters {

    protected RelativeTime deadline;
    protected RelativeTime deadline_backup;
    protected AsyncEventHandler missHandler;

    // @SCJAllowed
    protected ReleaseParameters() {
    }

    // @SCJAllowed(LEVEL_1)
    protected ReleaseParameters(RelativeTime deadline, AsyncEventHandler missHandler) {
        if (deadline == null || deadline.compareTo(RelativeTime.ZERO) <= 0)
            throw new IllegalArgumentException("Deadline cannot be null, negative, or zero !");
        this.deadline = new RelativeTime(deadline);
        this.deadline_backup = deadline;
        this.missHandler = missHandler;
    }

    // @SCJAllowed(LEVEL_1)
    public RelativeTime getDeadline() {
        return deadline;
    }

    // @SCJAllowed(LEVEL_1)
    public AsyncEventHandler getDeadlineMissHandler() {
        return missHandler;
    }
}
