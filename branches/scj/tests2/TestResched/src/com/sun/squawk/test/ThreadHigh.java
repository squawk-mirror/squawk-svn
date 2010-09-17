package com.sun.squawk.test;

public class ThreadHigh extends Thread {

    long sleepPeriod;
    TimeRecord record;

    ThreadHigh(long sleepTime, TimeRecord record) {
        this.record = record;
        this.sleepPeriod = sleepTime;
        this.setPriority(Thread.NORM_PRIORITY);
    }

    public void run() {
        record.highStart();
        for (int i = 0; i < Config.iterations; i++) {
            try {
                record.highFallAsleep(i);
                Thread.sleep(sleepPeriod);
                record.highWakeUp(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        record.highFinish();
    }
}
