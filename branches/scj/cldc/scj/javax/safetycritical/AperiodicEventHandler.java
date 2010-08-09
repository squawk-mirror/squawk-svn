package javax.safetycritical;

import javax.realtime.AperiodicParameters;
import javax.realtime.PriorityParameters;

//@SCJAllowed(LEVEL_1)
public abstract class AperiodicEventHandler extends ManagedEventHandler {

    // @SCJAllowed(LEVEL_1)
    public AperiodicEventHandler(PriorityParameters priority, AperiodicParameters aperiod,
            StorageParameters storage, long initMemSize) {
        this(priority, aperiod, storage, initMemSize, null);
    }

    // @SCJAllowed(LEVEL_1)
    public AperiodicEventHandler(PriorityParameters priority, AperiodicParameters aperiod,
            StorageParameters storage, long initMemSize, String name) {
        super(priority, aperiod, storage, initMemSize, name);
    }
}
