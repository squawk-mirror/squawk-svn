package javax.realtime;

import javax.safetycritical.PrivateMemory;
import javax.safetycritical.StorageParameters;

import com.sun.squawk.BackingStore;
import com.sun.squawk.VM;

//@SCJAllowed(LEVEL_1)
public class RealtimeThread extends Thread {

    /**
     * The total backing store space for this thread
     */
    private BackingStore bs;

    /**
     * 
     */
    private MemoryArea initArea;

    /**
	 * 
	 */
    private StorageParameters sPara;

    /**
	 * 
	 */
    private PriorityParameters pPara;

    /**
     * 
     * @param isolate
     * @param string
     */
    // @SCJAllowed(INFRASTRUCTURE)
    public RealtimeThread(String string, int stackSize, Runnable logic) {
        super(logic, string, stackSize);
        bs = BackingStore.getScoped();
        initArea = ImmortalMemory.instance();
        // TODO: default sPara and pPara??

        if (BackingStore.SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] Create primordial realtime thread ");
            bs.printInfo();
        }
    }

    public RealtimeThread(PriorityParameters pPara, StorageParameters sPara, long initSize,
            Runnable logic) {
        // TODO: check parameters and set priority
        super(logic, null, (int) checkStorageParameters(sPara).getJavaStackSize());
        this.pPara = checkPriorityParameters(pPara);
        this.sPara = sPara;
        this.bs = RealtimeThread.currentRealtimeThread().getBackingStore().excavate(
                (int) sPara.getTotalBackingStoreSize());
        this.initArea = new PrivateMemory(initSize, this);

        if (BackingStore.SCJ_DEBUG_ENABLED) {
            VM.print("[SCJ] Create RealtimeThread ");
            VM.println(getName());
        }
    }

    public RealtimeThread(PriorityParameters pPara, StorageParameters sPara, long initSize,
            Runnable logic, BackingStore bs) {
        // TODO: check parameters and set priority
        super(logic, null, (int) checkStorageParameters(sPara).getJavaStackSize());
        this.pPara = checkPriorityParameters(pPara);
        this.sPara = sPara;
        this.bs = bs;
        this.initArea = new PrivateMemory(initSize, this);

        if (BackingStore.SCJ_DEBUG_ENABLED) {
            VM.print("[SCJ] Create RealtimeThread ");
            VM.println(getName());
        }
    }

    public MemoryArea getInitArea() {
        return initArea;
    }

    protected StorageParameters getStorageParameters() {
        return sPara;
    }

    public BackingStore getBackingStore() {
        return bs;
    }

    /**
     * Allocates no memory. Does not allow this to escape local variables. The
     * returned object may reside in scoped memory, within a scope that encloses
     * this.
     */
    // @SCJAllowed(LEVEL_2)
    public MemoryArea getMemoryArea() {
        return getInitArea();
    }

    /**
     * Allocates no memory. Does not allow this to escape local variables. The
     * returned object may reside in scoped memory, within a scope that encloses
     * this.
     * <p>
     * No allocation because ReleaseParameters are immutable.
     */
    //
    // //@SCJAllowed(LEVEL_2)
    public ReleaseParameters getReleaseParameters() {
        return null;
    }

    /**
     * Allocates no memory. Does not allow this to escape local variables. The
     * returned object may reside in scoped memory, within a scope that encloses
     * this.
     * <p>
     * No allocation because SchedulingParameters are immutable.
     */
    public SchedulingParameters getSchedulingParameters() {
        return pPara;
    }

    // public void setBackingStore(BackingStore bs) {
    // this.bs = bs;
    // }
    //
    // public void setInitArea(MemoryArea initArea) {
    // runner.initArea = initArea;
    // }

    /**
     * Allocates no memory. Treats the implicit this argument as a variable
     * residing in scoped memory.
     */
    public void start() {
        if (bs == null) {
            throw new Error("Backing store must be specified before the thread can start!");
        }
        if (initArea == null) {
            throw new Error("Initial area must be specified before the thread can start!");
        }
        super.start();
    }

    /**
     * Allocates no memory. Returns an object that resides in the current
     * mission's MissionMemory.
     */
    // @SCJAllowed(LEVEL_2)
    public static RealtimeThread currentRealtimeThread() {
        return (RealtimeThread) Thread.currentThread();
    }

    /**
     * Allocates no memory. The returned object may reside in scoped memory,
     * within a scope that encloses the current execution context.
     */
    // @SCJAllowed(LEVEL_1)
    public static MemoryArea getCurrentMemoryArea() {
        return (MemoryArea) BackingStore.getCurrentContext().getMirror();
    }

    public static int getMemoryAreaStackDepth() {
        // TODO:
        return -1;
    }

    public static MemoryArea getOuterMemoryArea(int delta) {
        // TODO:
        return null;
    }

    // @SCJAllowed(LEVEL_2)
    public static void sleep(HighResolutionTime time) throws InterruptedException {

    }

    public static boolean waitForNextRelease() {
        // TODO:
        return false;
    }

    private static PriorityParameters checkPriorityParameters(PriorityParameters priority) {
        // TODO:
        return priority;
    }

    private static StorageParameters checkStorageParameters(StorageParameters storage) {
        // TODO:
        return storage;
    }

    public void startRun() {
        initArea.enter(this);
        initArea.destroyBS();
        initArea = null;
    }
}
