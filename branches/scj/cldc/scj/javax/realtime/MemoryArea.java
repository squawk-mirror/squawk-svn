package javax.realtime;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.sun.squawk.BackingStore;

/**
 * allocate backing store at construction
 */
// @SCJAllowed
public abstract class MemoryArea implements AllocationContext {

    /**
     * The backing store of this memory area
     */
    private BackingStore bs;

    /**
     * TODO: the size of BS is in "int" instead of "long"
     */
    private long size;

    /**
     * for ImmortalMemory use only.
     */
    MemoryArea(BackingStore immortal) {
        bs = immortal;
        bs.setMirror(this);
        size = immortal.getSize();
    }

    /**
     * allocate BS from current thread's BS
     * 
     * @param size
     */
    public MemoryArea(long size) {
        this(size, RealtimeThread.currentRealtimeThread());
    }

    /**
     * allocate BS from specified BS
     * 
     * @param size
     * @param container
     */
    public MemoryArea(long size, RealtimeThread thread) {
        if (size < 0)
            throw new Error("memory size cannot be negative");
        this.size = size;
        if (thread == null)
            throw new Error("thread is null");
        this.bs = thread.getBackingStore().excavate((int) size);
        this.bs.setMirror(this);
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
        bs.shrink((int) size);
    }

    /**
     * Empty the memory and set the backing store to include all the rest free
     * memory space of its container.
     */
    public void reset() {
        bs.reset();
    }

    // @SCJAllowed
    public static MemoryArea getMemoryArea(Object object) {
        return (MemoryArea) BackingStore.getBackingStore(object).getMirror();
    }

    // @SCJAllowed
    public void enter(Runnable logic) {
        BackingStore old = BackingStore.setCurrentContext(bs);
        try {
            logic.run();
        } finally {
            BackingStore.setCurrentContext(old);
        }
    }

    // @SCJAllowed
    public void executeInArea(Runnable logic) throws InaccessibleAreaException {
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
        Object ret = null;
        BackingStore old = BackingStore.setCurrentContext(bs);
        try {
            ret = type.newInstance();
        } finally {
            BackingStore.setCurrentContext(old);
        }
        return ret;
    }

    // @SCJAllowed
    public Object newInstance(Constructor c, Object[] args) throws IllegalAccessException,
            InstantiationException, OutOfMemoryError, InvocationTargetException {
        Object ret = null;
        BackingStore old = BackingStore.setCurrentContext(bs);
        try {
            ret = c.newInstance(args);
        } finally {
            BackingStore.setCurrentContext(old);
        }
        return ret;
    }

    // @SCJAllowed
    public Object newInstanceInArea(Object object, Class type) throws IllegalArgumentException,
            OutOfMemoryError, InaccessibleAreaException, InstantiationException,
            IllegalAccessException {
        return getMemoryArea(object).newInstance(type);
    }

    // @SCJAllowed
    public Object newArray(Class type, int size) {
        Object ret = null;
        BackingStore old = BackingStore.setCurrentContext(bs);
        try {
            ret = Array.newInstance(type, size);
        } finally {
            BackingStore.setCurrentContext(old);
        }
        return ret;
    }

    // @SCJAllowed
    public Object newArrayInArea(Object object, Class type, int size) {
        return getMemoryArea(object).newArray(type, size);
    }

    // @SCJAllowed
    public long memoryConsumed() {
        return bs.bsConsumed();
    }

    // @SCJAllowed
    public long memoryRemaining() {
        return bs.bsRemaining();
    }

    // @SCJAllowed
    public long size() {
        return size;
    }

    public void destroy() {
        bs.destroy();
    }

    public void destroyAllAbove() {
        bs.destroyAllAbove();
    }
}
