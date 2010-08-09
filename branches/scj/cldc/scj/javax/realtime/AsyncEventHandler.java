package javax.realtime;

import javax.safetycritical.StorageParameters;

import com.sun.squawk.VM;

//@SCJAllowed
public class AsyncEventHandler implements Schedulable {

    private long toDos = 0;

    private RealtimeThread thread;

    private volatile boolean stop = false;

    public AsyncEventHandler() {
        thread = RealtimeThread.currentRealtimeThread();
    }

    public AsyncEventHandler(PriorityParameters priority, StorageParameters storage,
            long initMemSize) {
        thread = new RealtimeThread(priority, storage, initMemSize, this);
        thread.start();
    }

    public void run() {
        while (!stop) {
            try {
                while (true) {
                    synchronized (thread) {
                        if (toDos == 0)
                            break;
                        toDos--;
                    }
                    handleAsyncEvent();
                }
                // toDos++ can happen here and a thread.notify() will be missed.
                // So need to check toDos again in following synchronized block.
                synchronized (thread) {
                    if (toDos == 0) {
                        // VM.println("[SCJ] " + this +
                        // " is going to wait ...");
                        thread.wait();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void release() {
        synchronized (thread) {
            if (toDos++ == 0) {
                // VM.println("[SCJ] " + this + " notified ...");
                thread.notify();
            }
        }
    }

    protected void stop() {
        stop = true;
    }

    protected void join() throws InterruptedException {
        thread.join();
    }

    protected MemoryArea getInitArea() {
        return thread.getInitArea();
    }

    // @SCJAllowed
    public void handleAsyncEvent() {
    }
}
