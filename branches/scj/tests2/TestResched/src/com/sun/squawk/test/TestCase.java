package com.sun.squawk.test;

public class TestCase {
    long sleepPeriod;
    int workType;
    int workload;
    TimeRecord record;
    ThreadLow low;
    ThreadHigh high;

    public TestCase(long sleep, int workType, int workload) {
        this.sleepPeriod = sleep;
        this.workType = workType;
        this.workload = workload;
        record = new TimeRecord(Config.iterations, sleepPeriod);
        low = new ThreadLow(new Work(workType, workload), record);
        high = new ThreadHigh(sleepPeriod, record);
    }

    public void run() {
        printInfo();
        low.start();
        high.start();
        try {
            high.join();
            low.stopIt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        printResult();
    }

    private void printInfo() {
        // System.out.println("---------------------- Test Case Info ----------------------");
        System.out.println();
        System.out.println("Iterations:\t" + Config.iterations);
        System.out.println("Sleep time:\t" + sleepPeriod + "ms");
        System.out.println("Work type:\t" + Work.typeToString(workType));
        System.out.println("Workload:\t" + workload + "X");
    }

    private void printResult() {
        System.out.println();
        System.out.println("High thread total time:   " + record.getHighTotalTime() + "\tms");
        System.out.println("Reschedule delay (AVG):   " + record.getHighAvgDelay() + "\tms");
        System.out.println("Reschedule delay (WC):    " + record.getHighMaxDelay() + "\tms");
        System.out.println("Per-iteration time (AVG): " + record.getLowAvgTimePerIter() + "\tms");
        System.out.println("Per-iteration time (WC):  " + record.getLowMaxTimePerIter() + "\tms");
        System.out.println("Delay record (ms):");
        long[] delays = record.getHighDelays();
        for (int i = 0; i < Config.iterations; i++) {
            System.out.print(delays[i] + " ");
        }
        System.out.println();
        System.out.println("--------------------------- End ----------------------------");

    }
}
