package javax.safetycritical;

import javax.realtime.PeriodicParameters;
import javax.realtime.PeriodicTimer;
import javax.realtime.PriorityParameters;

//@SCJAllowed
public abstract class PeriodicEventHandler extends ManagedEventHandler {

    private PeriodicTimer timer;

    // @SCJAllowed
    public PeriodicEventHandler(PriorityParameters priority, PeriodicParameters period,
            StorageParameters storage, long initMemSize) {
        this(priority, period, storage, initMemSize, null);
    }

    // @SCJAllowed(LEVEL_1)
    public PeriodicEventHandler(PriorityParameters priority, PeriodicParameters period,
            StorageParameters storage, long initMemSize, String name) {
        super(priority, period, storage, initMemSize, name);
        timer = new PeriodicTimer(period.getActualStart(), period.getActualPeriod(), this);
    }

    void start() {
        timer.start();
    }

    void stop() {
        timer.destroy();
        timer = null;
        cancel();
    }
}
