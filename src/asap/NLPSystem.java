/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RemoveType;

/**
 * Full representation of a system, grouping together the classifier, its
 * learning and/or evaluation sets, FeatureCalculators used for generating the
 * sets and correlation data for comparison with other systems.
 *
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class NLPSystem implements Serializable, Comparable<NLPSystem> {

    private static final int SEED = 0;
    private static final int NO_FOLDS = 10;

    private Classifier classifier;

    //private final FeatureCalculators[] featureCalculators;
    //private final Map<FeatureCalculator, List<Integer>> featuresMap;
    private Instances trainingSet;
    private Instances evaluationSet;

    private Instances trainingOriginalSet;
    private Instances evaluationOriginalSet;

    private double[] trainingPredictions;
    private double[] evaluationPredictions;

    private double trainingPearsonsCorrelation;
    private double crossValidationPearsonsCorrelation;
    private double evaluationPearsonsCorrelation;

    private boolean classifierBuilt;
    private boolean classifierBuiltWithCrossValidation;
    private boolean evaluated;
    private String filename;

    //--------------------------------------------------------------------------
    //-         public members                                                 -
    //--------------------------------------------------------------------------
//    public NLPSystem(Classifier classifier, Instances trainingSet,
//        Instances evaluationSet, List<FeatureCalculators> featureCalculators,
//        Map<FeatureCalculator, List<Integer>> featuresMap) {
    public NLPSystem(Classifier classifier, Instances trainingSet, Instances evaluationSet) {

        this.classifier = classifier;
        this.trainingOriginalSet = trainingSet;
        this.evaluationOriginalSet = evaluationSet;

        this.trainingSet = getFilteredSet(trainingSet);
        this.evaluationSet = getFilteredSet(evaluationSet);

//        this.featureCalculators = featureCalculators.toArray(
//              new FeatureCalculators[featureCalculators.size()]);
//        this.featuresMap = featuresMap;
        classifierBuilt = false;
        classifierBuiltWithCrossValidation = false;
        evaluated = false;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public synchronized double getCrossValidationPearsonsCorrelation() {
        if (!classifierBuiltWithCrossValidation) {
            return Double.NaN;
        }
        return crossValidationPearsonsCorrelation;
    }

    public synchronized double getEvaluationPearsonsCorrelation() {
        if (!evaluated) {
            return Double.NaN;
        }
        return evaluationPearsonsCorrelation;
    }

    public synchronized double[] getEvaluationPredictions() {
        if (!evaluated) {
            return null;
        }
        return evaluationPredictions;
    }

    public synchronized Instances getEvaluationSet() {
        return evaluationSet;
    }

    public synchronized double[] getTrainingPredictions() {
        if (!classifierBuilt) {
            return null;
        }
        return trainingPredictions;
    }

    public synchronized Instances getTrainingSet() {
        return trainingSet;
    }

    public Instances getTrainingOriginalSet() {
        return trainingOriginalSet;
    }

    public Instances getEvaluationOriginalSet() {
        return evaluationOriginalSet;
    }

    public synchronized boolean isClassifierBuilt() {
        return classifierBuilt;
    }

    public synchronized boolean isEvaluated() {
        return evaluated;
    }

    public synchronized void evaluate() {
        evaluateModel(false);
    }

    public String buildClassifier() {
        return buildClassifier(true);
    }

    public synchronized String buildClassifier(boolean runCrossValidation) {
        if (classifierBuilt && classifierBuiltWithCrossValidation == runCrossValidation) {
            return null;
        }
//        checkInstancesFeatures(trainingSet);
        final StringBuilder sb = new StringBuilder();
        sb.delete(0, sb.length());

        //build model with or without cross-validation
        if (Config.getNumThreads() > 1) {
            Thread buildThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    sb.append(_buildClassifier());
                }

            });
            buildThread.start();
            if (runCrossValidation) {
                sb.append(crossValidate(SEED, NO_FOLDS, null));
            }
            while (buildThread.isAlive()) {
                try {
                    buildThread.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if (Config.getNumThreads() == 1) {
            if (runCrossValidation) {
                sb.append(crossValidate(SEED, NO_FOLDS, null));
            }

            sb.append(_buildClassifier());
        }

        return sb.toString();
    }

    public synchronized void setEvaluationSet(Instances evaluationSet) {
        this.evaluationOriginalSet = evaluationSet;
        this.evaluationSet = getFilteredSet(evaluationSet);
        this.evaluationPredictions = null;
        evaluated = false;
    }

    private Instances getFilteredSet(Instances set) {
        //TODO: filter all unwanted features
        if (set == null) {
            return null;
        }
        
        RemoveType removeTypeFilter = new RemoveType();
        String[] removeTypeFilterOptions = {"-T", "string"};
        Instances filteredSet = null;
        try {
            removeTypeFilter.setInputFormat(set);
            removeTypeFilter.setOptions(removeTypeFilterOptions);
            filteredSet = Filter.useFilter(set, removeTypeFilter);
        } catch (Exception ex) {
            Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return filteredSet;
    }

    private double getComparableCorrelation() {
        if (evaluated) {
            return evaluationPearsonsCorrelation;
        }

        if (classifierBuiltWithCrossValidation) {
            return crossValidationPearsonsCorrelation;
        }

        if (classifierBuilt) {
            return trainingPearsonsCorrelation;
        }

        return -1;
    }

    @Override
    public int compareTo(NLPSystem o) {
        double diff = o.getComparableCorrelation() - getComparableCorrelation();

        if (diff > 0) {
            return 1;
        }
        if (diff < 0) {
            return -1;
        }
        return 0;
    }

    public void saveSystem(File dir, String systemFilename) {
        filename = systemFilename;
        File systemFile = new File(dir, systemFilename);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(systemFile))) {
            oos.writeObject(this);
            oos.flush();
            oos.close();
        } catch (IOException ex) {
            Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String shortName() {
        return getClassifier().getClass().getSimpleName() + hashCode();
    }

    @Override
    public String toString() {
        AbstractClassifier ac = (AbstractClassifier) classifier;
        return String.format("Classifier: %s %s\n", ac.getClass().getName(), Utils.joinOptions(ac.getOptions()));
    }

    private String _buildClassifier() {
        Evaluation eval;
        try {
            eval = new Evaluation(trainingSet);
        } catch (Exception ex) {
            Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
            return "Error creating evaluation instance for given data!";
        }

        try {
            classifier.buildClassifier(trainingSet);
        } catch (Exception ex) {
            Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            trainingPredictions = eval.evaluateModel(classifier, trainingSet);
            trainingPearsonsCorrelation = eval.correlationCoefficient();
        } catch (Exception ex) {
            Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
        }

        classifierBuilt = true;
        return "Classifier built (" + trainingPearsonsCorrelation + ").";
    }

    private String crossValidate(int seed, int folds, String modelOutputFile) {

        PerformanceCounters.startTimer("cross-validation");
        PerformanceCounters.startTimer("cross-validation init");

        AbstractClassifier abstractClassifier = (AbstractClassifier) classifier;
        // randomize data
        Random rand = new Random(seed);
        Instances randData = new Instances(trainingSet);
        randData.randomize(rand);
        if (randData.classAttribute().isNominal()) {
            randData.stratify(folds);
        }

        // perform cross-validation and add predictions
        Evaluation eval;
        try {
            eval = new Evaluation(randData);
        } catch (Exception ex) {
            Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
            return "Error creating evaluation instance for given data!";
        }
        List<Thread> foldThreads = (List<Thread>) Collections.synchronizedList(new LinkedList<Thread>());

        List<FoldSet> foldSets = (List<FoldSet>) Collections.synchronizedList(new LinkedList<FoldSet>());

        for (int n = 0; n < folds; n++) {
            try {
                foldSets.add(new FoldSet(randData.trainCV(folds, n),
                        randData.testCV(folds, n),
                        AbstractClassifier.makeCopy(abstractClassifier)
                ));
            } catch (Exception ex) {
                Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (n < Config.getNumThreads() - 1) {
                Thread foldThread = new Thread(
                        new CrossValidationFoldThread(n, foldSets, eval)
                );
                foldThreads.add(foldThread);
            }
        }

        PerformanceCounters.stopTimer("cross-validation init");
        PerformanceCounters.startTimer("cross-validation folds+train");

        if (Config.getNumThreads() > 1) {
            for (Thread foldThread : foldThreads) {
                foldThread.start();
            }
        } else {
            new CrossValidationFoldThread(0, foldSets, eval).run();
        }

        for (Thread foldThread : foldThreads) {
            while (foldThread.isAlive()) {
                try {
                    foldThread.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        PerformanceCounters.stopTimer("cross-validation folds+train");
        PerformanceCounters.startTimer("cross-validation post");
        // evaluation for output:
        String out = String.format("\n=== Setup ===\nClassifier: %s %s\n"
                + "Dataset: %s\nFolds: %s\nSeed: %s\n\n%s\n",
                abstractClassifier.getClass().getName(),
                Utils.joinOptions(abstractClassifier.getOptions()),
                trainingSet.relationName(),
                folds, seed,
                eval.toSummaryString(
                        String.format("=== %s-fold Cross-validation ===", folds),
                        false));

        try {
            crossValidationPearsonsCorrelation = eval.correlationCoefficient();
        } catch (Exception ex) {
            Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (modelOutputFile != null) {
            if (!modelOutputFile.isEmpty()) {
                try {
                    SerializationHelper.write(modelOutputFile, abstractClassifier);
                } catch (Exception ex) {
                    Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        classifierBuiltWithCrossValidation = true;
        PerformanceCounters.stopTimer("cross-validation post");
        PerformanceCounters.stopTimer("cross-validation");
        return out;
    }

    private void evaluateModel(boolean printEvaluation) {
//        checkInstancesFeatures(evaluationSet);
        PerformanceCounters.startTimer("evaluateModel");
        System.out.println("Evaluating model...");
        AbstractClassifier abstractClassifier = (AbstractClassifier) classifier;
        try {
            // evaluate classifier and print some statistics
            Evaluation eval = new Evaluation(evaluationSet);

            evaluationPredictions = eval.evaluateModel(abstractClassifier, evaluationSet);

            if (printEvaluation) {
                System.out.println("\tstats for model:" + abstractClassifier.getClass().getName() + " "
                        + Utils.joinOptions(abstractClassifier.getOptions()));
                System.out.println(eval.toSummaryString());
            }

            evaluationPearsonsCorrelation = eval.correlationCoefficient();
            evaluated = true;
        } catch (Exception ex) {
            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("\tevaluation done.");
        PerformanceCounters.stopTimer("evaluateModel");
    }

    //--------------------------------------------------------------------------
    //-         Inner classes                                                  -
    //--------------------------------------------------------------------------
    private static class CrossValidationFoldThread implements Runnable {

        final Evaluation eval;
        final List<FoldSet> foldSets;
        int threadNumber;

        public CrossValidationFoldThread(int threadNumber, List<FoldSet> foldSets, Evaluation eval) {
            this.threadNumber = threadNumber;
            this.foldSets = foldSets;
            this.eval = eval;
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

            FoldSet foldSet;
            Instances trainSet, testSet;
            Classifier cls;

            while (!foldSets.isEmpty()) {
                foldSet = foldSets.remove(0);
                trainSet = foldSet.getTrainSet();
                testSet = foldSet.getTestSet();
                cls = foldSet.getClassifier();

                try {
                    cls.buildClassifier(trainSet);
                } catch (Exception ex) {
                    Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    synchronized (eval) {
                        eval.evaluateModel(cls, testSet);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private static class FoldSet {

        private final Instances trainSet, testSet;
        private final Classifier cls;

        public FoldSet(Instances trainSet, Instances testSet, Classifier cls) {
            this.cls = cls;
            this.trainSet = trainSet;
            this.testSet = testSet;
        }

        public Instances getTrainSet() {
            return trainSet;
        }

        public Instances getTestSet() {
            return testSet;
        }

        public Classifier getClassifier() {
            return cls;
        }

    }

}
