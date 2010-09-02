package javax.safetycritical;

import javax.realtime.HighResolutionTime;
import javax.safetycritical.util.Utils;

/**
 * System wide information
 */
// @SCJAllowed
public class Services {

    /**
     * Captures the stack back trace for the current thread into its
     * thread-local stack back trace buffer and remembers that the current
     * contents of the stack back trace buffer is associated with the object
     * represented by the association argument. The size of the stack back trace
     * buffer is determined by the StorageParameters object that is passed as an
     * argument to the constructor of the corresponding Schedulable. If the
     * stack back trace buffer is not large enough to capture all of the stack
     * back trace information, the information is truncated in an implementation
     * dependent manner.
     */
    // @SCJAllowed
    public static void captureBackTrace(Throwable association) {
        Utils.unimplemented();
    }

    // @SCJAllowed
    // public static Level getDeploymentLevel() {
    // return LEVEL_0;
    // }

    /**
     * This is like sleep except that it is not interruptible and it uses
     * nanoseconds instead of milliseconds.
     * 
     * @param delay
     *            is the number of nanoseconds to suspend
     * 
     *            TBD: should this be called suspend or deepSleep to no have a
     *            ridiculously long name?
     * 
     *            TBD: should not be a long nanoseconds?
     */
    // @SCJAllowed(LEVEL_2)
    public static void delay(int nanos) {
        Utils.unimplemented();
    }

    /**
     * This is like sleep except that it is not interruptible and it uses
     * nanoseconds instead of milliseconds.
     * 
     * @param delay
     *            is the number of nanoseconds to suspend
     * 
     *            TBD: should this be called suspend or deepSleep to no have a
     *            ridiculously long name?
     * 
     *            TBD: should not be a long nanoseconds?
     */
    // @SCJAllowed(LEVEL_2)
    public static void delay(HighResolutionTime delay) {
        Utils.unimplemented();
    }

    /**
     * @return the default ceiling priority The value is the highest software
     *         priority.
     */
    // @SCJAllowed(LEVEL_1)
    public static int getDefaultCeiling() {
        Utils.unimplemented();
        return 0;
    }

    // // @SCJAllowed
    // public static Level getDeploymentLevel() {
    // }

    /**
     * Every interrupt has an implementation-defined integer id.
     * 
     * @return The priority of the code that the first-level interrupts code
     *         executes. The returned value is always greater than
     *         PriorityScheduler.getMaxPriority().
     * @throws IllegalArgument
     *             if unsupported InterruptId
     */
    // @SCJAllowed(LEVEL_1)
    public static int getInterruptPriority(int InterruptId) {
        Utils.unimplemented();
        return 0;
    }

    /**
     * Busy wait spinning loop (now plus nanos).
     * 
     * @param delay
     */
    // @@SCJAllowed(LEVEL_1)
    public static void nanoSpin(int nanos) {
        Utils.unimplemented();
    }

    // /**
    // * Registers an interrupt handler.
    // *
    // * @param InterruptId
    // * @param IH
    // */
    // // @SCJAllowed(LEVEL_1)
    // public static void registerInterruptHandler(int InterruptId,
    // InterruptHandler IH) {
    // }

    /**
     * sets the ceiling priority of object O The priority can be in the software
     * or hardware priority range.
     * 
     * @throws IllegalThreadState
     *             if called outside the mission phase
     */
    // @SCJAllowed(LEVEL_1)
    public static void setCeiling(Object obj, int pri) {
        Utils.unimplemented();
    }
}
