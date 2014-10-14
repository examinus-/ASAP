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

    static class Stat {

        String timerName;
        Long startTime;
        Long totalTime;
        Long noRuns;

        public Stat(String timerName) {
            this.timerName = timerName;
            this.startTime = 0L;
            this.totalTime = 0L;
            this.noRuns = 0L;
        }

        @Override
        public String toString() {
            Long avgTime = getAvgTime();
            if (avgTime < 60000)
                return "{" + timerName + "}\t " + noRuns + " runs\t\t "
                    + avgTime + "ms";
            
            Long minutes = avgTime / 60000;
            avgTime -= (minutes * 60000);
            
            return  "{" + timerName + "}\t " + noRuns + " runs\t\t "
                    + minutes + "minutes and " + avgTime + "ms";
        }

        public synchronized void startTimer() {
            if (startTime != 0L) {
                System.err.println("Counter had already started before! (" + timerName + ")");
            } else {
                startTime = System.currentTimeMillis();
            }
        }

        public synchronized void stopTimer() {
            if (startTime == 0L) {
                System.err.println("Counter hadn't start before! (" + timerName + ")");
            } else {
                totalTime += (System.currentTimeMillis() - startTime);
                noRuns++;
                startTime = 0L;
            }
        }

        public Long getAvgTime() {
            if (noRuns == 0)
                return Long.MAX_VALUE;
            
            return totalTime / noRuns;
        }
    }

    static PerformanceCounters pc = new PerformanceCounters();

    private PerformanceCounters() {
        counters = new HashMap<>();
        totalStartTime = System.currentTimeMillis();
    }

    public static void startTimer(String timerName) {
        Stat stat = pc.counters.get(timerName);
        if (stat == null) {
            stat = new Stat(timerName);
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
        System.out.println("(Total time: " + ((System.currentTimeMillis() - pc.totalStartTime) / 1000d) + " seconds)" );
    }
}
