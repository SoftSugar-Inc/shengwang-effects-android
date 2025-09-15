package com.softsugar.stmobile.engine.glutils;

import android.util.Log;

import java.util.ArrayList;
import java.util.LongSummaryStatistics;

public class CostTimeUtils {
    private static final String TAG = "CostTimeUtils";

    public static final int TIME_DETECT = 0;
    public static final int TIME_RENDER = 1;
    public static final int TIME_TOTAL = 2;
    public static final int TIME_PROCESS = 3;
    public static final int TIME_FPS = 4;

    private static volatile CostTimeUtils instance0;
    private static volatile CostTimeUtils instance1;
    private static volatile CostTimeUtils instance2;
    private static volatile CostTimeUtils instance3;
    private static volatile CostTimeUtils instance4;

    private String tagInfo = "";

//    public static CostTimeUtils getInstance() {
//        return getInstance(TIME_DETECT);
//    }

    public static CostTimeUtils getInstance(int client) {
        switch (client) {
            case TIME_DETECT:
                if (instance0 == null) {
                    synchronized (CostTimeUtils.class) {
                        instance0 = new CostTimeUtils("detect");
                    }
                }
                return instance0;
            case TIME_RENDER:
                if (instance1 == null) {
                    synchronized (CostTimeUtils.class) {
                        instance1 = new CostTimeUtils("render");
                    }
                }
                return instance1;
            case TIME_TOTAL:
                if (instance2 == null) {
                    synchronized (CostTimeUtils.class) {
                        instance2 = new CostTimeUtils("total");
                    }
                }
                return instance2;
            case TIME_PROCESS:
                if (instance3 == null) {
                    synchronized (CostTimeUtils.class) {
                        instance3 = new CostTimeUtils("pre process");
                    }
                }
                return instance3;
            case TIME_FPS:
                if (instance4 == null) {
                    synchronized (CostTimeUtils.class) {
                        instance4 = new CostTimeUtils("FPS");
                    }
                }
                return instance4;
            default:
                throw new IllegalArgumentException("Unknown client " + client);
        }
    }

    private CostTimeUtils(String tagInfo) {
        this.tagInfo = tagInfo;
    }

    private ArrayList<Long> runTimesList = new ArrayList<>();

    boolean FLAG = true;
    int MAX_RUN_COUNT = 200;

    public void printAverage(long runTime) {
        if (FLAG) {
            if (runTimesList == null) runTimesList = new ArrayList<>();
            if (runTimesList.size() < MAX_RUN_COUNT) runTimesList.add(runTime);
            if (runTimesList.size() == MAX_RUN_COUNT) {
                double averageTime;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    LongSummaryStatistics stats = runTimesList.stream().mapToLong((x) -> x).summaryStatistics();
                    averageTime = stats.getAverage();
                } else {
                    long sumTime = 0;
                    for (Long time : runTimesList) {
                        sumTime = sumTime + time;
                    }
                    averageTime = sumTime / MAX_RUN_COUNT;
                }
                STLogUtils.i(TAG, "time average:" + tagInfo + ":" + averageTime);
                runTimesList.clear();
            }
        }
    }
}
