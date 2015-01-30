/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.RemoveType;

/**
 * classe que recebe como input as instancias já com todas as features
 * calculadas gera as predictions como output
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt)
 */
public class PostProcess {

    //TODO: to remove after pearson's correlation is calculated without the Perl script
    private String goldStandardFile;

    private List<NLPSystem> systems = null;

    /**
     *
     */
    public PostProcess() {
//        classifiers = new LinkedList<>();
        systems = new LinkedList<>();
    }

//    /**
//     *
//     */
//    public void clear() {
//        classifiers = new LinkedList<>();
//        predictions = null;
//        predictionsAvg = null;
//        predictionsFiles = null;
//    }
//    /**
//     *
//     */
//    public void calculateAveragePredictions() {
//        PerformanceCounters.startTimer("calculateAveragePredictions");
//        System.out.println("Calculating average of predictions...");
//        if (predictionsAvg != null || predictions == null) {
//            return;
//        }
//
//        predictionsAvg = new double[predictions[0].length];
//
//        for (int j = 0; j < predictions[0].length; j++) {
//            predictionsAvg[j] = 0;
//            for (int i = 0; i < predictions.length; i++) {
//                predictionsAvg[j] += predictions[i][j];
//            }
//            predictionsAvg[j] /= predictions.length;
//        }
//        System.out.println("\tdone.");
//        PerformanceCounters.stopTimer("calculateAveragePredictions");
//    }
    /**
     *
     */
    public void calculateAverageSummary() {
        //TODO: calculate correlation, etc...
        throw new UnsupportedOperationException();
    }

//    /**
//     *
//     * @param featuresFilename
//     */
//    public void loadFeaturesFile(String featuresFilename) {
//        loadFeaturesFile(featuresFilename, "gold_standard");
//    }
//
//    /**
//     *
//     * @param featuresFilename
//     * @param classAttributeName
//     */
//    public void loadFeaturesFile(String featuresFilename, String classAttributeName) {
//        PerformanceCounters.startTimer("loadFeaturesFile");
//        System.out.println("Loading features file...");
//        try {
//            DataSource source = new DataSource(featuresFilename);
//            instancesTrainingSet = source.getDataSet();
//            // setting class attribute if the data format does not provide this information
//            // For example, the XRFF format saves the class attribute information as well
//            if (instancesTrainingSet.classIndex() == -1) {
//                instancesTrainingSet.setClass(instancesTrainingSet.attribute(classAttributeName));
//            }
//
//        } catch (Exception ex) {
//            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        System.out.println("\tdone.");
//        PerformanceCounters.stopTimer("loadFeaturesFile");
//    }
    /**
     *
     * @param modelsContainerPath
     */
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

        if (listOfFiles == null ? true : listOfFiles.length == 0) {
            System.out.println("\tNo models found. Can't test without prior model building and training!");
            PerformanceCounters.stopTimer("loadModels");
            throw new RuntimeException("Can't test - no models found.");
        }

