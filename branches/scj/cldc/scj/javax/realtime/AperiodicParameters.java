package javax.realtime;

//@SCJAllowed(LEVEL_1)
public class AperiodicParameters extends ReleaseParameters {
    // @SCJAllowed(LEVEL_1)
    public AperiodicParameters(RelativeTime deadline, AsyncEventHandler missHandler) {
        super(deadline, missHandler);
    }
}
