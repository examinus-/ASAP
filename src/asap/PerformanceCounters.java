/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.util.HashMap;

/**
 *
 * @author exam
 */
public class PerformanceCounters {

    private final static HashMap<Long, PerformanceCounters> pcs = new HashMap<>();
    private final static long totalStartTime = System.currentTimeMillis();

    HashMap<String, Stat> counters;
    private int longestCounterName;

    static class Stat {

        private final String timerName;
        private Long startTime;
        private Long startMem;
        private Long totalTime;
        private Long noRuns;
        private Long totalMemoryUsage;
        private final PerformanceCounters pc;
        private static final Runtime runtime = Runtime.getRuntime();

        public Stat(String timerName, PerformanceCounters pc) {
            this.timerName = timerName;
            this.startTime = 0L;
            this.startMem = 0L;
            this.totalTime = 0L;
            this.noRuns = 0L;
            this.totalMemoryUsage = 0L;
            this.pc = pc;
        }

        @Override
        public String toString() {
            double avgTime = getAvgTime() / 1000000d;
            String tmp = timerName;
            while (tmp.length() < pc.longestCounterName) {
                tmp += " ";
            }
            return "{" + tmp + "}\t " + noRuns + " runs\t\t "
                    + avgTime + "ms\t" + getAvgMemoryUsage() + "b";
        }

        public synchronized void startTimer() {
            if (startTime != 0L) {
                System.err.println("Counter had already started before! (" + timerName + ")");
            } else {
                startMem = runtime.totalMemory() - runtime.freeMemory();
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
                totalMemoryUsage += (runtime.totalMemory() - runtime.freeMemory()) - startMem;
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

        public Stat add(Stat s) {
            Stat r = new Stat(timerName, pc);
            r.noRuns = this.noRuns + s.noRuns;
            r.totalTime = this.totalTime + s.totalTime;
            r.totalMemoryUsage = this.totalMemoryUsage + s.totalMemoryUsage;
            r.startTime = 0L;
            return r;
        }

    }

    private PerformanceCounters() {
        counters = new HashMap<>();
        longestCounterName = 0;
    }

    private static PerformanceCounters getThreadPC(Thread t) {
        PerformanceCounters r;
        if (pcs.containsKey(t.getId())) {
            return pcs.get(t.getId());
        }
        r = new PerformanceCounters();
        pcs.put(t.getId(), r);
        return r;
    }

    public static void startTimer(String timerName) {
        PerformanceCounters pc = getThreadPC(Thread.currentThread());
        Stat stat = pc.counters.get(timerName);
        if (stat == null) {
            stat = new Stat(timerName, pc);
            if (timerName.length() > pc.longestCounterName) {
                pc.longestCounterName = timerName.length();
            }
            pc.counters.put(timerName, stat);
        }
        stat.startTimer();
    }

    public static void stopTimer(String timerName) {
        PerformanceCounters pc = getThreadPC(Thread.currentThread());
        Stat stat = pc.counters.get(timerName);
        if (stat == null) {
            System.err.println("Counter not found (" + timerName + ")");
        } else {
            stat.stopTimer();
        }
    }

    public static void printStats() {
        HashMap<String, Stat> list = new HashMap<>();

        for (PerformanceCounters pc : pcs.values()) {
            for (Stat value : pc.counters.values()) {
                if (list.containsKey(value.timerName)) {
                    Stat valueAdded = value.add(list.get(value.timerName));
                    list.put(value.timerName, valueAdded);
                } else {
                    list.put(value.timerName, value);
                }
            }
        }
        
        for (Stat value : list.values()) {
            System.out.println(value.toString());
        }

        System.out.println("(Total time: " + ((System.currentTimeMillis() - totalStartTime) / 1000d) + " seconds)");
    }
}
