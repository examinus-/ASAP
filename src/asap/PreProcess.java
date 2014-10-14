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
public class PreProcess {

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
        pp.loadSentencePairs(args[0]);
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

    public void loadSentencePairs(String filename) {
        PerformanceCounters.startTimer("loadSentencePairs");
        int i = 0;
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

                    for (String word : attributes[1].split(" ")) {
                        s1Words.add(word);
                        incrementWordFrequency(word);
                    }
                    for (String word : attributes[2].split(" ")) {
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
                    
                    instances.add(new Instance(attributes[1], attributes[2],
                            pair_ID, relatedness_groundtruth,
                            s1Words, s2Words));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Loaded " + i + " instances from " + filename);
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
            sb.append("@attribute 'relatedness_groundtruth' numeric\n\n@data\n");
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
        
        featureCalculators[0] = new LexicalCountWords("src/stopword-list.txt", "sw");
        featureCalculatorsClone[0] = new LexicalCountWords("src/stopword-list.txt", "sw");
        featureCalculators[1] = new LexicalOverlapFeaturesCalculator(0, 3);
        featureCalculatorsClone[1] = new LexicalOverlapFeaturesCalculator(0, 3);
        featureCalculators[2] = new SyntacticCountChunkTypesFeatures();
        featureCalculatorsClone[2] = new SyntacticCountChunkTypesFeatures();
        featureCalculators[3] = new SemanticSimilarityAndRelatednessCalculator();
        featureCalculatorsClone[3] = new SemanticSimilarityAndRelatednessCalculator();
        
        
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

    public void preProcessFile(String inputFilename, String preprocessedFilename) {
        loadSentencePairs(inputFilename);
        calculateFeatures();
        saveArffDataSet(preprocessedFilename);
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
                System.out.println("calculating topDown for instance@" + topDown);
                Instance currentInstance = it.next();
                try {
                    for (FeatureCalculator featureCalculator : featureCalculators) {
                        featureCalculator.calculate(currentInstance);
                    }

                } catch (Exception e) {

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
                System.out.println("calculating bottomUp for instance@" + bottomUp);

                Instance currentInstance = it.next();
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