        Object obj;
        for (File listOfFile : listOfFiles) {
            String modelFilename = listOfFile.getAbsolutePath();
            try {
                obj = SerializationHelper.read(modelFilename);
            } catch (Exception ex) {
                Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            if (obj instanceof AbstractClassifier) {
                AbstractClassifier abCl = (AbstractClassifier) obj;
//                classifiers.add(abCl);

                System.out.println("\tLoaded model : " + abCl.getClass().getName() + " "
                        + Utils.joinOptions(abCl.getOptions()));
            } else {
                System.out.println("\tModel filename given doesn't contain a valid built model!");
            }
        }

        System.out.println("\tdone.");
        PerformanceCounters.stopTimer("loadModels");
    }

//    /**
//     *
//     */
//    public void calculatePredictions() {
//        calculatePredictions(false, true);
//    }
//    /**
//     *
//     * @param calculateAverage
//     * @param printEvaluation
//     */
//    public void calculatePredictions(boolean calculateAverage, boolean printEvaluation) {
//        PerformanceCounters.startTimer("calculatePredictions");
//        System.out.println("Calculating predictions with all models...");
//        String idName = "pair_ID";
//        String classAttributeName = "gold_standard";
//        Attribute idAttribute = instancesTrainingSet.attribute(idName);
//        instancesTrainingSet.setClass(instancesTrainingSet.attribute(classAttributeName));
//
//        Remove removeFilter = new Remove();
//        removeFilter.setAttributeIndices("" + idAttribute.index() + 1);
//
//        RemoveType removeTypeFilter = new RemoveType();
//        String[] removeTypeFilterOptions = {"-T", "string"};
//
//        Instances evaluationSet;
//        try {
//            removeFilter.setInputFormat(instancesTrainingSet);
//            evaluationSet = Filter.useFilter(instancesTrainingSet, removeFilter);
//
//            removeTypeFilter.setInputFormat(evaluationSet);
//            removeTypeFilter.setOptions(removeTypeFilterOptions);
//            evaluationSet = Filter.useFilter(evaluationSet, removeTypeFilter);
//        } catch (Exception ex) {
//            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//            return;
//        }
//
//        evaluationSet.setClassIndex(evaluationSet.attribute("gold_standard").index());
//
//        if (predictions != null) {
//            PerformanceCounters.stopTimer("calculatePredictions");
//            return;
//        }
//        predictions = new double[classifiers.size()][];
//
//        for (int i = 0; i < classifiers.size(); i++) {
//            Classifier classifier = classifiers.get(i);
//
//            predictions[i] = evaluateModel((AbstractClassifier) classifier, evaluationSet, printEvaluation);
//
//        }
//
//        if (calculateAverage) {
//            calculateAveragePredictions();
//        }
//
//        roundPredictions();
//
//        System.out.println(String.format("\tpredictions calculated for %d instancesTrainingSet.", evaluationSet.size()));
//        PerformanceCounters.stopTimer("calculatePredictions");
//    }
    /**
     *
     * @param outputFilename
     */
    public void savePredictionsSemeval2015Task2Format(Instances instances, double[][] predictions, String outputFilename) {
        PerformanceCounters.startTimer("savePredictions");
        System.out.println("Saving predictions to file(s)...");
        String[] columnNames = {};
        String outputPath = "./";
        String outputBaseFilename = outputFilename;

        if (outputFilename.contains("\\")) {
            outputPath = outputFilename.substring(0, outputFilename.lastIndexOf("\\") + 1);
            outputBaseFilename = outputFilename.substring(outputFilename.lastIndexOf("\\") + 1);
        }
        if (outputFilename.contains("/")) {
            outputPath = outputFilename.substring(0, outputFilename.lastIndexOf("/") + 1);
            outputBaseFilename = outputFilename.substring(outputFilename.lastIndexOf("/") + 1);
        }

        if (predictions == null ? true : predictions.length == 0) {
            System.out.println("\tno predictions to save.");
            PerformanceCounters.stopTimer("savePredictions");
            return;
        }
        if (predictions.length == 1) {
            formatPredictions(instances, predictions[0], columnNames, 0, "relatedness_score",
                    "\t", outputPath + outputBaseFilename, false);
        } else {
            for (int i = 0; i < predictions.length; i++) {
                formatPredictions(instances, predictions[i], columnNames, 0, "relatedness_score",
                        "\t", outputPath + i + "-" + outputBaseFilename, false);
            }
        }
//
//        if (predictionsAvg != null) {
//            formatPredictions(predictionsAvg, columnNames, 0, "relatedness_score",
//                    "\t", outputPath + "avg-" + outputBaseFilename, false);
//
//            predictionsFiles = new String[predictions.length + 1];
//            for (int i = 0; i < predictions.length; i++) {
//                predictionsFiles[i] = outputPath + i + "-" + outputBaseFilename;
//            }
//            predictionsFiles[predictions.length] = outputPath + "avg-" + outputBaseFilename;
//
//        } else {
//
//            predictionsFiles = new String[predictions.length];
//            for (int i = 0; i < predictions.length; i++) {
//                predictionsFiles[i] = outputPath + i + "-" + outputBaseFilename;
//            }
//        }

        if (Config.logPredictionsErrors()) {
            for (int i = 0; i < predictions.length; i++) {
                File f = new File(Config.getLogPredictionsErrorsOutputDir());
                f.mkdirs();
                String errorsFilename = f.getPath() + File.separatorChar + i + outputBaseFilename;

                writePredictionErrors(instances, predictions[i], errorsFilename);
            }
        }

        System.out.println("\tpredictions saved.");
        PerformanceCounters.stopTimer("savePredictions");
    }

