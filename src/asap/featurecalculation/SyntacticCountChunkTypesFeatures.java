/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.featurecalculation;

import asap.Chunk;
import java.io.Serializable;
import asap.Instance;
import asap.PerformanceCounters;
import asap.textprocessing.TextProcessChunks;
import asap.textprocessing.TextProcessedPartKeyConsts;
import asap.textprocessing.TextProcesser;
import java.util.HashMap;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class SyntacticCountChunkTypesFeatures implements FeatureCalculator, TextProcessedPartKeyConsts, Serializable {

    private static final HashMap<Long, SyntacticCountChunkTypesFeatures> perThreadInstances = new HashMap<>();
    private final TextProcesser textProcessorDependency;

    /**
     *
     * @param t
     */
    public SyntacticCountChunkTypesFeatures(Thread t) {
        textProcessorDependency = TextProcessChunks.getTextProcessChunks(t);
        perThreadInstances.put(t.getId(), this);
    }

    /**
     *
     */
    public SyntacticCountChunkTypesFeatures() {
        this(Thread.currentThread());
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
        return new SyntacticCountChunkTypesFeatures(t);
    }

    /**
     *
     * @param i
     * @return
     */
    @Override
    public boolean textProcessingDependenciesMet(Instance i) {
        return i.isProcessed(textProcessorDependency);
    }

    /**
     *
     * @param i
     */
    @Override
    public void calculate(Instance i) {
        if (!textProcessingDependenciesMet(i)) {
            textProcessorDependency.process(i);
        }
        PerformanceCounters.startTimer("calculate SyntacticCountChunkTypes");

//        java.lang.System.out.println("calculating " + Arrays.toString(getFeatureNames())
//                + " for instance " + i.getAttributeAt(0));
        int sentence1Counters[];
        int sentence2Counters[];
        Object o;

        o = i.getProcessedTextPart(sentence1Chunks);
        if (!(o instanceof Chunk[])) {
            return;
        }
        sentence1Counters = countNpsVpsPPs((Chunk[]) o);

        o = i.getProcessedTextPart(sentence2Chunks);
        if (!(o instanceof Chunk[])) {
            return;
        }

        sentence2Counters = countNpsVpsPPs((Chunk[]) o);
        double[] features = {Math.abs(sentence1Counters[0] - sentence2Counters[0]),
            Math.abs(sentence1Counters[1] - sentence2Counters[1]),
            Math.abs(sentence1Counters[2] - sentence2Counters[2])};

        i.addAtribute(features);

//        java.lang.System.out.println("Completed adding " + Arrays.toString(getFeatureNames()));
        PerformanceCounters.stopTimer("calculate SyntacticCountChunkTypes");
    }

    /**
     *
     * @return
     */
    @Override
    public String[] getFeatureNames() {
        String r[] = {"syn_count_dif_NPs",
            "syn_count_dif_VPs",
            "syn_count_dif_PPs"};

        return r;
    }

    private int[] countNpsVpsPPs(Chunk[] chunks) {
        int[] counters = new int[3];

        for (Chunk chunk : chunks) {
            if (chunk.getChunkType().equalsIgnoreCase("NP")) {
                counters[0]++;
            } else if (chunk.getChunkType().equalsIgnoreCase("VP")) {
                counters[1]++;
            } else if (chunk.getChunkType().equalsIgnoreCase("PP")) {
                counters[2]++;
            }
        }
        return counters;
    }

    @Override
    public String toString() {
        return "SyntacticCountChunkTypesFeatures (3 features)";
    }

}
