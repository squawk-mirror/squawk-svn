package javax.safetycritical;

import javax.realtime.SizeEstimator;

import com.sun.squawk.BackingStore;

//@SCJAllowed
public final class PrivateMemory extends ManagedMemory {

    /**
     * 
     * @param size
     */
    // @SCJAllowed
    public PrivateMemory(long size) {
        super(size);
    }

    /**
     * 
     * @param estimator
     */
    // @SCJAllowed
    public PrivateMemory(SizeEstimator estimator) {
        this(estimator.getEstimate());
    }

    /**
     * Allocate the backing store of this private memory in the specified BS
     * "from". This constructor is only used by the RealtimeThread constructors
     * when creating the initial private memory.
     * 
     * @param size
     * @param from
     */
    public PrivateMemory(long size, BackingStore from) {
        super(size, from);
    }
}