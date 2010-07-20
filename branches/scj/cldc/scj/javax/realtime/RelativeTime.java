package javax.realtime;

//@SCJAllowed
public class RelativeTime extends HighResolutionTime {

    // @SCJAllowed
    public RelativeTime() {
    }

    // @SCJAllowed
    public RelativeTime(long ms, int ns) {
    }

    // @SCJAllowed
    public RelativeTime(Clock clock) {
    }

    // @SCJAllowed
    public RelativeTime(long ms, int ns, Clock clock) {
    }

    // @SCJAllowed
    public RelativeTime(RelativeTime time) {
    }

    // @SCJAllowed
    public RelativeTime add(long millis, int nanos) {
        return add(millis, nanos);
    }

    // @SCJAllowed
    public RelativeTime add(RelativeTime time) {
        return add(time);
    }

    // @SCJAllowed
    public RelativeTime add(long millis, int nanos, RelativeTime dest) {
        return null;
    }

    // @SCJAllowed
    public RelativeTime add(RelativeTime time, RelativeTime dest) {
        return null;
    }

    // @SCJAllowed
    public RelativeTime subtract(RelativeTime time) {
        return subtract(time);
    }

    // @SCJAllowed
    public RelativeTime subtract(RelativeTime time, RelativeTime dest) {
        return null;
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

    // clock conversion
    public RelativeTime(RelativeTime time, Clock clock) {
    }

    public AbsoluteTime absolute(Clock clock, AbsoluteTime destination) {
        return null;
    }

    public RelativeTime relative(Clock clock, RelativeTime destination) {
        return null;
    }

}