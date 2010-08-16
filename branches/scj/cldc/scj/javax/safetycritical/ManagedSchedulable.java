package javax.safetycritical;

/**
 * An interface implemented by all Safety Critical Java Schedulable classes. It
 * defines the register mechanism.
 */
// @SCJAllowed
public interface ManagedSchedulable {

    // @SCJAllowed
    public void register();

    public void start();

    public void stop();

    public void join() throws InterruptedException;

    public void cleanUp();

    // Schedulables can be linked together
    public ManagedSchedulable getNext();
    public void setNext(ManagedSchedulable next);
}
