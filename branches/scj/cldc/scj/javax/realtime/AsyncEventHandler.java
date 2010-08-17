package javax.realtime;


//@SCJAllowed
public class AsyncEventHandler implements Schedulable {

    private int fireCount = 0;

    private Object lock = new Object();

    private volatile boolean stop = false;

    // public AsyncEventHandler() {
    // }

    public void run() {
        while (!stop) {
            try {
                synchronized (lock) {
                    if (fireCount == 0) {
//                        VM.println("[SCJ] " + this + " is going to wait ...");
                        lock.wait();
                    }
                }
                while (getAndDecrementPendingFireCount() > 0)
                    handleAsyncEvent();
            } catch (InterruptedException e) {
            }
        }
    }

    public void release() {
        synchronized (lock) {
            if (fireCount++ == 0) {
//                VM.println("[SCJ] " + this + " notified ...");
                lock.notify();
            }
        }
    }

    public void stop() {
        synchronized (lock) {
            fireCount = 0;
            stop = true;
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

    // @SCJAllowed
    public void handleAsyncEvent() {
    }
}
