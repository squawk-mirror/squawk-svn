package com.sun.squawk;

import javax.realtime.Happening;

import com.sun.squawk.util.Assert;
import com.sun.squawk.util.IntHashtable;
import com.sun.squawk.util.SquawkHashtable;

public class VMHappening {

    private static int ASYNC_TRIGGER_EVENT = 0;
    private static volatile boolean event_signalled;

    private static class AsyncTriggerLogic implements Runnable {

        private static volatile boolean stop = false;

        public void run() {
            while (!stop) {
                event_signalled = false;
                while (headAsync != null) {
                    headAsync.apiHappening.trigger();
                    headAsync = headAsync.nextAsync;
                }
                VMThread.waitForOSEvent(ASYNC_TRIGGER_EVENT);
            }
        }
    }

    private static Thread asyncTriggerer = new Thread(new AsyncTriggerLogic());
    private static VMHappening headAsync;
    private static IntHashtable fromID = new IntHashtable();
    private static SquawkHashtable fromName = new SquawkHashtable();

    private int id;
    private String name;
    private boolean isAsync;
    private VMHappening nextAsync;
    private Happening apiHappening;

    public VMHappening(int id, String name, boolean isAsync, Happening apiHap) {
        this.id = checkID(id);
        this.name = checkName(this.id, name);
        Assert.that(apiHap != null);
        this.apiHappening = apiHap;
    }

    public static void addAsyncHappening(VMHappening vmHap) {
        vmHap.nextAsync = headAsync;
        if (headAsync == null)
            headAsync = vmHap;
        if (!event_signalled) {
            event_signalled = true;
            VMThread.signalOSEvent(ASYNC_TRIGGER_EVENT);
        }
    }

    public static Happening getHappening(String name) {
        return (Happening) fromName.get(name);
    }

    public static Happening getHappening(int id) {
        return (Happening) fromID.get(id);
    }

    public static void startHappening() {
        asyncTriggerer.setPriority(Thread.MAX_PRIORITY);
        asyncTriggerer.start();
    }

    public static void stopHappening() {
        AsyncTriggerLogic.stop = true;
        VMThread.signalOSEvent(ASYNC_TRIGGER_EVENT);
    }

    public static boolean trigger(int id) {
        return Happening.trigger(id);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void connectHappening() {
        fromID.put(id, apiHappening);
        fromName.put(name, apiHappening);
    }

    public void disconnectHappening() {
        fromID.remove(id);
        fromName.remove(name);
    }

    /**
     * Check and return the right Id. A right Id should be 1) negative; 2) not
     * used by any existing happenings.
     */
    private static int checkID(int id) {
        if (invalidId(id))
            return getNextID();
        if (fromID.containsKey(id))
            throw new IllegalArgumentException("Happening with ID: " + id + " already created");
        return id;
    }

    /**
     * Check and return the right name based on a given Id. A right name should
     * be 1) not null; 2) not used by any existing happenings.
     */
    private static String checkName(int id, String name) {
        if (invalidName(name))
            return id2Name(id);
        if (fromName.containsKey(name))
            throw new IllegalArgumentException("Happening with Name: " + name + " already created");
        return name;
    }

    /* ------------------- Platform Dependent Content --------------------- */
    /*
     * Following contains contents not specified by SCJ Spec. They may be
     * platform or implementation dependent and should be documented and
     * provided to users.
     */

    /* Documented Id list */
    public static final int POSIX_SIGINT = -1;
    /* End */

    /** The next available auto-generated Id */
    private static int nextAvailableId = POSIX_SIGINT - 1;
    /** The next available serial number for forming the auto-generated names */
    private static int idGenCtr = 0;

    private static boolean invalidId(int id) {
        return id >= 0;
    }

    private static boolean invalidName(String name) {
        return name == null;
    }

    private static int getNextID() {
        return nextAvailableId--;
    } // This method should be implemented per platform. It maps id to the name.

    private static String getNextName() {
        return "Happening-" + idGenCtr++;
    }

    /**
     * Return documented name for documented Id. Return a generated one
     * otherwise.
     */
    private static String id2Name(int id) {
        switch (id) {
        case POSIX_SIGINT:
            return "POSIX_SIGINT";
        default:
            return getNextName();
        }
    }
}
