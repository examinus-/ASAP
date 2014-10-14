/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.cmu.lti.jawjaw.pobj.Link;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.pobj.Synset;
import edu.cmu.lti.jawjaw.pobj.Word;
import edu.cmu.lti.ws4j.WS4J;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class SemanticSimilarityAndRelatednessCalculator implements FeatureCalculator {

    private TreeSet<LemmaSimilarity> lemmaPairSimilarityValues;
    private ArrayList<Chunk> sentence1Chunks;
    private ArrayList<Chunk> sentence2Chunks;
    private final double MAX_VALUE = 100;

    @Override
    public void calculate(Instance i) {
        System.out.println("calculating " + Arrays.toString(getFeatureNames())
                + " for instance " + i.getAttributeAt(0));

        sentence1Chunks = readSentenceChunks(i.getSentence1Chunks());
        sentence2Chunks = readSentenceChunks(i.getSentence2Chunks());
        System.out.println("sentence chunks parsed.");

        i.setSentence1ChunkLemmas(lemmatizeSentenceChunks(sentence1Chunks));
        i.setSentence2ChunkLemmas(lemmatizeSentenceChunks(sentence2Chunks));
        System.out.println("Lemmas found.");

        lemmaPairSimilarityValues = calculateLemmaPairSimilarityValues(i);
        System.out.println("lemma pair similarities calculated");

        resolveConflicts();
        System.out.println("best lemma pairs determined");

        i.addAtribute(getJCNValueSum());
        System.out.println("added max JCN sum measure");
    }

    private ArrayList<Chunk> readSentenceChunks(String[] sentenceChunks) {
        ArrayList<Chunk> chunks = new ArrayList<>();

        for (String sentenceChunk : sentenceChunks) {
            int spaceIndex = sentenceChunk.indexOf(" ");
            String sentenceChunkType = sentenceChunk.substring(0, spaceIndex);
            String sentenceChunkWords = sentenceChunk.substring(spaceIndex + 1);
            chunks.add(new Chunk(sentenceChunkType, sentenceChunkWords));

        }
        System.out.println("chunks = " + Arrays.toString(chunks.toArray()));
        return chunks;
    }

    private List<Entry<Integer, Integer>> getChunkPairs() {
        LinkedList<Entry<Integer, Integer>> pairList
                = new LinkedList<>();

        for (int i = 0; i < sentence1Chunks.size(); i++) {
            Chunk sentence1Chunk = sentence1Chunks.get(i);
            if (sentence1Chunk.chunkType.equalsIgnoreCase("NP")
                    || sentence1Chunk.chunkType.equalsIgnoreCase("VP")) {
                for (int j = 0; j < sentence2Chunks.size(); j++) {
                    Chunk sentence2Chunk = sentence2Chunks.get(j);
                    if (sentence1Chunk.getChunkType().equals(sentence2Chunk.getChunkType())) {
                        pairList.add(new Entry<>(i, j));
                    }
                }
            }
        }

        return pairList;
    }

    private void resolveConflicts() {
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

    private String[] lemmatizeSentenceChunks(List<Chunk> sentenceChunks) {
        LinkedList<String> chunkLemmas = new LinkedList<>();
        String lemma;

        for (Chunk sentenceChunk : sentenceChunks) {
            lemma = lemmatizeChunk(sentenceChunk);
            if (lemma != null) {
                chunkLemmas.add(lemma);
            }
        }

        String[] tmp = new String[chunkLemmas.size()];
        tmp = chunkLemmas.toArray(tmp);
        System.out.println("sentenceChunkLemmas = " + Arrays.toString(tmp));
        return tmp;
    }

    @Override
    public String[] getFeatureNames() {
        String[] featureNames = {"JCN_sum_max"};
        return featureNames;
    }

    private String lemmatizeChunk(Chunk chunk) {
        String lemma = chunk.getChunkText();
        String validLemma = WNCheckValid(lemma, chunk.toPOS());
        while (validLemma == null) {
            int indexOfSpace = lemma.indexOf(" ");
            if (indexOfSpace < 0) {
                return "";
            }
            lemma = lemma.substring(indexOfSpace + 1);

            validLemma = WNCheckValid(lemma, chunk.toPOS());
        }
        System.out.println("Chunk's (" + chunk.getChunkText()
                + ") lemma is:" + validLemma);
        return validLemma;
    }

    private String WNCheckValid(String lemma, POS pos) {
        //USE ws4J:
        List<Synset> synsetList = edu.cmu.lti.jawjaw.util.WordNetUtil.wordToSynsets(lemma, pos);
        
        ArrayList<Word> words = new ArrayList<Word>();
        for (Synset synset : synsetList) {
            words.addAll(edu.cmu.lti.jawjaw.util.WordNetUtil.synsetToWords(synset.getSynset()));
        }
        Iterator<Word> it = words.iterator();
        
        while (it.hasNext()) {
            if (it.next().getLang().equals(Lang.jpn)) {
                it.remove();
            }
        }
        
        if (words.isEmpty())
            return null;
        
        return words.get(0).getLemma();
        //USE JAWS:
        /*
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets;
        try {
            synsets = database.getSynsets(lemma);
        } catch (WordNetException wne) {
            return null;
        }
        return Arrays.toString(synsets);
        */
    }

    private double getJCNValueOf(String wordA, String wordB) {
        double value = WS4J.runJCN(wordA, wordB);
        System.out.println("jcn(" + wordA + "," + wordB + ")=" + value);
        return value;
    }

    private double getJCNValueSum() {
        double sum = 0d;
        for (LemmaSimilarity lemmaPairSimilarityValue : lemmaPairSimilarityValues) {
            sum += lemmaPairSimilarityValue.getSimilarityValue();
        }
        if (sum >= MAX_VALUE) {
            return 500;
        }

        return sum;
    }

    private TreeSet<LemmaSimilarity> calculateLemmaPairSimilarityValues(Instance i) {
        TreeSet<LemmaSimilarity> lemmaSimilarities
                = new TreeSet<>();

        for (Map.Entry<Integer, Integer> chunkPair : getChunkPairs()) {
            LemmaSimilarity lemmaSimilarity
                    = new LemmaSimilarity(chunkPair.getKey(),
                            chunkPair.getValue(),
                            getJCNValueOf(
                                    i.getSentence1ChunkLemmas()[chunkPair.getKey()],
                                    i.getSentence2ChunkLemmas()[chunkPair.getValue()]
                            ));

            if (lemmaSimilarity.getSimilarityValue() > 0) {
                lemmaSimilarities.add(lemmaSimilarity);
            }
        }

        return lemmaSimilarities;
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

    private class Chunk {

        private final String chunkType;
        private final String chunkText;

        public Chunk(String chunkType, String chunkText) {
            this.chunkType = chunkType;
            this.chunkText = chunkText;
        }

        public String getChunkType() {
            return chunkType;
        }

        public String getChunkText() {
            return chunkText;
        }

        @Override
        public String toString() {
            return "[" + chunkType + " " + chunkText + "]";
        }

        private POS toPOS() {
            switch(chunkType) {
                case "NP":
                    return POS.n;
                case "VP":
                    return POS.v;
            }
            return POS.a;
        }

    }

    private class LemmaSimilarity implements Comparable<LemmaSimilarity> {

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
            return Double.compare(similarityValue, o.similarityValue);
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
