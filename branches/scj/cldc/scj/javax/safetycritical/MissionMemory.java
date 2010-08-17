package javax.safetycritical;


public final class MissionMemory extends ManagedMemory {

    MissionMemory(long size) {
        super(size);
    }

//    /**
//     * allocate BS from specified BS
//     * 
//     * @param size
//     * @param container
//     */
//    public MissionMemory(long size, BackingStore from) {
//        super(size, from);
//    }

    // @SCJAllowed
    public String toString() {
        return "MissionMemory";
    }

    protected void reset() {
        super.destroyAllAboveBS();
        super.reset();
    }
}
