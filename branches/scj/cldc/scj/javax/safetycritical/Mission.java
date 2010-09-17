package javax.safetycritical;


//@SCJAllowed
public abstract class Mission {

    MissionManager manager;

    // @SCJAllowed(LEVEL_1)
    public Mission() {
    }

    // @SCJAllowed(LEVEL_1)
    protected void cleanUp() {
    }

    // @SCJAllowed
    protected abstract void initialize();

    // @SCJAllowed
    public abstract long missionMemorySize();

    // @SCJAllowed
    public void requestTermination() {
        manager.requestTermination();
    }

    // @SCJAllowed
    public final void requestSequenceTermination() {
        manager.requestSequenceTermination();
    }

    // @SCJAllowed
    public final boolean terminationPending() {
        return manager.terminationPending();
    }

    // @SCJAllowed
    public final boolean sequenceTerminationPending() {
        return manager.sequenceTerminationPending();
    }

    // @SCJAllowed
    public static Mission getCurrentMission() {
        return ManagedMemory.getCurrentManageMemory().getManager().getMission();
    }

    void setManager(MissionManager manager) {
        //BackingStore.disableScopeCheck();
        this.manager = manager;
        //BackingStore.enableScopeCheck();
    }
}
