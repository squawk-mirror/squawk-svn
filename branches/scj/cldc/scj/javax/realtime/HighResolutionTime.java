package javax.realtime;

import com.sun.squawk.VM;
import com.sun.squawk.util.Assert;

//@SCJAllowed
public abstract class HighResolutionTime implements Comparable {

    protected static int NANOS_PER_MILLI = 1000000;

    protected Clock clock;

    protected long millis;

    protected int nanos;

    protected HighResolutionTime(long ms, int ns, Clock clock) {
        if (clock == null)
            this.clock = Clock.getRealtimeClock();
        set(ms, ns);
        // VM.print("[SCJ] time ");
        // VM.print(ms);
        // VM.print(":");
        // VM.print(ns);
        // VM.print(" clock: ");
        // VM.print(this.clock.toString());
        // VM.println(" created");
    }

    // @SCJAllowed
    public Clock getClock() {
        return clock;
    }

    // @SCJAllowed
    public final long getMilliseconds() {
        return millis;
    }

    // @SCJAllowed
    public final int getNanoseconds() {
        return nanos;
    }

    // @SCJAllowed
    public void set(HighResolutionTime time) {
        if (time == null)
            throw new IllegalArgumentException("Target time cannot be null!");
        if (getClass() != time.getClass())
            throw new ClassCastException("Target time is of different type");
        clock = time.clock;
        set(time.millis, time.nanos);
    }

    // @SCJAllowed
    public void set(long millis) {
        set(millis, 0);
    }

    // @SCJAllowed
    public void set(long millis, int nanos) {
        this.millis = millis;
        this.nanos = nanos;
        normalize();
    }

    // @SCJAllowed
    public boolean equals(HighResolutionTime time) {
        return getClass() == time.getClass() && clock == time.clock && nanos == time.nanos
                && millis == time.millis;
    }

    // @SCJAllowed
    public boolean equals(Object object) {
        if (object instanceof HighResolutionTime)
            return equals((HighResolutionTime) object);
        return false;
    }

    // @SCJAllowed
    public int compareTo(HighResolutionTime time) {
        if (getClass() != time.getClass())
            throw new ClassCastException("Attempt to compare times of different classes");
        if (clock != time.clock)
            throw new IllegalArgumentException(
                    "Attempt to compare times regarding different types of clocks:" + clock
                            + " vs " + time.clock);
        if (millis < time.millis)
            return -1;
        if (millis > time.millis)
            return 1;
        if (nanos < time.nanos)
            return -1;
        if (nanos > time.nanos)
            return 1;
        return 0;
    }

    // @SCJAllowed
    public int compareTo(Object o) {
        return compareTo((HighResolutionTime) o);
    }

    /**
     * ALERT: Current implementation may incur memory allocation for an
     * AbsoluteTime object.
     * 
     * @param target
     * @param time
     * @throws java.lang.InterruptedException
     */
    // @SCJAllowed(LEVEL_2)
    public static void waitForObject(java.lang.Object target, HighResolutionTime time)
            throws java.lang.InterruptedException {
        long waitMillis = Long.MAX_VALUE;
        int waitNanos = 0;
        if (time instanceof RelativeTime) {
            int res = ((RelativeTime) time).compareTo(RelativeTime.ZERO);
            if (res < 0)
                throw new IllegalArgumentException("Timeout cannot be negative!");
            if (res > 0) {
                waitMillis = time.millis;
                waitNanos = time.nanos;
            }
        } else if (time != null) {
            time = ((AbsoluteTime) time).subtract(time.clock.getTime());
            if (time.compareTo(RelativeTime.ZERO) > 0) {
                waitMillis = time.millis;
                waitNanos = time.nanos;
            } else
                return;
        }
        target.wait(time.millis, time.nanos);
    }

    protected HighResolutionTime add(long millis, int nanos, HighResolutionTime dest) {
        Assert.always(dest != null);

        long newMillis = this.millis + millis;

        if ((this.millis > 0 && millis > 0 && newMillis < 0)
                || (this.millis < 0 && millis < 0 && newMillis > 0))
            throw new IllegalArgumentException("Overflow while adding");

        dest.millis = newMillis;
        // impossible to overflow for nanos field of normalized time
        dest.nanos = this.nanos + nanos;
        normalize();

        return dest;
    }

    private void normalize() {
        long msDelta = nanos < 0 ? nanos / NANOS_PER_MILLI - 1 : nanos / NANOS_PER_MILLI;
        long newMillis = millis + msDelta;

        if ((millis > 0 && msDelta > 0 && newMillis < 0)
                || (millis < 0 && msDelta < 0 && newMillis > 0))
            throw new IllegalArgumentException("Overflow while normalizing");

        millis = newMillis;
        nanos -= msDelta * NANOS_PER_MILLI;
    }

    /**
     * If overflow, return Long.MAX_VALUE
     * 
     * @return
     */
    long toNanos() {
        long mayOverflow = millis * NANOS_PER_MILLI + nanos;
        return mayOverflow < 0 ? Long.MAX_VALUE : mayOverflow;
    }

    HighResolutionTime clone() {
        HighResolutionTime ret = null;
        if (this instanceof AbsoluteTime)
            ret = new AbsoluteTime((AbsoluteTime) this);
        else
            ret = new RelativeTime((RelativeTime) this);
        return ret;
    }
}
