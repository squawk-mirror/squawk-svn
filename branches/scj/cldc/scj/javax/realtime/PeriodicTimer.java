package javax.realtime;

public class PeriodicTimer extends Timer {

    public PeriodicTimer(HighResolutionTime start, RelativeTime interval, Clock clock,
            AsyncEventHandler handler) {
        super(start, clock, handler);
        setInterval(interval);
    }

    public PeriodicTimer(HighResolutionTime start, RelativeTime interval, AsyncEventHandler handler) {
        this(start, interval, interval.clock, handler);
    }

    public RelativeTime getInterval() {
        synchronized (lock) {
            return period;
        }
    }

    // If interval is zero or null, the period is ignored and the firing
    // behavior of the PeriodicTimer is that of a OneShotTimer.
    public void setInterval(RelativeTime interval) {
        synchronized (lock) {
            if (interval != null) {
                int res = interval.compareTo(RelativeTime.ZERO);
                if (res < 0)
                    throw new IllegalArgumentException("Period cannot be negative. Timer: " + this);
                if (res > 0) {
                    period.set(interval);
                    periodic = true;
                    return;
                }
            }
            // if periodic timer becomes one shot timer, clear
            // firstShotDone
            periodic = firstShotDone = false;
        }
    }
}
