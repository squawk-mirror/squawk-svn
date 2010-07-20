package javax.realtime;

//@SCJAllowed
public class PriorityScheduler extends Scheduler {
    /**
     * Priority-based dispatching is supported at Levels 1 and 2. The only
     * access to the priority scheduler is for obtaining the maximum priority.
     * 
     * No allocation here, because the primordial instance is presumed allocated
     * at within the <clinit> code.
     */
    public static PriorityScheduler instance() {
        return null;
    }

    // @SCJAllowed
    public int getMaxPriority() {
        return 39;
    }

    // @SCJAllowed
    public int getNormPriority() {
        return 25;
    }

    // @SCJAllowed
    public int getMinPriority() {
        return 11;
    }
}
