package javax.realtime;

import com.sun.squawk.BackingStore;

/**
 * TBD: we need additional methods to allow SizeEstimation of thread stacks. In
 * particular, we need to be able to reserve memory for backing store. Perhaps
 * this belongs in a javax.safetycritical variant of SizeEstimator.
 */
// @SCJAllowed
public final class SizeEstimator {

    private long sum;

    // @SCJAllowed
    public SizeEstimator() {
    }

    /**
     * JSR 302 tightens the semantic requirements on the implementation of
     * getEstimate. For compliance with JSR 302, getEstimate() must return a
     * conservative upper bound on the amount of memory required to represent
     * all of the memory reservations associated with this SizeEstimator object.
     */

    // @SCJAllowed
    public long getEstimate() {
        return sum;
    }

    // @SCJAllowed
    public void reserve(Class clazz, int num) {
        if (clazz == null)
            throw new IllegalArgumentException("Class cannot be null");
        if (num < 0)
            throw new IllegalArgumentException("Number cannot be negative");
        sum += BackingStore.getConsumedMemorySize(clazz, -1) * num;
    }

    // @SCJAllowed
    public void reserve(SizeEstimator estimator) {
        reserve(estimator, 1);
    }

    // @SCJAllowed
    public void reserve(SizeEstimator estimator, int num) {
        if (estimator == null)
            throw new IllegalArgumentException("Class cannot be null");
        if (num < 0)
            throw new IllegalArgumentException("Number cannot be negative");
        sum += estimator.sum * num;
    }

    // @SCJAllowed
    public void reserveArray(int length) {
        reserveArray(length, Object.class);
    }

    // @SCJAllowed
    public void reserveArray(int length, Class clazz) {
        if (clazz == null)
            throw new IllegalArgumentException("Component class cannot be null");
        if (length < 0)
            throw new IllegalArgumentException("Array length cannot be negative");
        sum += BackingStore.getConsumedMemorySize(clazz, length);
    }
}
