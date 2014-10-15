/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author exam
 */
public class PerformanceCounters {

    Map<String, Stat> counters;
    private final long totalStartTime;
    private int longestCounterName;

    static class Stat {

        String timerName;
        Long startTime;
        Long totalTime;
        Long noRuns;
        Long totalMemoryUsage;
        static final Runtime runtime = Runtime.getRuntime();

        public Stat(String timerName) {
            this.timerName = timerName;
            this.startTime = 0L;
            this.totalTime = 0L;
            this.noRuns = 0L;
            this.totalMemoryUsage = 0L;
        }

        @Override
        public String toString() {
            double avgTime = getAvgTime() / 1000000d;
            String tmp = timerName;
            while (tmp.length() < pc.longestCounterName) {
                tmp += " ";
            }
            return "{" + timerName + "}\t " + noRuns + " runs\t\t "
                    + avgTime + "ms\t" + getAvgMemoryUsage() + "b";
        }

        public synchronized void startTimer() {
            if (startTime != 0L) {
                System.err.println("Counter had already started before! (" + timerName + ")");
            } else {
                startTime = System.nanoTime();
            }
        }

        public synchronized void stopTimer() {
            if (startTime == 0L) {
                System.err.println("Counter hadn't start before! (" + timerName + ")");
            } else {
                totalTime += (System.nanoTime() - startTime);
                noRuns++;
                startTime = 0L;
                totalMemoryUsage += runtime.totalMemory() - runtime.freeMemory();
            }
        }

        public double getAvgTime() {
            if (noRuns == 0) {
                return Double.MAX_VALUE;
            }

            return ((double) totalTime) / ((double) noRuns);
        }

        public Long getAvgMemoryUsage() {
            if (noRuns == 0) {
                return 0l;
            }

            return totalMemoryUsage / noRuns;
        }
    }

    static PerformanceCounters pc = new PerformanceCounters();

    private PerformanceCounters() {
        counters = new HashMap<>();
        longestCounterName = 0;
        totalStartTime = System.currentTimeMillis();
    }

    public static void startTimer(String timerName) {
        Stat stat = pc.counters.get(timerName);
        if (stat == null) {
            stat = new Stat(timerName);
            if (timerName.length() > pc.longestCounterName) {
                pc.longestCounterName = timerName.length();
            }
            pc.counters.put(timerName, stat);
        }
        stat.startTimer();
    }

    public static void stopTimer(String timerName) {
        Stat stat = pc.counters.get(timerName);
        if (stat == null) {
            System.err.println("Counter not found (" + timerName + ")");
        } else {
            stat.stopTimer();
        }
    }

    public static void printStats() {
        for (Stat value : pc.counters.values()) {
            System.out.println(value.toString());
        }
        System.out.println("(Total time: " + ((System.currentTimeMillis() - pc.totalStartTime) / 1000d) + " seconds)");
    }
}
