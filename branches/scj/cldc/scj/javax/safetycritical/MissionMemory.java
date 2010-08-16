package javax.safetycritical;

import javax.realtime.RealtimeThread;

public final class MissionMemory extends ManagedMemory {

    MissionMemory(long size) {
        super(size);
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

    // @SCJAllowed
    public String toString() {
        return "MissionMemory";
    }

    protected void reset() {
        super.destroyAllAboveBS();
        super.reset();
    }
}
