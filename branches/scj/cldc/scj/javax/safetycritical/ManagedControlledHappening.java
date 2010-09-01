package javax.safetycritical;

import javax.realtime.AsyncEvent;
import javax.realtime.ControlledHappening;
import javax.safetycritical.util.Utils;

//@SCJAllowed(LEVEL_2)
public abstract class ManagedControlledHappening extends ControlledHappening {

    // @SCJAllowed(LEVEL_2)
    public ManagedControlledHappening() {
        super();
    }

    // @SCJAllowed(LEVEL_2)
    public ManagedControlledHappening(int id) {
        super(id);
    }

    // @SCJAllowed(LEVEL_2)
    public ManagedControlledHappening(String name) {
        super(name);
    }

    // @SCJAllowed(INFRASTRUCTURE)
    public final void attach(AsyncEvent ae) {
        Utils.unimplemented();
    }

    // @SCJAllowed(INFRASTRUCTURE)
    public final void detach(AsyncEvent ae) {
        Utils.unimplemented();
    }
}
