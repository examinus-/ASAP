/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * classe que recebe os pares de frases e gera as features numericas para que
 * possam ser usadas pelos classificadores
 *
 * @author exam
 */
public class PreProcess extends TextProcessHashWords {

    FeatureCalculator featureCalculators[];
    FeatureCalculator featureCalculatorsClone[];
    List<Instance> instances;
    int topDown;
    int bottomUp;

    double maxRelatedness_groundtruth, minRelatedness_groundtruth;

    private static HashMap<String, WordFrequency> wordFrequencies;
    private static TreeSet<WordFrequency> sortedWordFrequencies;

    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        if (args.length < 2) {
            return;
        }

        if (!(new File(args[0]).isFile())) {
            return;
        }

        PreProcess pp = new PreProcess();
        pp.loadSentencePairsWithGoldStandard(args[0]);
        pp.calculateFeatures();
        pp.saveDataSet(args[1]);

        if (args.length == 3) {
            pp.saveEnrichedInput(args[2]);
        }
    }

    public static void incrementWordFrequency(String word) {
        if (wordFrequencies.containsKey(word)) {
            wordFrequencies.get(word).incFrequency();
        } else {
            wordFrequencies.put(word, new WordFrequency(word));
        }
    }

    public static int getWordFrequency(String word) {
        if (wordFrequencies.containsKey(word)) {
            return wordFrequencies.get(word).getFrequency();
        }
        return 0;
    }

    public static TreeSet<String> getTopWords() {
        if (sortedWordFrequencies == null) {
            sortedWordFrequencies = new TreeSet(wordFrequencies.values());
        }

        TreeSet<String> r = new TreeSet<>();

        Iterator<WordFrequency> it = sortedWordFrequencies.iterator();

        while (it.hasNext()) {
            r.add(it.next().getWord());
        }
        return r;
    }

    public PreProcess() {
        instances = new LinkedList<>();
        wordFrequencies = new HashMap<>();

        maxRelatedness_groundtruth = Double.MIN_VALUE;
        minRelatedness_groundtruth = Double.MAX_VALUE;
    }

    public void loadSentencePairsWithSideGoldStandardFile(String pairsFilename, String gsFilename) {
        PerformanceCounters.startTimer("loadSentencePairs");
        double relatedness_groundtruth;
        HashSet<String> s1Words;
        HashSet<String> s2Words;

        try (FileInputStream fis = new FileInputStream(pairsFilename);
                FileInputStream gsFis = new FileInputStream(gsFilename)) {
            //ignore column headers:
            try (Scanner sc = new Scanner(fis);
                    Scanner gsSc = new Scanner(gsFis)) {

                /* NO COLUMN NAMES IN THE BEGGINING::
                 sc.nextLine();
                 gsSc.nextLine();
                 */
                while (sc.hasNextLine() && gsSc.hasNextLine()) {
                    s1Words = new HashSet<>();
                    s2Words = new HashSet<>();
                    String line = sc.nextLine();
                    //ignore empty lines:
                    if (line.isEmpty()) {
                        gsSc.nextLine();
                        continue;
                    }
                    //String attributes[] = line.split(" \\|\\|\\| ");
                    String attributes[] = line.split("\t");
                    //sentences word set:

                    for (String word : attributes[0].toLowerCase().split(" ")) {
                        s1Words.add(word);
                        incrementWordFrequency(word);
                    }
                    for (String word : attributes[1].toLowerCase().split(" ")) {
                        s2Words.add(word);
                        incrementWordFrequency(word);
                    }
                    relatedness_groundtruth = Double.parseDouble(gsSc.nextLine());

                    if (relatedness_groundtruth < minRelatedness_groundtruth) {
                        minRelatedness_groundtruth = relatedness_groundtruth;
                    }
                    if (relatedness_groundtruth > maxRelatedness_groundtruth) {
                        maxRelatedness_groundtruth = relatedness_groundtruth;
                    }
                    //NO pair_ID column either:
                    //int pair_ID = Integer.parseInt(attributes[0]);
                    int pair_ID = instances.size();

                    Instance i = new Instance(attributes[0].toLowerCase(), attributes[1].toLowerCase(),
                            pair_ID, relatedness_groundtruth);

                    i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence1Words, s1Words.toArray());
                    i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence2Words, s2Words.toArray());
                    i.addProcessed(this);

                    instances.add(i);

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Loaded " + instances.size() + " instances from " + pairsFilename + " with gold standard values from " + gsFilename);
        System.out.println("\tMin relatedness:" + minRelatedness_groundtruth);
        System.out.println("\tMax relatedness:" + maxRelatedness_groundtruth);
        PerformanceCounters.stopTimer("loadSentencePairs");
    }

    public void loadSentencePairsWithGoldStandard(String filename) {
        PerformanceCounters.startTimer("loadSentencePairs");
        double relatedness_groundtruth;
        HashSet<String> s1Words;
        HashSet<String> s2Words;

        try (FileInputStream fis = new FileInputStream(filename)) {
            //ignore column headers:
            try (Scanner sc = new Scanner(fis)) {
                sc.nextLine();

                while (sc.hasNextLine()) {
                    s1Words = new HashSet<>();
                    s2Words = new HashSet<>();
                    String line = sc.nextLine();
                    //ignore empty lines:
                    if (line.isEmpty()) {
                        continue;
                    }
                    //String attributes[] = line.split(" \\|\\|\\| ");
                    String attributes[] = line.split("\t");
                    //sentences word set:

                    for (String word : attributes[1].toLowerCase().split(" ")) {
                        s1Words.add(word);
                        incrementWordFrequency(word);
                    }
                    for (String word : attributes[2].toLowerCase().split(" ")) {
                        s2Words.add(word);
                        incrementWordFrequency(word);
                    }
                    relatedness_groundtruth = Double.parseDouble(attributes[3]);

                    if (relatedness_groundtruth < minRelatedness_groundtruth) {
                        minRelatedness_groundtruth = relatedness_groundtruth;
                    }
                    if (relatedness_groundtruth > maxRelatedness_groundtruth) {
                        maxRelatedness_groundtruth = relatedness_groundtruth;
                    }
                    int pair_ID = Integer.parseInt(attributes[0]);

                    Instance i = new Instance(attributes[1].toLowerCase(), attributes[2].toLowerCase(),
                            pair_ID, relatedness_groundtruth);

                    i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence1Words, s1Words.toArray());
                    i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence2Words, s2Words.toArray());
                    i.addProcessed(this);

                    instances.add(i);

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Loaded " + instances.size() + " instances from " + filename);
        System.out.println("\tMin relatedness:" + minRelatedness_groundtruth);
        System.out.println("\tMax relatedness:" + maxRelatedness_groundtruth);
        PerformanceCounters.stopTimer("loadSentencePairs");
    }

    private void saveEnrichedInput(String filename) {
        String r = "pair_ID\t";
        try (FileOutputStream fos = new FileOutputStream(filename)) {

            for (int i = 0; i < featureCalculators.length; i++) {
                FeatureCalculator featureCalculator = featureCalculators[i];
                r += featureCalculator.getFeatureNames();

                if (i + 1 < featureCalculators.length) {
                    r += "\t";
                }
            }
            r += "\trelatedness_groundtruth\tsentence1\tsentence2\tpossentence1\tpossentence2\n";

            fos.write(r.getBytes());

            for (Instance instance : instances) {
                fos.write((instance.toStringFull() + "\n").getBytes());
            }
        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Enriched dataset saved.");
    }

    private void saveArffDataSet(String filename) {
        PerformanceCounters.startTimer("saveArffDataSet");
        StringBuilder sb = new StringBuilder();
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            sb.append("@relation 'semeval_preprocessed'\n@attribute 'pair_ID' integer\n");

            for (FeatureCalculator featureCalculator : featureCalculators) {
                for (String featureName : featureCalculator.getFeatureNames()) {
                    sb.append(String.format("@attribute '%s' numeric\n", featureName));
                }
            }
            if (instances.get(0).hasGoldStandard()) {
                sb.append("@attribute 'gold_standard' numeric\n\n@data\n");
            }
            fos.write(sb.toString().getBytes());

            for (Instance instance : instances) {
                fos.write((instance.valuesToArffCsvFormat() + "\n").getBytes());
            }
        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Arff dataset saved.");
        PerformanceCounters.stopTimer("saveArffDataSet");
    }

    private void saveDataSet(String filename) {
        String r = "pair_ID\t";
        try (FileOutputStream fos = new FileOutputStream(filename)) {

            for (int i = 0; i < featureCalculators.length; i++) {
                FeatureCalculator featureCalculator = featureCalculators[i];
                for (String featureName : featureCalculator.getFeatureNames()) {
                    r += featureName;
                }

                if (i + 1 < featureCalculators.length) {
                    r += "\t";
                }
            }
            r += "\n";

            fos.write(r.getBytes());

            for (Instance instance : instances) {
                fos.write((instance.toString() + "\n").getBytes());
            }
        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Dataset saved.");
    }

    private void calculateFeatures() {
        PerformanceCounters.startTimer("calculateFeatures");
        if (instances.isEmpty()) {
            System.out.println("Nothing to calculate.");
            PerformanceCounters.stopTimer("calculateFeatures");
            return;
        }

        System.out.println("Preparing feature calculation...");
        featureCalculators = new FeatureCalculator[4];
        featureCalculatorsClone = new FeatureCalculator[4];

        //featureCalculators[0] = new LexicalCountWords("src/negative-stopword-list.txt", "nsw");
        featureCalculators[0] = new LexicalCountWords("stopword-list.txt", "sw");
        featureCalculatorsClone[0] = new LexicalCountWords("stopword-list.txt", "sw");

        featureCalculators[1] = new SemanticSimilarityAndRelatednessCalculator();
        featureCalculatorsClone[1] = new SemanticSimilarityAndRelatednessCalculator();

        featureCalculators[2] = new LexicalOverlapFeaturesCalculator(0, 3);
        featureCalculatorsClone[2] = new LexicalOverlapFeaturesCalculator(0, 3);
        featureCalculators[3] = new SyntacticCountChunkTypesFeatures();
        featureCalculatorsClone[3] = new SyntacticCountChunkTypesFeatures();

        Thread worker = new Thread(new FeatureCalculatorWorker(featureCalculators));
        Thread iWorker = new Thread(new FeatureCalculatorInvertedWorker(featureCalculatorsClone));

        System.out.println("Starting feature calculation...");
        worker.start();
        //iWorker.start();

        try {
            worker.join();
            //iWorker.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Features calculated.");
        PerformanceCounters.stopTimer("calculateFeatures");
    }

    public void preProcessFileWithGoldStandard(String inputFilename, String preprocessedFilename) {
        PerformanceCounters.startTimer("preProcessFileWithGoldStandard");
        loadSentencePairsWithGoldStandard(inputFilename);
        calculateFeatures();
        saveArffDataSet(preprocessedFilename);
        PerformanceCounters.stopTimer("preProcessFileWithGoldStandard");
    }

    public void preProcessFileWithGoldStandard(String inputPairsFilename, String inputGoldStandardFilename, String preprocessedFilename) {
        PerformanceCounters.startTimer("preProcessFileWithGoldStandard");
        loadSentencePairsWithSideGoldStandardFile(inputPairsFilename, inputGoldStandardFilename);
        calculateFeatures();
        saveArffDataSet(preprocessedFilename);
        PerformanceCounters.stopTimer("preProcessFileWithGoldStandard");
    }

    public void runBenchmark(String sentencePairsFile, String preprocessedFilename) {
        System.out.println("Running performance tests...");
        int runs = 10, i;

        for (i = 0; i < runs; i++) {
            System.out.println("\ttest iteration " + i);
            PreProcess.this.preProcessFileWithGoldStandard(sentencePairsFile, preprocessedFilename);

            instances = new LinkedList<>();
            featureCalculators = null;
            featureCalculatorsClone = null;
            bottomUp = Integer.MAX_VALUE;
            topDown = Integer.MIN_VALUE;
            wordFrequencies = new HashMap<>();

            maxRelatedness_groundtruth = Double.MIN_VALUE;
            minRelatedness_groundtruth = Double.MAX_VALUE;
        }
        System.out.println("\ttests done.");
    }

    public void runBenchmark(String sentencePairsFile, String goldStandardsFile, String preprocessedFilename) {
        System.out.println("Running performance tests...");
        int runs = 10, i;

        for (i = 0; i < runs; i++) {
            System.out.println("\ttest iteration " + i);
            preProcessFileWithGoldStandard(sentencePairsFile, goldStandardsFile, preprocessedFilename);

            instances = new LinkedList<>();
            featureCalculators = null;
            featureCalculatorsClone = null;
            bottomUp = Integer.MAX_VALUE;
            topDown = Integer.MIN_VALUE;
            wordFrequencies = new HashMap<>();

            maxRelatedness_groundtruth = Double.MIN_VALUE;
            minRelatedness_groundtruth = Double.MAX_VALUE;
        }
        System.out.println("\ttests done.");
    }

    public void preProcessFileWithoutGoldStandards(String inputFilename, String preprocessedFilename) {

        PerformanceCounters.startTimer("preProcessFileWithoutGoldStandards");
        loadSentencePairsWithoutGoldStandard(inputFilename);
        calculateFeatures();
        saveArffDataSet(preprocessedFilename);
        PerformanceCounters.stopTimer("preProcessFileWithoutGoldStandards");
    }

    private void loadSentencePairsWithoutGoldStandard(String filename) {
        PerformanceCounters.startTimer("loadSentencePairsWithoutGoldStandard");
//        double relatedness_groundtruth;
        HashSet<String> s1Words;
        HashSet<String> s2Words;

        try (FileInputStream fis = new FileInputStream(filename)) {
            //ignore column headers:
            try (Scanner sc = new Scanner(fis)) {
                sc.nextLine();

                while (sc.hasNextLine()) {
                    s1Words = new HashSet<>();
                    s2Words = new HashSet<>();
                    String line = sc.nextLine();
                    //ignore empty lines:
                    if (line.isEmpty()) {
                        continue;
                    }
                    //String attributes[] = line.split(" \\|\\|\\| ");
                    String attributes[] = line.split("\t");
                    //sentences word set:

                    for (String word : attributes[0].toLowerCase().split(" ")) {
                        s1Words.add(word);
                        incrementWordFrequency(word);
                    }
                    for (String word : attributes[1].toLowerCase().split(" ")) {
                        s2Words.add(word);
                        incrementWordFrequency(word);
                    }
//                    relatedness_groundtruth = Double.parseDouble(attributes[3]);
//
//                    if (relatedness_groundtruth < minRelatedness_groundtruth) {
//                        minRelatedness_groundtruth = relatedness_groundtruth;
//                    }
//                    if (relatedness_groundtruth > maxRelatedness_groundtruth) {
//                        maxRelatedness_groundtruth = relatedness_groundtruth;
//                    }
//                    int pair_ID = Integer.parseInt(attributes[0]);
                    int pair_ID = instances.size();

                    Instance i = new Instance(attributes[0].toLowerCase(), attributes[1].toLowerCase(),
                            pair_ID);

                    i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence1Words, s1Words.toArray());
                    i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence2Words, s2Words.toArray());
                    i.addProcessed(this);

                    instances.add(i);

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Loaded " + instances.size() + " instances from " + filename);
//        System.out.println("\tMin relatedness:" + minRelatedness_groundtruth);
//        System.out.println("\tMax relatedness:" + maxRelatedness_groundtruth);
        PerformanceCounters.stopTimer("loadSentencePairsWithoutGoldStandard");
    }

    private class FeatureCalculatorWorker implements Runnable {

        Iterator<Instance> it;
        FeatureCalculator featureCalculators[];

        public FeatureCalculatorWorker(FeatureCalculator featureCalculators[]) {
            this.it = instances.listIterator();
            this.featureCalculators = featureCalculators;
            topDown = -1;
        }

        @Override
        public void run() {
            while (it.hasNext()) {

                synchronized (PreProcess.this) {
                    topDown++;
                    if (bottomUp <= topDown) {
                        return;
                    }
                }
                Instance currentInstance = it.next();
//                System.out.println("calculating topDown for (" + currentInstance.getAttributeAt(0) + "):\n|" + currentInstance.getSentence1() + "\n|" + currentInstance.getSentence2());
                try {
                    for (FeatureCalculator featureCalculator : featureCalculators) {
                        featureCalculator.calculate(currentInstance);
                    }

                } catch (Exception ex) {
                    Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

    }

    private class FeatureCalculatorInvertedWorker implements Runnable {

        Iterator<Instance> it;
        FeatureCalculator featureCalculators[];

        public FeatureCalculatorInvertedWorker(FeatureCalculator featureCalculators[]) {
            this.it = new ListReverser<>(instances).iterator();
            this.featureCalculators = featureCalculators;
            bottomUp = instances.size();
        }

        @Override
        public void run() {
            while (it.hasNext()) {

                synchronized (PreProcess.this) {
                    bottomUp--;
                    if (bottomUp <= topDown) {
                        return;
                    }
                }
                Instance currentInstance = it.next();
//                System.out.println("calculating bottomUp for (" + currentInstance.getAttributeAt(0) + "):\n|" + currentInstance.getSentence1() + "\n|" + currentInstance.getSentence2());

                try {
                    for (FeatureCalculator featureCalculator : featureCalculators) {
                        featureCalculator.calculate(currentInstance);
                    }

                } catch (Exception e) {

                }
            }
        }
    }

    class ListReverser<T> implements Iterable<T> {

        private final ListIterator<T> listIterator;

        public ListReverser(List<T> wrappedList) {
            this.listIterator = wrappedList.listIterator(wrappedList.size());
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {

                @Override
                public boolean hasNext() {
                    return listIterator.hasPrevious();
                }

                @Override
                public T next() {
                    return listIterator.previous();
                }

                @Override
                public void remove() {
                    listIterator.remove();
                }

            };
        }

    }
}
