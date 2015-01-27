/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

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
import weka.core.converters.ConverterUtils.DataSource;
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
@Deprecated
public class CrossValidation {

    /**
     * Performs the cross-validation. See Javadoc of class for information on
     * command-line parameters.
     *
     * @param args the command-line parameters
     * @throws Exception if something goes wrong
     */
    public static void main(String[] args) throws Exception {
        java.lang.System.out.println(
                performCrossValidation(
                        Utils.getOption("t", args),
                        Utils.getOption("c", args),
                        Utils.getOption("i", args),
                        Utils.splitOptions(Utils.getOption("W", args)),
                        Integer.parseInt(Utils.getOption("s", args)),
                        Integer.parseInt(Utils.getOption("x", args)),
                        Utils.getOption("m", args)
                )
        );

    }

    /**
     *
     * @param dataInput
     * @param classIndex
     * @param removeIndices
     * @param classifierCmd
     * @param seed
     * @param folds
     * @param modelOutputFile
     * @return
     * @throws Exception
     */
    public static String performCrossValidation(String dataInput,
            String classIndex,
            String removeIndices,
            String[] classifierCmd,
            int seed,
            int folds,
            String modelOutputFile) throws Exception {

        // classifier
        String classname = classifierCmd[0];
        classifierCmd[0] = "";
        AbstractClassifier cls = (AbstractClassifier) Utils.forName(Classifier.class, classname, classifierCmd);

        return performCrossValidation(dataInput, classIndex, removeIndices, cls, seed, folds, modelOutputFile);
    }

    /**
     *
     * @param dataInput
     * @param classIndex
     * @param removeIndices
     * @param cls
     * @param seed
     * @param folds
     * @param modelOutputFile
     * @return
     * @throws Exception
     */
    public static String performCrossValidation(String dataInput,
            String classIndex,
            String removeIndices,
            AbstractClassifier cls,
            int seed,
            int folds,
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

    /**
     *
     * @param dataInput
     * @param classIndex
     * @param removeIndices
     * @param cls
     * @param seed
     * @param folds
     * @param modelOutputFile
     * @return
     * @throws Exception
     */
    public static String performCrossValidationMT(String dataInput,
            String classIndex,
            String removeIndices,
            AbstractClassifier cls,
            int seed,
            int folds,
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
        List<Thread> foldThreads = (List<Thread>) Collections.synchronizedList(new LinkedList<Thread>());

        List<FoldSet> foldSets = (List<FoldSet>) Collections.synchronizedList(new LinkedList<FoldSet>());

        for (int n = 0; n < folds; n++) {
            foldSets.add(new FoldSet(randData.trainCV(folds, n),
                    randData.testCV(folds, n),
                    AbstractClassifier.makeCopy(cls)
            ));

            if (n < Config.getNumThreads() - 1) {
                Thread foldThread = new Thread(
                        new CrossValidationFoldThread(n, foldSets, eval)
                );
                foldThreads.add(foldThread);
            }
        }

        PerformanceCounters.stopTimer("cross-validation init MT");
        PerformanceCounters.startTimer("cross-validation folds+train MT");
//paralelize!!:--------------------------------------------------------------
        if (Config.getNumThreads() > 1) {
            for (Thread foldThread : foldThreads) {
                foldThread.start();
            }
        } else {
            //use the current thread to run the cross-validation instead of using the Thread instance created here:
            new CrossValidationFoldThread(0, foldSets, eval).run();
        }

        cls.buildClassifier(data);

        for (Thread foldThread : foldThreads) {
            foldThread.join();
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

    static String performCrossValidationMT(Instances data, AbstractClassifier cls, int seed, int folds, String modelOutputFile) {

        PerformanceCounters.startTimer("cross-validation MT");

        PerformanceCounters.startTimer("cross-validation init MT");

        // randomize data
        Random rand = new Random(seed);
        Instances randData = new Instances(data);
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
                        AbstractClassifier.makeCopy(cls)
                ));
            } catch (Exception ex) {
                Logger.getLogger(CrossValidation.class.getName()).log(Level.SEVERE, null, ex);
            }

            //TODO: use Config.getNumThreads() for limiting these::
            if (n < Config.getNumThreads() - 1) {
                Thread foldThread = new Thread(
                        new CrossValidationFoldThread(n, foldSets, eval)
                );
                foldThreads.add(foldThread);
            }
        }

        PerformanceCounters.stopTimer("cross-validation init MT");
        PerformanceCounters.startTimer("cross-validation folds+train MT");
//paralelize!!:--------------------------------------------------------------
        if (Config.getNumThreads() > 1) {
            for (Thread foldThread : foldThreads) {
                foldThread.start();
            }
        } else {
            new CrossValidationFoldThread(0, foldSets, eval).run();
        }

        try {
            cls.buildClassifier(data);
        } catch (Exception ex) {
            Logger.getLogger(CrossValidation.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (Thread foldThread : foldThreads) {
            try {
                foldThread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(CrossValidation.class.getName()).log(Level.SEVERE, null, ex);
            }
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

        if (modelOutputFile != null) {
            if (!modelOutputFile.isEmpty()) {
                try {
                    SerializationHelper.write(modelOutputFile, cls);
                } catch (Exception ex) {
                    Logger.getLogger(CrossValidation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        PerformanceCounters.stopTimer("cross-validation post MT");
        PerformanceCounters.stopTimer("cross-validation MT");
        return out;
    }

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
