package com.sun.squawk.test;

public class TimeRecord {
	private int iterations;
	private long[] highFallAsleepTime;
	private long[] highWakeUpTime;
	private long highStartTime;
	private long highFinishTime;
	long sleepPeriod;

	private long[] highDelays;
	private float highAvgDelay = -1;
	private long highMaxDelay = -1;
	private long highTotalTime = -1;

	private long[] lowStartIterTime;
	private long[] lowFinishIterTime;
	private float lowAvgTimePerIter = -1;

	private long lowMaxTimePerIter = -1;

	private int iterCounter = 0;

	private boolean doneComputing = false;

	public TimeRecord(int iter, long sleepPeriod) {
		this.iterations = iter;
		this.sleepPeriod = sleepPeriod;
		highFallAsleepTime = new long[iter];
		highWakeUpTime = new long[iter];
		highDelays = new long[iter];
		lowStartIterTime = new long[iter];
		lowFinishIterTime = new long[iter];
	}

	public void lowStartIteration() {
		if (iterCounter < iterations) {
			lowStartIterTime[iterCounter] = System.currentTimeMillis();
		}
	}

	public void lowFinishIteration() {
		if (iterCounter < iterations) {
			lowFinishIterTime[iterCounter++] = System.currentTimeMillis();
		}
	}

	public void highStart() {
		highStartTime = System.currentTimeMillis();
	}

	public void highFinish() {
		highFinishTime = System.currentTimeMillis();
	}

	public void highFallAsleep(int i) {
		highFallAsleepTime[i] = System.currentTimeMillis();
	}

	public void highWakeUp(int i) {
		highWakeUpTime[i] = System.currentTimeMillis();
	}

	public float getHighAvgDelay() {
		if (!doneComputing)
			compute();
		return highAvgDelay;
	}

	public long[] getHighDelays() {
		if (!doneComputing)
			compute();
		return highDelays;
	}

	public long getHighMaxDelay() {
		if (!doneComputing)
			compute();
		return highMaxDelay;
	}

	public long getHighTotalTime() {
		if (!doneComputing)
			compute();
		return highTotalTime;
	}

	public float getLowAvgTimePerIter() {
		if (!doneComputing)
			compute();
		return lowAvgTimePerIter;
	}

	public long getLowMaxTimePerIter() {
		if (!doneComputing)
			compute();
		return lowMaxTimePerIter;
	}

	private void compute() {
		long sum = 0;
		for (int i = 0; i < iterations; i++) {
			highDelays[i] = highWakeUpTime[i] - highFallAsleepTime[i]
					- sleepPeriod;
			sum += highDelays[i];
			if (highMaxDelay < highDelays[i])
				highMaxDelay = highDelays[i];
		}
		highAvgDelay = (float) sum / iterations;
		highTotalTime = highFinishTime - highStartTime;

		sum = 0;
		for (int i = 0; i < iterations; i++) {
			long timePerIter = lowFinishIterTime[i] - lowStartIterTime[i];
			sum += timePerIter;
			if (lowMaxTimePerIter < timePerIter)
				lowMaxTimePerIter = timePerIter;
		}
		if (iterCounter != 0) {
			lowAvgTimePerIter = (float) sum / iterCounter;
		}

		doneComputing = true;
	}
}
