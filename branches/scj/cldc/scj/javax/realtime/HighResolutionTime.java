package javax.realtime;

//@SCJAllowed
public abstract class HighResolutionTime implements Comparable {

    Clock clock;

    long milli;

    int nano;

    // @SCJAllowed
    public Clock getClock() {
        return null;
    }

    // @SCJAllowed
    public final long getMilliseconds() {
        return -1L;
    }

    // @SCJAllowed
    public final int getNanoseconds() {
        return -1;
    }

    // @SCJAllowed
    public void set(HighResolutionTime time) {
    }

    // @SCJAllowed
    public void set(long millis) {
        set(millis, 0);
    }

    // @SCJAllowed
    public void set(long millis, int nanos) {
    }

    // @SCJAllowed
    public boolean equals(HighResolutionTime time) {
        return false;
    }

    // @SCJAllowed
    public boolean equals(java.lang.Object object) {
        return false;
    }

    // @SCJAllowed
    public int compareTo(HighResolutionTime time) {
        return -1;
    }

    // @SCJAllowed
    public int compareTo(Object o) {
        return 0;
    }

    // @SCJAllowed(LEVEL_2)
    public static void waitForObject(java.lang.Object target,
            HighResolutionTime time) throws java.lang.InterruptedException {
    }
}
