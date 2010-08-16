package javax.realtime;

//@SCJAllowed
public class PriorityParameters extends SchedulingParameters {

    private int priority;

    // @SCJAllowed
    public PriorityParameters(int priority) {
        this.priority = priority;
    }

    // @SCJAllowed
    public int getPriority() {
        return priority;
    }

    // not scj allowed
    public void setPriority(int priority) throws IllegalArgumentException {
        this.priority = priority;
    }
}
