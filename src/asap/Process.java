/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Instances;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.M5P;
import weka.classifiers.meta.Stacking;

import weka.classifiers.meta.Vote;
import weka.classifiers.rules.ZeroR;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.IsotonicRegression;
import weka.core.SerializationHelper;
import weka.core.Utils;

/**
 * classe que recebe como input as instancias já com todas as features
 * calculadas gera as predictions como output
 *
 * @author exam
 */
public class Process {

    private Instances instances;
    private Classifier classifiers[];
    private double[][] predictions;
    private double[] predictionsAvg;

    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        if (args.length < 3) {
            return;
        }

        if (!(new File(args[0]).isFile()) || !(new File(args[1]).isDirectory())) {
            return;
        }
        Process p = new Process();

        p.loadFeaturesFile(args[0]);
        p.loadModels(args[1]);
        p.calculatePredictions(true);
        p.savePredictions(args[2]);
    }

    public void calculateAveragePredictions() {
        PerformanceCounters.startTimer("calculateAveragePredictions");
        System.out.println("Calculating average of predictions...");
        if (predictionsAvg != null) {
            return;
        }

        predictionsAvg = new double[predictions[0].length];

        for (int j = 0; j < predictions[0].length; j++) {
            predictionsAvg[j]=0;
            for (int i = 0; i < predictions.length; i++) {
                predictionsAvg[j] += predictions[i][j];
            }
            predictionsAvg[j] /= predictions.length;
        }
        System.out.println("\tdone.");
        PerformanceCounters.stopTimer("calculateAveragePredictions");
    }
    
    public void calculateAverageSummary() {
        //TODO: calculate correlation, etc...
        
    }

//consider not using file system (streams...)
    public void loadFeaturesFile(String featuresFilename) {
        loadFeaturesFile(featuresFilename, "relatedness_groundtruth");
    }

