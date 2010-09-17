package com.sun.squawk.scj;

import javax.realtime.ImmortalMemory;
import javax.realtime.Timer;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;

import com.sun.squawk.Isolate;
import com.sun.squawk.Klass;
import com.sun.squawk.VMHappening;
import com.sun.squawk.VMThread;

public class SafeletLauncher {

    /** Purely static class should not be instantiated. */
    private SafeletLauncher() {
    }

    public static void main(String[] args) throws Exception {
        preInitialization();

        String className = "com.sun.squawk.test.MySafelet";
        Isolate iso = Isolate.currentIsolate();

        Klass klass;

        // Give the Isolate and thread sensible names...
        iso.setName(className);
        VMThread.currentThread().setName(className + " - main");

        try {
            klass = Klass.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Safelet class specified, " + className
                    + ", was not found");
        }
        if (!Safelet.class.isAssignableFrom(Klass.asClass(klass))) {
            throw new IllegalArgumentException("Specified class, " + className
                    + ", must be subclass of javax.safetycritical.Safelet");
        }
        Safelet safelet = (Safelet) klass.newInstance();

        Timer.startTimerThread();
        VMHappening.startHappening();
        safelet.setUp();
        MissionSequencer seq = safelet.getSequencer();
        seq.start();
        seq.join();
        safelet.tearDown();
        VMHappening.stopHappening();
        Timer.stopTimerThread();
    }

    public static void preInitialization() {
        ImmortalMemory.instance();
        // initialize all classes in all suites
        // Suite suite = iso.getLeafSuite();
        // while (suite != null) {
        // BackingStore.preInitializeClassInSuite(suite);
        // suite = suite.getParent();
        // }
    }
}
