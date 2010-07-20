package javax.safetycritical;

/**
 * An interface implemented by all Safety Critical Java Schedulable classes. It
 * defines the register mechanism.
 */
// @SCJAllowed
public interface ManagedSchedulable {
    /**
     * Register the task with its Mission.
     */
    // @SCJAllowed
    public void register();

    public void start();

    public void join() throws InterruptedException;
}
