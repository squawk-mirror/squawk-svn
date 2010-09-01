package javax.safetycritical;

import javax.realtime.AsyncEventHandler;
import javax.realtime.PriorityParameters;
import javax.realtime.RealtimeThread;
import javax.realtime.ReleaseParameters;

//@SCJAllowed
public abstract class ManagedEventHandler extends AsyncEventHandler implements ManagedSchedulable {

    private String name;

    private RealtimeThread thread;

    private ManagedSchedulable next;

    ManagedEventHandler(PriorityParameters priority, ReleaseParameters release,
            StorageParameters storage, long initMemSize, String name) {
        thread = new RealtimeThread(priority, storage, initMemSize, this);
        thread.setManagedSchedulable(this);
        ManagedMemory initPrivate = (ManagedMemory) thread.getMemoryArea();
        initPrivate.setOwner(this);
        initPrivate.setManager(ManagedMemory.getCurrentManageMemory().getManager());
        this.name = name;
        thread.start();
    }

    // @SCJAllowed
    public String getName() {
        return name;
    }

    // @SCJAllowed
    public void register() {
        ManagedMemory.getCurrentManageMemory().getManager().regSchedulable(this);
    }

    public void join() throws InterruptedException {
        thread.join();
    }

    // @SCJAllowed
    public void cleanUp() {
    }

    public ManagedSchedulable getNext() {
        return next;
    }

    public void setNext(ManagedSchedulable next) {
        this.next = next;
    }

    // @SCJAllowed
    public abstract void handleAsyncEvent();
}
