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

    /**
     * TODO: the size of BS is in "int" instead of "long"
     */
    private long size;

    /**
     * The backing store of this memory area
     */
    private BackingStore bs;

    int indexOnStack;

    MemoryArea immediateOuter;

    /**
     * Only and must only called in ImmortalMemory constructor.
     */
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
    public MemoryArea(long size, RealtimeThread from) {
        reserveBackingStore(size, from);
        immediateOuter = RealtimeThread.getCurrentMemoryArea();
        indexOnStack = immediateOuter.indexOnStack + 1;
    }

    /**
     * Truncate the size of the backing store associated with this MissionMemory
     * object to newSize.
     * 
     * @throws IllegalStateException
     *             if the objects already allocated within this MissionMemory
     *             consume more than newSize bytes.
     * 
     * @throws IllegalArgumentException
     *             if newSize is larger than the current size of the
     *             MissionMemory.
     */
    public final void resize(long size) {
        Assert.always(bs != null, "MemoryArea: cannot resize the backing store which is null.");
        bs.shrink((int) size);
    }

    /**
     * Empty the memory and set the backing store to include all the rest free
     * memory space of its container.
     */
    protected void reset() {
        Assert.always(bs != null, "MemoryArea: cannot reset the backing store which is null.");
        bs.reset();
    }

    // @SCJAllowed
    public static MemoryArea getMemoryArea(Object object) {
        return (MemoryArea) BackingStore.getBackingStore(object).getMirror();
    }

    // @SCJAllowed
    public void enter(Runnable logic) {
        Assert.always(bs != null, "MemoryArea: cannot enter while the backing store is null.");
        BackingStore old = BackingStore.setCurrentContext(bs);
        try {
            logic.run();
        } finally {
            BackingStore.setCurrentContext(old);
        }
    }

    // @SCJAllowed
    public void executeInArea(Runnable logic) throws InaccessibleAreaException {
        Assert.always(bs != null,
                "MemoryArea: cannot executeInArea while the backing store is null.");
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
        Assert
                .always(bs != null,
                        "MemoryArea: cannot newInstance while the backing store is null.");
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
        Assert
                .always(bs != null,
                        "MemoryArea: cannot newInstance while the backing store is null.");
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
        Assert.always(bs != null, "MemoryArea: cannot newArray while the backing store is null.");
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
        Assert
                .always(bs != null,
                        "MemoryArea: cannot inquire about the comsumed size while the backing store is null.");
        return bs.bsConsumed();
    }

    // @SCJAllowed
    public long memoryRemaining() {
        Assert
                .always(bs != null,
                        "MemoryArea: cannot inquire about the remaining size while the backing store is null.");
        return bs.bsRemaining();
    }

    // @SCJAllowed
    public long size() {
        return size;
    }

    public void destroyBS() {
        Assert.always(bs != null, "MemoryArea: cannot destroy the backing store which is null.");
        if (this != ImmortalMemory.instance()) {
            bs.destroy();
            bs = null;
        }
    }

    public void reserveBackingStore(long size, RealtimeThread from) {
        Assert.always(bs == null,
                "MemoryArea: cannot allocate new backing store without destroying the old one.");
        if (size < 0)
            throw new IllegalArgumentException("Memory size cannot be negative.");
        this.size = size;
        if (from == null)
            throw new Error("Resource thread cannot be null.");

        /* if[SCJ] */
        BackingStore.disableScopeCheck();
        /* end[SCJ] */
        bs = from.getBackingStore().excavate((int) size);
        /* if[SCJ] */
        BackingStore.enableScopeCheck();
        /* end[SCJ] */
        bs.setMirror(this);
        Assert.always(bs != null, "MemoryArea: failed to allocate new backing store.");
    }

    public void destroyAllAboveBS() {
        Assert
                .always(bs != null,
                        "MemoryArea: cannot destroy this and all above backing stores while the current one is null.");
        bs.destroyAllAboves();
    }

    // just for debug
    public BackingStore getBS() {
        return this.bs;
    }
}
