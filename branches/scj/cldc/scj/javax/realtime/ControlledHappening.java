package javax.realtime;

import javax.safetycritical.util.Utils;

/**
 * Controlled happenings are those that when triggered allow the application to
 * take control. The application schedulable object calls one of the takeControl
 * methods. That method calls the process method each time the happening is
 * triggered. The takeControl method returns when the happening is unregistered.
 * In SCJ, all happenings are managed, hence there are no visible constructors
 * for the class.
 */
// @SCJAllowed(LEVEL_2)
public class ControlledHappening extends EventHappening {

    // @SCJAllowed(LEVEL_2)
    public ControlledHappening() {
        super();
    }

    // @SCJAllowed(LEVEL_2)
    public ControlledHappening(int id) {
        super(id);
    }

    // @SCJAllowed(LEVEL_2)
    public ControlledHappening(int id, String name) {
        super(id, name);
    }

    // @SCJAllowed(LEVEL_2)
    public ControlledHappening(String name) {
        super(name);
    }

    /**
     * inherited
     */
    // @SCJAllowed(LEVEL_2)
    // public final void attach(AsyncEvent ae) {} ;

    // @SCJAllowed(LEVEL_2)
    protected void process() {
    }

    // @SCJAllowed(LEVEL_2)
    public final void takeControl() {
        Utils.unimplemented();
    }

    // @SCJAllowed(LEVEL_2)
    public final void takeControlInterruptible() {
        Utils.unimplemented();
    }

    // @SCJAllowed(LEVEL_2)
    protected final Object visit(EventExaminer logic) {
        Utils.unimplemented();
        return null;
    }
}
