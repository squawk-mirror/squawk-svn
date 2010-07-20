package javax.realtime;

/**
 * TBD: we need additional methods to allow SizeEstimation of thread stacks. In
 * particular, we need to be able to reserve memory for backing store. Perhaps
 * this belongs in a javax.safetycritical variant of SizeEstimator.
 */
// @SCJAllowed
public final class SizeEstimator {

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
        return 0;
    }

    // @SCJAllowed
    public void reserve(Class clazz, int num) {
    }

    // @SCJAllowed
    public void reserve(SizeEstimator size) {
    }

    // @SCJAllowed
    public void reserve(SizeEstimator size, int num) {
    }

    // @SCJAllowed
    public void reserveArray(int length) {
    }

    // @SCJAllowed
    public void reserveArray(int length, Class type) {
    }
}
