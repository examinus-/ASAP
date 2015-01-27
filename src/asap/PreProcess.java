/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import asap.featurecalculation.ExtraFeatures;
import asap.featurecalculation.FeatureCalculator;
import asap.textprocessing.TextProcessHashWords;
import asap.textprocessing.TextProcessedPartKeyConsts;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * classe que recebe os pares de frases e gera as features numericas para que
 * possam ser usadas pelos classificadores
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public final class PreProcess extends TextProcessHashWords {

    private static PreProcess preprocess;

    List<FeatureCalculator> featureCalculators;
    List<Instance> instances, aux;
    List<String> inputFiles;
    List<InputFileInstances> inputFileWithInstances;

    int topDown;
    int bottomUp;

    double maxGoldStandard, minGoldStandard;

    private static HashMap<String, WordFrequency> wordFrequencies;
    private static TreeSet<WordFrequency> sortedWordFrequencies;

    /**
     *
     * @param args
     */
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

    /**
     *
     * @param word
     */
    public static void incrementWordFrequency(String word) {
        if (wordFrequencies.containsKey(word)) {
            wordFrequencies.get(word).incFrequency();
        } else {
            wordFrequencies.put(word, new WordFrequency(word));
        }
    }

    /**
     *
     * @param word
     * @return
     */
    public static int getWordFrequency(String word) {
        if (wordFrequencies.containsKey(word)) {
            return wordFrequencies.get(word).getFrequency();
        }
        return 0;
    }

    /**
     *
     * @return
     */
    public static TreeSet<String> getTopWords() {

        //TODO: CAREFULL, avoid instead:
        if (wordFrequencies == null) {
            return null;
        }
        //------------------------------

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

    /**
     *
     */
    public PreProcess() {
        instances = Collections.synchronizedList(
                new LinkedList<Instance>());
        wordFrequencies = new HashMap<>();
        inputFiles = new LinkedList<>();
        inputFileWithInstances = new LinkedList<>();

        maxGoldStandard = Double.MIN_VALUE;
        minGoldStandard = Double.MAX_VALUE;

        preprocess = this;
    }

    /**
     *
     * @param extraFeaturesFilenames
     */
    public PreProcess(List<String> extraFeaturesFilenames) {
        this();
        for (String extraFeaturesFilename : extraFeaturesFilenames) {
            loadFeaturesFromFile(extraFeaturesFilename);
        }
    }

    /**
     *
     * @param pairsFilename
     */
    public void loadSentencePairsWithoutSideGoldStandardFile(String pairsFilename) {
        PerformanceCounters.startTimer("loadSentencePairs");
        HashSet<String> s1Words = null, s1WordsCS = null;
        HashSet<String> s2Words = null, s2WordsCS = null;
        int currentNumberOfInstances = instances.size();

        InputFileInstances ifi = new InputFileInstances(pairsFilename, currentNumberOfInstances, -1);
        inputFileWithInstances.add(ifi);

        try (FileInputStream fis = new FileInputStream(pairsFilename)) {
            //ignore column headers:
            try (Scanner sc = new Scanner(fis)) {

                /* NO COLUMN NAMES IN THE BEGGINING::
                 sc.nextLine();
                 gsSc.nextLine();
                 */
                while (sc.hasNextLine()) {
                    if (Config.calculateWithCaseSensitivity()) {

                        s1WordsCS = new HashSet<>();
                        s2WordsCS = new HashSet<>();
                    }
                    if (Config.calculateWithoutCaseSensitivity()) {
                        s1Words = new HashSet<>();
                        s2Words = new HashSet<>();
                    }

                    String line = sc.nextLine();
                    //ignore empty lines:
                    if (line.isEmpty()) {
                        continue;
                    }
                    //String attributes[] = line.split(" \\|\\|\\| ");
                    String attributes[] = line.split("\t");
                    //sentences word set:

                    for (String word : attributes[0].split(" ")) {
                        if (Config.calculateWithoutCaseSensitivity()) {
                            s1Words.add(word.toLowerCase());
                        }

                        if (Config.calculateWithCaseSensitivity()) {
                            s1WordsCS.add(word);
                        }
                        incrementWordFrequency(word);
                    }
                    for (String word : attributes[1].split(" ")) {
                        if (Config.calculateWithoutCaseSensitivity()) {
                            s2Words.add(word.toLowerCase());
                        }

                        if (Config.calculateWithCaseSensitivity()) {
                            s2WordsCS.add(word);
                        }
                        incrementWordFrequency(word);
                    }

                    //NO pair_ID column either:
                    //int pair_ID = Integer.parseInt(attributes[0]);
                    int pair_ID = instances.size() + 1;

                    Instance i = new Instance(pairsFilename, attributes[0], attributes[1],
                            pair_ID);
                    if (Config.calculateWithoutCaseSensitivity()) {
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence1Words, s1Words.toArray(new String[s1Words.size()]));
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence2Words, s2Words.toArray(new String[s2Words.size()]));
                    }
                    if (Config.calculateWithCaseSensitivity()) {
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence1WordsCaseSensitive, s1WordsCS.toArray(new String[s1WordsCS.size()]));
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence2WordsCaseSensitive, s2WordsCS.toArray(new String[s2WordsCS.size()]));
                    }

                    i.addProcessed(this);

                    instances.add(i);
                    ifi.addInstance(i);

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        inputFiles.add(pairsFilename);

        System.out.println(String
                .format("Loaded %s instances from %s without gold standard values.",
                        (instances.size() - currentNumberOfInstances), pairsFilename));

        PerformanceCounters.stopTimer("loadSentencePairs");
    }

    /**
     *
     * @param pairsFilename
     * @param gsFilename
     */
    public void loadSentencePairsWithSideGoldStandardFile(String pairsFilename, String gsFilename) {
        PerformanceCounters.startTimer("loadSentencePairs");
        double relatedness_groundtruth;
        HashSet<String> s1Words = null, s1WordsCS = null;
        HashSet<String> s2Words = null, s2WordsCS = null;
        int currentNumberOfInstances = instances.size();

        InputFileInstances ifi = new InputFileInstances(pairsFilename, currentNumberOfInstances, -1);
        inputFileWithInstances.add(ifi);

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
                    if (Config.calculateWithCaseSensitivity()) {

                        s1WordsCS = new HashSet<>();
                        s2WordsCS = new HashSet<>();
                    }
                    if (Config.calculateWithoutCaseSensitivity()) {
                        s1Words = new HashSet<>();
                        s2Words = new HashSet<>();
                    }

                    String line = sc.nextLine();
                    //ignore empty lines:
                    if (line.isEmpty()) {
                        gsSc.nextLine();
                        continue;
                    }
                    //String attributes[] = line.split(" \\|\\|\\| ");
                    String attributes[] = line.split("\t");
                    //sentences word set:

                    for (String word : attributes[0].split(" ")) {
                        if (Config.calculateWithoutCaseSensitivity()) {
                            s1Words.add(word.toLowerCase());
                        }

                        if (Config.calculateWithCaseSensitivity()) {
                            s1WordsCS.add(word);
                        }
                        incrementWordFrequency(word);
                    }
                    for (String word : attributes[1].split(" ")) {
                        if (Config.calculateWithoutCaseSensitivity()) {
                            s2Words.add(word.toLowerCase());
                        }

                        if (Config.calculateWithCaseSensitivity()) {
                            s2WordsCS.add(word);
                        }
                        incrementWordFrequency(word);
                    }
                    relatedness_groundtruth = Double.parseDouble(gsSc.nextLine());

                    if (relatedness_groundtruth < minGoldStandard) {
                        minGoldStandard = relatedness_groundtruth;
                    }
                    if (relatedness_groundtruth > maxGoldStandard) {
                        maxGoldStandard = relatedness_groundtruth;
                    }
                    //NO pair_ID column either:
                    //int pair_ID = Integer.parseInt(attributes[0]);
                    int pair_ID = instances.size() + 1;

                    Instance i = new Instance(pairsFilename, attributes[0], attributes[1],
                            pair_ID, relatedness_groundtruth);

                    if (Config.calculateWithoutCaseSensitivity()) {
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence1Words, s1Words.toArray(new String[s1Words.size()]));
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence2Words, s2Words.toArray(new String[s2Words.size()]));
                    }
                    if (Config.calculateWithCaseSensitivity()) {
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence1WordsCaseSensitive, s1WordsCS.toArray(new String[s1WordsCS.size()]));
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence2WordsCaseSensitive, s2WordsCS.toArray(new String[s2WordsCS.size()]));
                    }

                    i.addProcessed(this);

                    instances.add(i);
                    ifi.addInstance(i);

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        inputFiles.add(pairsFilename);

        System.out.println(String.format("Loaded %s instances from %s with gold standard values from %s", instances.size() - currentNumberOfInstances, pairsFilename, gsFilename));
        System.out.println("\tMin relatedness:" + minGoldStandard);
        System.out.println("\tMax relatedness:" + maxGoldStandard);
        PerformanceCounters.stopTimer("loadSentencePairs");
    }

    /**
     *
     * @param filename
     */
    public void loadSentencePairsWithGoldStandard(String filename) {
        PerformanceCounters.startTimer("loadSentencePairs");
        double relatedness_groundtruth;
        HashSet<String> s1Words = null, s1WordsCS = null;
        HashSet<String> s2Words = null, s2WordsCS = null;
        int currentNumberOfInstances = instances.size();

        InputFileInstances ifi = new InputFileInstances(filename, currentNumberOfInstances, -1);
        inputFileWithInstances.add(ifi);

        try (FileInputStream fis = new FileInputStream(filename)) {
            try (Scanner sc = new Scanner(fis)) {

                while (sc.hasNextLine()) {
                    if (Config.calculateWithCaseSensitivity()) {

                        s1WordsCS = new HashSet<>();
                        s2WordsCS = new HashSet<>();
                    }
                    if (Config.calculateWithoutCaseSensitivity()) {
                        s1Words = new HashSet<>();
                        s2Words = new HashSet<>();
                    }

                    String line = sc.nextLine();
                    String attributes[] = line.split("\t");
                    //ignore empty lines:
                    if (line.isEmpty()) {
                        continue;

                    } else //ignore lines that don't start with a number (like headers): 
                    if (!attributes[0].matches("\\d+")) {
                        continue;
                    }

                    //sentences word set:
                    for (String word : attributes[1].split(" ")) {
                        if (Config.calculateWithoutCaseSensitivity()) {
                            s1Words.add(word.toLowerCase());
                        }

                        if (Config.calculateWithCaseSensitivity()) {
                            s1WordsCS.add(word);
                        }
                        incrementWordFrequency(word);
                    }
                    for (String word : attributes[2].split(" ")) {
                        if (Config.calculateWithoutCaseSensitivity()) {
                            s2Words.add(word.toLowerCase());
                        }

                        if (Config.calculateWithCaseSensitivity()) {
                            s2WordsCS.add(word);
                        }
                        incrementWordFrequency(word);
                    }
                    relatedness_groundtruth = Double.parseDouble(attributes[3]);

                    if (relatedness_groundtruth < minGoldStandard) {
                        minGoldStandard = relatedness_groundtruth;
                    }
                    if (relatedness_groundtruth > maxGoldStandard) {
                        maxGoldStandard = relatedness_groundtruth;
                    }
                    int pair_ID = Integer.parseInt(attributes[0]);

                    Instance i = new Instance(filename, attributes[1], attributes[2],
                            pair_ID, relatedness_groundtruth);

                    if (Config.calculateWithoutCaseSensitivity()) {
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence1Words, s1Words.toArray(new String[s1Words.size()]));
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence2Words, s2Words.toArray(new String[s2Words.size()]));
                    }
                    if (Config.calculateWithCaseSensitivity()) {
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence1WordsCaseSensitive, s1WordsCS.toArray(new String[s1WordsCS.size()]));
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence2WordsCaseSensitive, s2WordsCS.toArray(new String[s2WordsCS.size()]));
                    }

                    i.addProcessed(this);

                    instances.add(i);
                    ifi.addInstance(i);

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        inputFiles.add(filename);

        System.out.println(String.format("Loaded %s instances from %s", instances.size() - currentNumberOfInstances, filename));
        System.out.println("\tMin relatedness:" + minGoldStandard);
        System.out.println("\tMax relatedness:" + maxGoldStandard);
        PerformanceCounters.stopTimer("loadSentencePairs");
    }

    private void loadSentencePairsWithoutGoldStandard(String filename) {
        PerformanceCounters.startTimer("loadSentencePairsWithoutGoldStandard");
//        double relatedness_groundtruth;
        HashSet<String> s1Words = null, s1WordsCS = null;
        HashSet<String> s2Words = null, s2WordsCS = null;

        InputFileInstances ifi = new InputFileInstances(filename, -1, -1);
        inputFileWithInstances.add(ifi);

        try (FileInputStream fis = new FileInputStream(filename)) {
            try (Scanner sc = new Scanner(fis)) {

                while (sc.hasNextLine()) {
                    if (Config.calculateWithCaseSensitivity()) {

                        s1WordsCS = new HashSet<>();
                        s2WordsCS = new HashSet<>();
                    }
                    if (Config.calculateWithoutCaseSensitivity()) {
                        s1Words = new HashSet<>();
                        s2Words = new HashSet<>();
                    }

                    String line = sc.nextLine();
                    String attributes[] = line.split("\t");
                    //ignore empty lines:
                    if (line.isEmpty()) {
                        continue;
                    } else //ignore lines that don't start with a number (like headers): 
                    if (!attributes[0].matches("\\d+")) {
                        continue;
                    }
                    //String attributes[] = line.split(" \\|\\|\\| ");
                    //sentences word set:

                    for (String word : attributes[0].split(" ")) {
                        if (Config.calculateWithoutCaseSensitivity()) {
                            s1Words.add(word.toLowerCase());
                        }

                        if (Config.calculateWithCaseSensitivity()) {
                            s1WordsCS.add(word);
                        }
                        incrementWordFrequency(word);
                    }
                    for (String word : attributes[1].split(" ")) {
                        if (Config.calculateWithoutCaseSensitivity()) {
                            s2Words.add(word.toLowerCase());
                        }

                        if (Config.calculateWithCaseSensitivity()) {
                            s2WordsCS.add(word);
                        }
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
                    int pair_ID = instances.size() + 1;

                    Instance i = new Instance(filename, attributes[0], attributes[1],
                            pair_ID);

                    if (Config.calculateWithoutCaseSensitivity()) {
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence1Words, s1Words.toArray(new String[s1Words.size()]));
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence2Words, s2Words.toArray(new String[s2Words.size()]));
                    }
                    if (Config.calculateWithCaseSensitivity()) {
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence1WordsCaseSensitive, s1WordsCS.toArray(new String[s1WordsCS.size()]));
                        i.addProcessedTextPart(TextProcessedPartKeyConsts.sentence2WordsCaseSensitive, s2WordsCS.toArray(new String[s2WordsCS.size()]));
                    }

                    i.addProcessed(this);

                    instances.add(i);
                    ifi.addInstance(i);

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        inputFiles.add(filename);

        System.out.println("Loaded " + instances.size() + " instances from " + filename);
//        System.out.println("\tMin relatedness:" + minRelatedness_groundtruth);
//        System.out.println("\tMax relatedness:" + maxRelatedness_groundtruth);
        PerformanceCounters.stopTimer("loadSentencePairsWithoutGoldStandard");
    }

    private void saveEnrichedInput(String filename) {
        String r = "pair_ID\t";
        try (FileOutputStream fos = new FileOutputStream(filename)) {

            for (int i = 0; i < featureCalculators.size(); i++) {
                FeatureCalculator featureCalculator = featureCalculators.get(i);
                r += featureCalculator.getFeatureNames();

                if (i + 1 < featureCalculators.size()) {
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
            sb.append("@relation 'semeval_preprocessed'\n"
                    + "@attribute 'source_file' string\n"
                    + "@attribute 'pair_ID' integer\n");

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

            for (int i = 0; i < featureCalculators.size(); i++) {
                FeatureCalculator featureCalculator = featureCalculators.get(i);
                for (String featureName : featureCalculator.getFeatureNames()) {
                    r += featureName;
                }

                if (i + 1 < featureCalculators.size()) {
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

    /**
     *
     */
    public void calculateFeatures() {
        PerformanceCounters.startTimer("calculateFeatures");
        if (instances.isEmpty()) {
            System.out.println("Nothing to calculate.");
            PerformanceCounters.stopTimer("calculateFeatures");
            return;
        }

        System.out.println("Preparing feature calculation...");

        int size = instances.size();
        double percentPerInstance = 100d / (double) size;
        double percentProcessed = 0d;
        aux = instances;
        instances = Collections.synchronizedList(new LinkedList());

        StringBuilder sb = new StringBuilder("Calculating features with:\n");

        List<List<FeatureCalculator>> featureCalculatorsLists = new LinkedList<>();
        List<Thread> featureCalculatorWorkers = new LinkedList<>();
        for (int i = 0; i < Config.getNumThreads(); i++) {
            List<FeatureCalculator> tmp = new LinkedList<>();
            if (i == 0) {
                featureCalculators = tmp;
            }
            featureCalculatorWorkers.add(
                    new Thread(
                            new FeatureCalculatorWorker(tmp)
                    )
            );
            featureCalculatorsLists.add(tmp);
        }

        for (FeatureCalculator fc : Config.getFeatureCalculators()) {
            for (int i = 0; i < featureCalculatorWorkers.size(); i++) {
                Thread featureCalculatorWorker = featureCalculatorWorkers.get(i);
                List<FeatureCalculator> featureCalculatorsList = featureCalculatorsLists.get(i);
                featureCalculatorsList.add(fc.getInstance(featureCalculatorWorker));
            }

            sb.append("\t")
                    .append(fc.toString())
                    .append("\n");
        }

        System.out.println(sb.toString());

        for (Thread featureCalculatorWorker : featureCalculatorWorkers) {
            featureCalculatorWorker.start();
        }

        for (Thread featureCalculatorWorker : featureCalculatorWorkers) {
            while (featureCalculatorWorker.isAlive()) {
                try {
                    featureCalculatorWorker.join(10000);
                    if (Config.showProgress() && featureCalculatorWorker.isAlive()) {
                        percentProcessed = (size - aux.size()) * percentPerInstance;
                        System.out.println("percentProcessed = " + percentProcessed);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        //put instances back in order:
        Collections.sort(instances);

        System.out.println("\n\tdone.");
        PerformanceCounters.stopTimer("calculateFeatures");
    }

    /**
     *
     * @param inputFilename
     * @param preprocessedFilename
     */
    public void preProcessFileWithGoldStandard(String inputFilename, String preprocessedFilename) {
        PerformanceCounters.startTimer("preProcessFileWithGoldStandard");
        loadSentencePairsWithGoldStandard(inputFilename);
        calculateFeatures();
        saveArffDataSet(preprocessedFilename);
        PerformanceCounters.stopTimer("preProcessFileWithGoldStandard");
    }

    /**
     *
     * @param inputPairsFilename
     * @param inputGoldStandardFilename
     * @param preprocessedFilename
     */
    public void preProcessFileWithGoldStandard(String inputPairsFilename, String inputGoldStandardFilename, String preprocessedFilename) {
        PerformanceCounters.startTimer("preProcessFileWithGoldStandard");
        loadSentencePairsWithSideGoldStandardFile(inputPairsFilename, inputGoldStandardFilename);
        calculateFeatures();
        saveArffDataSet(preprocessedFilename);
        PerformanceCounters.stopTimer("preProcessFileWithGoldStandard");
    }

    /**
     *
     * @param sentencePairsFile
     * @param preprocessedFilename
     */
    public void runBenchmark(String sentencePairsFile, String preprocessedFilename) {
        System.out.println("Running performance tests...");
        int runs = 10, i;

        for (i = 0; i < runs; i++) {
            System.out.println("\ttest iteration " + i);
            PreProcess.this.preProcessFileWithGoldStandard(sentencePairsFile, preprocessedFilename);

            instances = new LinkedList<>();
            featureCalculators = null;
            bottomUp = Integer.MAX_VALUE;
            topDown = Integer.MIN_VALUE;
            wordFrequencies = new HashMap<>();

            maxGoldStandard = Double.MIN_VALUE;
            minGoldStandard = Double.MAX_VALUE;
        }
        System.out.println("\ttests done.");
    }

    /**
     *
     * @param sentencePairsFile
     * @param goldStandardsFile
     * @param preprocessedFilename
     */
    public void runBenchmark(String sentencePairsFile, String goldStandardsFile, String preprocessedFilename) {
        System.out.println("Running performance tests...");
        int runs = 10, i;

        for (i = 0; i < runs; i++) {
            System.out.println("\ttest iteration " + i);
            preProcessFileWithGoldStandard(sentencePairsFile, goldStandardsFile, preprocessedFilename);

            instances = new LinkedList<>();
            featureCalculators = null;
            bottomUp = Integer.MAX_VALUE;
            topDown = Integer.MIN_VALUE;
            wordFrequencies = new HashMap<>();

            maxGoldStandard = Double.MIN_VALUE;
            minGoldStandard = Double.MAX_VALUE;
        }
        System.out.println("\ttests done.");
    }

    /**
     *
     * @param inputFilename
     * @param preprocessedFilename
     */
    public void preProcessFileWithoutGoldStandards(String inputFilename, String preprocessedFilename) {

        PerformanceCounters.startTimer("preProcessFileWithoutGoldStandards");
        loadSentencePairsWithoutGoldStandard(inputFilename);
        calculateFeatures();
        saveArffDataSet(preprocessedFilename);
        PerformanceCounters.stopTimer("preProcessFileWithoutGoldStandards");
    }

    /**
     *
     * @param filename
     */
    public void savePreprocessedFeatures(String filename) {
        PerformanceCounters.startTimer("savePreprocessedFeatures");
        StringBuilder sb = new StringBuilder();
        File f = new File(filename);
        f.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(filename)) {

            sb.append(String.format("@relation '%s'\n",
                    Arrays.toString(
                            inputFiles.toArray(new String[inputFiles.size()])
                    )
            ));

            sb.append("@attribute 'source_file' string\n");
            sb.append("@attribute 'pair_ID' integer\n");

            for (FeatureCalculator featureCalculator : featureCalculators) {
                for (String featureName : featureCalculator.getFeatureNames()) {
                    sb.append(String.format("@attribute '%s' numeric\n", featureName));
                }
            }

//            if (instances.get(0).hasGoldStandard()) {
            sb.append("@attribute 'gold_standard' numeric\n");
//            }
            sb.append("\n@data\n");

            for (Instance instance : instances) {
                sb.append((instance.valuesToArffCsvFormat() + "\n"));
            }

            fos.write(sb.toString().getBytes());

        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        PerformanceCounters.stopTimer("savePreprocessedFeatures");
    }

    /**
     *
     */
    public void clear() {
        instances.clear();
        for (int i = 0; i < featureCalculators.size(); i++) {
            FeatureCalculator featureCalculator = featureCalculators.get(i);

            if (featureCalculator instanceof ExtraFeatures) {
                featureCalculators.remove(i);
            }
        }
        inputFiles.clear();

        System.out.println("Cleared dataset.");
    }

    /**
     *
     * @param percent
     * @return
     */
    public PreProcessOutputStream getOutputStream(int percent) {
        int numInstances = (int) Math.round(((double) percent) / 100d * (double) instances.size());
        Random r = new Random();

        aux = new LinkedList<>();

        while (instances.size() > numInstances) {
            aux.add(instances.remove(r.nextInt(instances.size())));
        }

        return getOutputStream();
    }

    /**
     *
     * @return
     */
    public PreProcessOutputStream getOutputStreamLeftOver() {
        if (aux == null) {
            throw new IllegalStateException("Dataset is still whole.");
        }

        if (aux.isEmpty()) {
            throw new IllegalStateException("Dataset is still whole.");
        }

        List<Instance> tmp = aux;
        aux = instances;
        instances = tmp;

        return getOutputStream();
    }

    /**
     *
     */
    public void rejoin() {
        if (aux == null) {
            throw new IllegalStateException("Dataset is still whole.");
        }

        if (aux.isEmpty()) {
            throw new IllegalStateException("Dataset is still whole.");
        }

        instances.addAll(aux);
    }

    /**
     *
     * @return
     */
    public PreProcessOutputStream getOutputStream() {
        PerformanceCounters.startTimer("getOutputStream");

        if (instances.isEmpty()) {
            PerformanceCounters.stopTimer("getOutputStream");
            return null;
        }

        StringBuilder sb = new StringBuilder();
        PreProcessOutputStream ppos;

        sb.append(String.format("@relation '%s'\n",
                Arrays.toString(
                        inputFiles.toArray(new String[inputFiles.size()])
                )
        ));

        sb.append("@attribute 'source_file' string\n");
        sb.append("@attribute 'pair_ID' integer\n");

        for (FeatureCalculator featureCalculator : featureCalculators) {
            for (String featureName : featureCalculator.getFeatureNames()) {
                sb.append(String.format("@attribute '%s' numeric\n", featureName));
            }
        }
//        if (instances.get(0).hasGoldStandard()) {
        sb.append("@attribute 'gold_standard' numeric\n");
//        }
        sb.append("\n@data\n");

        for (Instance instance : instances) {
            sb.append((instance.valuesToArffCsvFormat() + "\n"));
        }

        ppos = new PreProcessOutputStream(sb.toString().getBytes());
        System.out.println("OutputStream created.");
        PerformanceCounters.stopTimer("getOutputStream");
        return ppos;
    }

    //TODO: use function...
    private InputFormat detectInputFileFormat(String filename) {

        if (filename.matches(".+[\\" + File.pathSeparator + "\\;].+")) {
            //TODO: needs to be moved since it's two files:
            //  first only contains
            //s1    s2
            //  second only contains
            //gs

            //TODO: check each file's format:
            File f = new File(filename.split("[\\" + File.pathSeparator + "\\;]")[0]);

            //weka.core.converters.ArffLoader arffLoader = new ArffLoader();
            //arffLoader.setFile(f);
            try (FileInputStream fis = new FileInputStream(f)) {
                Scanner sc = new Scanner(fis);

                String firstLine = sc.nextLine();
                //2014 task10 (and 2015 task2)
                if (firstLine.matches("^[^\\t]+\\t[^\\n]+$")) {
                    return InputFormat.SEMEVAL2014_TASK10_TRAIN;
                }
                if (firstLine.matches("^.+ {8}.+$")) {
                    //needs pre- reformat! (8 spaces -> tab)
                    StringBuilder sb = new StringBuilder(firstLine);
                    while (sc.hasNextLine()) {
                        sb.append("\n").append(sc.nextLine());
                    }

                    fis.close();
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(
                            sb.toString().replaceAll(" {8}", "\t").getBytes());

                    return InputFormat.SEMEVAL2014_TASK10_TRAIN;
                }

            } catch (IOException ex) {
                Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
            }
            return InputFormat.UNKNOWN;
        }
        File f = new File(filename);
        if (!f.exists() || !f.isFile()) {
            return InputFormat.UNKNOWN;
        }
        //TODO: format detection...
        try (FileInputStream fis = new FileInputStream(f)) {
            Scanner sc = new Scanner(fis);

            String firstLine = sc.nextLine();
            String secondLine = sc.nextLine();
            //2014 task1
            //  test set contains a header (columns name) line:
            //pair_id    s1 s2  gs  entailment
            if (firstLine.matches("^pair_ID\\tsentence_A\\tsentence_B\\trelatedness_score\\tentailment_judgment$")) {
                //TODO: matches only columns line, should check for gold standard presence...
                if (secondLine.matches("^\\d+\\t[^\\t]+\\t[^\\t]+\\t\\-?[\\d\\.]+\\t\\w+$")) {
                    return InputFormat.SEMEVAL2014_TASK1_TRAIN;
                }
                if (secondLine.matches("^\\d+\\t[^\\t]+\\t[^\\n]+$")) {
                    return InputFormat.SEMEVAL2014_TASK1_EVAL;
                }
                return InputFormat.UNKNOWN;
            }
            //  trial and train sets didn't contain the header line:
            if (firstLine.matches("^\\d+\\t[^\\t]+\\t[^\\t]+\\t\\-?[\\d\\.]+\\t\\w+$")) {
                return InputFormat.SEMEVAL2014_TASK1_TRAIN;
            }
            if (firstLine.matches("^\\d+\\t[^\\t]+\\t[^\\n]+$")) {
                return InputFormat.SEMEVAL2014_TASK1_EVAL;
            }

            //2014 task10 (and 2015 task2)
            //test set (without gold standards)
            if (firstLine.matches("^[^\\t]+\\t[^\\n]+$")) {
                return InputFormat.SEMEVAL2014_TASK10_EVAL;
            }

            //2015 task1
            //trial dataset
            //some_number Trending_topic_name   s1  s2  LABEL   s1Tagged    s2Tagged
            if (firstLine.matches("^\\d+\\t[^\\t]+\\t[^\\t]+\\t[^\\t]+\\t\\(\\d\\, \\d\\)\\t[^\\t]+\\t[^\\n]+$")) {
                return InputFormat.SEMEVAL2015_TASK1;
            }
        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        return InputFormat.UNKNOWN;
    }

    /**
     *
     * @param extraTestingFeaturesFilename
     */
    public void concatenateFeaturesFromFile(String extraTestingFeaturesFilename) {
        PerformanceCounters.startTimer("concatenateFeatures");

        featureCalculators.add(new ExtraFeatures(extraTestingFeaturesFilename, instances));

        PerformanceCounters.stopTimer("concatenateFeatures");
    }

    /**
     *
     * @param extraTestingFeaturesFilename
     */
    public void loadFeaturesFromFile(String extraTestingFeaturesFilename) {
        PerformanceCounters.startTimer("concatenateFeatures");

        featureCalculators.add(new ExtraFeatures(extraTestingFeaturesFilename, instances));

        PerformanceCounters.stopTimer("concatenateFeatures");
    }

    /**
     *
     * @param firstFilename
     * @param secondFilename
     */
    public void saveSeparatePreprocessFeatureDatasets(String firstFilename, String secondFilename) {
        PerformanceCounters.startTimer("saveSeparatePreprocessFeatureDatasets");
        StringBuilder sb = new StringBuilder();
        File f = new File(firstFilename);
        f.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(firstFilename)) {

            sb.append(String.format("@relation '%s'\n",
                    Arrays.toString(
                            inputFiles.toArray(new String[inputFiles.size()])
                    )
            ));

            sb.append("@attribute 'source_file' string\n");
            sb.append("@attribute 'pair_ID' integer\n");

            for (FeatureCalculator featureCalculator : featureCalculators) {
                for (String featureName : featureCalculator.getFeatureNames()) {
                    sb.append(String.format("@attribute '%s' numeric\n", featureName));
                }
            }
//            if (instances.get(0).hasGoldStandard()) {
            sb.append("@attribute 'gold_standard' numeric\n");
//            }
            sb.append("\n@data\n");

            for (Instance instance : aux) {
                sb.append((instance.valuesToArffCsvFormat() + "\n"));
            }

            fos.write(sb.toString().getBytes());

        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        sb = new StringBuilder();
        f = new File(secondFilename);
        f.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(secondFilename)) {

            sb.append(String.format("@relation '%s'\n",
                    Arrays.toString(
                            inputFiles.toArray(new String[inputFiles.size()])
                    )
            ));
            sb.append("@attribute 'source_file' string\n");
            sb.append("@attribute 'pair_ID' integer\n");

            for (FeatureCalculator featureCalculator : featureCalculators) {
                for (String featureName : featureCalculator.getFeatureNames()) {
                    sb.append(String.format("@attribute '%s' numeric\n", featureName));
                }
            }
//            if (instances.get(0).hasGoldStandard()) {
            sb.append("@attribute 'gold_standard' numeric\n");
//            }

            sb.append("\n@data\n");

            for (Instance instance : instances) {
                sb.append((instance.valuesToArffCsvFormat() + "\n"));
            }

            fos.write(sb.toString().getBytes());

        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        PerformanceCounters.stopTimer("saveSeparatePreprocessFeatureDatasets");
    }

    /**
     *
     * @param inputTrainingFilename
     */
    public void loadInput(String inputTrainingFilename) {

        String regex = "[\\" + File.pathSeparator + "\\;]";
        InputFormat inFormat = detectInputFileFormat(inputTrainingFilename);
        System.out.println("Detected input format = " + inFormat);
        String tmp[] = inputTrainingFilename.split(regex);

        switch (inFormat) {
            case SEMEVAL2014_TASK1_TRAIN:
                loadSentencePairsWithGoldStandard(inputTrainingFilename);
                break;
            case SEMEVAL2014_TASK1_EVAL:
                loadSentencePairsWithoutGoldStandard(inputTrainingFilename);
                break;
            case SEMEVAL2014_TASK10_TRAIN:
                loadSentencePairsWithSideGoldStandardFile(tmp[0], tmp[1]);
                break;
            case SEMEVAL2014_TASK10_EVAL:
                loadSentencePairsWithoutSideGoldStandardFile(inputTrainingFilename);
                break;
            case SEMEVAL2015_TASK1:
                loadSentencePairsWithTagging(inputTrainingFilename);
                break;
        }
    }

    /**
     *
     * @param filename
     */
    public void savePreprocessedText(String filename) {
        File f = new File(filename);
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (Instance instance : instances) {
            String preprocessedInstance = instance.getPreprocessedText();

            sb.append(preprocessedInstance.replace("\t", "\n"))
                    .append("\n");
        }

        try (FileOutputStream fos = new FileOutputStream(f, true)) {
            fos.write(sb.toString().getBytes());
        } catch (IOException ex) {
            Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadSentencePairsWithTagging(String inputTrainingFilename) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param sourceFile
     * @param instanceID
     * @return
     */
    public static Instance getInstance(String sourceFile, int instanceID) {

        for (InputFileInstances inputFileWithInstance : preprocess.inputFileWithInstances) {
            if (inputFileWithInstance.getFilename().equalsIgnoreCase(sourceFile)
                    && inputFileWithInstance.containsId(instanceID)) {
                return inputFileWithInstance.getInstance(instanceID);
            }
        }

        return null;
    }

    private static class InputFileInstances {

        private final String filename;
        private int startID;
        private int stopID;
        private final List<Instance> instances;

        public InputFileInstances(String filename, int startID, int stopID, List<Instance> instances) {
            this.filename = filename;
            this.startID = startID;
            this.stopID = stopID;
            this.instances = instances;
        }

        public InputFileInstances(String filename, int startID, int stopID) {
            this.filename = filename;
            this.startID = startID;
            this.stopID = stopID;
            this.instances = new ArrayList<>();
        }

        public void addInstance(Instance instance) {
            if (startID == -1 || startID > instance.getPair_ID()) {
                startID = instance.getPair_ID();
            }
            if (stopID == -1 || stopID < instance.getPair_ID()) {
                stopID = instance.getPair_ID();
            }
            this.instances.add(instance);
        }

        public Instance getInstance(int instanceID) {
            if (!containsId(instanceID)) {
                throw new InvalidParameterException("ID is not in this set!");
            }

            for (Instance instance : instances) {
                if (instance.getPair_ID() == instanceID) {
                    return instance;
                }
            }
            return null;
        }

        public String getFilename() {
            return filename;
        }

        public int getStartID() {
            return startID;
        }

        public int getStopID() {
            return stopID;
        }

        public boolean containsId(int id) {
            return id <= stopID && id >= startID;
        }
    }

    private enum InputFormat {

        UNKNOWN,
        SEMEVAL2014_TASK1_TRAIN,
        SEMEVAL2014_TASK1_EVAL,
        SEMEVAL2014_TASK10_TRAIN,
        SEMEVAL2014_TASK10_EVAL,
        SEMEVAL2015_TASK1
    }

    private class FeatureCalculatorWorker implements Runnable {

        List<FeatureCalculator> featureCalculators;
        LinkedList<Instance> processed;

        public FeatureCalculatorWorker(List<FeatureCalculator> featureCalculators) {
            this.featureCalculators = featureCalculators;
            this.processed = new LinkedList<>();
        }

        @Override
        public void run() {
            System.out.print(".");
            while (!aux.isEmpty()) {

                try {
                    Instance currentInstance = aux.remove(0);
                    //                System.out.println("calculating topDown for (" + currentInstance.getAttributeAt(0) + "):\n|" + currentInstance.getSentence1() + "\n|" + currentInstance.getSentence2());
                    for (FeatureCalculator featureCalculator : featureCalculators) {
                        featureCalculator.calculate(currentInstance);
                    }
                    processed.add(currentInstance);
                } catch (Exception ex) {
                    Logger.getLogger(PreProcess.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

            instances.addAll(processed);
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
