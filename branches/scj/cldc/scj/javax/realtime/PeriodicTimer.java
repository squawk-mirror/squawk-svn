package javax.realtime;

public class PeriodicTimer extends Timer {

    public PeriodicTimer(HighResolutionTime start, RelativeTime interval,
            Clock cclock, AsyncEventHandler handler) {
        super(start, cclock, handler);
        // TODO Auto-generated constructor stub
    }

    public PeriodicTimer(HighResolutionTime start, RelativeTime interval,
            AsyncEventHandler handler) {
        this(start, interval, Clock.getRealtimeClock(), handler);
        // TODO Auto-generated constructor stub
    }
}
