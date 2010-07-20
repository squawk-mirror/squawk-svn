package javax.realtime;

//@SCJAllowed
public class PriorityParameters extends SchedulingParameters {

    // @SCJAllowed
    public PriorityParameters(int priority) {
    }

    // @SCJAllowed
    public int getPriority() {
        return -1; // skeleton
    }

    // not scj allowed
    public void setPriority(int priority) throws IllegalArgumentException {
    }
}
