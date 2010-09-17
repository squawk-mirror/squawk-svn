package com.sun.squawk.test;

class ThreadLow extends Thread {

    Work work;
    TimeRecord record;
    volatile boolean stop = false;

    ThreadLow(Work work, TimeRecord record) {
        this.work = work;
        this.record = record;
        this.setPriority(Thread.NORM_PRIORITY - 1);
    }

    void stopIt() {
        stop = true;
    }

    public void run() {
        while (!stop) {
            record.lowStartIteration();
            work.doIt();
            record.lowFinishIteration();
        }
    }
}
