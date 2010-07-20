package javax.realtime;

//@SCJAllowed
public class PeriodicParameters extends ReleaseParameters {

    // @SCJAllowed
    public PeriodicParameters(HighResolutionTime start, RelativeTime period) {
    }

    // @SCJAllowed(LEVEL_1)
    public PeriodicParameters(HighResolutionTime start, RelativeTime period,
            RelativeTime deadline, AsyncEventHandler missHandler) {
    }

    // @SCJAllowed
    public HighResolutionTime getStart() {
        return null;
    }

    // @SCJAllowed
    public RelativeTime getPeriod() {
        return null;
    }
}
