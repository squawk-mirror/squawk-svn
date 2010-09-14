package com.sun.squawk.test;

class ThreadLow extends Thread {

	int iterations;

	Work work;

	TimeRecord record;

	volatile boolean stop = false;

	ThreadLow(int iter, Work work, TimeRecord record) {
		this.iterations = iter;
		this.work = work;
		this.record = record;
		this.setPriority(Thread.NORM_PRIORITY - 1);
	}

	public void run() {
		while (!stop) {
			record.lowStartIteration();
			work.doIt();
			record.lowFinishIteration();
		}
	}
}