    /**
     *
     * @param outputFilename
     */
    public void savePredictionsSemeval2014Task1Format(Instances instances, double[][] predictions, String outputFilename) {
        PerformanceCounters.startTimer("savePredictions");
        System.out.println("Saving predictions to file(s)...");
        String[] columnNames = {"pair_ID"};
        String outputPath = "./";
        String outputBaseFilename = outputFilename;

        if (outputFilename.contains("\\")) {
            outputPath = outputFilename.substring(0, outputFilename.lastIndexOf("\\") + 1);
            outputBaseFilename = outputFilename.substring(outputFilename.lastIndexOf("\\") + 1);
        }
        if (outputFilename.contains("/")) {
            outputPath = outputFilename.substring(0, outputFilename.lastIndexOf("/") + 1);
            outputBaseFilename = outputFilename.substring(outputFilename.lastIndexOf("/") + 1);
        }

        if (predictions == null ? true : predictions.length == 0) {
            System.out.println("\tno predictions to save.");
            PerformanceCounters.stopTimer("savePredictions");
            return;
        }
        if (predictions.length == 1) {
            formatPredictions(instances, predictions[0], columnNames, 1, "relatedness_score",
                    "\t", outputPath + outputBaseFilename, true);
        } else {
            for (int i = 0; i < predictions.length; i++) {
                formatPredictions(instances, predictions[i], columnNames, 1, "relatedness_score",
                        "\t", outputPath + i + "-" + outputBaseFilename, true);
            }
        }

//        if (predictionsAvg != null) {
//            formatPredictions(predictionsAvg, columnNames, 1, "relatedness_score",
//                    "\t", outputPath + "avg-" + outputBaseFilename, true);
//
//            predictionsFiles = new String[predictions.length + 1];
//            for (int i = 0; i < predictions.length; i++) {
//                predictionsFiles[i] = outputPath + i + "-" + outputBaseFilename;
//            }
//            predictionsFiles[predictions.length] = outputPath + "avg-" + outputBaseFilename;
//
//        } else {
//
//            predictionsFiles = new String[predictions.length];
//            for (int i = 0; i < predictions.length; i++) {
//                predictionsFiles[i] = outputPath + i + "-" + outputBaseFilename;
//            }
//        }
        if (Config.logPredictionsErrors()) {
            for (int i = 0; i < predictions.length; i++) {
                File f = new File(Config.getLogPredictionsErrorsOutputDir());
                f.mkdirs();
                String errorsFilename = f.getPath() + File.separatorChar + i + outputBaseFilename;

                writePredictionErrors(instances, predictions[i], errorsFilename);
            }
        }

        System.out.println("\tpredictions saved.");
        PerformanceCounters.stopTimer("savePredictions");
    }

//    /**
//     * @param featuresFilename
//     */
//    public void runBenchmark(String featuresFilename) {
//        System.out.println("Running performance tests...");
//        int runs = 10, i;
//
//        for (i = 0; i < runs; i++) {
//            System.out.println("\ttest iteration " + i);
//            loadFeaturesFile(featuresFilename);
//            buildModelsTo("weka-models");
//            calculatePredictions(true, false);
//            savePredictionsSemeval2014Task1Format("outputs/predictions/" + i + "-test/out");
//            calculatePearsonsCorrelations();
//            // reset variables for next iteration:
//            clear();
//        }
//        System.out.println("\ttests done.");
//    }
    private void formatPredictions(Instances instances, double[] predictions, String[] columnNames, int predictionsColumnIndex, String predictionsColumnName, String columnSeparator, String outputFilename, boolean writeColumnsHeaderLine) {
        PerformanceCounters.startTimer("formatPredictions");

        System.out.println("Formatting predictions to file " + outputFilename + "...");
        File outputFile = new File(outputFilename);
        PrintWriter writer;

        try {
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
            writer = new PrintWriter(outputFile, "UTF-8");
        } catch (IOException ex) {
            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.US));
        df.setMaximumFractionDigits(3);

