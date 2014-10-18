/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.util.Arrays;

public class SyntacticCountChunkTypesFeatures implements FeatureCalculator, TextProcessedPartKeyConsts {

    private static final TextProcesser textProcessorDependency = new TextProcessChunks();

    public SyntacticCountChunkTypesFeatures() {

    }

    @Override
    public boolean textProcessingDependenciesMet(Instance i) {
        return i.isProcessed(textProcessorDependency);
    }

    @Override
    public void calculate(Instance i) {
        if (!textProcessingDependenciesMet(i)) {
            textProcessorDependency.process(i);
        }
        PerformanceCounters.startTimer("calculate SyntacticCountChunkTypes");

//        System.out.println("calculating " + Arrays.toString(getFeatureNames())
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

//        System.out.println("Completed adding " + Arrays.toString(getFeatureNames()));
        PerformanceCounters.stopTimer("calculate SyntacticCountChunkTypes");
    }

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

}
