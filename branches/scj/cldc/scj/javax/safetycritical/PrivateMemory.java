package javax.safetycritical;

import javax.realtime.RealtimeThread;
import javax.realtime.SizeEstimator;

//@SCJAllowed
public class PrivateMemory extends ManagedMemory {

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
    public PrivateMemory(long size, RealtimeThread thread) {
        super(size, thread);
    }
}