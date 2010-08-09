package javax.realtime;

//@SCJAllowed
public class PeriodicParameters extends ReleaseParameters {

    HighResolutionTime start;
    RelativeTime period;

    HighResolutionTime start_backup;
    RelativeTime period_backup;

    // @SCJAllowed
    public PeriodicParameters(HighResolutionTime start, RelativeTime period) {
        this(start, period, null, null);
    }

    // @SCJAllowed(LEVEL_1)
    public PeriodicParameters(HighResolutionTime start, RelativeTime period, RelativeTime deadline,
            AsyncEventHandler missHandler) {
        this.start = start == null ? new RelativeTime() : start.clone();
        this.start_backup = start;

        if (period == null || period.compareTo(RelativeTime.ZERO) <= 0)
            throw new IllegalArgumentException(
                    "Period parameter cannot be null, negative, or zero !");
        this.period = new RelativeTime(period);
        this.period_backup = period;

        if (deadline == null)
            this.deadline = new RelativeTime(period);
        else if (deadline.compareTo(RelativeTime.ZERO) > 0)
            this.deadline = new RelativeTime(deadline);
        else
            throw new IllegalArgumentException("Deadline cannot be negative or zero !");
        this.deadline_backup = deadline;

        this.missHandler = missHandler;
    }

    // @SCJAllowed
    public HighResolutionTime getStart() {
        return start_backup;
    }

    public HighResolutionTime getActualStart() {
        return start;
    }

    // @SCJAllowed
    public RelativeTime getPeriod() {
        return period_backup;
    }

    public RelativeTime getActualPeriod() {
        return period;
    }
}
