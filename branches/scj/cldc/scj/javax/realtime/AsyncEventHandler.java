package javax.realtime;

//@SCJAllowed
public class AsyncEventHandler implements Schedulable {

    /**
     * A count of pending fires - the fires that have not been handled yet.
     */
    private int fireCount = 0;

    private Object lock = new Object();

    /**
     * If the thread loop gets cancelled.
     */
    private volatile boolean cancelled = false;

    void release() {
        synchronized (lock) {
            if (fireCount++ == 0) {
                // VM.println("[SCJ] " + this + " notified ...");
                lock.notify();
            }
        }
    }

    // For subclasses that in javax.safetycritical package use
    protected void release_protected() {
        release();
    }

    protected void cancel() {
        synchronized (lock) {
            fireCount = 0;
            cancelled = true;
            lock.notify();
        }
    }

    protected int getAndClearPendingFireCount() {
        synchronized (lock) {
            int fc = fireCount;
            fireCount = 0;
            return fc;
        }
    }

    protected int getAndDecrementPendingFireCount() {
        synchronized (lock) {
            if (fireCount == 0)
                return 0;
            int fc = fireCount;
            fireCount--;
            return fc;
        }
    }

    protected int getAndIncrementPendingFireCount() {
        synchronized (lock) {
            int fc = fireCount;
            fireCount++;
            return fc;
        }
    }

    /**
     * The thread loop where all fires get handled.
     */
    public void run() {
        while (!cancelled) {
            try {
                synchronized (lock) {
                    if (fireCount == 0) {
                        // VM.println("[SCJ] " + this +
                        // " is going to wait ...");
                        lock.wait();
                    }
                }
                while (getAndDecrementPendingFireCount() > 0)
                    handleAsyncEvent();
            } catch (InterruptedException e) {
            }
        }
    }

    // @SCJAllowed
    public void handleAsyncEvent() {
    }
}
