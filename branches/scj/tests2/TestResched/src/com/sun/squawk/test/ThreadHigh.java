package com.sun.squawk.test;

public class ThreadHigh extends Thread {

	int iteration;

	long sleepPeriod;

	TimeRecord record;

	private Object o = new Object();

	ThreadHigh(int iter, long sleepTime, TimeRecord record) {
		this.iteration = iter;
		this.record = record;
		this.sleepPeriod = sleepTime;
		this.setPriority(Thread.NORM_PRIORITY);
	}

	public void run() {
		synchronized (o) {
			record.highStart();
			for (int i = 0; i < iteration; i++) {
				try {
					record.highFallAsleep(i);
					o.wait(sleepPeriod, 0);
					record.highWakeUp(i);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			record.highFinish();
		}
	}
}
