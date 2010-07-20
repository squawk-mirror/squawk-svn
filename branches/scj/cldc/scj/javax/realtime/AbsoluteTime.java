package javax.realtime;

//@SCJAllowed
public class AbsoluteTime extends HighResolutionTime {

    // @SCJAllowed
    public AbsoluteTime(long millis, int nanos) {
    }

    // @SCJAllowed
    public AbsoluteTime(AbsoluteTime time) {
    }

    // @SCJAllowed
    public AbsoluteTime(long millis, int nanos, Clock clock) {
    }

    // @SCJAllowed
    public AbsoluteTime(Clock clock) {
        this(0, 0, clock);
    }

    // @SCJAllowed
    public AbsoluteTime add(long millis, int nanos) {
        return add(millis, nanos, null);
    }

    // @SCJAllowed
    public AbsoluteTime add(long millis, int nanos, AbsoluteTime dest) {
        return null;
    }

    // @SCJAllowed
    public AbsoluteTime add(RelativeTime time) {
        return add(time);
    }

    // @SCJAllowed
    public AbsoluteTime add(RelativeTime time, AbsoluteTime dest) {
        return null;
    }

    // @SCJAllowed
    public RelativeTime subtract(AbsoluteTime time) {
        return subtract(time);
    }

    // @SCJAllowed
    public RelativeTime subtract(AbsoluteTime time, RelativeTime dest) {
        return null;
    }

    // @SCJAllowed
    public AbsoluteTime subtract(RelativeTime time) {
        return subtract(time);
    }

    // @SCJAllowed
    public AbsoluteTime subtract(RelativeTime time, AbsoluteTime dest) {
        return null;
    }
}
