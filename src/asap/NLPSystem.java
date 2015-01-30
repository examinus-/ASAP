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

    private final Classifier classifier;
    //private FeatureCalculators[] featureCalculators;
    private final Instances trainingSet;
    private Instances evaluationSet;

    private double[] trainingPredictions;
    private double[] evaluationPredictions;

    private double crossValidationPearsonsCorrelation;
    private double evaluationPearsonsCorrelation;

    private boolean classifierBuilt;
    private boolean classifierBuiltWithCrossValidation;
    private boolean evaluated;

    //--------------------------------------------------------------------------
    //-         public members                                                 -
    //--------------------------------------------------------------------------
    public NLPSystem(Classifier classifier, Instances trainingSet, Instances evaluationSet) {
        this.classifier = classifier;
        this.trainingSet = trainingSet;
        this.evaluationSet = evaluationSet;
        classifierBuilt = false;
        classifierBuiltWithCrossValidation = false;
        evaluated = false;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public synchronized double getCrossValidationPearsonsCorrelation() {
        if (classifierBuiltWithCrossValidation) {
            return Double.NaN;
        }
        return crossValidationPearsonsCorrelation;
    }

    public synchronized double getEvaluationPearsonsCorrelation() {
        if (evaluated) {
            return Double.NaN;
        }
        return evaluationPearsonsCorrelation;
    }

    public synchronized double[] getEvaluationPredictions() {
        if (evaluated) {
            return null;
        }
        return evaluationPredictions;
    }

    public synchronized Instances getEvaluationSet() {
        return evaluationSet;
    }

    public synchronized double[] getTrainingPredictions() {
        if (classifierBuilt) {
            return null;
        }
        return trainingPredictions;
    }

    public synchronized Instances getTrainingSet() {
        return trainingSet;
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
        return buildClassifier(false);
    }

    public synchronized String buildClassifier(boolean runCrossValidation) {
        if (classifierBuilt && classifierBuiltWithCrossValidation == runCrossValidation) {
            return null;
        }
        String r = "";

        //build model with or without cross-validation
        if (Config.getNumThreads() > 1) {
            new Thread() {

                @Override
                public void run() {
                    try {
                        classifier.buildClassifier(trainingSet);
                        classifierBuilt = true;
                    } catch (Exception ex) {
                        Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            };
            if (runCrossValidation) {
                r = crossValidate(SEED, NO_FOLDS, null);
                classifierBuiltWithCrossValidation = true;
            }
        }

        if (Config.getNumThreads() == 1) {
            if (runCrossValidation) {
                r = crossValidate(SEED, NO_FOLDS, null);
                classifierBuiltWithCrossValidation = true;
            }

            try {
                classifier.buildClassifier(trainingSet);
                classifierBuilt = true;
            } catch (Exception ex) {
                Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return r;
    }
    
    public synchronized void setEvaluationSet(Instances evaluationSet) {
        this.evaluationSet = evaluationSet;
        this.evaluationPredictions = null;
        evaluated = false;
    }

    @Override
    public int compareTo(NLPSystem o) {
        if (evaluated && o.evaluated) {
            return (int) (o.evaluationPearsonsCorrelation - evaluationPearsonsCorrelation);
        }

        if (evaluated) {
            if (o.classifierBuiltWithCrossValidation) {
                return (int) (o.crossValidationPearsonsCorrelation - evaluationPearsonsCorrelation);
            }
            //let's assume models with correlation data have higher order than those without correlation data
            return -1;
        }

        if (o.evaluated && classifierBuiltWithCrossValidation) {
            return (int) (o.evaluationPearsonsCorrelation - crossValidationPearsonsCorrelation);
        }

        if (o.classifierBuiltWithCrossValidation && classifierBuiltWithCrossValidation) {
            return (int) (o.crossValidationPearsonsCorrelation - crossValidationPearsonsCorrelation);
        }

        //let's assume models without correlation data have lower order than those with correlation data
        return 1;
    }

    private String crossValidate(int seed, int folds, String modelOutputFile) {

        PerformanceCounters.startTimer("cross-validation MT");
        PerformanceCounters.startTimer("cross-validation init MT");

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
            Logger.getLogger(CrossValidation.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(CrossValidation.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (n < Config.getNumThreads() - 1) {
                Thread foldThread = new Thread(
                        new CrossValidationFoldThread(n, foldSets, eval)
                );
                foldThreads.add(foldThread);
            }
        }

        PerformanceCounters.stopTimer("cross-validation init MT");
        PerformanceCounters.startTimer("cross-validation folds+train MT");

        if (Config.getNumThreads() > 1) {
            for (Thread foldThread : foldThreads) {
                foldThread.start();
            }
        } else {
            new CrossValidationFoldThread(0, foldSets, eval).run();
        }

        for (Thread foldThread : foldThreads) {
            try {
                foldThread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(CrossValidation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        PerformanceCounters.stopTimer("cross-validation folds+train MT");
        PerformanceCounters.startTimer("cross-validation post MT");
        // evaluation for output:
        String out = "\n" + "=== Setup ===\n"
                + "Classifier: " + abstractClassifier.getClass().getName() + " "
                + Utils.joinOptions(abstractClassifier.getOptions()) + "\n"
                + "Dataset: " + trainingSet.relationName() + "\n"
                + "Folds: " + folds + "\n"
                + "Seed: " + seed + "\n"
                + "\n"
                + eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false)
                + "\n";

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
                    Logger.getLogger(CrossValidation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        PerformanceCounters.stopTimer("cross-validation post MT");
        PerformanceCounters.stopTimer("cross-validation MT");
        return out;
    }

    private double[] evaluateModel(boolean printEvaluation) {
        PerformanceCounters.startTimer("evaluateModel");
        System.out.println("Evaluating model...");
        AbstractClassifier abstractClassifier = (AbstractClassifier) classifier;
        double[] predictions = null;
        try {
            // evaluate classifier and print some statistics
            Evaluation eval = new Evaluation(evaluationSet);

            predictions = eval.evaluateModel(abstractClassifier, evaluationSet);

            if (printEvaluation) {
                System.out.println("\tstats for model:" + abstractClassifier.getClass().getName() + " "
                        + Utils.joinOptions(abstractClassifier.getOptions()));
                System.out.println(eval.toSummaryString());
            }

            evaluationPearsonsCorrelation = eval.correlationCoefficient();
        } catch (Exception ex) {
            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("\tevaluation done.");
        PerformanceCounters.stopTimer("evaluateModel");
        return predictions;
    }

    public void saveSystem(File dir, String systemFilename) {
        File systemFile = new File(dir, systemFilename);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(systemFile))){
            oos.writeObject(this);
            oos.flush();
            oos.close();
        } catch (IOException ex) {
            Logger.getLogger(NLPSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                    Logger.getLogger(CrossValidation.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    synchronized (eval) {
                        eval.evaluateModel(cls, testSet);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(CrossValidation.class.getName()).log(Level.SEVERE, null, ex);
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
