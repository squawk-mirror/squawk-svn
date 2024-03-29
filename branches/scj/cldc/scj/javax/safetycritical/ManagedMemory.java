package javax.safetycritical;

import javax.realtime.MemoryArea;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import com.sun.squawk.BackingStore;
import com.sun.squawk.util.Assert;

//@SCJAllowed
public abstract class ManagedMemory extends ScopedMemory {

    private PrivateMemory nested;

    private MissionManager manager;

    private ManagedSchedulable owner;

    // @SCJAllowed(INFRASTRUCTURE)
    ManagedMemory(long size) {
        this(size, RealtimeThread.currentRealtimeThread().getBackingStore());
    }

    /**
     * allocate BS from specified BS
     * 
     * @param size
     * @param container
     */
    public ManagedMemory(long size, BackingStore from) {
        super(size, from);
    }

    /**
     * 
     * @return the current managed memory.
     * 
     * @throws IllegaleStateException
     *             when called from immortal.
     */
    public static ManagedMemory getCurrentManageMemory() {
        BackingStore bs = BackingStore.getCurrentContext();
        if (bs == BackingStore.getImmortal())
            throw new IllegalStateException();
        MemoryArea area = (MemoryArea) bs.getMirror();
        Assert.always(area instanceof ManagedMemory);
        return (ManagedMemory) area;
    }

    /**
     * If private memory does not exist, create one; otherwise set its size;
     * then, enter the private memory; and finally, set the size of private
     * memory to zero.
     * 
     * FIXME: not sure what does the requirement mean, so the current
     * implementation is done with the assumptions that 1) current managed
     * memory is not mission memory (must be private memory); 2) the nested
     * private memory is not that having been entered but not exited yet (it can
     * be the case if enter -> move down along the private memory stack -> try
     * to enter again).
     * 
     * @param size
     * @param logic
     * 
     * @throws IllegalStateException
     *             when called from another memory area or from a thread that
     *             does not own the current managed memory.
     * 
     */
    // @SCJAllowed
    public void enterPrivateMemory(long size, Runnable logic) {
        RealtimeThread current = RealtimeThread.currentRealtimeThread();
        if (current.getManagedSchedulable() != getOwner())
            throw new IllegalStateException("Cannot enter private memory not owned. Current:["
                    + current.getManagedSchedulable() + "] Owner:[" + getOwner() + "]");
        if (nested == null) {
            //BackingStore.disableScopeCheck();
            nested = new PrivateMemory(size);
            //BackingStore.enableScopeCheck();
            nested.setManager(getManager());
            nested.setOwner(getOwner());
        } else {
            nested.reserveBS_protected(size);
        }
        nested.enter(logic);
        nested.destroyBS();
    }

    /**
     * 
     * @return the ManagedSchedulable that owns this managed memory.
     */
    // @SCJAllowed
    public ManagedSchedulable getOwner() {
        // Assert.always(owner != null, "Owner of " + this + " is null!");
        return owner;
    }

    /**
     * 
     * @param owner
     */
    // @SCJAllowed(INFRASTRUCTURE)
    void setOwner(ManagedSchedulable owner) {
        this.owner = owner;
    }

    /**
     * 
     * @param mngr
     */
    // @SCJAllowed(INFRASTRUCTURE)
    void setManager(MissionManager mngr) {
        //BackingStore.disableScopeCheck();
        manager = mngr;
        //BackingStore.enableScopeCheck();
    }

    /**
     * 
     * @return
     */
    // @SCJAllowed(INFRASTRUCTURE)
    public MissionManager getManager() {
        Assert.always(manager != null, "Manager of " + this + " is null!");
        return manager;
    }

    public void destroyBS() {
        destroyBS_protected();
        nested = null;
    }
}
