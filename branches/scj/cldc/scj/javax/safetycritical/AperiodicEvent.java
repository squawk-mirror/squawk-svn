package javax.safetycritical;

import javax.realtime.AsyncEvent;
import javax.safetycritical.util.Utils;

//@SCJAllowed(LEVEL_1)
public class AperiodicEvent extends AsyncEvent {

    // @SCJAllowed(LEVEL_1)
    public AperiodicEvent(AperiodicEventHandler handler) {
        setHandler(handler);
    }

    // @SCJAllowed(LEVEL_1)
    public AperiodicEvent(AperiodicEventHandler[] handlers) {
        Utils.unimplemented();
    }
}
