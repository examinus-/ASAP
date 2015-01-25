/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.featurecalculation;

import asap.Instance;
import asap.PerformanceCounters;
import asap.PreProcess;
import asap.textprocessing.TextProcessHashWords;
import asap.textprocessing.TextProcessedPartKeyConsts;
import asap.textprocessing.TextProcesser;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class LexicalOverlapFeaturesCalculator implements FeatureCalculator, TextProcessedPartKeyConsts {

    private static final HashMap<Long, LexicalOverlapFeaturesCalculator> perThreadInstances
            = new HashMap<>();
    private final int minStopWords;
    private final int maxStopWords;
    private TreeSet<String> topWords;
    private final TextProcesser textProcesserDependency;

    /**
     *
     * @param minStopWords
     * @param maxStopWords
     * @param t
     */
    public LexicalOverlapFeaturesCalculator(int minStopWords, int maxStopWords, Thread t) {
        this.minStopWords = minStopWords;
        this.maxStopWords = maxStopWords;
        this.topWords = PreProcess.getTopWords();
        textProcesserDependency = TextProcessHashWords.getTextProcessHashWords(t);

        perThreadInstances.put(t.getId(), this);
    }

    /**
     *
     * @param minStopWords
     * @param maxStopWords
     */
    public LexicalOverlapFeaturesCalculator(int minStopWords, int maxStopWords) {
        this(minStopWords, maxStopWords, Thread.currentThread());
    }

    /**
     *
     * @param i
     * @return
     */
    @Override
    public boolean textProcessingDependenciesMet(Instance i) {
        return i.isProcessed(textProcesserDependency);
    }

    /**
     *
     * @param i
     */
    @Override
    public void calculate(Instance i) {
        if (!textProcessingDependenciesMet(i)) {
            textProcesserDependency.process(i);
        }
        if (this.topWords == null) {
            this.topWords = PreProcess.getTopWords();
        }
        PerformanceCounters.startTimer("calculate LexicalOverlapFeatures");

//        System.out.println("calculating " + Arrays.toString(getFeatureNames())
//                + " for instance " + i.getAttributeAt(0));
        HashSet<String> intersection, s1, s2;
        Object o;

        o = i.getProcessedTextPart(sentence1Words);
        if (!(o instanceof Object[])) {
            //PerformanceCounters.stopTimer("calculate LexicalOverlapFeatures");
            return;
        }
        String[] s1Words = (String[]) o;

        o = i.getProcessedTextPart(sentence2Words);
        if (!(o instanceof Object[])) {
            PerformanceCounters.stopTimer("calculate LexicalOverlapFeatures");
            return;
        }
        String[] s2Words = (String[]) o;

        s1 = new HashSet<>();
        s2 = new HashSet<>();

        s1.addAll(Arrays.asList(s1Words));
        s2.addAll(Arrays.asList(s2Words));

        double overlap;

        Iterator<String> it = topWords.iterator();

        for (int k = 0; it.hasNext() && k < minStopWords; k++) {
            String word = it.next();
            s1.remove(word);
            s2.remove(word);
        }
        for (int j = minStopWords; j <= maxStopWords; j++) {
            intersection = new HashSet<>(s1);
            intersection.retainAll(s2);

            if (Math.max(s1.size(), s2.size()) == 0) {
                overlap = 1;
            } else {
                overlap = ((double) intersection.size())
                        / ((double) Math.max(s1.size(), s2.size()));
            }

            i.addAtribute(overlap);

            if (it.hasNext()) {
                String word = it.next();
                s1.remove(word);
                s2.remove(word);
            } else {
                //no more stop words to remove, speed it up:
            }
        }
//        System.out.println("Completed adding " + Arrays.toString(getFeatureNames()));
        PerformanceCounters.stopTimer("calculate LexicalOverlapFeatures");
    }

    /**
     *
     * @return
     */
    @Override
    public String[] getFeatureNames() {
        String r[] = new String[maxStopWords + 1 - minStopWords];

        for (int i = minStopWords; i <= maxStopWords; i++) {
            r[i] = "lex_overlap_rem_" + i + "sw";
        }

        return r;
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public FeatureCalculator getInstance(Thread t) {

        if (perThreadInstances.containsKey(t.getId())) {
            return perThreadInstances.get(t.getId());
        }
        return new LexicalOverlapFeaturesCalculator(minStopWords, maxStopWords, t);
    }

    @Override
    public String toString() {
        return "LexicalOverlapFeaturesCalculator (" + (1 + maxStopWords - minStopWords) + " features)";
    }

}
