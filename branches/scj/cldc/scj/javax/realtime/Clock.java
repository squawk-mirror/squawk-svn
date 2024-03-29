package javax.realtime;

import javax.safetycritical.util.Utils;

import com.sun.squawk.VM;
import com.sun.squawk.util.Assert;

/**
 * A clock marks the passing of time. It has a concept of now that can be
 * queried through Clock.getTime(), and it can have events queued on it which
 * will be fired when their appointed time is reached.
 * 
 * The Clock instance returned by getRealtimeClock() may be used in any context
 * that requires a clock.
 * 
 * TBD: is the following still true for us? I (MS) assume that Kelvin would like
 * to drive scheduling with user defined clocks.
 * 
 * HighResolutionTime instances that use other clocks are not valid for any
 * purpose that involves sleeping or waiting, including in members of the
 * RealtimeThread.waitForNextPeriod() family. They may, however, be used in the
 * fire time and the period of OneShotTimer and PeriodicTimer.
 */
// @SCJAllowed
public abstract class Clock {

    private static class RealtimeClock extends Clock {

        private RelativeTime resolution;

        // @SCJAllowed
        public RelativeTime getEpochOffset() {
            Utils.unimplemented();
            return null;
        }

        // @SCJAllowed
        public RelativeTime getResolution() {
            return resolution;
        }

        // @SCJAllowed
        public RelativeTime getResolution(RelativeTime dest) {
            dest.set(resolution.getMilliseconds(), resolution.getNanoseconds());
            return dest;
        }

        // @SCJAllowed
        public AbsoluteTime getTime() {
            return getTime(new AbsoluteTime());
        }

        // @SCJAllowed
        public AbsoluteTime getTime(AbsoluteTime dest) {
            // FIXME: nanos field is ignored.
            dest.set(VM.getTimeMillis(), 0);
            return dest;
        }

        // @SCJAllowed
        protected boolean drivesEvents() {
            Utils.unimplemented();
            return false;
        }

        // @SCJAllowed(LEVEL_1)
        protected void registerCallBack(AbsoluteTime time, ClockCallBack clockEvent) {
            Utils.unimplemented();
        }

        // @SCJAllowed(LEVEL_1)
        protected boolean resetTargetTime(AbsoluteTime time) {
            Utils.unimplemented();
            return false;
        }

        // @SCJAllowed(LEVEL_1)
        protected void setResolution(RelativeTime resolution) {
            resolution.set(resolution);
        }
    }

    private static RealtimeClock rtc = new RealtimeClock();

    /**
     * Constructor for the abstract class.
     * 
     * Allocates resolution here.
     */

    // @SCJAllowed
    public Clock() {
    }

    /**
     * There is always at least one clock object available: the system real-time
     * clock. This is the default Clock.
     * 
     * @return The singleton instance of the default Clock.
     */

    // @SCJAllowed
    public static Clock getRealtimeClock() {
        Assert.that(rtc != null);
        return rtc;
    }

    /**
     * Gets the current time in a newly allocated object. Note: This method will
     * return an absolute time value that represents the clock's notion of an
     * absolute time. For clocks that do not measure calendar time this absolute
     * time may not represent a wall clock time.
     * 
     * @return A newly allocated instance of AbsoluteTime in the current
     *         allocation context, representing the current time. The returned
     *         object is associated with this clock.
     */

    // @SCJAllowed
    public abstract AbsoluteTime getTime();

    /**
     * Gets the current time in an existing object. The time represented by the
     * given AbsoluteTime is changed at some time between the invocation of the
     * method and the return of the method. Note: This method will return an
     * absolute time value that represents the clock's notion of an absolute
     * time. For clocks that do not measure calendar time this absolute time may
     * not represent a wall clock time.
     * 
     * @param dest
     *            The instance of AbsoluteTime object which will be updated in
     *            place. The clock association of the dest parameter is ignored.
     *            When dest is not null the returned object is associated with
     *            this clock. If dest is null, then nothing happens.
     * @return The instance of AbsoluteTime passed as parameter, representing
     *         the current time, associated with this clock, or null if dest was
     *         null.
     */

    // @SCJAllowed
    public abstract AbsoluteTime getTime(AbsoluteTime dest);

    /**
     * Gets the resolution of the clock, the nominal interval between ticks.
     * 
     * @return previously allocated resolution object.
     */

    // @SCJAllowed
    public abstract RelativeTime getResolution();

    /**
     * Gets the resolution of the clock, the nominal interval between ticks.
     * 
     * TBD: getTime with a destination null will ignore it and return null. This
     * method (getResolution) will allocated a new object when dest is null.
     * 
     * @param dest
     *            return the relative time value in dest. If dest is null,
     *            allocate a new RelativeTime instance to hold the returned
     *            value.
     * @return dest set to values representing the resolution of this. The
     *         returned object is associated with this clock.
     */

    // @SCJAllowed
    public abstract RelativeTime getResolution(RelativeTime dest);

    /**
     * Returns the relative time of the offset of the epoch of this clock from
     * the Epoch. For the real-time clock it will return a RelativeTime value
     * equal to 0. An UnsupportedOperationException is thrown if the clock does
     * not support the concept of date.
     * 
     * @return A newly allocated RelativeTime object in the current execution
     *         context with the offset past the Epoch for this clock. The
     *         returned object is associated with this clock.
     */

    // @SCJAllowed
    public abstract RelativeTime getEpochOffset();

    /**
     * Returns true if and only if this Clock is able to trigger the execution
     * of time-driven activities. Some user-defined clocks may be read-only,
     * meaning the clock can be used to obtain timestamps, but the clock cannot
     * be used to trigger the execution of events. If a clock that does not
     * return drivesEvents() equal true is used to configure a Timer or a
     * sleep() request, an IllegalArgumentException will be thrown by the
     * infrastructure.
     * 
     * The default real-time clock does drive events.
     * 
     * @return true if the clock can drive events.
     */

    // @SCJAllowed
    protected abstract boolean drivesEvents();

    /**
     * Code in the abstract base Clock class makes this call to the subclass.
     * The method is expected to implement a mechanism that will invoke atTime()
     * in ClockCallBack at time time, and if this clock is subject to
     * discontinuities, invoke ClockCallBack.discontinuity(javax.realtime.Clock,
     * javax.realtime.RelativeTime) each time a clock discontinuity is detected.
     * 
     * This method behaves effectively as if it and invocations of clock events
     * by this clock hold a common lock.
     * 
     * @param time
     *            The absolute time value on this clock at which
     *            ClockCallBack.atTime(Clock) should be invoked.
     * @param clockEvent
     *            The object that should be notified at time. If clockEvent is
     *            null, unregister the current clock event.
     */

    // @SCJAllowed(LEVEL_1)
    protected abstract void registerCallBack(AbsoluteTime time, ClockCallBack clockEvent);

    /**
     * Replace the target time being used by the ClockCallBack registered by
     * registerCallBack(AbsoluteTime, ClockCallBack).
     * 
     * @param time
     *            The new target time.
     * @return false if no ClockEvent is currently registered.
     */

    // @SCJAllowed(LEVEL_1)
    protected abstract boolean resetTargetTime(AbsoluteTime time);

    /**
     * Set the resolution of this. TBD: do we keep this in SCJ?
     * 
     * MS: I don't think we should support this.
     * 
     * @param resolution
     */
    // @SCJAllowed(LEVEL_1)
    protected abstract void setResolution(javax.realtime.RelativeTime resolution);
}
