package javax.safetycritical;

import javax.realtime.AsyncEventHandler;
import javax.realtime.PriorityParameters;
import javax.realtime.RealtimeThread;
import javax.realtime.ReleaseParameters;

//@SCJAllowed
public abstract class ManagedEventHandler extends AsyncEventHandler implements ManagedSchedulable {

    private String name;

    private RealtimeThread thread;

    ManagedEventHandler next;

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

    void join() throws InterruptedException {
        thread.join();
    }

    void start() {
    }

    void stop() {
        cancel();
    }

    // @SCJAllowed
    public String getName() {
        return name;
    }

    // @SCJAllowed
    public void register() {
        ManagedMemory.getCurrentManageMemory().getManager().regManagedEventHandler(this);
    }

    // @SCJAllowed
    public void cleanUp() {
    }

    // @SCJAllowed
    public abstract void handleAsyncEvent();
}
