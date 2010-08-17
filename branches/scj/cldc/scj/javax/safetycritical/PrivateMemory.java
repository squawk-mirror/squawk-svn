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
     * allocate BS from specified BS
     * 
     * @param size
     * @param container
     */
    public PrivateMemory(long size, BackingStore from) {
        super(size, from);
    }
}