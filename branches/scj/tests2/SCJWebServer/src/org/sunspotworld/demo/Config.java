package org.sunspotworld.demo;

import javax.realtime.AperiodicParameters;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.StorageParameters;

public class Config {
    public static long B = 1;
    public static long KB = 1024 * B;
    public static long MB = 1024 * KB;

    public static PriorityParameters priority = new PriorityParameters(Thread.NORM_PRIORITY);
    public static PeriodicParameters period = new PeriodicParameters(new RelativeTime(),
            new RelativeTime(Long.MAX_VALUE, 0));
    public static AperiodicParameters aperiod = new AperiodicParameters(new RelativeTime(
            Long.MAX_VALUE, 0), null);
    public static StorageParameters storage = new StorageParameters(400 * KB, 0, 5 * KB);
    public static long privateSize = 100 * KB;
    public static long initPrivateSize = 2 * KB;
    public static int threadPoolSize = 4;
    
    public static long missionMemSize = 50 * KB;
    
    public static boolean DEBUG = false;
}
