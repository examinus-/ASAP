/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import edu.cmu.lti.ws4j.WS4J;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SemanticSimilarityAndRelatednessCalculator implements FeatureCalculator, TextProcessedPartKeyConsts {

    private final double MAX_VALUE = 100;

    private static final TextProcesser textProcesserDependency = new TextProcessChunkLemmas();

    @Override
    public boolean textProcessingDependenciesMet(Instance i) {
        return i.isProcessed(textProcesserDependency);
    }

    @Override
    public void calculate(Instance i) {
        if (!textProcessingDependenciesMet(i)) {
            textProcesserDependency.process(i);
        }
        PerformanceCounters.startTimer("calculate SemanticSimilarityAndRelatedness");
        
//        System.out.println("calculating " + Arrays.toString(getFeatureNames())
//                + " for instance " + i.getAttributeAt(0));
        
        Object o;
        Chunk[] s1Chunks, s2Chunks;
        String[] s1Lemmas, s2Lemmas;
        TreeSet<LemmaSimilarity> lemmaPairSimilarityValues;
        o = i.getProcessedTextPart(sentence1Chunks);
        if (!(o instanceof Chunk[]))
            return;
        s1Chunks = (Chunk[]) o;
        
        o = i.getProcessedTextPart(sentence2Chunks);
        if (!(o instanceof Chunk[]))
            return;
        s2Chunks = (Chunk[]) o;
        
        
        o = i.getProcessedTextPart(sentence1ChunkLemmas);
        if (!(o instanceof String[]))
            return;
        s1Lemmas = (String[])o;
        
        o = i.getProcessedTextPart(sentence2ChunkLemmas);
        if (!(o instanceof String[]))
            return;
        s2Lemmas = (String[])o;
        
        
        lemmaPairSimilarityValues = calculateLemmaPairSimilarityValues(s1Lemmas, s2Lemmas, getEqualChunkPairs(s1Chunks, s2Chunks));
        //System.out.println("lemma pair similarities calculated");

        resolveConflicts(lemmaPairSimilarityValues);
        //System.out.println("best lemma pairs determined");

        i.addAtribute(getTotalSimilarityValueOf(lemmaPairSimilarityValues));
        //System.out.println("added max JCN sum measure");
        
//        System.out.println("Completed adding " + Arrays.toString(getFeatureNames()));
        PerformanceCounters.stopTimer("calculate SemanticSimilarityAndRelatedness");
    }

    //maybe should be another TextProcesser?
    private List<Entry<Integer, Integer>> getEqualChunkPairs(Chunk[] s1Chunks, Chunk[] s2Chunks) {
        LinkedList<Entry<Integer, Integer>> pairList
                = new LinkedList<>();

        for (int i = 0; i < s1Chunks.length; i++) {
            Chunk sentence1Chunk = s1Chunks[i];
            if (sentence1Chunk.getChunkType().equalsIgnoreCase("NP")
                    || sentence1Chunk.getChunkType().equalsIgnoreCase("VP")) {
                for (int j = 0; j < s2Chunks.length; j++) {
                    Chunk sentence2Chunk = s2Chunks[j];
                    if (sentence1Chunk.getChunkType().equals(sentence2Chunk.getChunkType())) {
                        pairList.add(new Entry<>(i, j));
                    }
                }
            }
        }

        return pairList;
    }

    private void resolveConflicts(TreeSet<LemmaSimilarity> lemmaPairSimilarityValues) {
        LemmaSimilarity[] lemmaSimilarities;

        //TODO: if slow, there must be a better way than always converting the whole collection to array...
        for (int i = 0; i + 1 < lemmaPairSimilarityValues.size(); i++) {
            lemmaSimilarities = lemmaPairSimilarityValues.toArray(new LemmaSimilarity[lemmaPairSimilarityValues.size()]);

            LemmaSimilarity lemmaPairSimilarityValue = lemmaSimilarities[i];

            Iterator<LemmaSimilarity> it = lemmaPairSimilarityValues.tailSet(lemmaSimilarities[i + 1]).iterator();

            while (it.hasNext()) {
                LemmaSimilarity lemmaSimilarity = it.next();
                if (lemmaSimilarity.isEither(lemmaPairSimilarityValue.getSentence1ChunkIndex(),
                        lemmaPairSimilarityValue.getSentence2ChunkIndex())) {
                    it.remove();
                }
            }
        }
    }

    @Override
    public String[] getFeatureNames() {
        String[] featureNames = {"JCN_sum_max"};
        return featureNames;
    }


    private double getJCNValueOf(String wordA, String wordB) {
        double value = WS4J.runJCN(wordA, wordB);
        //System.out.println("jcn(" + wordA + "," + wordB + ")=" + value);
        return value;
    }

    private double getJCNValueSum(Set<LemmaSimilarity> lemmaPairSimilarityValues) {
        double sum = 0d;
        for (LemmaSimilarity lemmaPairSimilarityValue : lemmaPairSimilarityValues) {
            sum += lemmaPairSimilarityValue.getSimilarityValue();
        }
        //Check what to do in this case::
        if (sum >= MAX_VALUE) {
            return 1000;
        }

        return sum;
    }

    private TreeSet<LemmaSimilarity> calculateLemmaPairSimilarityValues(String[] s1ChunkLemmas, 
            String[] s2ChunkLemmas, List<Entry<Integer, Integer>> chunkPairs) {
        TreeSet<LemmaSimilarity> lemmaSimilarities
                = new TreeSet<>();
        
        for (Entry<Integer, Integer> chunkPair : chunkPairs) {
            LemmaSimilarity lemmaSimilarity
                    = new LemmaSimilarity(chunkPair.getKey(),
                            chunkPair.getValue(),
                            getSimilarityValueOf(
                                    s1ChunkLemmas[chunkPair.getKey()],
                                    s2ChunkLemmas[chunkPair.getValue()]
                            ));

            if (lemmaSimilarity.getSimilarityValue() > 0) {
                lemmaSimilarities.add(lemmaSimilarity);
            }
        }

        return lemmaSimilarities;
    }

    protected double getSimilarityValueOf(String s1ChunkLemma, String s2ChunkLemma) {
        return getJCNValueOf(s1ChunkLemma, s2ChunkLemma);
    }
    
    protected double getTotalSimilarityValueOf(Set<LemmaSimilarity> values) {
        return getJCNValueSum(values);
    }

    
    
    
    
    private class Entry<K, V> implements Map.Entry {

        private final K key;
        private final V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    protected class LemmaSimilarity implements Comparable<LemmaSimilarity> {

        private final int sentence1ChunkIndex;
        private final int sentence2ChunkIndex;
        private final double similarityValue;

        public LemmaSimilarity(int sentence1ChunkIndex, int sentence2ChunkIndex, double similarityValue) {
            this.sentence1ChunkIndex = sentence1ChunkIndex;
            this.sentence2ChunkIndex = sentence2ChunkIndex;
            this.similarityValue = similarityValue;
        }

        @Override
        public int compareTo(LemmaSimilarity o) {
            return Double.compare(o.similarityValue, similarityValue);
        }

        public boolean isEither(int sentence1ChunkIndex, int sentence2ChunkIndex) {
            return this.sentence1ChunkIndex == sentence1ChunkIndex
                    || this.sentence2ChunkIndex == sentence2ChunkIndex;
        }

        public int getSentence1ChunkIndex() {
            return sentence1ChunkIndex;
        }

        public int getSentence2ChunkIndex() {
            return sentence2ChunkIndex;
        }

        public double getSimilarityValue() {
            return similarityValue;
        }

    }

}
