/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.featurecalculation;

import asap.Chunk;
import asap.Config;
import asap.Instance;
import asap.PerformanceCounters;
import asap.textprocessing.TextProcessChunkLemmas;
import asap.textprocessing.TextProcessChunkLemmasRemoveSRule;
import asap.textprocessing.TextProcessedPartKeyConsts;
import asap.textprocessing.TextProcesser;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class SemanticSimilarityAndRelatednessCalculator implements FeatureCalculator, TextProcessedPartKeyConsts {

    private static final HashMap<Long, SemanticSimilarityAndRelatednessCalculator> perThreadInstances = new HashMap<>();

    private final double MAX_VALUE = 100;

    private final TextProcesser textProcesserDependency;
    private static ILexicalDatabase db = new NictWordNet();

    private final LinkedList<edu.cmu.lti.ws4j.RelatednessCalculator> relatednessCalculators;
    private final HashMap<RelatednessCalculator, HashMap<String, Calculation>> previousCalculations;

    /**
     *
     * @param t
     */
    public SemanticSimilarityAndRelatednessCalculator(Thread t) {
        PerformanceCounters.startTimer("SemanticSimilarityAndRelatednessCalculator");
        if (Config.useDBPediaLemmaLookup()) {
            this.textProcesserDependency = TextProcessChunkLemmasRemoveSRule
                    .getTextProcessChunkLemmasRemoveSRule(t);
        } else {
            this.textProcesserDependency
                    = TextProcessChunkLemmas.getTextProcessChunkLemmas(t);
        }
        relatednessCalculators = new LinkedList<>();

        previousCalculations = new HashMap<>();
        perThreadInstances.put(t.getId(), this);
        PerformanceCounters.stopTimer("SemanticSimilarityAndRelatednessCalculator");
    }

    /**
     *
     */
    public SemanticSimilarityAndRelatednessCalculator() {
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
        return new SemanticSimilarityAndRelatednessCalculator(t);
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
        if (relatednessCalculators.isEmpty()) {
            addCalculators();
        }

        PerformanceCounters.startTimer("calculate SemanticSimilarityAndRelatedness");

//        System.out.println("calculating " + Arrays.toString(getFeatureNames())
//                + " for instance " + i.getAttributeAt(0));
        Object o;
        Chunk[] s1Chunks, s2Chunks;
        String[] s1Lemmas, s2Lemmas;
        List<LemmaSimilarity> lemmaPairSimilarityValues;
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

        o = i.getProcessedTextPart(sentence1ChunkLemmas);
        if (!(o instanceof String[])) {
            return;
        }
        s1Lemmas = (String[]) o;

        o = i.getProcessedTextPart(sentence2ChunkLemmas);
        if (!(o instanceof String[])) {
            return;
        }
        s2Lemmas = (String[]) o;

        for (RelatednessCalculator relatednessCalculator : relatednessCalculators) {
            lemmaPairSimilarityValues = calculateLemmaPairSimilarityValues(s1Chunks, s2Chunks, getEqualChunkPairs(s1Chunks, s2Chunks), relatednessCalculator);
            //System.out.println("lemma pair similarities calculated");

            resolveConflicts(lemmaPairSimilarityValues);
            //System.out.println("best lemma pairs determined");

            i.addAtribute(getTotalSimilarityValueOf(lemmaPairSimilarityValues));

        }

//        System.out.println("Completed adding " + Arrays.toString(getFeatureNames()));
        PerformanceCounters.stopTimer("calculate SemanticSimilarityAndRelatedness");
    }

    //maybe should be another TextProcesser?
    private List<Entry<Integer, Integer>> getEqualChunkPairs(Chunk[] s1Chunks, Chunk[] s2Chunks) {
        PerformanceCounters.startTimer("getEqualChunkPairs()");
        LinkedList<Entry<Integer, Integer>> pairList
                = new LinkedList<>();

        for (int i = 0; i < s1Chunks.length; i++) {
            Chunk sentence1Chunk = s1Chunks[i];
            if (sentence1Chunk.getChunkType().equalsIgnoreCase("NP")
                    || sentence1Chunk.getChunkType().equalsIgnoreCase("VP")) {
                for (int j = 0; j < s2Chunks.length; j++) {
                    Chunk sentence2Chunk = s2Chunks[j];
                    if (sentence1Chunk.getChunkType().equals(sentence2Chunk.getChunkType())
                            && !(sentence1Chunk.getLemma() == null ? true
                                    : sentence1Chunk.getLemma().isEmpty())
                            && !(sentence2Chunk.getLemma() == null ? true
                                    : sentence2Chunk.getLemma().isEmpty())) {
                        pairList.add(new Entry<>(i, j));
                    }
                }
            }
        }

        PerformanceCounters.stopTimer("getEqualChunkPairs()");
        return pairList;
    }

    private void resolveConflicts(List<LemmaSimilarity> lemmaPairSimilarityValues) {
        PerformanceCounters.startTimer("resolveConflicts()");
        LemmaSimilarity[] lemmaPairSimilarityValuesArray;

        Collections.sort(lemmaPairSimilarityValues);

        //TODO: if slow, there must be a better way than always converting the whole collection to array...
        for (int i = 0; i + 1 < lemmaPairSimilarityValues.size(); i++) {
            lemmaPairSimilarityValuesArray = lemmaPairSimilarityValues.toArray(new LemmaSimilarity[lemmaPairSimilarityValues.size()]);

            LemmaSimilarity lemmaPairSimilarityValue = lemmaPairSimilarityValuesArray[i];

            Iterator<LemmaSimilarity> it = lemmaPairSimilarityValues.subList(i + 1, lemmaPairSimilarityValues.size()).iterator();

            while (it.hasNext()) {
                LemmaSimilarity lemmaSimilarity = it.next();
                if (lemmaSimilarity.isEither(lemmaPairSimilarityValue.getSentence1ChunkIndex(),
                        lemmaPairSimilarityValue.getSentence2ChunkIndex())) {
                    it.remove();
                }
            }
        }
        PerformanceCounters.stopTimer("resolveConflicts()");
    }

    /**
     *
     * @return
     */
    @Override
    public String[] getFeatureNames() {
        List<String> featureNamesList = new LinkedList<>();

        for (RelatednessCalculator relatednessCalculator : relatednessCalculators) {
            String rcName = relatednessCalculator.getClass().getSimpleName();
            featureNamesList.add(rcName + "_sum_max");
            featureNamesList.add(rcName + "_lin_norm");
            featureNamesList.add(rcName + "_log_norm");
        }

        String[] featureNames = featureNamesList.toArray(new String[featureNamesList.size()]);

        return featureNames;
    }

    private double[] getValueSum(List<LemmaSimilarity> lemmasPairSimilarityValues) {
        return getValueSum(lemmasPairSimilarityValues, 1000000000d);
    }

    private double[] getValueSum(List<LemmaSimilarity> lemmaPairSimilarityValues, double max) {
        PerformanceCounters.startTimer("getValueSum()");

        double sum = 0d, linNormalizedSum = 0d, logNormalizedSum = 0d, current;

        for (LemmaSimilarity lemmaPairSimilarityValue : lemmaPairSimilarityValues) {
            current = lemmaPairSimilarityValue.getSimilarityValue();

            //ignore exact matches:
            if (Config.ignoreExactMatchesSemanticSimilarityAndRelatednessCalculatorPreSum()
                    && current == Double.MAX_VALUE) {
                continue;
            }
            //avoid values greater than 'max':
            if (Config.limitSemanticSimilarityAndRelatednessCalculatorPreSum()
                    && current >= Config.getSemanticSimilarityAndRelatednessCalculatorPreSumValueLimit()) {
                current = Config.getSemanticSimilarityAndRelatednessCalculatorPreSumValueLimit();

            }
            sum += current;
            linNormalizedSum += current / max;
            logNormalizedSum += Math.log(current) / Math.log(max);
        }
        double[] ret = {sum, linNormalizedSum, logNormalizedSum};

        //always avoid infinity so we don't get any errors in post-processing:
        for (int i = 0; i < ret.length; i++) {
            double s = ret[i];
            if (ret[i] > Double.MAX_VALUE) {
                ret[i] = Double.MAX_VALUE;
            }
        }
        //avoid values greater than 'max':
        if (Config.limitSemanticSimilarityAndRelatednessCalculatorPostSum()) {

            for (int i = 0; i < ret.length; i++) {
                double s = ret[i];
                if (ret[i] > Config.getSemanticSimilarityAndRelatednessCalculatorPostSumValueLimit()) {
                    ret[i] = Config.getSemanticSimilarityAndRelatednessCalculatorPostSumValueLimit();
                }
            }
        }
        PerformanceCounters.stopTimer("getValueSum()");
        return ret;
    }

    private List<LemmaSimilarity> calculateLemmaPairSimilarityValues(Chunk[] s1Chunks,
            Chunk[] s2Chunks, List<Entry<Integer, Integer>> chunkPairs,
            RelatednessCalculator relatednessCalculator) {

        PerformanceCounters.startTimer("calculateLemmaPairSimilarityValues()");
        List<LemmaSimilarity> lemmaSimilarities
                = new ArrayList<>();

        for (Entry<Integer, Integer> chunkPair : chunkPairs) {
            LemmaSimilarity lemmaSimilarity
                    = new LemmaSimilarity(chunkPair.getKey(),
                            chunkPair.getValue(),
                            getSimilarityValueOf(relatednessCalculator,
                                    s1Chunks[chunkPair.getKey()].getLemma(),
                                    s2Chunks[chunkPair.getValue()].getLemma()
                            ));

            if (lemmaSimilarity.getSimilarityValue() > 0) {
                lemmaSimilarities.add(lemmaSimilarity);
            }
        }

        PerformanceCounters.stopTimer("calculateLemmaPairSimilarityValues()");
        return lemmaSimilarities;
    }

    /**
     *
     * @param relatednessCalculator
     * @param s1ChunkLemma
     * @param s2ChunkLemma
     * @return
     */
    protected double getSimilarityValueOf(
            RelatednessCalculator relatednessCalculator, String s1ChunkLemma,
            String s2ChunkLemma) {
        PerformanceCounters.startTimer("getSimilarityValueOf()");

        String key = s1ChunkLemma + " - " + s2ChunkLemma;
        if (previousCalculations.get(relatednessCalculator).containsKey(key)) {
            PerformanceCounters.stopTimer("getSimilarityValueOf()");
            return previousCalculations.get(relatednessCalculator).get(key).getResult();
        }

        Calculation calc = new Calculation(relatednessCalculator, s1ChunkLemma, s2ChunkLemma);
        previousCalculations.get(relatednessCalculator).put(key, calc);
        PerformanceCounters.stopTimer("getSimilarityValueOf()");
        return calc.getResult();
    }

    /**
     *
     * @param values
     * @return
     */
    protected double[] getTotalSimilarityValueOf(List<LemmaSimilarity> values) {
        return getValueSum(values);
    }

    /**
     *
     */
    protected void addCalculators() {
        PerformanceCounters.startTimer("addCalculators()");
        relatednessCalculators.add(new edu.cmu.lti.ws4j.impl.JiangConrath(db));
        relatednessCalculators.add(new edu.cmu.lti.ws4j.impl.Lesk(db));
        relatednessCalculators.add(new edu.cmu.lti.ws4j.impl.Resnik(db));

        for (RelatednessCalculator relatednessCalculator : relatednessCalculators) {
            previousCalculations.put(relatednessCalculator, new HashMap<String, Calculation>());
        }
        PerformanceCounters.stopTimer("addCalculators()");

    }

    private class Calculation {

        private final RelatednessCalculator relatednessCalculator;
        private final String s1;
        private final String s2;
        private final double result;

        public Calculation(RelatednessCalculator relatednessCalculator, String s1, String s2) {
            this.relatednessCalculator = relatednessCalculator;
            this.s1 = s1;
            this.s2 = s2;
            result = relatednessCalculator.calcRelatednessOfWords(s1, s2);
        }

        public double getResult() {
            return result;
        }

        @Override
        public int hashCode() {
            return (s1 + " - " + s2).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Calculation other = (Calculation) obj;
            if (!Objects.equals(this.relatednessCalculator, other.relatednessCalculator)) {
                return false;
            }
            if (!Objects.equals(this.s1, other.s1)) {
                return false;
            }
            return Objects.equals(this.s2, other.s2);
        }

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

    /**
     *
     */
    protected class LemmaSimilarity implements Comparable<LemmaSimilarity> {

        private final int sentence1ChunkIndex;
        private final int sentence2ChunkIndex;
        private final double similarityValue;

        /**
         *
         * @param sentence1ChunkIndex
         * @param sentence2ChunkIndex
         * @param similarityValue
         */
        public LemmaSimilarity(int sentence1ChunkIndex, int sentence2ChunkIndex, double similarityValue) {
            this.sentence1ChunkIndex = sentence1ChunkIndex;
            this.sentence2ChunkIndex = sentence2ChunkIndex;
            this.similarityValue = similarityValue;
        }

        @Override
        public int compareTo(LemmaSimilarity o) {
            return Double.compare(o.similarityValue, similarityValue);
        }

        /**
         *
         * @param sentence1ChunkIndex
         * @param sentence2ChunkIndex
         * @return
         */
        public boolean isEither(int sentence1ChunkIndex, int sentence2ChunkIndex) {
            return this.sentence1ChunkIndex == sentence1ChunkIndex
                    || this.sentence2ChunkIndex == sentence2ChunkIndex;
        }

        /**
         *
         * @return
         */
        public int getSentence1ChunkIndex() {
            return sentence1ChunkIndex;
        }

        /**
         *
         * @return
         */
        public int getSentence2ChunkIndex() {
            return sentence2ChunkIndex;
        }

        /**
         *
         * @return
         */
        public double getSimilarityValue() {
            return similarityValue;
        }

    }

    @Override
    public String toString() {
        return "SemanticSimilarityAndRelatednessCalculator (" + 3 * relatednessCalculators.size() + " features)";
    }

}
