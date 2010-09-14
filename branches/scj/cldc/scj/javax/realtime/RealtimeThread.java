package javax.realtime;

import javax.safetycritical.ManagedSchedulable;
import javax.safetycritical.PrivateMemory;
import javax.safetycritical.StorageParameters;

import com.sun.squawk.BackingStore;
import com.sun.squawk.VM;
import com.sun.squawk.VMThread;

//@SCJAllowed(LEVEL_1)
public class RealtimeThread extends Thread {

    /** The backing store this thread owns */
    private BackingStore bs;

    /**
     * The initial memory area which this thread will be running in after
     * started
     */
    private MemoryArea initArea;

    /** The storage configuration parameter */
    private StorageParameters storage;

    /** The priority parameter */
    private PriorityParameters priority;

    /** The managed schedulable that this thread is dedicated to */
    private ManagedSchedulable schedulable;

    /** The number of realtime threads created */
    private static int counter = 0;

    private static boolean DEBUG = false;

    /**
     * This constructor takes all the listed parameters which are clear enough
     * by reading the name. The backing store is taken from the parent real-time
     * thread's backing store. Then the initial private memory takes its backing
     * store from "this".
     * 
     * @param priority
     * @param storage
     * @param initMemSize
     * @param logic
     */
    public RealtimeThread(PriorityParameters priority, StorageParameters storage, long initMemSize,
            Runnable logic) {
        this(priority, storage, initMemSize, null, null, logic);
    }

    /**
     * Only used in creating mission sequencer thread since such thread does not
     * initialize its run in private memory.
     * 
     * @param priority
     * @param storage
     * @param initMemSize
     * @param logic
     * @param bs
     */
    public RealtimeThread(PriorityParameters priority, StorageParameters storage,
            MemoryArea initArea, Runnable logic) {
        // if initArea is immortal, this is the primordial real time, so take
        // the entire scoped backing store as my backing store
        this(priority, storage, -1, initArea, initArea == ImmortalMemory.instance() ? BackingStore
                .getScoped() : null, logic);

        if (DEBUG) {
            VM.print("[SCJ] Created mission sequencer thread: ");
            VM.println(getName());
        }
    }

    private RealtimeThread(PriorityParameters priority, StorageParameters storage,
            long initMemSize, MemoryArea initArea, BackingStore bs, Runnable logic) {
        super(logic, forgeName(), (int) checkStorageParameters(storage).getJavaStackSize());
        this.storage = storage;
        this.priority = checkPriorityParameters(priority);
        if (bs == null)
            bs = RealtimeThread.currentRealtimeThread().getBackingStore().excavate(
                    (int) storage.getTotalBackingStoreSize());
        //BackingStore.disableScopeCheck();
        this.bs = bs;
        //BackingStore.enableScopeCheck();
        if (initArea == null)
            initArea = new PrivateMemory(initMemSize, bs);
        this.initArea = initArea;
        this.setPriority(priority.getPriority());

        if (DEBUG) {
            VM.print("[SCJ] Created realtime thread: ");
            VM.println(getName());
        }
    }

    protected StorageParameters getStorageParameters() {
        return storage;
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
        return initArea;
    }

    /**
     * Allocates no memory. Does not allow this to escape local variables. The
     * returned object may reside in scoped memory, within a scope that encloses
     * this.
     * <p>
     * No allocation because ReleaseParameters are immutable.
     */
    // @SCJAllowed(LEVEL_2)
    public ReleaseParameters getReleaseParameters() {
        return null;
    }

    // /**
    // * Allocates no memory. Does not allow this to escape local variables. The
    // * returned object may reside in scoped memory, within a scope that
    // encloses
    // * this.
    // * <p>
    // * No allocation because SchedulingParameters are immutable.
    // */
    // public SchedulingParameters getSchedulingParameters() {
    // return priority;
    // }

    public ManagedSchedulable getManagedSchedulable() {
        return schedulable;
    }

    public void setManagedSchedulable(ManagedSchedulable schedulable) {
        this.schedulable = schedulable;
    }

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
     * Called in VMThread.callRun() for entering the initial area.
     */
    public final void startRun() {
        initArea.enter(this);
        initArea.destroyBS();
        initArea = null;
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

    // public static int getMemoryAreaStackDepth() {
    // return getCurrentMemoryArea().indexOnStack + 1;
    // }
    //
    // public static MemoryArea getOuterMemoryArea(int index) {
    // MemoryArea area = getCurrentMemoryArea();
    // if (index < 0 || area.indexOnStack < index)
    // return null;
    // while (true) {
    // if (area.indexOnStack == index)
    // return area;
    // area = area.immediateOuter;
    // // The index must fall in the stack range. If not, something goes
    // // wrong.
    // Assert.always(area != null);
    // }
    // }

    // @SCJAllowed(LEVEL_2)
    public static void sleep(HighResolutionTime time) throws InterruptedException {
        // FIXME: Nanos are simply ignored for now.
        if (time instanceof AbsoluteTime) {
            VMThread.sleepAbsolute(time.getMilliseconds());
        } else {
            VMThread.sleep(time.getMilliseconds());
        }
    }

    private static PriorityParameters checkPriorityParameters(PriorityParameters priority) {
        // TODO: revise the check
        if (priority == null)
            throw new IllegalArgumentException(
                    "Priority parameter cannot be null; No default for now!");
        int prio = priority.getPriority();
        if (prio < Thread.MIN_PRIORITY || prio > Thread.MAX_PRIORITY)
            throw new IllegalArgumentException("Priority must fall in the range of "
                    + Thread.MIN_PRIORITY + " - " + Thread.MAX_PRIORITY + ". Current priority: "
                    + prio);
        return priority;
    }

    private static StorageParameters checkStorageParameters(StorageParameters storage) {
        if (storage == null)
            throw new IllegalArgumentException(
                    "Storage parameter cannot be null; No default for now!");
        return storage;
    }

    private static String forgeName() {
        return "SCJ-Thread-" + counter++;
    }
}
