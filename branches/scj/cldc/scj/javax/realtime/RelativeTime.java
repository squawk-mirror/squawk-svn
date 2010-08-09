package javax.realtime;

//@SCJAllowed
public class RelativeTime extends HighResolutionTime {

    static RelativeTime ZERO = new RelativeTime(0, 0);

    // @SCJAllowed
    public RelativeTime() {
        this(0, 0, null);
    }

    // @SCJAllowed
    public RelativeTime(long millis, int nanos) {
        this(millis, nanos, null);
    }

    // @SCJAllowed
    public RelativeTime(Clock clock) {
        this(0, 0, clock);
    }

    // @SCJAllowed
    public RelativeTime(RelativeTime time) {
        this();
        set(time);
    }

    // @SCJAllowed
    public RelativeTime(long ms, int ns, Clock clock) {
        super(ms, ns, clock);
    }

    // @SCJAllowed
    public RelativeTime add(long millis, int nanos) {
        return add(millis, nanos, null);
    }

    // @SCJAllowed
    public RelativeTime add(RelativeTime time) {
        return add(time, null);
    }

    // @SCJAllowed
    public RelativeTime add(RelativeTime time, RelativeTime dest) {
        if (time == null)
            throw new IllegalArgumentException("Time parameter cannot be null!");
        return add(time.millis, time.nanos, dest);
    }

    // @SCJAllowed
    public RelativeTime subtract(RelativeTime time) {
        return subtract(time, null);
    }

    // @SCJAllowed
    public RelativeTime subtract(RelativeTime time, RelativeTime dest) {
        if (time == null)
            throw new IllegalArgumentException("Time parameter cannot be null!");
        return add(-time.millis, -time.nanos, dest);
    }

    // @SCJAllowed
    public RelativeTime add(long millis, int nanos, RelativeTime dest) {
        if (dest == null)
            dest = new RelativeTime(clock);
        return (RelativeTime) super.add(millis, nanos, dest);
    }

    /************** unused RTSJ methods ******************************/

    /**
     * Note: it is not "safe" to automatically convert from one clock basis to
     * another.
     */
    public AbsoluteTime absolute(Clock clock) {
        return absolute(clock);
    }

    /**
     * Note: it is not "safe" to automatically convert from one clock basis to
     * another.
     */
    public RelativeTime relative(Clock clock) {
        return null;
    }

//    // clock conversion
//    public RelativeTime(RelativeTime time, Clock clock) {
//    }

    public AbsoluteTime absolute(Clock clock, AbsoluteTime destination) {
        return null;
    }

    public RelativeTime relative(Clock clock, RelativeTime destination) {
        return null;
    }
}