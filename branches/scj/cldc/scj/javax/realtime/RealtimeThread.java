package javax.realtime;

import javax.safetycritical.ManagedSchedulable;
import javax.safetycritical.PrivateMemory;
import javax.safetycritical.StorageParameters;

import com.sun.squawk.BackingStore;
import com.sun.squawk.VM;
import com.sun.squawk.util.Assert;

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
    private StorageParameters storage;

    /**
	 * 
	 */
    private PriorityParameters priority;

    /**
     * 
     */
    private ManagedSchedulable schedulable;

    /**
     * 
     * @param isolate
     * @param string
     */
    // @SCJAllowed(INFRASTRUCTURE)
    public RealtimeThread(String string, int stackSize, Runnable logic) {
        super(logic, string, stackSize);

/*if[SCJ]*/
        BackingStore.disableScopeCheck();
/*end[SCJ]*/
        bs = BackingStore.getScoped();
/*if[SCJ]*/
        BackingStore.enableScopeCheck();
/*end[SCJ]*/

        initArea = ImmortalMemory.instance();
        // TODO: default sPara and pPara??

        if (BackingStore.SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] Create primordial realtime thread ");
            bs.printInfo();
        }
    }

    public RealtimeThread(PriorityParameters priority, StorageParameters storage, long initMemSize,
            Runnable logic) {
        // TODO: check parameters and set priority
        super(logic, null, (int) checkStorageParameters(storage).getJavaStackSize());
        this.storage = storage;
        this.priority = checkPriorityParameters(priority);
        this.setPriority(priority.getPriority());

/*if[SCJ]*/
        BackingStore.disableScopeCheck();
/*end[SCJ]*/
        this.bs = RealtimeThread.currentRealtimeThread().getBackingStore().excavate(
                (int) storage.getTotalBackingStoreSize());
/*if[SCJ]*/
        BackingStore.enableScopeCheck();
/*end[SCJ]*/

        this.initArea = new PrivateMemory(initMemSize, this);

        if (BackingStore.SCJ_DEBUG_ENABLED) {
            VM.print("[SCJ] Create RealtimeThread ");
            VM.println(getName());
        }
    }

    public RealtimeThread(PriorityParameters priority, StorageParameters storage, long initMemSize,
            Runnable logic, BackingStore bs) {
        // TODO: check parameters and set priority
        super(logic, null, (int) checkStorageParameters(storage).getJavaStackSize());
        this.storage = storage;
        this.priority = checkPriorityParameters(priority);
        this.setPriority(priority.getPriority());

/*if[SCJ]*/
        BackingStore.disableScopeCheck();
/*end[SCJ]*/
        this.bs = bs;
/*if[SCJ]*/
        BackingStore.enableScopeCheck();
/*end[SCJ]*/

        this.initArea = new PrivateMemory(initMemSize, this);

        if (BackingStore.SCJ_DEBUG_ENABLED) {
            VM.print("[SCJ] Create RealtimeThread ");
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
        return priority;
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
        return getCurrentMemoryArea().indexOnStack + 1;
    }

    public static MemoryArea getOuterMemoryArea(int index) {
        MemoryArea area = getCurrentMemoryArea();
        if (index < 0 || area.indexOnStack < index)
            return null;
        while (true) {
            if (area.indexOnStack == index)
                return area;
            area = area.immediateOuter;
            // The index must fall in the stack range. If not, something goes
            // wrong.
            Assert.always(area != null);
        }
    }

    // @SCJAllowed(LEVEL_2)
    public static void sleep(HighResolutionTime time) throws InterruptedException {
        // FIXME: this is an temporary implementation. Not real-time at all!!
        long millisToSleep = 0;
        if (time instanceof AbsoluteTime) {
            millisToSleep = time.getMilliseconds() - VM.getTimeMillis();
        } else {
            millisToSleep = time.getMilliseconds();
        }
        if (millisToSleep > 0) {
            // VM.println("[SCJ] timer thread is going to sleep " +
            // millisToSleep + "ms");
            Thread.sleep(millisToSleep);
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
        // TODO: what to do?
        return storage;
    }

    public void startRun() {
        initArea.enter(this);
        initArea.destroyBS();
        initArea = null;
    }

    public ManagedSchedulable getManagedSchedulable() {
        return schedulable;
    }

    public void setManagedSchedulable(ManagedSchedulable schedulable) {
        this.schedulable = schedulable;
    }
}
