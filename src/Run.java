/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import asap.Config;
import asap.GrammarCounters;
import asap.LemmasInDBPedia;
import asap.LemmasNotFound;
import asap.NamedEntitiesFound;
import asap.PerformanceCounters;
import asap.PostProcess;
import asap.PreProcess;
import asap.PreProcessOutputStream;
import asap.textprocessing.TextProcessChunkLemmasWithDBPediaLookups;
import java.io.File;
import java.security.InvalidParameterException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that represents the full system's pipeline. It takes care of Config,
 * PreProcess and PostProcess initialization and runs the bits according to the
 * given parameters.
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class Run {

    private static PreProcess preProcess;
    private static PreProcessOutputStream pposTrainingData, pposEvaluationData;
    private static PostProcess postProcess;
    private static final String PROGRAM_NAME = "ASAP";
    private static final String PROGRAM_DESCRIPTION = "Paraphrase detection tool.";

    /**
     * This function will take care of all needed preprocessing like
     * text-processing and feature extraction
     */
    public static void runPreprocess() {
        System.out.println("\n\n-----------------PRE-PROCESS-----------------");
        if (Config.savePreprocessedText()) {
            File f = new File(Config.getSavePreprocessedTextOutputFilename());
            f.delete();
        }

        preProcess = new PreProcess();
        if (!Config.getInputTrainingFilenames().isEmpty()) {
            for (String inputTrainingFilename : Config.getInputTrainingFilenames()) {
                preProcess.loadInput(inputTrainingFilename);

            }
            preProcess.calculateFeatures();
            if (Config.savePreprocessedText()) {
                preProcess.savePreprocessedText(Config.getSavePreprocessedTextOutputFilename() + ".train");
            }
        }

        for (String extraTrainingFeaturesFilename : Config.getExtraTrainingFeaturesFilenames()) {
            preProcess.concatenateFeaturesFromFile(extraTrainingFeaturesFilename);
        }

        if (Config.needsDatasetSeparation()) {
            pposTrainingData = preProcess.getOutputStream(100 - Config.getPercentDatasetDivisionForTest());
            pposEvaluationData = preProcess.getOutputStreamLeftOver();
            if (Config.saveTextFeatures()) {
                preProcess.saveSeparatePreprocessFeatureDatasets(Config.getSaveTextFeaturesOutputFilename() + ".train", Config.getSaveTextFeaturesOutputFilename() + ".eval");
                preProcess.rejoin();
                preProcess.savePreprocessedFeatures(Config.getSaveTextFeaturesOutputFilename());
            }
        } else {

            if (!Config.getInputTrainingFilenames().isEmpty()
                    || !Config.getExtraTrainingFeaturesFilenames().isEmpty()) {
                pposTrainingData = preProcess.getOutputStream();
                if (Config.saveTextFeatures()) {
                    preProcess.savePreprocessedFeatures(Config.getSaveTextFeaturesOutputFilename());
                }
                preProcess.clear();
            }

            if (!Config.getInputEvaluationFilenames().isEmpty()) {
                for (String inputEvaluationFilename : Config.getInputEvaluationFilenames()) {
                    preProcess.loadInput(inputEvaluationFilename);
                }
                preProcess.calculateFeatures();

                if (Config.savePreprocessedText()) {
                    preProcess.savePreprocessedText(Config.getSavePreprocessedTextOutputFilename() + ".eval");
                }
            }

            for (String extraTestingFeaturesFilename : Config.getExtraEvaluationFeaturesFilenames()) {
                preProcess.concatenateFeaturesFromFile(extraTestingFeaturesFilename);
            }

            if (!Config.getInputEvaluationFilenames().isEmpty()
                    || !Config.getExtraEvaluationFeaturesFilenames().isEmpty()) {

                if (Config.saveTextFeatures()) {
                    preProcess.savePreprocessedFeatures(Config.getSaveTextFeaturesOutputFilename() + ".eval");
                }
            }

            pposEvaluationData = preProcess.getOutputStream();
        }
    }

    /**
     * This function will take care of all needed post-processing like model
     * building and data evaluation
     */
    private static void runPostProcess() {
        System.out.println("\n\n-----------------POST-PROCESS----------------");
        postProcess = new PostProcess();

        //TODO: take care of output and generate summary
//        if (Config.needsModelBuildingAndTraining()) {
//            postProcess.loadFeaturesStream(pposTrainingData);
//            postProcess.buildModels(Config.getSerializedModelFilesDirectory());
//            postProcess.calculatePredictions(true, true);
//            postProcess.savePredictionsSemeval2014Task1Format(Config.getPredictionsOutputOldFormatFilename() + ".training");
//            postProcess.calculatePearsonsCorrelations();
//
//            postProcess.clearInput();
//        }
//
//        if (Config.needsModelLoading()) {
//            postProcess.loadModels(Config.getSerializedModelFilesDirectory());
//        }
//
//        if (Config.needsModelEvaluation()) {
//            postProcess.loadFeaturesStream(pposEvaluationData);
//            postProcess.calculatePredictions(true, true);
//            postProcess.savePredictionsSemeval2014Task1Format(Config.getPredictionsOutputOldFormatFilename() + ".evaluation");
//            //postProcess.calculatePearsonsCorrelations();
//        }
        if (Config.needsModelBuildingAndTraining()) {
            postProcess.loadTrainingDataStream(pposTrainingData);
            
            postProcess.buildModels(Config.getSerializedModelFilesDirectory());
        }
        if (Config.needsModelLoading()) {
            postProcess.loadModels(Config.getSerializedModelFilesDirectory());
        }
        if (Config.needsModelEvaluation()) {
            postProcess.loadEvaluationDataStream(pposEvaluationData);
            postProcess.evaluateAll(Config.getPredictionsOutputFilename(), Config.getPredictionsOutputFormat());
        }
        
    }

    /**
     * Flushes out Logs
     */
    private static void finishPreProcessLogs() {
        if (Config.logLemmasNotFound()) {
            LemmasNotFound.finishLog();
        }
        if (Config.logGrammarCounters()) {
            GrammarCounters.finishLog();
        }
        if (Config.logNamedEntitiesFound()) {
            NamedEntitiesFound.finishLog();
        }
        if (Config.logLemmasFoundInDBPedia()) {
            LemmasInDBPedia.finishLog();
        }

        if (Config.useDBPediaLemmaLookup()) {
            TextProcessChunkLemmasWithDBPediaLookups.saveLookups();
        }
    }

    /**
     * Flushes out Logs
     */
    private static void finishPostProcessLogs() {
        /*TODO: move prediction errors to a log class, add config vars, etc. add above generated class log flush here
         */

    }

    /**
     * Flushes out Logs
     */
    private static void finishCommonLogs() {
        if (Config.logTimings()) {
            PerformanceCounters.printStats();
        }
    }

    /**
     * Prints usage to stdout
     */
    private static void printUsage() {
//TODO: fix according to Config._loadConfig(String []args)
        String usageString = new StringBuilder()
                .append("\nUsage:  ")
                .append(PROGRAM_NAME)
                .append(" [options]\n")
                .append("OR:     ")
                .append("java -jar ASAP_Project.jar")
                .append(" [options]\n")
                .append(PROGRAM_DESCRIPTION)
                //
                .append("\nPlease note that this is a work in progress, parameters marked with * may not work as expected.\n")
                //
                .append("\n\n    INPUT:\n")
                .append(String.format("%c %-22s%s\n", ' ',
                                "-i FILE",
                                "Builds models training them on data from FILE. Can be repeated."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-t FILE",
                                "Evaluate models on data from FILE. Can be repeated."))
                .append(String.format("%c %-22s%s\n", '*',
                                "-fi FILE",
                                "Adds data from FILE to training data as extra features. Can be repeated."))
                .append(String.format("%c %-22s%s\n", '*',
                                "-ft FILE",
                                "Adds data from FILE to evaluation data as extra features. Can be repeated."))
                .append(String.format("%c %-22s%s\n", '*',
                                "-fb FILE",
                                "Adds data from FILE to training and evaluation data as extra features. Can be repeated."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-mi PATH",
                                "Deserializes models for evaluation from this directory."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-pt PERCENTAGE",
                                "Take PERCENTAGE% of the training data and use it exclusively as evaluation data."))
                //
                .append("\n    OUTPUT:\n")
                .append(String.format("%c %-22s%s\n", '*',
                                "-o FILE",
                                "Write predictions into FILE in the specified output format or 2014_TEST_FORMAT (default)."))
                .append(String.format("%c %-22s%s\n", '*',
                                "-of FORMAT",
                                "Set format output to FORMAT.")) //TODO: add available options
                .append(String.format("%c %-22s%s\n", ' ',
                                "-fo FILE",
                                "Write features into FILE."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-mo PATH",
                                "Builds models and serializes them into this directory."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-to",
                                "Save preprocessed text parts."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-tof FILE",
                                "Save preprocessed text parts into FILE."))
                //
                .append("\n    Additional Logs:\n")
                .append(String.format("%c %-22s%s\n", ' ',
                                "-ll",
                                "Logs lemmas not found."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-llf FILE",
                                "Logs lemmas not found into FILE."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-lfl",
                                "Logs found lemmas with DBPedia."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-lflf FILE",
                                "Logs found lemmas with DBPedia into FILE."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-lg",
                                "Logs grammar counters."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-lgf FILE",
                                "Logs grammar counters into FILE."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-lt",
                                "Logs timings."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-ltf FILE",
                                "Logs timings into FILE."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-lner",
                                "Logs found Named Entities."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-lnerf FILE",
                                "Logs found Named Entities into FILE."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-lpef FILE",
                                "Logs Prediction Errors into FILE."))
                //
                .append("\n    Others:\n")
                .append(String.format("%c %-22s%s\n", ' ',
                                "-mt NUMBER",
                                "Use NUMBER of Threads instead of 1.")) //should this default to number of available processor threads?
                .append(String.format("%c %-22s%s\n", ' ',
                                "-p",
                                "Show progress indicators."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-pre-only",
                                "Ignore all post-processing."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-useSPOS",
                                "Use Stanford Part-of-Speech Tagger instead of OpenNLP's."))
                .append(String.format("%c %-22s%s\n", ' ',
                                "-useSNER",
                                "Use Stanford Named Entity Recognizer instead of OpenNLP's."))
                .append(String.format("%c %-22s%s\n", '*',
                                "-ssaropiem",
                                "Ignores exact matches before adding to sum, during Semantic similarity and relatedness calculations."))
                .append(String.format("%c %-22s%s\n", '*',
                                "-ssaroplprev NUMBER",
                                "Limits values to NUMBER before adding to sum, during Semantic similarity and relatedness calculations."))
                .append(String.format("%c %-22s%s\n", '*',
                                "-ssaroplpostv NUMBER",
                                "Limits value of sum to NUMBER after it's calculated, during Semantic similarity and relatedness calculations."))
                .append(String.format("%c %-22s%s\n", '*',
                                "-r N",
                                "Runs all N times. Useful for increased timing accuracy."))
                .append(String.format("%c %-22s%s\n", '*',
                                "-s FILE",
                                "Save configuration into FILE."))
                .append(String.format("%c %-22s%s\n", '*',
                                "-l FILE",
                                "Loads configuration from FILE. All other parameters will be overridden"))
                .toString();

        System.out.println(usageString);
    }

    /**
     * Main program entry
     *
     * @param args
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            printUsage();
            return;
        }

        //System.out.println(Arrays.toString(args)); //maybe useful as logging purpose?
        try {
            Config.loadConfig(args);
        } catch (InvalidParameterException ex) {
            Logger.getLogger(Run.class.getName()).log(Level.SEVERE, null, ex);
            printUsage();
            return;
        }

        //only runs preprocess if needed, else reads precalculated features from files:
        if (Config.needsPreProcessing()) {
            runPreprocess();
        } else {
            preProcess = new PreProcess();
            for (String extraTrainingFeaturesFilename : Config.getExtraTrainingFeaturesFilenames()) {
                preProcess.loadFeaturesFromFile(extraTrainingFeaturesFilename);
            }
            pposTrainingData = preProcess.getOutputStream();
            for (String extraTestingFeaturesFilename : Config.getExtraEvaluationFeaturesFilenames()) {
                preProcess.loadFeaturesFromFile(extraTestingFeaturesFilename);
            }
        }

        finishPreProcessLogs();

        //only runs postprocess if needed:
        if (Config.needsPostProcessing()) {
            runPostProcess();
        }
        finishPostProcessLogs();

        System.out.println("\n\n");
        finishCommonLogs();

    }

}