//consider not using file system (streams...)
    public void loadFeaturesFile(String featuresFilename, String classAttributeName) {
        PerformanceCounters.startTimer("loadFeaturesFile");
        System.out.println("Loading features file...");
        try {
            DataSource source = new DataSource(featuresFilename);
            instances = source.getDataSet();
            // setting class attribute if the data format does not provide this information
            // For example, the XRFF format saves the class attribute information as well
            if (instances.classIndex() == -1) {
                instances.setClass(instances.attribute(classAttributeName));
            }

            /*Remove remove = new Remove();
             remove.setAttributeIndices("" + (data.attribute(idAttributeName).index()+1));
             remove.setInputFormat(data);
             data = Filter.useFilter(data, remove);*/
        } catch (Exception ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("\tdone.");
        PerformanceCounters.stopTimer("loadFeaturesFile");
    }

    public void loadModels(String modelsContainerPath) {
        PerformanceCounters.startTimer("loadModels");
        System.out.println("Loading weka models...");

        File folder = new File(modelsContainerPath);
        File[] listOfFiles = folder.listFiles(
                //JDK < 8:                
                new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return (file.getName().contains(".model") && !file.getName().contains(".empty"));
                    }
                });

        if (listOfFiles.length == 0) {

            System.out.println("\tNo models found. Assuming they have to be built...");
            createModels(modelsContainerPath);
            System.out.println("\tmodels built.");
            PerformanceCounters.stopTimer("loadModels");
            return;
        }

        classifiers = new Classifier[listOfFiles.length];
        Object obj;
        int failed = 0;
        for (int i = 0; i < listOfFiles.length; i++) {
            String modelFilename = listOfFiles[i].getAbsolutePath();
            try {
                obj = SerializationHelper.read(modelFilename);
            } catch (Exception ex) {
                Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
                failed++;
                continue;
            }
            if (obj instanceof AbstractClassifier) {
                AbstractClassifier abCl = (AbstractClassifier) obj;
                classifiers[i] = abCl;

                System.out.println("\tLoaded model : " + abCl.getClass().getName() + " "
                        + Utils.joinOptions(abCl.getOptions()));
            } else {
                System.out.println("\tModel filename given doesn't contain a valid built model!");
                failed++;
            }
        }
        //TODO: take care with models that failed to load. array is "corrupt" now.

        System.out.println("\tdone.");
        PerformanceCounters.stopTimer("loadModels");
    }

    public void calculatePredictions() {
        calculatePredictions(false);
    }
    public void calculatePredictions(boolean calculateAverage) {
        PerformanceCounters.startTimer("calculatePredictions");
        System.out.println("Calculating predictions with all models...");
        String idName = "pair_ID";
        String classAttributeName = "relatedness_groundtruth";
        instances.setClass(instances.attribute(classAttributeName));

        if (predictions != null) {
            PerformanceCounters.stopTimer("calculatePredictions");
            return;
        }
        predictions = new double[classifiers.length][];

        for (int i = 0; i < classifiers.length; i++) {
            Classifier classifier = classifiers[i];

            predictions[i] = evaluateModel((AbstractClassifier) classifier, instances);
        }
        
        if (calculateAverage) {
            calculateAveragePredictions();
        }
        
        System.out.println("\tpredictions calculated.");
        PerformanceCounters.stopTimer("calculatePredictions");
    }

    public void savePredictions(String outputFilename) {
        PerformanceCounters.startTimer("savePredictions");
        System.out.println("Saving predictions to file(s)...");
        String[] columnNames = {"pair_ID"};
        String outputPath = "./";
        String outputBaseFilename = outputFilename;

        if (outputFilename.contains("\\")) {
            outputPath = outputFilename.substring(0, outputFilename.lastIndexOf("\\")+1);
            outputBaseFilename = outputFilename.substring(outputFilename.lastIndexOf("\\")+1);
        }
        if (outputFilename.contains("/")) {
            outputPath = outputFilename.substring(0, outputFilename.lastIndexOf("/")+1);
            outputBaseFilename = outputFilename.substring(outputFilename.lastIndexOf("/")+1);
        }
        
        
        if (predictions.length == 0) {
            System.out.println("\tno predictions to save.");
            PerformanceCounters.stopTimer("savePredictions");
            return;
        }
        if (predictions.length == 1) {
            formatPredictions(predictions[0], columnNames, 1, "relatedness_score",
                    "\t", outputPath + outputBaseFilename);
        } else {
            for (int i = 0; i < predictions.length; i++) {
                formatPredictions(predictions[i], columnNames, 1, "relatedness_score",
                        "\t", outputPath + i + "-" + outputBaseFilename);
            }
        }
        
        if (predictionsAvg != null) {
            formatPredictions(predictionsAvg, columnNames, 1, "relatedness_score",
                    "\t", outputPath + "avg-" + outputBaseFilename);
        }
        System.out.println("\tpredictions saved.");
        PerformanceCounters.stopTimer("savePredictions");
    }

    /**
     * @param featuresFilename
     */
    public void runTests(String featuresFilename) {
        System.out.println("Running performance tests...");
        int runs = 10, i;
        

        for (i = 0; i < runs; i++) {
            System.out.println("\ttest iteration " + i);
            loadFeaturesFile(featuresFilename);
            createModels("src/weka-models");
            calculatePredictions(true);
            savePredictions(i + "-test/out");
            // reset variables for next iteration:
            classifiers = null;
            predictions = null;
            predictionsAvg = null;
        }
        System.out.println("\ttests done.");
    }

    private void formatPredictions(double[] predictions, String[] columnNames,
            int predictionsColumnIndex, String predictionsColumnName,
            String columnSeparator, String outputFilename) {
        PerformanceCounters.startTimer("formatPredictions");

        System.out.println("Formatting predictions to file " + outputFilename + "...");
        File outputFile = new File(outputFilename);
        PrintWriter writer;

        try {
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
            writer = new PrintWriter(outputFile, "UTF-8");
        } catch (IOException ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.US));
        df.setMaximumFractionDigits(3);

        for (int i = -1; i < instances.numInstances(); i++) {
            sb.delete(0, sb.length());

            for (int j = 0; j < columnNames.length; j++) {
                if (j > 0) {
                    sb.append(columnSeparator);
                }

                if (j == predictionsColumnIndex) {
                    if (i < 0) {
                        sb.append(predictionsColumnName);
                    } else {
                        sb.append(df.format(predictions[i]));
                    }
                    sb.append(columnSeparator);
                }
                if (i < 0) {
                    sb.append(columnNames[j]);
                } else {
                    if (columnNames[j].toLowerCase().contains("id")) {
                        sb.append((int) instances.instance(i).value(j));
                    } else {
                        sb.append(df.format(instances.instance(i).value(j)));
                    }
                }
            }

            if (columnNames.length == predictionsColumnIndex) {
                sb.append(columnSeparator);
                if (i < 0) {
                    sb.append(predictionsColumnName);
                } else {
                    sb.append(df.format(predictions[i]));
                }
            }

            writer.println(sb);
        }
        writer.flush();
        writer.close();
        System.out.println("\tdone.");
        PerformanceCounters.stopTimer("formatPredictions");
    }

    private static double[] evaluateModel(AbstractClassifier cl, Instances data) {
        PerformanceCounters.startTimer("evaluateModel");
        System.out.println("Evaluating model...");
        double[] predictions = null;

        try {
            // evaluate classifier and print some statistics
            Evaluation eval = new Evaluation(data);

            predictions = eval.evaluateModel(cl, data);
            System.out.println("\tstats for model:" + cl.getClass().getName() + " "
                    + Utils.joinOptions(cl.getOptions()));
            System.out.println(eval.toSummaryString());
        } catch (Exception ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("\tevaluation done.");
        PerformanceCounters.stopTimer("evaluateModel");
        return predictions;
    }

    public void processFile(String preprocessedFilename, String outputFilename) {
        PerformanceCounters.startTimer("processFile");
        String modelsContainerDirectory = "src/weka-models";
        loadFeaturesFile(preprocessedFilename);
        loadModels(modelsContainerDirectory);
        calculatePredictions(true);
        savePredictions(outputFilename);
        PerformanceCounters.stopTimer("processFile");
    }

    private void createModels(String modelsContainerPath) {
        PerformanceCounters.startTimer("createModels");
        System.out.println("Creating models...");
        classifiers = new Classifier[4];
        createTestModel1();
        createTestModel2();
        createTestModel3();
        createTestModel4();
        System.out.println("\tall models created.");
        PerformanceCounters.stopTimer("createModels");
        
    }

    private void createTestModel1() {
        PerformanceCounters.startTimer("createTestModel1");
        System.out.println("Creating test model 1...");
        Stacking stack = new Stacking();

        Classifier[] baseClassifiers = new Classifier[3];
        //String[] options = new String[1];
        M5P tree = new M5P();         // new instance of tree
        IBk knn = new IBk(1);
        LinearRegression lr = new LinearRegression();
        M5P metatree = new M5P();         // new instance of tree

        //options[0] = "-U";            // unpruned tree
        baseClassifiers[0] = tree;
        baseClassifiers[1] = knn;
        baseClassifiers[2] = lr;
        //tree.setOptions(options);     // set the options

        stack.setClassifiers(baseClassifiers);
        stack.setMetaClassifier(metatree);

        try {
            stack.buildClassifier(instances);
        } catch (Exception ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        classifiers[0] = stack;
        System.out.println("\tdone.");
        PerformanceCounters.stopTimer("createTestModel1");
    }

    private void createTestModel2() {
        PerformanceCounters.startTimer("createTestModel2");
        System.out.println("Creating test model 2...");
        Stacking stack = new Stacking();

        Classifier[] baseClassifiers = new Classifier[5];
        String[] options = new String[2];
        M5P tree = new M5P();         // new instance of tree
        IBk knn = new IBk(1);
        LinearRegression lr = new LinearRegression();
        ZeroR zeroR = new ZeroR();

        IsotonicRegression isotonicRegression = new IsotonicRegression();
        M5P metatree = new M5P();         // new instance of tree

        baseClassifiers[0] = tree;
        baseClassifiers[1] = knn;
        baseClassifiers[2] = lr;
        baseClassifiers[3] = zeroR;
        baseClassifiers[4] = isotonicRegression;

        options[0] = "-M";            // unpruned tree
        options[1] = "4";            // unpruned tree
        try {
            tree.setOptions(options);     // set the options
        } catch (Exception ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }

        stack.setClassifiers(baseClassifiers);
        stack.setMetaClassifier(metatree);

        try {
            stack.buildClassifier(instances);
        } catch (Exception ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        classifiers[1] = stack;
        System.out.println("\tdone.");
        PerformanceCounters.stopTimer("createTestModel2");
    }

    private void createTestModel3() {
        PerformanceCounters.startTimer("createTestModel3");
        System.out.println("Creating test model 3...");
        Vote vote = new Vote();

        Classifier[] baseClassifiers = new Classifier[7];
        String[] options = new String[2];
        M5P tree = new M5P();         // new instance of tree
        IBk knn = new IBk(1);
        M5P tree2 = new M5P();         // new instance of tree
        IBk knn2 = new IBk(3);
        M5P tree3 = new M5P();         // new instance of tree
        IBk knn3 = new IBk(5);
        LinearRegression lr = new LinearRegression();

        baseClassifiers[0] = tree;
        baseClassifiers[1] = knn;
        baseClassifiers[2] = tree2;
        baseClassifiers[3] = knn2;
        baseClassifiers[4] = tree3;
        baseClassifiers[5] = knn3;
        baseClassifiers[6] = lr;

        options[0] = "-M";            // ...
        options[1] = "4";            // number of ...
        try {
            tree.setOptions(options);     // set the options
        } catch (Exception ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }

        options = new String[3];
        options[0] = "-M";            // ...
        options[1] = "10";            // number of ...
        options[2] = "-R";           //unpruned tree
        try {
            tree2.setOptions(options);     // set the options
        } catch (Exception ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }

        options = new String[2];
        options[0] = "-M";            // ...
        options[1] = "20";            // number of ...
        try {
            tree3.setOptions(options);     // set the options
        } catch (Exception ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }

        vote.setClassifiers(baseClassifiers);

        try {
            vote.buildClassifier(instances);
        } catch (Exception ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        classifiers[2] = vote;
        System.out.println("\tdone.");
        PerformanceCounters.stopTimer("createTestModel3");
    }

    private void createTestModel4() {
        PerformanceCounters.startTimer("createTestModel4");
        System.out.println("Creating test model 4...");
        Stacking stack = new Stacking();

        Classifier[] baseClassifiers = new Classifier[7];

        String[] options = new String[2];
        M5P tree = new M5P();         // new instance of tree
        IBk knn = new IBk(1);
        M5P tree2 = new M5P();         // new instance of tree
        IBk knn2 = new IBk(3);
        M5P tree3 = new M5P();         // new instance of tree
        IBk knn3 = new IBk(5);
        LinearRegression lr = new LinearRegression();

        M5P metatree = new M5P();         // new instance of tree

        baseClassifiers[0] = tree;
        baseClassifiers[1] = knn;
        baseClassifiers[2] = tree2;
        baseClassifiers[3] = knn2;
        baseClassifiers[4] = tree3;
        baseClassifiers[5] = knn3;
        baseClassifiers[6] = lr;

        options[0] = "-M";            // unpruned tree
        options[1] = "4";            // unpruned tree
        try {
            tree.setOptions(options);     // set the options
        } catch (Exception ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }

        options = new String[3];
        options[0] = "-M";            // ...
        options[1] = "10";            // number of ...
        options[2] = "-R";           //unpruned tree
        try {
            tree2.setOptions(options);     // set the options
        } catch (Exception ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }

        options = new String[2];
        options[0] = "-M";            // unpruned tree
        options[1] = "20";            // unpruned tree
        try {
            tree3.setOptions(options);     // set the options
        } catch (Exception ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }

        stack.setClassifiers(baseClassifiers);
        stack.setMetaClassifier(metatree);

        try {
            stack.buildClassifier(instances);
        } catch (Exception ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        classifiers[3] = stack;
        System.out.println("\tdone.");
        PerformanceCounters.stopTimer("createTestModel4");
    }
}
