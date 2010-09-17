package com.sun.squawk.test;

import javax.realtime.AperiodicParameters;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.StorageParameters;

public class Config {
    public static boolean DEBUG = true;

    public static long B = 1;
    public static long KB = 1024 * B;
    public static long MB = 1024 * KB;

    public static int nMissions = 3;
    public static int iterations = 5;
    public static int privateDepth = 5;
    public static int countDown = 3;
    public static int threadPoolSize = 4;

    public static long missionMemSize = 500 * KB;
    public static long privateSize = 200 * KB;
    public static long initPrivateSize = 100 * KB;
    public static long threadBackStoreSize = 500 * KB;
    public static long javaStackSize = 5 * KB;

    public static RelativeTime rel_0ms_0ns = new RelativeTime(0, 0);
    public static RelativeTime rel_50ms_0ns = new RelativeTime(50, 0);
    public static RelativeTime rel_500ms_0ns = new RelativeTime(500, 0);
    public static RelativeTime rel_1000ms_0ns = new RelativeTime(1000, 0);
    public static RelativeTime rel_max = new RelativeTime(Long.MAX_VALUE, 0);

    public static PriorityParameters priority = new PriorityParameters(Thread.NORM_PRIORITY);
    public static PeriodicParameters period = new PeriodicParameters(rel_0ms_0ns, rel_max);
    public static AperiodicParameters aperiod = new AperiodicParameters(rel_max, null);
    public static StorageParameters storage = new StorageParameters(threadBackStoreSize, 0,
            javaStackSize);
    
    public static int SIGQUIT = -1;
}
