package javax.safetycritical;

/**
 * An interface implemented by all Safety Critical Java Schedulable classes. It
 * defines the register mechanism.
 */
// @SCJAllowed
public interface ManagedSchedulable {
    // @SCJAllowed
    public void register();
    // @SCJAllowed
    public void cleanUp();
}
