package com.sun.squawk.test;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class StartApplication extends MIDlet {

    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
    }

    protected void pauseApp() {
    }

    protected void startApp() throws MIDletStateChangeException {
        TestCase[] tcs = new TestCase[1000];
        int c = 0;
        int workType;
        for (long sleep = 1; sleep <= 1; sleep += 2) {
            workType = Work.FLAT_ADD;
            tcs[c++] = new TestCase(sleep, workType, Work.X1);
            tcs[c++] = new TestCase(sleep, workType, Work.X16);
            tcs[c++] = new TestCase(sleep, workType, Work.X64);
            tcs[c++] = new TestCase(sleep, workType, Work.X256);
            tcs[c++] = new TestCase(sleep, workType, Work.X1024);
            tcs[c++] = new TestCase(sleep, workType, Work.X4096);
            tcs[c++] = new TestCase(sleep, workType, Work.X16384);
            tcs[c++] = new TestCase(sleep, workType, Work.X65536);
            tcs[c++] = new TestCase(sleep, workType, Work.X131072);
            tcs[c++] = new TestCase(sleep, workType, Work.X262144);
            tcs[c++] = new TestCase(sleep, workType, Work.X524288);
            tcs[c++] = new TestCase(sleep, workType, Work.X1048576);
            // tcs[c++] = new TestCase(sleep, workType, Work.X2097152);
            // tcs[c++] = new TestCase(sleep, workType, Work.X4194304);

            // workType = Work.RECUR_CALL;
            // tcs[c++] = new TestCase( sleep, workType, Work.X1);
            // tcs[c++] = new TestCase( sleep, workType, Work.X4);
            // tcs[c++] = new TestCase( sleep, workType, Work.X16);
            // tcs[c++] = new TestCase( sleep, workType, Work.X64);
            // tcs[c++] = new TestCase( sleep, workType, Work.X256);
            // tcs[c++] = new TestCase( sleep, workType, Work.X1024);
            // tcs[c++] = new TestCase( sleep, workType, Work.X4096);
            // tcs[c++] = new TestCase( sleep, workType, Work.X16384);

            // workType = Work.RECUR_CALL_W_ADD;
            // tcs[c++] = new TestCase( sleep, workType, Work.X1);
            // tcs[c++] = new TestCase( sleep, workType, Work.X4);
            // tcs[c++] = new TestCase( sleep, workType, Work.X16);
            // tcs[c++] = new TestCase( sleep, workType, Work.X64);
            // tcs[c++] = new TestCase( sleep, workType, Work.X256);
            // tcs[c++] = new TestCase( sleep, workType, Work.X1024);
            // tcs[c++] = new TestCase( sleep, workType, Work.X4096);
        }

        for (int i = 0; i < tcs.length && tcs[i] != null; i++) {
            System.out.println("Test Case - " + i);
            tcs[i].run();
        }
        this.notifyDestroyed();
    }
}