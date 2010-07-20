package javax.safetycritical;

import javax.realtime.HighResolutionTime;
import javax.realtime.PriorityParameters;
import javax.realtime.RealtimeThread;

//@SCJAllowed(LEVEL_2)
public class ManagedThread extends RealtimeThread implements ManagedSchedulable {

    /**
     * Does not allow this to escape local variables. Creates a link from the
     * constructed object to the scheduling, memory, and logic parameters .
     * Thus, all of these parameters must reside in a scope that enclose "this".
     * <p>
     * The priority represented by scheduling parameter is consulted only once,
     * at construction time. If scheduling.getPriority() returns different
     * values at different times, only the initial value is honored.
     */
    // @SCJAllowed(LEVEL_2)
    public ManagedThread(PriorityParameters priority, StorageParameters storage, long initSize,
            Runnable logic) {
        super(priority, storage, initSize, logic);
    }

    // @SCJAllowed(LEVEL_2)
    public void start() {
        super.start();
    }

    // @SCJAllowed(LEVEL_2)
    public void delay(HighResolutionTime time) {
    }

    // @SCJAllowed(LEVEL_2)
    public boolean terminationPending() {
        return true;
    }

    // @SCJAllowed
    public void register() {
        ManagedMemory.getCurrentManageMemory().getManager().addScheduble(this);
    }
}
