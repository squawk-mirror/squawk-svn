package javax.realtime;

import com.sun.squawk.BackingStore;

public abstract class ScopedMemory extends MemoryArea implements ScopedAllocationContext {

    protected Object portal;

    protected ScopedMemory parent;

    /**
     * allocate BS from specified BS
     * 
     * @param size
     * @param container
     */
    public ScopedMemory(long size, BackingStore from) {
        super(size, from);
    }

    public Object getPortal() throws MemoryAccessError, IllegalAssignmentError {
        return portal;
    }

    public void setPortal(Object object) throws IllegalThreadStateException,
            IllegalAssignmentError, InaccessibleAreaException {
        portal = object;
    }
}
