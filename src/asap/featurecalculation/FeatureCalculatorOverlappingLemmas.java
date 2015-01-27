/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.featurecalculation;

import asap.Chunk;
import asap.Config;
import asap.Instance;
import java.io.Serializable;
import asap.PerformanceCounters;
import asap.textprocessing.TextProcessChunkLemmas;
import asap.textprocessing.TextProcessChunkLemmasWithDBPediaLookups;
import asap.textprocessing.TextProcessedPartKeyConsts;
import asap.textprocessing.TextProcesser;
import java.util.HashMap;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class FeatureCalculatorOverlappingLemmas implements FeatureCalculator, TextProcessedPartKeyConsts, Serializable {

    private static final HashMap<Thread, FeatureCalculatorOverlappingLemmas> fcols
            = new HashMap<>();

    private final TextProcesser textProcesserDependency;

    /**
     *
     * @param t
     * @return
     */
    public static FeatureCalculator getFeatureCalculatorOverlappingLemmas(Thread t) {
        if (fcols.containsKey(t)) {
            return fcols.get(t);
        }

        FeatureCalculatorOverlappingLemmas fcol = new FeatureCalculatorOverlappingLemmas();
        fcols.put(t, fcol);
        return fcol;
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public FeatureCalculator getInstance(Thread t) {
        return getFeatureCalculatorOverlappingLemmas(t);
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
        PerformanceCounters.startTimer("calculate FeatureCalculatorOverlappingLemmas");

        Object o;
        Chunk[] s1Chunks, s2Chunks;

        o = i.getProcessedTextPart(sentence1Chunks);
        if (!(o instanceof Chunk[])) {
            return;
        }
        s1Chunks = (Chunk[]) o;

        o = i.getProcessedTextPart(sentence2Chunks);
        if (!(o instanceof Chunk[])) {
            return;
        }
        s2Chunks = (Chunk[]) o;

        double[] features = {s1Chunks.length, s2Chunks.length, 0d};

        for (Chunk s1Chunk : s1Chunks) {
            String s1Lemma;
            if (s1Chunk.getLemma() == null ? true : s1Chunk.getLemma().isEmpty()) {
                s1Lemma = s1Chunk.getChunkText();
            } else {
                s1Lemma = s1Chunk.getLemma();
            }

            for (Chunk s2Chunk : s2Chunks) {
                String s2Lemma;

                if (s2Chunk.getLemma() == null ? true : s2Chunk.getLemma().isEmpty()) {
                    s2Lemma = s2Chunk.getChunkText();
                } else {
                    s2Lemma = s2Chunk.getLemma();
                }

                if (s1Lemma.equalsIgnoreCase(s2Lemma)) {
                    features[2]++;
                    break;
                }
            }
        }

        i.addAtribute(features);

        PerformanceCounters.stopTimer("calculate FeatureCalculatorOverlappingLemmas");
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
     * @return
     */
    @Override
    public String[] getFeatureNames() {
        String[] r = {
            "no_s1_lemmas",
            "no_s2_lemmas",
            "no_overlapping_lemmas",};
        return r;
    }

    /**
     *
     */
    public FeatureCalculatorOverlappingLemmas() {
        this(Thread.currentThread());
    }

    /**
     *
     * @param t
     */
    public FeatureCalculatorOverlappingLemmas(Thread t) {
        if (Config.useDBPediaLemmaLookup()) {
            this.textProcesserDependency
                    = TextProcessChunkLemmasWithDBPediaLookups.getTextProcessChunkLemmas(t);
        } else {
            this.textProcesserDependency
                    = TextProcessChunkLemmas.getTextProcessChunkLemmas(t);
        }
    }

    @Override
    public String toString() {
        return "FeatureCalculatorOverlappingLemmas (3 features)";
    }

}
