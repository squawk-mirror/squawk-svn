package javax.realtime;

//@SCJAllowed
public class AbsoluteTime extends HighResolutionTime {

    static AbsoluteTime ZERO = new AbsoluteTime(0, 0);

    // @SCJAllowed
    public AbsoluteTime() {
        this(0, 0, null);
    }

    // @SCJAllowed
    public AbsoluteTime(long millis, int nanos) {
        this(millis, nanos, null);
    }

    // @SCJAllowed
    public AbsoluteTime(Clock clock) {
        this(0, 0, clock);
    }

    // @SCJAllowed
    public AbsoluteTime(long millis, int nanos, Clock clock) {
        super(millis, nanos, clock);
    }

    // @SCJAllowed
    public AbsoluteTime(AbsoluteTime time) {
        this();
        set(time);
    }

    // @SCJAllowed
    public AbsoluteTime add(long millis, int nanos) {
        return add(millis, nanos, null);
    } // @SCJAllowed

    public AbsoluteTime add(RelativeTime time) {
        return add(time, null);
    }

    // @SCJAllowed
    public AbsoluteTime add(RelativeTime time, AbsoluteTime dest) {
        if (time == null)
            throw new IllegalArgumentException("Time parameter cannot be null!");
        return add(time.millis, time.nanos, dest);
    }

    // @SCJAllowed
    public AbsoluteTime add(long millis, int nanos, AbsoluteTime dest) {
        if (dest == null)
            dest = new AbsoluteTime(clock);
        return (AbsoluteTime) super.add(millis, nanos, dest);
    }

    // @SCJAllowed
    public RelativeTime subtract(AbsoluteTime time) {
        return subtract(time, null);
    }

    // @SCJAllowed
    public RelativeTime subtract(AbsoluteTime time, RelativeTime dest) {
        if (time == null)
            throw new IllegalArgumentException("Time parameter cannot be null!");
        if (dest == null)
            dest = new RelativeTime(clock);
        return (RelativeTime) super.add(-time.millis, -time.nanos, dest);
    }

    // @SCJAllowed
    public AbsoluteTime subtract(RelativeTime time) {
        return subtract(time, null);
    }

    // @SCJAllowed
    public AbsoluteTime subtract(RelativeTime time, AbsoluteTime dest) {
        if (time == null)
            throw new IllegalArgumentException("Time parameter cannot be null!");
        if (dest == null)
            dest = new AbsoluteTime(clock);
        return (AbsoluteTime) super.add(-time.millis, -time.nanos, dest);
    }
}
