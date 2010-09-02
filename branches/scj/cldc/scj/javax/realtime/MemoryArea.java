package javax.realtime;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.sun.squawk.BackingStore;
import com.sun.squawk.util.Assert;

/**
 * allocate backing store at construction
 */
// @SCJAllowed
public abstract class MemoryArea implements AllocationContext {

    /** TODO: the size of BS is in "int" instead of "long" */
    private long size;

    /** The backing store of this memory area */
    private BackingStore bs;

    /** Immortal's index is 0 */
    int indexOnStack;

    /** The immediate out memory area on the scope stack */
    MemoryArea immediateOuter;

    /** Only and must only called in ImmortalMemory constructor. */
    protected MemoryArea(BackingStore immortal) {
        bs = immortal;
        bs.setMirror(this);
        size = immortal.getSize();
        immediateOuter = null;
        indexOnStack = 0;
    }

    /**
     * Allocate my BS from specified parent BS
     * 
     * @param size
     * @param container
     */
    protected MemoryArea(long size, BackingStore from) {
        reserveBS(size, from);
        immediateOuter = RealtimeThread.getCurrentMemoryArea();
        indexOnStack = immediateOuter.indexOnStack + 1;
    }

    /**
     * Decrease the size of this memory area. Called only by MissionMemory.
     * 
     * @throws IllegalStateException
     *             if the objects already allocated within this MissionMemory
     *             consume more than newSize bytes.
     * 
     * @throws IllegalArgumentException
     *             if newSize is larger than the current size of the
     *             MissionMemory.
     */
    protected final void shrink0(long newSize) {
        Assert.always(bs != null, "MemoryArea: backing store is null @ shrink0()");
        bs.shrink((int) newSize);
        size = newSize;
    }

    /**
     * Empty the memory area and set the backing store to include all the rest
     * free memory space of its container.
     */
    protected void reset() {
        Assert.always(bs != null, "MemoryArea: backing store is null @ reset().");
        bs.reset();
        size = bs.getSize();
    }

    /**
     * Destroy all backing stores above me (excluded) on the stack.
     */
    protected void destroyAllAboveBS() {
        Assert.always(bs != null, "MemoryArea: backing store is null @ destroyAllAboveBS().");
        bs.destroyAllAboves();
    }

    protected void reserveBS_protected(long size) {
        reserveBS(size, RealtimeThread.currentRealtimeThread().getBackingStore());
    }

    protected void destroyBS_protected() {
        destroyBS();
    }

    void reserveBS(long size, BackingStore from) {
        Assert.always(bs == null,
                "MemoryArea: cannot allocate new backing store without destroying the old one.");
        if (size < 0)
            throw new IllegalArgumentException("Memory size cannot be negative.");
        this.size = size;
        if (from == null)
            throw new Error("Resource backing store cannot be null.");

        //BackingStore.disableScopeCheck();
        bs = from.excavate((int) size);
        //BackingStore.enableScopeCheck();
        bs.setMirror(this);
        Assert.always(bs != null, "MemoryArea: failed to allocate new backing store.");
    }

    void destroyBS() {
        Assert.always(bs != null, "MemoryArea: cannot destroy the backing store which is null.");
        if (this != ImmortalMemory.instance()) {
            bs.destroy();
            bs = null;
        }
    }

    // @SCJAllowed
    public static MemoryArea getMemoryArea(Object object) {
        return (MemoryArea) BackingStore.getBackingStore(object).getMirror();
    }

    // @SCJAllowed
    public void enter(Runnable logic) {
        Assert.always(bs != null, "MemoryArea: backing store is null @ enter().");
        BackingStore old = BackingStore.setCurrentContext(bs);
        try {
            logic.run();
        } finally {
            BackingStore.setCurrentContext(old);
        }
    }

    // @SCJAllowed
    public void executeInArea(Runnable logic) throws InaccessibleAreaException {
        Assert.always(bs != null, "MemoryArea: backing store is null @ executeInArea().");
        BackingStore old = BackingStore.setCurrentContext(bs);
        try {
            logic.run();
        } finally {
            BackingStore.setCurrentContext(old);
        }
    }

    // @SCJAllowed
    public Object newInstance(Class type) throws IllegalArgumentException, InstantiationException,
            OutOfMemoryError, InaccessibleAreaException, IllegalAccessException {
        Assert.always(bs != null, "MemoryArea: backing store is null @ newInstance().");
        BackingStore old = BackingStore.setCurrentContext(bs);
        try {
            return type.newInstance();
        } finally {
            BackingStore.setCurrentContext(old);
        }
    }

    // @SCJAllowed
    public Object newInstance(Constructor c, Object[] args) throws IllegalAccessException,
            InstantiationException, OutOfMemoryError, InvocationTargetException {
        Assert.always(bs != null, "MemoryArea: backing store is null @ newInstance().");
        BackingStore old = BackingStore.setCurrentContext(bs);
        try {
            return c.newInstance(args);
        } finally {
            BackingStore.setCurrentContext(old);
        }
    }

    // @SCJAllowed
    public Object newInstanceInArea(Object object, Class type) throws IllegalArgumentException,
            OutOfMemoryError, InaccessibleAreaException, InstantiationException,
            IllegalAccessException {
        return getMemoryArea(object).newInstance(type);
    }

    // @SCJAllowed
    public Object newArray(Class type, int size) {
        Assert.always(bs != null, "MemoryArea: backing store is null @ newArray().");
        BackingStore old = BackingStore.setCurrentContext(bs);
        try {
            return Array.newInstance(type, size);
        } finally {
            BackingStore.setCurrentContext(old);
        }
    }

    // @SCJAllowed
    public Object newArrayInArea(Object object, Class type, int size) {
        return getMemoryArea(object).newArray(type, size);
    }

    // @SCJAllowed
    public long memoryConsumed() {
        Assert.always(bs != null, "MemoryArea: backing store is null @ memoryConsumed().");
        return bs.bsConsumed();
    }

    // @SCJAllowed
    public long memoryRemaining() {
        Assert.always(bs != null, "MemoryArea: backing store is null @ memoryRemaining().");
        return bs.bsRemaining();
    }

    // @SCJAllowed
    public long size() {
        return size;
    }

    // Just for debug
    // public BackingStore getBS() {
    // return bs;
    // }
}
