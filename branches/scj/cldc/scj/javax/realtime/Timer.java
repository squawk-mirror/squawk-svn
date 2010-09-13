package javax.realtime;

import com.sun.squawk.VM;

public abstract class Timer extends AsyncEvent {

    /*
     * the dummy timer which will always be the last in the queue due to its
     * infinite expiring time
     */
    private final static class NeverFireTimer extends Timer {

        private final static AbsoluteTime infinity = new AbsoluteTime(Long.MAX_VALUE, 0);

        protected NeverFireTimer() {
            super(infinity, Clock.getRealtimeClock(), null);
            super.active = true;
            super.enabled = true;
            super.targetTime.set(super.nextTargetTime);
        }
    }

    /*
     * NOTE: the current implementation assumes that only realtime clock is used
     * in the system.
     */
    private static class TimerThread extends Thread {

        private Timer head;
        private Object lock;
        private volatile boolean stop = false;

        private TimerThread() {
            lock = new Object();
            head = new NeverFireTimer();
            head.prev = head;
            head.next = head;
            setPriority(MAX_PRIORITY);
        }

        public void run() {
            // VM.println("[SCJ] timer thread started ...");
            Clock rtc = Clock.getRealtimeClock();
            AbsoluteTime currentTime = new AbsoluteTime();
            AbsoluteTime nextWakeupTime = new AbsoluteTime();
            while (!stop) {
                rtc.getTime(currentTime);
                // VM.println();
                // VM.println("[SCJ] timer thread checks fire @ currentTime: " +
                // currentTime.millis);
                while (getHead().fireIfExpired(currentTime, nextWakeupTime))
                    ;
                try {
                    // VM.print("[SCJ] timer thread is going to sleep til ");
                    // VM.println(timeToSleep.millis);
                    RealtimeThread.sleep(nextWakeupTime);
                } catch (InterruptedException e) {
                    // more urgent timers added (expected) or some other
                    // interruptions happened (unexpected). Anyway, check for
                    // the need to fire.
                    // VM.println("[SCJ] timer thread interrupted ... ");
                }
            }
        }

        // timer must not in the queue: prev == next == null
        private void add(Timer timer) {
            synchronized (lock) {
                Timer next = head;
                /* if[SCJ] */
                //BackingStore.disableScopeCheck();
                /* end[SCJ] */
                while (timer.targetTime.compareTo(next.targetTime) > 0)
                    next = next.next;
                timer.next = next;
                timer.prev = next.prev;
                timer.prev.next = timer;
                timer.next.prev = timer;
                if (next == head) {
                    head = timer;
                    // VM.println("[SCJ] interrupt timer thread ...");
                    interrupt();
                }
                /* if[SCJ] */
                //BackingStore.enableScopeCheck();
                /* end[SCJ] */
                // VM.println("[SCJ] " + timer + " added ");
                // printQueue();
            }
        }

        /**
         * 
         * @param timer
         * @return false if timer is not in the queue
         */
        private Timer remove(Timer timer) {
            synchronized (lock) {
                if (timer.next != null) {
                    /* if[SCJ] */
                    //BackingStore.disableScopeCheck();
                    /* end[SCJ] */
                    timer.prev.next = timer.next;
                    timer.next.prev = timer.prev;
                    if (timer == head)
                        head = timer.next;
                    /* if[SCJ] */
                    //BackingStore.enableScopeCheck();
                    /* end[SCJ] */
                    timer.prev = null;
                    timer.next = null;
                }
                // VM.println("[SCJ] " + timer + " removed ");
                // printQueue();
                return timer;
            }
        }

        private Timer getHead() {
            synchronized (lock) {
                return head;
            }
        }

        private void printQueue() {
            synchronized (lock) {
                Timer current = head;
                VM.print("[Q] ");
                do {
                    VM.print(current.toString());
                    VM.print(" - ");
                    current = current.next;
                } while (current != head);
                VM.print("\n");
            }
        }

        private void stop() {
            stop = true;
            interrupt();
        }
    }

    private static TimerThread thread = new TimerThread();

    private Timer prev;
    private Timer next;

    private Clock clock;
    protected Object lock = new Object();

    private AbsoluteTime targetTime = new AbsoluteTime();
    private AbsoluteTime nextTargetTime = new AbsoluteTime();
    private RelativeTime nextDurationTime = new RelativeTime();

    private boolean active = false;
    private boolean enabled = false;
    private boolean destroyed = false;
    private boolean last_rescheduled_with_AbsoluteTime;

    protected boolean periodic = false;
    protected boolean firstShotDone = false;
    protected RelativeTime period = new RelativeTime();

    protected Timer(HighResolutionTime time, Clock clock, AsyncEventHandler handler) {
        if (time instanceof RelativeTime && time.compareTo(RelativeTime.ZERO) < 0)
            throw new IllegalArgumentException("Timer start time cannot be relative negative!");

        if (time instanceof AbsoluteTime) {
            nextTargetTime.set(time);
            last_rescheduled_with_AbsoluteTime = true;
        } else {
            // time is RelativeTime or null
            nextDurationTime.set(time == null ? RelativeTime.ZERO : time);
            last_rescheduled_with_AbsoluteTime = false;
        }
        // simply ignore all clock settings.
        this.clock = Clock.getRealtimeClock();
        setHandler(handler);

        // VM.print("[SCJ] " + this + " created. Start time: ");
        // VM.println(time.millis);
    }

