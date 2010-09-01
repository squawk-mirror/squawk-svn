package javax.realtime;

import javax.safetycritical.ManagedMemory;

import com.sun.squawk.VMHappening;

/**
   *
   */
// @SCJAllowed(LEVEL_1)
public abstract class Happening {

    private VMHappening vmHappening;

    private boolean registered = false;

    protected Happening() {
        this(0, null);
    }

    protected Happening(int id) {
        this(id, null);
    }

    protected Happening(String name) {
        this(0, name);
    }

    protected Happening(int id, String name) {
        boolean isAsync = this instanceof EventHappening;
        vmHappening = new VMHappening(id, name, isAsync, this);
    }

    /** Find a happening by its name. */
    // @SCJAllowed(LEVEL_1)
    public static Happening getHappening(String name) {
        return VMHappening.getHappening(name);
    }

    /**
     * Return the ID of the happening with the name name. If there is not
     * happening with that name return 0.
     */
    // @SCJAllowed(LEVEL_1)
    public static int getId(String name) {
        Happening hap = VMHappening.getHappening(name);
        if (hap == null)
            return 0;
        return hap.getId();
    }

    /**
     * Is there a Happening with name name?
     * 
     * @return True if there is.
     */
    // @SCJAllowed(LEVEL_1)
    public static boolean isHappening(String name) {
        return getHappening(name) != null;
    }

    /**
     * Causes the happening corresponding to happeningId to occur.
     * 
     * @return true if a happening with id happeningId was found, false
     *         otherwise.
     */
    // @SCJAllowed(LEVEL_1)
    public static final boolean trigger(int id) {
        // System.err.print("[SCJ] Trigger Happening ID: ");
        // System.err.println(id);
        Happening hap = VMHappening.getHappening(id);
        if (hap == null || !hap.isRegistered())
            return false;
        if (hap.isAsync())
            VMHappening.addAsyncHappening(hap.vmHappening);
        else
            hap.trigger();

        // System.err.print("[SCJ] The Happening name:");
        // System.err.print(hap.getName());
        // System.err.print(" ID: ");
        // System.err.println(hap.getId());

        return true;
    }

    /**
     * Return the id of this happening.
     */
    // @SCJAllowed(LEVEL_1)
    public final int getId() {
        return vmHappening.getId();
    }

    /**
     * Returns the string name of this happening
     */
    // @SCJAllowed(LEVEL_1)
    public final String getName() {
        return vmHappening.getName();
    }

    /**
     * @return Return true if this happening is presently registered.
     */
    // @SCJAllowed(LEVEL_1)
    public boolean isRegistered() {
        return registered;
    }

    /**
     * Register this Happening.
     * 
     * @throws ???? if called from outside the mission initialization phase.
     */
    // @SCJAllowed(LEVEL_1)
    // @SCJRestricted(phase = INITIALIZATION)
    public final void register() {
        registered = true;
        vmHappening.connectHappening();
        ManagedMemory.getCurrentManageMemory().getManager().regHappening(this);
    }

    /**
     * Unregister this Happening.
     * 
     * @throws ???? if called from outside the mission initialization phase.
     */
    // @SCJAllowed(LEVEL_1)
    // @SCJRestricted(phase = CLEANUP)
    public final void unRegister() {
        if (registered) {
            registered = false;
            vmHappening.disconnectHappening();
        }
    }

    public abstract void trigger();

    protected abstract boolean isAsync();
}