        int i = -1;
        if (!writeColumnsHeaderLine) {
            i = 0;
        }
        for (; i < instances.numInstances(); i++) {
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
                        Attribute attribute = instances.attribute(columnNames[j]);
                        if (attribute != null) {
                            sb.append((int) instances.instance(i).value(attribute.index()));
                        } else {
                            sb.append(0);
                        }
                    } else {
                        Attribute attribute = instances.attribute(columnNames[j]);
                        if (attribute != null) {
                            sb.append(instances.instance(i).value(attribute.index()));
                        } else {
                            sb.append(df.format(0d));
                        }
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

    private static double[] evaluateModel(AbstractClassifier cl, Instances data, boolean printEvaluation) {
        PerformanceCounters.startTimer("evaluateModel");
        System.out.println("Evaluating model...");
        double[] predictions = null;

        try {
            // evaluate classifier and print some statistics
            Evaluation eval = new Evaluation(data);

            predictions = eval.evaluateModel(cl, data);

            if (printEvaluation) {
                System.out.println("\tstats for model:" + cl.getClass().getName() + " "
                        + Utils.joinOptions(cl.getOptions()));
                System.out.println(eval.toSummaryString());
            }
        } catch (Exception ex) {
            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("\tevaluation done.");
        PerformanceCounters.stopTimer("evaluateModel");
        return predictions;
    }

//    /**
//     *
//     * @param preprocessedFilename
//     * @param outputFilename
//     */
//    public void buildModelsFromFile(String preprocessedFilename, String outputFilename) {
//        PerformanceCounters.startTimer("buildModelsFromFile");
//        String modelsContainerDirectory = "weka-models";
//        loadFeaturesFile(preprocessedFilename);
//        //loadModels(modelsContainerDirectory);
//        buildModelsTo(modelsContainerDirectory);
//        calculatePredictions(true, false);
//        savePredictionsSemeval2014Task1Format(outputFilename);
//        calculatePearsonsCorrelations();
//        PerformanceCounters.stopTimer("buildModelsFromFile");
//    }
//    /**
//     *
//     * @param modelsContainerPath
//     */
//    public void buildModelsTo(String modelsContainerPath) {
//        PerformanceCounters.startTimer("buildModelsTo");
//
//        Attribute idAttribute = instancesTrainingSet.attribute("pair_ID");
//        Remove removeFilter = new Remove();
//        removeFilter.setAttributeIndices("" + idAttribute.index() + 1);
//
//        RemoveType removeTypeFilter = new RemoveType();
//        String[] removeTypeFilterOptions = {"-T", "string"};
//
//        Instances trainSet;
//        try {
//            removeFilter.setInputFormat(instancesTrainingSet);
//            trainSet = Filter.useFilter(instancesTrainingSet, removeFilter);
//
//            removeTypeFilter.setInputFormat(trainSet);
//            removeTypeFilter.setOptions(removeTypeFilterOptions);
//            trainSet = Filter.useFilter(trainSet, removeTypeFilter);
//
//        } catch (Exception ex) {
//            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//            return;
//        }
//
//        trainSet.setClassIndex(trainSet.attribute("gold_standard").index());
//
//        System.out.println("Creating models...");
//        createTestModel1(trainSet);
//        createTestModel2(trainSet);
//        createTestModel3(trainSet);
//        createTestModel4(trainSet);
//        System.out.println("\tall models created.");
//        saveModels(modelsContainerPath);
//        PerformanceCounters.stopTimer("buildModelsTo");
//    }
//    private void createTestModel1(Instances trainSet) {
//        PerformanceCounters.startTimer("createTestModel1");
//        System.out.println("Creating test model 1...");
//        Stacking stack = new Stacking();
//
//        Classifier[] baseClassifiers = new Classifier[3];
//        String[] options = new String[1];
//        M5P tree = new M5P();         // new instance of tree
//        IBk knn = new IBk(1);
//        LinearRegression lr = new LinearRegression();
//        M5P metatree = new M5P();         // new instance of tree
//
//        options[0] = "-R";
//        baseClassifiers[0] = tree;
//        baseClassifiers[1] = knn;
//        baseClassifiers[2] = lr;
//        try {
//            tree.setOptions(options);     // set the options
//        } catch (Exception ex) {
//            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        stack.setClassifiers(baseClassifiers);
//        stack.setMetaClassifier(metatree);
//
//        int seed = 0;
//        int folds = 10;
//        System.out.println(
//                CrossValidation.performCrossValidationMT(trainSet, stack,
//                        seed, folds, null)
//        );
//        /*
//         try {
//         stack.buildClassifier(trainSet);
//         } catch (Exception ex) {
//         Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//         }
//         */
//
//        classifiers.add(stack);
//        System.out.println("\tdone.");
//        PerformanceCounters.stopTimer("createTestModel1");
//    }
//
//    private void createTestModel2(Instances trainSet) {
//        PerformanceCounters.startTimer("createTestModel2");
//        System.out.println("Creating test model 2...");
//        Stacking stack = new Stacking();
//
//        Classifier[] baseClassifiers = new Classifier[5];
//        String[] options = new String[3];
//        M5P tree = new M5P();         // new instance of tree
//        IBk knn = new IBk(1);
//        LinearRegression lr = new LinearRegression();
//        ZeroR zeroR = new ZeroR();
//
//        IsotonicRegression isotonicRegression = new IsotonicRegression();
//        M5P metatree = new M5P();         // new instance of tree
//
//        baseClassifiers[0] = tree;
//        baseClassifiers[1] = knn;
//        baseClassifiers[2] = lr;
//        baseClassifiers[3] = zeroR;
//        baseClassifiers[4] = isotonicRegression;
//
//        options[0] = "-M";            // unpruned tree
//        options[1] = "4";            // unpruned tree
//        options[2] = "-R";
//        try {
//            tree.setOptions(options);     // set the options
//        } catch (Exception ex) {
//            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        stack.setClassifiers(baseClassifiers);
//        stack.setMetaClassifier(metatree);
//
//        int seed = 0;
//        int folds = 10;
//        System.out.println(
//                CrossValidation.performCrossValidationMT(trainSet, stack,
//                        seed, folds, null)
//        );
//        /*
//         try {
//         stack.buildClassifier(trainSet);
//         } catch (Exception ex) {
//         Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//         }
//         */
//
//        classifiers.add(stack);
//        System.out.println("\tdone.");
//        PerformanceCounters.stopTimer("createTestModel2");
//    }
//
//    private void createTestModel3(Instances trainSet) {
//        PerformanceCounters.startTimer("createTestModel3");
//        System.out.println("Creating test model 3...");
//        Vote vote = new Vote();
//
//        Classifier[] baseClassifiers = new Classifier[7];
//        String[] options = new String[3];
//        M5P tree = new M5P();         // new instance of tree
//        IBk knn = new IBk(1);
//        M5P tree2 = new M5P();         // new instance of tree
//        IBk knn2 = new IBk(3);
//        M5P tree3 = new M5P();         // new instance of tree
//        IBk knn3 = new IBk(5);
//        LinearRegression lr = new LinearRegression();
//
//        baseClassifiers[0] = tree;
//        baseClassifiers[1] = knn;
//        baseClassifiers[2] = tree2;
//        baseClassifiers[3] = knn2;
//        baseClassifiers[4] = tree3;
//        baseClassifiers[5] = knn3;
//        baseClassifiers[6] = lr;
//
//        options[0] = "-M";            // ...
//        options[1] = "4";            // number of ...
//        options[2] = "-R";
//        try {
//            tree.setOptions(options);     // set the options
//        } catch (Exception ex) {
//            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        options = new String[3];
//        options[0] = "-M";            // ...
//        options[1] = "10";            // number of ...
//        options[2] = "-R";           //unpruned tree
//        try {
//            tree2.setOptions(options);     // set the options
//        } catch (Exception ex) {
//            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        options = new String[3];
//        options[0] = "-M";            // ...
//        options[1] = "20";            // number of ...
//        options[2] = "-R";
//        try {
//            tree3.setOptions(options);     // set the options
//        } catch (Exception ex) {
//            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        vote.setClassifiers(baseClassifiers);
//
//        int seed = 0;
//        int folds = 10;
//        System.out.println(
//                CrossValidation.performCrossValidationMT(trainSet, vote,
//                        seed, folds, null)
//        );
//        /*
//         try {
//         stack.buildClassifier(trainSet);
//         } catch (Exception ex) {
//         Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//         }
//         */
//
//        classifiers.add(vote);
//        System.out.println("\tdone.");
//        PerformanceCounters.stopTimer("createTestModel3");
//    }
//
//    private void createTestModel4(Instances trainSet) {
//        PerformanceCounters.startTimer("createTestModel4");
//        System.out.println("Creating test model 4...");
//        Stacking stack = new Stacking();
//
//        Classifier[] baseClassifiers = new Classifier[7];
//
//        String[] options = new String[3];
//        M5P tree = new M5P();         // new instance of tree
//        IBk knn = new IBk(1);
//        M5P tree2 = new M5P();         // new instance of tree
//        IBk knn2 = new IBk(3);
//        M5P tree3 = new M5P();         // new instance of tree
//        IBk knn3 = new IBk(5);
//        LinearRegression lr = new LinearRegression();
//
//        M5P metatree = new M5P();         // new instance of tree
//
//        baseClassifiers[0] = tree;
//        baseClassifiers[1] = knn;
//        baseClassifiers[2] = tree2;
//        baseClassifiers[3] = knn2;
//        baseClassifiers[4] = tree3;
//        baseClassifiers[5] = knn3;
//        baseClassifiers[6] = lr;
//
//        options[0] = "-M";            // unpruned tree
//        options[1] = "4";            // unpruned tree
//        options[2] = "-R";
//        try {
//            tree.setOptions(options);     // set the options
//        } catch (Exception ex) {
//            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        options = new String[3];
//        options[0] = "-M";            // ...
//        options[1] = "10";            // number of ...
//        options[2] = "-R";           //unpruned tree
//        try {
//            tree2.setOptions(options);     // set the options
//        } catch (Exception ex) {
//            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        options = new String[3];
//        options[0] = "-M";            // unpruned tree
//        options[1] = "20";            // unpruned tree
//        options[2] = "-R";
//        try {
//            tree3.setOptions(options);     // set the options
//        } catch (Exception ex) {
//            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        stack.setClassifiers(baseClassifiers);
//        stack.setMetaClassifier(metatree);
//
//        int seed = 0;
//        int folds = 10;
//        System.out.println(
//                CrossValidation.performCrossValidationMT(trainSet, stack,
//                        seed, folds, null)
//        );
//        /*
//         try {
//         stack.buildClassifier(trainSet);
//         } catch (Exception ex) {
//         Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//         }
//         */
//
//        classifiers.add(stack);
//        System.out.println("\tdone.");
//        PerformanceCounters.stopTimer("createTestModel4");
//    }
//    /**
//     *
//     * @param preprocessedFilename
//     * @param outputFilename
//     * @param modelsDirectory
//     */
//    public void loadModelsAndTestFile(String preprocessedFilename, String outputFilename, String modelsDirectory) {
//        PerformanceCounters.startTimer("loadModelsAndTestFile");
//        loadModels(modelsDirectory);
//        calculatePredictions(true, true);
//        savePredictionsSemeval2014Task1Format(outputFilename);
//        calculatePearsonsCorrelations();
//        PerformanceCounters.stopTimer("loadModelsAndTestFile");
//    }
//    /**
//     *
//     * @param preprocessedFilename
//     * @param outputFilename
//     */
//    public void loadModelsAndTestFile(String preprocessedFilename, String outputFilename) {
//        loadModelsAndTestFile(preprocessedFilename, outputFilename, "weka-models");
//    }
//
//    private void saveModels(String modelsContainerPath) {
//        PerformanceCounters.startTimer("saveModels");
//        for (int i = 0; i < classifiers.size(); i++) {
//            AbstractClassifier classifier = (AbstractClassifier) classifiers.get(i);
//
//            String filename = i + classifier.getClass().getName();
//            try {
//                SerializationHelper.write(modelsContainerPath + File.separator + filename, classifier);
//            } catch (Exception ex) {
//                Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//
//        PerformanceCounters.stopTimer("saveModels");
//    }
    private void generateGoldStandardFile(Instances instances) {
        File tmp;
        try {
            tmp = File.createTempFile("input", ".tmp", new File("."));
            tmp.deleteOnExit();
        } catch (IOException ex) {
            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        goldStandardFile = tmp.getAbsolutePath();

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(tmp);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        for (weka.core.Instance instance : instances) {
            try {
                fos.write((instance.classValue() + "\n").getBytes());
            } catch (IOException ex) {
                Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        }
        try {
            fos.flush();
            fos.close();
        } catch (IOException ex) {
            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     */
    public void calculatePearsonsCorrelations(Instances instances, String[] predictionsFiles) {

        generateGoldStandardFile(instances);
        NumberFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(4);

        double maxCorrelation = Double.NEGATIVE_INFINITY;
        String bestPredictedFile = "";

        for (String predictionsFile : predictionsFiles) {
            double correlation = PerlCorrelation
                    .getCorrelation(goldStandardFile, predictionsFile);

            if (maxCorrelation < correlation) {
                maxCorrelation = correlation;
                bestPredictedFile = predictionsFile;
            }

            System.out.println("Pearson's Correlation Coefficient ("
                    + predictionsFile + "):"
                    + nf.format(correlation)
                    + "\n");

        }

        System.out.println("Best correlation (model " + bestPredictedFile + ") = " + nf.format(maxCorrelation));
    }

//    /**
//     *
//     * @param pposTrainingData
//     */
//    public void loadFeaturesStream(PreProcessOutputStream pposTrainingData) {
//        loadFeaturesStream(pposTrainingData, "gold_standard");
//    }
//
//    /**
//     *
//     * @param pposTrainingData
//     * @param classAttributeName
//     */
//    public void loadFeaturesStream(PreProcessOutputStream pposTrainingData, String classAttributeName) {
//        PerformanceCounters.startTimer("loadFeaturesStream");
//        System.out.print("Loading features stream...");
//        try {
//            DataSource source = new DataSource(pposTrainingData);
//            instancesTrainingSet = source.getDataSet();
//            // setting class attribute if the data format does not provide this information
//            // For example, the XRFF format saves the class attribute information as well
//            if (instancesTrainingSet.classIndex() == -1) {
//                instancesTrainingSet.setClass(instancesTrainingSet.attribute(classAttributeName));
//            }
//
//            /*Remove remove = new Remove();
//             remove.setAttributeIndices("" + (data.attribute(idAttributeName).index()+1));
//             remove.setInputFormat(data);
//             data = Filter.useFilter(data, remove);*/
//        } catch (Exception ex) {
//            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        System.out.println("\tdone.");
//        PerformanceCounters.stopTimer("loadFeaturesStream");
//    }
    public void buildModels(String modelDirectory) {
        File dir = new File(modelDirectory);
        if (!dir.isDirectory() && dir.exists()) {
            throw new IllegalArgumentException("Given path is not a directory (" + modelDirectory + ")!");
        }
        
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        for (NLPSystem system : systems) {
            String systemFilename = system.toString().replaceAll("@", "_");
            system.buildClassifier();
            system.saveSystem(dir, systemFilename);
        }
    }

//    /**
//     *
//     * @param modelDirectory
//     */
//    public void buildModels(Instances instances, String modelDirectory) {
//        int i = 0;
//
//        Attribute idAttribute = instances.attribute("pair_ID");
//        Remove removeFilter = new Remove();
//        removeFilter.setAttributeIndices("" + idAttribute.index() + 1);
//        RemoveType removeTypeFilter = new RemoveType();
//        String[] removeTypeFilterOptions = {"-T", "string"};
//
//        Instances trainSet;
//        try {
//            removeFilter.setInputFormat(instances);
//            trainSet = Filter.useFilter(instances, removeFilter);
//
//            removeTypeFilter.setInputFormat(trainSet);
//            removeTypeFilter.setOptions(removeTypeFilterOptions);
//            trainSet = Filter.useFilter(trainSet, removeTypeFilter);
//
//        } catch (Exception ex) {
//            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
//            return;
//        }
//
//        trainSet.setClassIndex(trainSet.attribute("gold_standard").index());
//
//        File f = new File(modelDirectory);
//        f.mkdirs();
//
//        //TODO: rest...
//    }
//
//    /**
//     *
//     */
//    public void clearInput() {
//        instancesTrainingSet = null;
//        goldStandardFile = null;
//        predictions = null;
//        predictionsAvg = null;
//        predictionsFiles = null;
//    }

    private void writePredictionErrors(Instances instances, double[] predictions, String errorsFilename) {

        TreeSet<PredictionError> errors = new TreeSet<>();

        for (int i = 0; i < predictions.length; i++) {
            double prediction = predictions[i];
            double expected = instances.get(i).classValue();
            int pairId = (int) instances.get(i).value(instances.attribute("pair_ID"));
            String sourceFile = instances.get(i).stringValue(instances.attribute("source_file"));
            PredictionError pe = new PredictionError(prediction, expected,
                    pairId, sourceFile, instances.get(i));

            //if (pe.getError()>=0.5d)
            errors.add(pe);
        }

        StringBuilder sb = new StringBuilder();

        for (PredictionError error : errors) {
            sb.append(error.toString())
                    .append("\n");
        }

        File f = new File(errorsFilename);
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(sb.toString().getBytes());
        } catch (IOException ex) {
            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void roundPredictions(double[][] predictions) {

        for (int i = 0; i < predictions.length; i++) {
            double[] prediction = predictions[i];
            for (int j = 0; j < prediction.length; j++) {
                double q = prediction[j];
                predictions[i][j] = Double.parseDouble(String.format("%.3f", q));
            }
        }
    }

    public void loadTrainingDataStream(PreProcessOutputStream pposTrainingData) {
        Instances instancesTrainingSet = null;
        Instances filteredInstancesTrainingSet = null;

        DataSource source = new DataSource(pposTrainingData);
        try {
            instancesTrainingSet = source.getDataSet();

        } catch (Exception ex) {
            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        // setting class attribute if the data format does not provide this information
        if (instancesTrainingSet.classIndex() == -1) {
            instancesTrainingSet.setClass(instancesTrainingSet.attribute("gold_standard"));
        }

        Remove remove = new Remove();
        remove.setAttributeIndices("1");
        try {
            remove.setInputFormat(instancesTrainingSet);
            filteredInstancesTrainingSet = Filter.useFilter(instancesTrainingSet, remove);
        } catch (Exception ex) {
            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (String wekaModelsCmd : Config.getWekaModelsCmd()) {
            String[] classifierCmd;
            try {
                classifierCmd = Utils.splitOptions(wekaModelsCmd);
            } catch (Exception ex) {
                Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            String classname = classifierCmd[0];
            classifierCmd[0] = "";
            try {
                AbstractClassifier cl = (AbstractClassifier) Utils.forName(Classifier.class, classname, classifierCmd);
//                String modelName = String.format("%s%s%s%s.model", modelDirectory, File.separatorChar, i, classname);
//                System.out.println(String.format("\tBuilding model %s (%s) and doing cross-validation...", i++, modelName));
//                System.out.println(CrossValidation.performCrossValidationMT(trainSet, cl, Config.getCrossValidationSeed(), Config.getCrossValidationFolds(), modelName));
                systems.add(new NLPSystem(cl, filteredInstancesTrainingSet, null));
            } catch (Exception ex) {
                Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void loadEvaluationDataStream(PreProcessOutputStream pposEvaluationData) {

        Instances instancesEvaluationSet = null;
        Instances filteredInstancesEvaluationSet = null;

        DataSource source = new DataSource(pposEvaluationData);

        try {
            instancesEvaluationSet = source.getDataSet();
        } catch (Exception ex) {
            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        // setting class attribute if the data format does not provide this information
        if (instancesEvaluationSet.classIndex() == -1) {
            instancesEvaluationSet.setClass(instancesEvaluationSet.attribute("gold_standard"));
        }

        Remove remove = new Remove();
        remove.setAttributeIndices("1");
        try {
            remove.setInputFormat(instancesEvaluationSet);
            filteredInstancesEvaluationSet = Filter.useFilter(instancesEvaluationSet, remove);
        } catch (Exception ex) {
            Logger.getLogger(PostProcess.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        for (NLPSystem system : systems) {
            system.setEvaluationSet(filteredInstancesEvaluationSet);
        }
    }

    public void evaluateAll(String predictionsOutputFilename, OutputFormat predictionsOutputFormat) {
        for (NLPSystem system : systems) {
            system.evaluate();
        }
    }

    private static class PredictionError implements Comparable<PredictionError> {

        private final double prediction;
        private final double expected;
        private final int pairID;
        private final String sourceFile;
        private final Double error;
        private final weka.core.Instance wInstance;
        private final Instance instance;

        public PredictionError(double prediction, double expected, int pairID,
                String sourceFile, weka.core.Instance wInstance) {

            this.prediction = prediction;
            this.expected = expected;
            this.pairID = pairID;
            this.sourceFile = sourceFile;
            this.wInstance = wInstance;
            //this.instance = null;
            this.instance = PreProcess.getInstance(sourceFile, pairID);

            this.error = Math.abs(this.expected - this.prediction);
        }

        public double getError() {
            return error;
        }

        public double getExpected() {
            return expected;
        }

        public int getPairID() {
            return pairID;
        }

        public double getPrediction() {
            return prediction;
        }

        @Override
        public int compareTo(PredictionError o) {
            if (o.getError() > getError()) {
                return 1;
            }
            return -1;
        }

        @Override
        public String toString() {

            StringBuilder log = new StringBuilder(String.format("%5.3f error (predicted = %5s | expected = %5s) on %5s from %s", getError(), prediction, expected, pairID, sourceFile));

            log.append("\n")
                    .append("\tsentences:\n")
                    .append("\t\t")
                    .append(instance.getSentence1())
                    .append("\n")
                    .append("\t\t")
                    .append(instance.getSentence2())
                    .append("\n")
                    .append("\n")
                    .append("\ttext-processed-parts:\n");

            for (Map.Entry<String, Object> processedTextPart : instance.getProcessedTextParts()) {
                String value = processedTextPart.getValue().toString();

                if (processedTextPart.getValue().getClass().getName().startsWith("[L")) {

                    value = Arrays.deepToString((Object[]) processedTextPart.getValue());

                }

                log.append("\t")
                        .append(processedTextPart.getKey())
                        .append(" = ")
                        .append(value)
                        .append("\n");
            }

            log.append("\n\tfeatures:\n");
            Enumeration<Attribute> attributes = wInstance.enumerateAttributes();

            while (attributes.hasMoreElements()) {
                Attribute attribute = attributes.nextElement();
                if (attribute.isString() || attribute.name().toLowerCase().endsWith("id")) {
                    continue;
                }

                log.append("\t")
                        .append(String.format("%43s", attribute.name()))
                        .append(" = ")
                        .append(wInstance.value(attribute))
                        .append("\n");
            }
            log.append("\n");

            return log.toString();
        }

    }
}
