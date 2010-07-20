package javax.safetycritical;

import javax.realtime.RealtimeThread;

public class MissionMemory extends ManagedMemory {

    MissionMemory() {
        super(0);
    }

    /**
     * allocate BS from specified BS
     * 
     * @param size
     * @param container
     */
    public MissionMemory(long size, RealtimeThread thread) {
        super(size, thread);
    }

    /**
     * A string representation of the MissionMemory
     * <p>
     * TBD: do we want to allocate the string in the current scope? Or we could
     * permanently allocate the string representation in the constructor, and
     * simply return a reference to a single shared String object.
     * 
     * @return a string representing the MissionMemory
     */
    // @SCJAllowed
    public String toString() {
        return "MissionMemory";
    }

    public void reset() {
        destroyAllAbove();
        super.reset();
    }
}
