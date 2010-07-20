package javax.realtime;

public abstract class ScopedMemory extends MemoryArea implements
        ScopedAllocationContext {

    protected Object portal;

    protected ScopedMemory parent;

    private static ScopedMemory primordialScope;

    public ScopedMemory(long size) {
        super(size);
    }

    /**
     * allocate BS from specified BS
     * 
     * @param size
     * @param container
     */
    public ScopedMemory(long size, RealtimeThread thread) {
        super(size, thread);
    }

    public Object getPortal() throws MemoryAccessError, IllegalAssignmentError {
        return portal;
    }

    public void setPortal(Object object) throws IllegalThreadStateException,
            IllegalAssignmentError, InaccessibleAreaException {
        portal = object;
    }
}
