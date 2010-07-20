package javax.realtime;

//@SCJAllowed
public interface ScopedAllocationContext extends AllocationContext {

    // @SCJAllowed
    public Object getPortal() throws MemoryAccessError, IllegalAssignmentError;

    // @SCJAllowed
    public void setPortal(Object object) throws IllegalThreadStateException,
            IllegalAssignmentError, InaccessibleAreaException;
}
