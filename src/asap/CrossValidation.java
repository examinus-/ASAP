/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Utils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.AbstractClassifier;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Performs a single run of cross-validation and adds the prediction on the test
 * set to the dataset.
 *
 * Command-line parameters:
 * <ul>
 * <li>-t filename - the dataset to use</li>
 * <li>-o filename - the output file to store dataset with the predictions
 * in</li>
 * <li>-x int - the number of folds to use</li>
 * <li>-s int - the seed for the random number generator</li>
 * <li>-c int - the class index, "first" and "last" are accepted as well; "last"
 * is used by default</li>
 * <li>-W classifier - classname and options, enclosed by double quotes; the
 * classifier to cross-validate</li>
 * </ul>
 *
 * Example command-line:
 * <pre>
 * java CrossValidation -t anneal.arff -c last -o predictions.arff -x 10 -s 1 -W "weka.classifiers.trees.J48 -C 0.25"
 * </pre>
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class CrossValidation {

    /**
     * Performs the cross-validation. See Javadoc of class for information on
     * command-line parameters.
     *
     * @param args the command-line parameters
     * @throws Exception if something goes wrong
     */
    public static void main(String[] args) throws Exception {
        System.out.println(
                performCrossValidation(
                        Utils.getOption("t", args),
                        Utils.getOption("c", args),
                        Utils.getOption("i", args),
                        Utils.splitOptions(Utils.getOption("W", args)),
                        Integer.parseInt(Utils.getOption("s", args)),
                        Integer.parseInt(Utils.getOption("x", args)),
                        Utils.getOption("o", args),
                        Utils.getOption("m", args)
                )
        );

    }

    public static String performCrossValidation(String dataInput,
            String classIndex,
            String removeIndices,
            String[] classifierCmd,
            int seed,
            int folds,
            String dataOutputFile,
            String modelOutputFile) throws Exception {

        // classifier
        String classname = classifierCmd[0];
        classifierCmd[0] = "";
        AbstractClassifier cls = (AbstractClassifier) Utils.forName(Classifier.class, classname, classifierCmd);

        return performCrossValidation(dataInput, classIndex, removeIndices, cls, seed, folds, dataOutputFile, modelOutputFile);
    }

    public static String performCrossValidation(String dataInput,
            String classIndex,
            String removeIndices,
            AbstractClassifier cls,
            int seed,
            int folds,
            String dataOutputFilename,
            String modelOutputFile) throws Exception {
        
        PerformanceCounters.startTimer("cross-validation ST");

        PerformanceCounters.startTimer("cross-validation init ST");

        // loads data and set class index
        Instances data = DataSource.read(dataInput);
        String clsIndex = classIndex;

        switch (clsIndex) {
            case "first":
                data.setClassIndex(0);
                break;
            case "last":
                data.setClassIndex(data.numAttributes() - 1);
                break;
            default:
                try {
                    data.setClassIndex(Integer.parseInt(clsIndex) - 1);
                } catch (NumberFormatException e) {
                    data.setClassIndex(data.attribute(clsIndex).index());
                }
                break;
        }

        Remove removeFilter = new Remove();
        removeFilter.setAttributeIndices(removeIndices);
        removeFilter.setInputFormat(data);
        data = Filter.useFilter(data, removeFilter);

        // randomize data
        Random rand = new Random(seed);
        Instances randData = new Instances(data);
        randData.randomize(rand);
        if (randData.classAttribute().isNominal()) {
            randData.stratify(folds);
        }

        // perform cross-validation and add predictions
        Evaluation eval = new Evaluation(randData);
        Instances trainSets[] = new Instances[folds];
        Instances testSets[] = new Instances[folds];
        Classifier foldCls[] = new Classifier[folds];


        for (int n = 0; n < folds; n++) {
            trainSets[n] = randData.trainCV(folds, n);
            testSets[n] = randData.testCV(folds, n);
            foldCls[n] = AbstractClassifier.makeCopy(cls);
        }


        PerformanceCounters.stopTimer("cross-validation init ST");
        PerformanceCounters.startTimer("cross-validation folds+train ST");
//paralelize!!:--------------------------------------------------------------
        for (int n = 0; n < folds; n++) {
            Instances train = trainSets[n];
            Instances test = testSets[n];

            // the above code is used by the StratifiedRemoveFolds filter, the
            // code below by the Explorer/Experimenter:
            // Instances train = randData.trainCV(folds, n, rand);
            // build and evaluate classifier
            Classifier clsCopy = foldCls[n];
            clsCopy.buildClassifier(train);
            eval.evaluateModel(clsCopy, test);
        }

        cls.buildClassifier(data);
//until here!-----------------------------------------------------------------

        PerformanceCounters.stopTimer("cross-validation folds+train ST");
        PerformanceCounters.startTimer("cross-validation post ST");
        // output evaluation
        String out = "\n"
                + "=== Setup ===\n"
                + "Classifier: " + cls.getClass().getName() + " "
                + Utils.joinOptions(cls.getOptions()) + "\n"
                + "Dataset: " + data.relationName() + "\n"
                + "Folds: " + folds + "\n"
                + "Seed: " + seed + "\n"
                + "\n"
                + eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false)
                + "\n";

        if (!modelOutputFile.isEmpty()) {
            SerializationHelper.write(modelOutputFile, cls);
        }

       
        PerformanceCounters.stopTimer("cross-validation post ST");
        PerformanceCounters.stopTimer("cross-validation ST");
        
        return out;
    }

    public static String performCrossValidationMT(String dataInput,
            String classIndex,
            String removeIndices,
            AbstractClassifier cls,
            int seed,
            int folds,
            String dataOutputFilename,
            String modelOutputFile) throws Exception {
        
        PerformanceCounters.startTimer("cross-validation MT");

        PerformanceCounters.startTimer("cross-validation init MT");
        
        // loads data and set class index
        Instances data = DataSource.read(dataInput);
        String clsIndex = classIndex;

        switch (clsIndex) {
            case "first":
                data.setClassIndex(0);
                break;
            case "last":
                data.setClassIndex(data.numAttributes() - 1);
                break;
            default:
                try {
                    data.setClassIndex(Integer.parseInt(clsIndex) - 1);
                } catch (NumberFormatException e) {
                    data.setClassIndex(data.attribute(clsIndex).index());
                }
                break;
        }

        Remove removeFilter = new Remove();
        removeFilter.setAttributeIndices(removeIndices);
        removeFilter.setInputFormat(data);
        data = Filter.useFilter(data, removeFilter);

        // randomize data
        Random rand = new Random(seed);
        Instances randData = new Instances(data);
        randData.randomize(rand);
        if (randData.classAttribute().isNominal()) {
            randData.stratify(folds);
        }

        // perform cross-validation and add predictions
        Evaluation eval = new Evaluation(randData);
        Instances trainSets[] = new Instances[folds];
        Instances testSets[] = new Instances[folds];
        Classifier foldCls[] = new Classifier[folds];
        Thread[] foldThreads = new Thread[folds];
        
        for (int n = 0; n < folds; n++) {
            trainSets[n] = randData.trainCV(folds, n);
            testSets[n] = randData.testCV(folds, n);
            foldCls[n] = AbstractClassifier.makeCopy(cls);
            foldThreads[n] = new Thread(
                    new CrossValidationFoldThread(n, foldCls[n], trainSets[n],
                    testSets[n], eval)
                    );
        }

        PerformanceCounters.stopTimer("cross-validation init MT");
        PerformanceCounters.startTimer("cross-validation folds+train MT");
//paralelize!!:--------------------------------------------------------------
        for (int n = 0; n < folds; n++) {
            foldThreads[n].start();
        }

        cls.buildClassifier(data);
        
        for (int n = 0; n < folds; n++) {
            foldThreads[n].join();
        }
        
//until here!-----------------------------------------------------------------
        
        PerformanceCounters.stopTimer("cross-validation folds+train MT");
        PerformanceCounters.startTimer("cross-validation post MT");
        // evaluation for output:
        String out = "\n"
                + "=== Setup ===\n"
                + "Classifier: " + cls.getClass().getName() + " "
                + Utils.joinOptions(cls.getOptions()) + "\n"
                + "Dataset: " + data.relationName() + "\n"
                + "Folds: " + folds + "\n"
                + "Seed: " + seed + "\n"
                + "\n"
                + eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false)
                + "\n";

        if (!modelOutputFile.isEmpty()) {
            SerializationHelper.write(modelOutputFile, cls);
        }

        PerformanceCounters.stopTimer("cross-validation post MT");
        PerformanceCounters.stopTimer("cross-validation MT");
        return out;
    }

    private static class CrossValidationFoldThread implements Runnable {

        Classifier cls;
        Instances trainSet, testSet;
        final Evaluation eval;
        int fold;

        public CrossValidationFoldThread(int aFold, Classifier aCls, Instances aTrainSet, Instances aTestSet, Evaluation aEval) {
            fold = aFold;
            cls = aCls;
            trainSet = aTrainSet;
            testSet = aTestSet;
            eval = aEval;
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            
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
