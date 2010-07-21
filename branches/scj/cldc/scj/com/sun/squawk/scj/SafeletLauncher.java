package com.sun.squawk.scj;

import javax.realtime.RealtimeThread;
import javax.safetycritical.Safelet;

import com.sun.squawk.Isolate;
import com.sun.squawk.Klass;
import com.sun.squawk.VMThread;

public class SafeletLauncher {

    /**
     * Purely static class should not be instantiated.
     */
    private SafeletLauncher() {
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String className = "com.sun.squawk.test.MySafelet";
        Isolate iso = Isolate.currentIsolate();

        Klass klass;

        // Give the Isolate and thread sensible names...
        iso.setName(className);
        VMThread.currentThread().setName(className + " - main");

        // initialize all classes in all suites
        // Suite suite = iso.getLeafSuite();
        // while (suite != null) {
        // BackingStore.preInitializeClassInSuite(suite);
        // suite = suite.getParent();
        // }

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
        final Safelet safelet = (Safelet) klass.newInstance();

        Runnable runner = new Runnable() {

            public void run() {
                safelet.setUp();
                safelet.getSequencer().exec();
                safelet.tearDown();
            }
        };

        RealtimeThread thread = new RealtimeThread("SCJ-init", 500000, runner);
        thread.start();
        thread.join();
    }
}
