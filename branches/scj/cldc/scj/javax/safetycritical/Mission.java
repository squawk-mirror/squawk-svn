package javax.safetycritical;

//@SCJAllowed
public abstract class Mission {

    // @SCJAllowed(LEVEL_1)
    public Mission() {
    }

    // @SCJAllowed(LEVEL_1)
    protected void cleanUp() {
    }

    // @SCJAllowed
    protected abstract void initialize();

    // @SCJAllowed
    public void requestTermination() {
    }

    // @SCJAllowed
    public final void requestSequenceTermination() {
    }

    // @SCJAllowed
    public final boolean terminationPending() {
        return false;
    }

    // @SCJAllowed
    public final boolean sequenceTerminationPending() {
        return false;
    }

    // @SCJAllowed
    abstract public long missionMemorySize();

    // @SCJAllowed
    public static Mission getCurrentMission() {
        return ManagedMemory.getCurrentManageMemory().getManager().getMission();
    }
}
