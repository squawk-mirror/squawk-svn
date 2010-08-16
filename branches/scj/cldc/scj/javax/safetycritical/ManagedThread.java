package javax.safetycritical;

import javax.realtime.HighResolutionTime;
import javax.realtime.PriorityParameters;
import javax.realtime.RealtimeThread;

//@SCJAllowed(LEVEL_2)
public class ManagedThread extends RealtimeThread implements ManagedSchedulable {

    private ManagedSchedulable next;

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

    public void stop() {
        // TODO: how to tell a thread to stop??? This is level 2 though; put our
        // worry in the future ...
    }

    public void cleanUp() {
    }

    public ManagedSchedulable getNext() {
        return next;
    }

    public void setNext(ManagedSchedulable next) {
        this.next = next;
    }
}
