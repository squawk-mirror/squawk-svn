package javax.safetycritical;

import javax.realtime.AsyncEventHandler;
import javax.realtime.PriorityParameters;
import javax.realtime.ReleaseParameters;

//@SCJAllowed
public abstract class ManagedEventHandler extends AsyncEventHandler implements ManagedSchedulable {

    private String name;

    ManagedEventHandler(PriorityParameters priority, ReleaseParameters release,
            StorageParameters storage, long initMemSize, String name) {
        super(priority, storage, initMemSize);
        ((ManagedMemory)getInitArea()).setOwner(this);
        this.name = name;
    }

    // @SCJAllowed
    public String getName() {
        return name;
    }

    // @SCJAllowed
    public void register() {
        ManagedMemory.getCurrentManageMemory().getManager().addScheduble(this);
    }

    public void join() throws InterruptedException {
        super.join();
    }

    public void stop() {
        super.stop();
    }

    // @SCJAllowed
    protected void cleanUp() {
    }

    // @SCJAllowed
    public abstract void handleAsyncEvent();
}
