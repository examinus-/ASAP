/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt)
 */
public class PerformanceCounters {

    private final static HashMap<Long, PerformanceCounters> pcs = new HashMap<>();
    private final static long totalStartTime = System.currentTimeMillis();

    HashMap<String, Stat> counters;
    private int longestCounterName;

    static class Stat implements Comparable<Stat> {

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
            return String.format("{%s}\t %6s runs\t\t %10.7sms\t\t %10sb", tmp, noRuns, avgTime, getAvgMemoryUsage());
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

        @Override
        public int compareTo(Stat o) {
            return (int)(o.getAvgTime() - this.getAvgTime());
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

    /**
     *
     * @param timerName
     */
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

    /**
     *
     * @param timerName
     */
    public static void stopTimer(String timerName) {
        PerformanceCounters pc = getThreadPC(Thread.currentThread());
        Stat stat = pc.counters.get(timerName);
        if (stat == null) {
            System.err.println("Counter not found (" + timerName + ")");
        } else {
            stat.stopTimer();
        }
    }

    /**
     *
     */
    public static void printStats() {
        StringBuilder sb = new StringBuilder();
        HashMap<String, Stat> list = new HashMap<>();
        int maxLongestCounterName = 0;
        for (PerformanceCounters pc : pcs.values()) {
            if (maxLongestCounterName < pc.longestCounterName) {
                maxLongestCounterName = pc.longestCounterName;
            }
            for (Stat value : pc.counters.values()) {
                if (list.containsKey(value.timerName)) {
                    Stat valueAdded = value.add(list.get(value.timerName));
                    list.put(value.timerName, valueAdded);
                } else {
                    list.put(value.timerName, value);
                }
            }
        }

        for (PerformanceCounters pc : pcs.values()) {
            pc.longestCounterName = maxLongestCounterName;
        }

        ArrayList<Stat> stats = new ArrayList<>(list.values());
        
        Collections.sort(stats);
        
        for (Stat value : stats) {
            sb.append(value.toString())
                    .append("\n");
        }

        sb.append("(Total time: ")
                .append((System.currentTimeMillis() - totalStartTime) / 1000d)
                .append(" seconds)")
                .append("\n");

        String out = sb.toString();

        java.lang.System.out.println(out);

        try (FileOutputStream fos = new FileOutputStream(Config.getLogTimingsOutputFilename())) {
            fos.write(out.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(PerformanceCounters.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
