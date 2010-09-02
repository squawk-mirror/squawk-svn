package javax.safetycritical;

import javax.realtime.HighResolutionTime;
import javax.realtime.PriorityParameters;
import javax.realtime.RealtimeThread;

//@SCJAllowed(LEVEL_2)
public class ManagedThread extends RealtimeThread implements ManagedSchedulable {

    ManagedThread next;

    // @SCJAllowed(LEVEL_2)
    public ManagedThread(PriorityParameters priority, StorageParameters storage, long initMemSize,
            Runnable logic) {
        super(priority, storage, initMemSize, logic);
        setManagedSchedulable(this);
        ((ManagedMemory) getMemoryArea()).setOwner(this);
    }

    // @SCJAllowed(LEVEL_2)
    public void start() {
        super.start();
    }

    // @SCJAllowed(LEVEL_2)
    public void delay(HighResolutionTime time) throws InterruptedException {
        RealtimeThread.sleep(time);
    }

    // @SCJAllowed(LEVEL_2)
    public boolean terminationPending() {
        return true;
    }

    // @SCJAllowed
    public void register() {
        ManagedMemory.getCurrentManageMemory().getManager().regManagedThread(this);
    }

    // @SCJAllowed
    public void cleanUp() {
    }
}