    private boolean fireIfExpired(AbsoluteTime currentTime, AbsoluteTime nextWakeupTime) {
        synchronized (lock) {
            // VM.println("[SCJ] checkes timer " + this + " on targetTime: " +
            // targetTime.millis);
            if (targetTime.compareTo(currentTime) <= 0) {
                thread.remove(this);
                fire();
                return true;
            } else {
                nextWakeupTime.set(targetTime);
                return false;
            }
        }
    }

    public void enable() {
        synchronized (lock) {
            if (destroyed)
                throw new IllegalStateException("Cannot enable destroyed timer: " + this);
            // if active and disabled, enable it. Otherwise, do nothing.
            if (active)
                enabled = true;
        }
    }

    public void disable() {
        synchronized (lock) {
            if (destroyed)
                throw new IllegalStateException("Cannot disable destroyed timer: " + this);
            // if active and enabled, disable it. Otherwise, do nothing.
            if (active)
                enabled = false;
        }
    }

    public boolean stop() {
        synchronized (lock) {
            if (destroyed)
                throw new IllegalStateException("Cannot stop destroyed timer: " + this);
            if (!active)
                return false;
            // if active, inactivate and disable it
            thread.remove(this);
            active = false;
            enabled = false;
            firstShotDone = false;
            return true;
        }
    }

    public void start(boolean disabled) {
        synchronized (lock) {
            if (destroyed)
                throw new IllegalStateException("Cannot start destroyed timer: " + this);
            if (active)
                throw new IllegalStateException("Timer already started: " + this);
            if (last_rescheduled_with_AbsoluteTime) {
                clock.getTime(targetTime);
                if (targetTime.compareTo(nextTargetTime) < 0)
                    targetTime.set(nextTargetTime);
            } else {
                targetTime = clock.getTime(targetTime).add(nextDurationTime, targetTime);
            }
            active = true;
            enabled = !disabled;
            thread.add(this);
        }
    }

    public void reschedule(HighResolutionTime time) {
        synchronized (lock) {
            if (destroyed)
                throw new IllegalStateException("Cannot reschedule destroyed timer: " + this);
            boolean useAbs = time instanceof AbsoluteTime;
            if (!active || firstShotDone) {
                // if inactive as type of timer or active but having done the
                // first shot as
                // periodic timer, rescheduled target time takes effect after
                // the next time it becomes active
                if (useAbs) {
                    nextTargetTime.set(time);
                    last_rescheduled_with_AbsoluteTime = true;
                } else {
                    nextDurationTime.set(time);
                    last_rescheduled_with_AbsoluteTime = false;
                }
            } else {
                // if active but not done the first shot yet (for periodic
                // timer), rescheduled target time takes effect at next
                // fire/skip
                thread.remove(this);
                if (useAbs)
                    targetTime.set(time);
                else
                    targetTime = clock.getTime(targetTime).add((RelativeTime) time, targetTime);
                // since target time changed, update the position in timer queue
                thread.add(this);
            }
        }
    }

    public void destroy() {
        synchronized (lock) {
            if (destroyed)
                throw new IllegalStateException("Cannot destroy destroyed timer: " + this);
            destroyed = true;
            thread.remove(this);
        }
    }

    public void fire() {
        // VM.println("[SCJ] " + this + " fired ...");
        if (enabled)
            super.fire();
        if (periodic) {
            // if periodic timer, tell that the first shot is
            // done,reschedule for
            // the next shot
            firstShotDone = true;
            try {
                targetTime = targetTime.add(period, targetTime);
            } catch (ArithmeticException e) {
                // overflow happened. Schedule next shot at forever
                targetTime.set(Long.MAX_VALUE, 0);
            }
            thread.add(this);
        } else {
            // if one shot timer, inactivate and disable it
            active = enabled = false;
        }
    }

    public void start() {
        // VM.println("[SCJ] " + this + " started ");
        start(false);
    }

    public Clock getClock() {
        return clock;
    }

    public boolean isRunning() {
        synchronized (lock) {
            if (destroyed)
                throw new IllegalStateException("Cannot test if a destroyed timer is running: "
                        + this);
            return enabled;
        }
    }

    public AbsoluteTime getFireTime() {
        return getFireTime(new AbsoluteTime());
    }

    public AbsoluteTime getFireTime(AbsoluteTime dest) {
        synchronized (lock) {
            if (destroyed)
                throw new IllegalStateException();
            if (!active)
                throw new IllegalStateException();
            dest.set(targetTime);
            return dest;
        }
    }

    public static void startTimerThread() {
        thread.start();
    }

    public static void stopTimerThread() {
        thread.stop();
    }
}
