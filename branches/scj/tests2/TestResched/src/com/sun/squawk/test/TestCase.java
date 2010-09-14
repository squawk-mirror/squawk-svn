package com.sun.squawk.test;

public class TestCase {
	int iterations;
	long sleepPeriod;
	int workType;
	int workload;
	TimeRecord record;

	public TestCase(int iter, long sleep, int workType, int workload) {
		this.iterations = iter;
		this.sleepPeriod = sleep;
		this.workType = workType;
		this.workload = workload;
		record = new TimeRecord(iterations, sleepPeriod);
	}

	public void run() {
		ThreadLow low = new ThreadLow(iterations, new Work(workType, workload),
				record);
		ThreadHigh high = new ThreadHigh(iterations, sleepPeriod, record);

		printInfo();
		low.start();
		high.start();
		try {
			high.join();
			low.stop = true;
			printResult();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void printResult() {
		System.out.println("Total time (thread high): "
				+ record.getHighTotalTime() + "ms");
		System.out.println("Average reschedule delay: "
				+ record.getHighAvgDelay() + "ms");
		System.out.println("Worst-case reschedule delay: "
				+ record.getHighMaxDelay() + "ms");
		System.out.println("Average time per iteration: "
				+ record.getLowAvgTimePerIter() + "ms");
		System.out.println("Worst-case time per iteration: "
				+ record.getLowMaxTimePerIter() + "ms");
		System.out.println("Delay record (ms):");
		long[] delays = record.getHighDelays();
		for (int i = 0; i < iterations; i++) {
			System.out.print(delays[i] + " ");
		}
		System.out
				.println("\n---------------------------- End ----------------------------");

	}

	private void printInfo() {
		System.out.println("Test case info:");
		System.out.println("Iterations: " + iterations);
		System.out.println("Sleep period: " + sleepPeriod + "ms");
		System.out.println("Work type: " + Work.typeToString(workType));
		System.out.println("Workload: " + workload + "X");
	}
}
