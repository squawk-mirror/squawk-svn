package com.sun.squawk.test;

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
    public static PeriodicParameters periodHello = new PeriodicParameters(new RelativeTime(0, 0),
            new RelativeTime(500, 0));
    public static PeriodicParameters periodWord = new PeriodicParameters(new RelativeTime(50, 0),
            new RelativeTime(500, 0));
    public static AperiodicParameters aperiod = new AperiodicParameters(new RelativeTime(
            Long.MAX_VALUE, 0), null);
    public static StorageParameters storage = new StorageParameters(400 * KB, 0, 5 * KB);
    public static long privateSize = 100 * KB;
    public static long initPrivateSize = 100 * KB;

    public static long missionMemSize = 50 * KB;

    public static int iterations = 10;
    public static int privateDepth = 3;
    public static int nMissions = 3;

    public static boolean DEBUG = false;
}
