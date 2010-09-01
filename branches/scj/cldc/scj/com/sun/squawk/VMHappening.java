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

    public static void addAsyncHappening(VMHappening vmHap) {
        vmHap.nextAsync = headAsync;
        if (headAsync == null)
            headAsync = vmHap;
        if (!event_signalled) {
            event_signalled = true;
            VMThread.signalOSEvent(ASYNC_TRIGGER_EVENT);
        }
    }

    public int getId() {
        return id;
    }

    // public Happening getAPIHappening() {
    // return apiHappening;
    // }

    public String getName() {
        return name;
    }

    private static int checkID(int id) {
        if (id >= 0)
            return getNextID();
        if (fromID.containsKey(id))
            throw new IllegalArgumentException("Happening with ID: " + id + " already created");
        return id;
    }

    private static String checkName(int id, String name) {
        if (name == null)
            return id2Name(id);
        if (fromName.containsKey(name))
            throw new IllegalArgumentException("Happening with Name: " + name + " already created");
        return name;
    }

    public static Happening getHappening(String name) {
        return (Happening) fromName.get(name);
    }

    public static Happening getHappening(int id) {
        return (Happening) fromID.get(id);
    }

    public void connectHappening() {
        fromID.put(id, apiHappening);
        fromName.put(name, apiHappening);
    }

    public void disconnectHappening() {
        fromID.remove(id);
        fromName.remove(name);
    }

    /*
     * Following stuff are platform dependent. They should not be here. Instead
     * they should have been implemented at a platform specific place and in a
     * platform specific way. We provide them here just for demo with the
     * assumption that the platform is POSIX complaint.
     */

    // The supported external events and their ids should be provided to
    // programmers by documents.
    public static final int POSIX_SIGINT = -1;
    private static int highest_unused_id = POSIX_SIGINT - 1;
    private static int counter_in_name = 0;

    private static int getNextID() {
        return highest_unused_id--;
    } // This method should be implemented per platform. It maps id to the name.

    static String id2Name(int id) {
        switch (id) {
        case POSIX_SIGINT:
            return "POSIX_SIGINT";
        default:
            return getNextName();
        }
    }

    private static String getNextName() {
        return "TempName-" + counter_in_name++;
    }
}
