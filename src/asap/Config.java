/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import asap.featurecalculation.DummyFeatureCalculator;
import asap.featurecalculation.FeatureCalculator;
import asap.featurecalculation.FeatureCalculatorOverlappingLemmas;
import asap.featurecalculation.FeatureCalculatorSentenceLengths;
import asap.featurecalculation.LexicalCountWords;
import asap.featurecalculation.LexicalOverlapFeaturesCalculator;
import asap.featurecalculation.SemanticSimilarityAndRelatednessCalculator;
import asap.featurecalculation.SyntacticCountChunkTypesFeatures;
import asap.textprocessing.TextProcessNamedEntities;
import asap.textprocessing.TextProcessNamedEntitiesStanford;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author David Simões
 */
public class Config {

    private static boolean saveConfig = false;
    private static boolean loadConfig = false;
    private static String configFilename = "asap.conf";

    //--------------------------------------------------------------------------
    //-         Interface config                                         -
    //--------------------------------------------------------------------------
    private static boolean showProgress = false;

    //--------------------------------------------------------------------------
    //-         Multiprocessing config                                         -
    //--------------------------------------------------------------------------
    private static boolean useMultithread = true;
    private static int numThreads = 2;

    //--------------------------------------------------------------------------
    //-         LOG Config                                                     -
    //--------------------------------------------------------------------------
    private static boolean logLemmasNotFound = false;
    private static String logLemmasNotFoundOutputFilename = "outputs/LemmasNotFound.log";

    private static boolean logGrammarCounters = false;
    private static String logGrammarCountersOutputFilename = "outputs/GrammarCounters.log";

    private static boolean logTimings = false;
    private static String logTimingsOutputFilename = "outputs/Timings.log";

    private static boolean logNamedEntitiesFound = false;
    private static String logNamedEntitiesFoundOutputFilename = "outputs/NamedEntitiesFound.log";

    private static boolean logLemmasInDBPedia = false;
    private static String logLemmasInDBPediaOutputFilename = "outputs/LemmasFoundInDBPedia.log";

    private static boolean logPredictionsErrors;
    private static String logPredictionsErrorsOutputFilename;

    //--------------------------------------------------------------------------
    //-         Repeat processing for more accurate timings measuring          -
    //--------------------------------------------------------------------------
    private static boolean repeatProcessing = false;
    private static int repeatProcessingCount = 10;

    //--------------------------------------------------------------------------
    //-         Input/Output formats and other options                         -
    //--------------------------------------------------------------------------
    private static boolean savePreprocessedText = false;
    private static String savePreprocessedTextOutputFilename = "outputs/Preprocessed.txt";
    private static boolean saveTextFeatures = false;
    private static String saveTextFeaturesOutputFilename = "outputs/Features.txt";

    private static String predictionsOutputOldFormatFilename = "outputs/predictions.txt";
    private static String predictionsOutputNewFormatFilename = "outputs/predictions_new-format.txt";

    private static boolean generateSerializedModelFiles = false;
    private static boolean readSerializedModelFiles = false;
    //used for both input/output:
    private static String serializedModelFilesDirectory = "outputs/models";

    //TODO: change all possible configurations like preprocessing features and weka models here.
    //--------------------------------------------------------------------------
    //-         Preprocessing options                                          -
    //--------------------------------------------------------------------------
    private static int lexicalOverlapMinStopWordRemoval = 0;
    private static int lexicalOverlapMaxStopWordRemoval = 3;
    //if false, use openNLP equivalent model
    private static boolean useStanfordPOSTagger = false;
    private static boolean useStanfordTokenizer = false;
    private static boolean useStanfordNER = false;
    //case sensitivite features
    private static boolean calculateWithCaseSensitivity = false;
    private static boolean calculateWithoutCaseSensitivity = true;
    //use internet DBPedia extra lookups for lemmas that are not found using normal method:
    private static boolean useDBPediaLemmaLookup = false;
    //default library model directories:
    private static String openNlpModelsDirectory = "opennlp-models";
    private static String stanfordModelsDirectory = "stanford-models";
    //default is to use these openNLP models:
    private static String tokenizerModelFilename = "en-token.bin";
    private static String posTaggerModelFilename = "en-pos-maxent.bin";
    private static String chunkerModelFilename = "en-chunker.bin";
    private static String[] nerModelFilenames = {"en-ner-location.bin", "en-ner-organization.bin", "en-ner-person.bin"};
    //SemanticSimilarityAndRelatednessCalculator specific options:
    private static boolean limitSemanticSimilarityAndRelatednessCalculatorPostSum = false;
    private static double semanticSimilarityAndRelatednessCalculatorPostSumValueLimit = Double.MAX_VALUE;
    private static double semanticSimilarityAndRelatednessCalculatorPreSumValueLimit = Double.MAX_VALUE;
    private static boolean limitSemanticSimilarityAndRelatednessCalculatorPreSum = false;
    private static boolean ignoreExactMatchesSemanticSimilarityAndRelatednessCalculatorPreSum = false;
    //don't run post processing:
    private static boolean preProcessOnly = false;

    //--------------------------------------------------------------------------
    //-         Postprocessing options                                         -
    //--------------------------------------------------------------------------
    private static int crossValidationSeed = 0;
    private static int crossValidationFolds = 10;
    private static boolean needsDatasetSeparation = false;
    private static int percentDatasetDivisionForTest = 30;
    private static boolean outputPredictionsErrors = true;
    private static String outputPredictionsErrorsOutputFilename = "outputs/predictions/errors";

    //--------------------------------------------------------------------------
    //-         Local stuff                                                    -
    //--------------------------------------------------------------------------
    //holds itself:
    private static final Config config = new Config();
    //holds each calculator to use:
    private static List<FeatureCalculator> featureCalculators;
    //holds each input filename to PreProcess for later training:
    private static List<String> inputTrainingFilenames;
    //holds each input filename to PreProcess for later evaluation:
    private static List<String> inputEvaluationFilenames;
    //holds each extra features filename to concatenate to the PreProcessing result:
    private static List<String> extraTrainingFeaturesFilenames;
    //holds each extra features filename to concatenate to the PreProcessing result:
    private static List<String> extraEvaluationFeaturesFilenames;
    //holds each word list filename for the lexical count words feature:
    private static List<String> lexicalCountWordsWordListFilenames;
    //holds the command string equivalent to the weka models configuration
    private static List<String> wekaModelsCmd;

    /**
     *
     * @return
     */
    public static boolean limitSemanticSimilarityAndRelatednessCalculatorPostSum() {
        return limitSemanticSimilarityAndRelatednessCalculatorPostSum;
    }

    /**
     *
     * @return
     */
    public static double getSemanticSimilarityAndRelatednessCalculatorPostSumValueLimit() {
        return semanticSimilarityAndRelatednessCalculatorPostSumValueLimit;
    }

    /**
     *
     * @return
     */
    public static double getSemanticSimilarityAndRelatednessCalculatorPreSumValueLimit() {
        return semanticSimilarityAndRelatednessCalculatorPreSumValueLimit;
    }

    /**
     *
     * @return
     */
    public static boolean limitSemanticSimilarityAndRelatednessCalculatorPreSum() {
        return limitSemanticSimilarityAndRelatednessCalculatorPreSum;
    }

    /**
     *
     * @return
     */
    public static boolean ignoreExactMatchesSemanticSimilarityAndRelatednessCalculatorPreSum() {
        return ignoreExactMatchesSemanticSimilarityAndRelatednessCalculatorPreSum;
    }

    /**
     *
     * @return
     */
    public static boolean calculateWithCaseSensitivity() {
        return calculateWithCaseSensitivity;
    }

    /**
     *
     * @return
     */
    public static boolean calculateWithoutCaseSensitivity() {
        return calculateWithoutCaseSensitivity;
    }

    /**
     *
     * @return
     */
    public static boolean showProgress() {
        return showProgress;
    }

    private Config() {
        featureCalculators = new LinkedList<>();
        inputTrainingFilenames = new LinkedList<>();
        inputEvaluationFilenames = new LinkedList<>();
        extraTrainingFeaturesFilenames = new LinkedList<>();
        extraEvaluationFeaturesFilenames = new LinkedList<>();
        lexicalCountWordsWordListFilenames = new LinkedList<>();

        wekaModelsCmd = new LinkedList<>();
    }

    private void _loadConfig(String[] args) throws InvalidParameterException {
        int tmp;
        double tmpd;
        java.lang.System.out.print("Loading config...");
        for (int i = 0; i < args.length; i++) {
            String argument = args[i];
            switch (argument) {
                case "-i":
                    inputTrainingFilenames.add(args[++i]);
                    break;
                case "-t":
                    inputEvaluationFilenames.add(args[++i]);
                    break;
                case "-fi":
                    extraTrainingFeaturesFilenames.add(args[++i]);
                    break;
                case "-ft":
                    extraEvaluationFeaturesFilenames.add(args[++i]);
                    break;
                case "-fb":
                    extraTrainingFeaturesFilenames.add(args[++i]);
                    extraEvaluationFeaturesFilenames.add(args[i]);
                    break;
                case "-o":
                    predictionsOutputOldFormatFilename = args[++i];
                    break;
                case "-no":
                    predictionsOutputNewFormatFilename = args[++i];
                    break;
                case "-tp":
                    tmp = 2;
                    try {
                        tmp = Integer.parseInt(args[i + 1]);
                        i++;
                        needsDatasetSeparation = true;
                        percentDatasetDivisionForTest = tmp;
                    } catch (NumberFormatException ex) {
                        //TODO: add exception logger line
                    }
                    break;
                case "-ll":
                    logLemmasNotFound = true;
                    break;
                case "-llf":
                    logLemmasNotFound = true;
                    logLemmasNotFoundOutputFilename = args[++i];
                    break;
                case "-lg":
                    logGrammarCounters = true;
                    break;
                case "-lner":
                    logNamedEntitiesFound = true;
                    break;
                case "-lgf":
                    logGrammarCounters = true;
                    logGrammarCountersOutputFilename = args[++i];
                    break;
                case "-lt":
                    logTimings = true;
                    break;
                case "-ltf":
                    logTimings = true;
                    logTimingsOutputFilename = args[++i];
                    break;
                case "-lnerf":
                    logNamedEntitiesFound = true;
                    logNamedEntitiesFoundOutputFilename = args[++i];
                    break;
                case "-lfl":
                    useDBPediaLemmaLookup = true;
                    logLemmasInDBPedia = true;
                    break;
                case "-lflf":
                    useDBPediaLemmaLookup = true;
                    logLemmasInDBPedia = true;
                    logLemmasInDBPediaOutputFilename = args[++i];
                    break;
                case "-lpef":
                    logPredictionsErrors = true;
                    logPredictionsErrorsOutputFilename = args[++i];
                    break;
                case "-p":
                    showProgress = true;
                    break;
                case "-mt":
                    useMultithread = true;
                    tmp = 2;
                    try {
                        tmp = Integer.parseInt(args[i + 1]);
                        i++;
                        numThreads = tmp;
                    } catch (NumberFormatException ex) {
                        //TODO: add exception logger line
                    }
                    break;
                case "-r":
                    repeatProcessing = true;
                    tmp = 10;
                    try {
                        tmp = Integer.parseInt(args[i + 1]);
                        i++;
                        repeatProcessingCount = tmp;
                    } catch (NumberFormatException ex) {
                        //TODO: add exception logger line
                    }
                    break;
                case "-fo":
                    saveTextFeatures = true;
                    saveTextFeaturesOutputFilename = args[++i];
                    break;
                case "-to":
                    savePreprocessedText = true;
                    break;
                case "-tof":
                    savePreprocessedText = true;
                    savePreprocessedTextOutputFilename = args[++i];
                    break;
                case "-useSPOS":
                    useStanfordPOSTagger = true;
                    //TODO: check this...
                    useStanfordTokenizer = true;
                    posTaggerModelFilename = "english-caseless-left3words-distsim.tagger";
                    break;
                case "-useSNER":
                    useStanfordNER = true;
                    nerModelFilenames = new String[2];
//                    nerModelFilenames[0] = "english.all.3class.caseless.distsim.crf.ser.gz";
//                    nerModelFilenames[1] = "english.all.3class.distsim.crf.ser.gz";
//                    nerModelFilenames[2] = "english.conll.4class.caseless.distsim.crf.ser.gz";
//                    nerModelFilenames[3] = "english.conll.4class.distsim.crf.ser.gz";
                    nerModelFilenames[0] = "english.muc.7class.caseless.distsim.crf.ser.gz";
                    nerModelFilenames[1] = "english.muc.7class.distsim.crf.ser.gz";
                    //nerModelFilenames[6] = "english.nowiki.3class.caseless.distsim.crf.ser.gz";

                    break;
                case "-mo":
                    if (readSerializedModelFiles) {
                        throw new InvalidParameterException("Can't read AND write weka models in the same run.");
                    }
                    generateSerializedModelFiles = true;
                    serializedModelFilesDirectory = args[++i];
                    break;
                case "-mi":
                    if (generateSerializedModelFiles) {
                        throw new InvalidParameterException("Can't read AND write weka models in the same run.");
                    }
                    readSerializedModelFiles = true;
                    serializedModelFilesDirectory = args[++i];
                    break;
                case "-ssaropiem":
                    ignoreExactMatchesSemanticSimilarityAndRelatednessCalculatorPreSum = true;
                    break;
                case "-ssaroplprev":
                    tmpd = Double.MAX_VALUE;
                    try {
                        tmpd = Double.parseDouble(args[i + 1]);
                        i++;
                        semanticSimilarityAndRelatednessCalculatorPreSumValueLimit = tmpd;
                        limitSemanticSimilarityAndRelatednessCalculatorPreSum = true;
                    } catch (NumberFormatException ex) {
                        //TODO: add exception logger line
                    }
                    break;
                case "-ssaroplpostv":

                    tmpd = Double.MAX_VALUE;
                    try {
                        tmpd = Double.parseDouble(args[i + 1]);
                        i++;
                        semanticSimilarityAndRelatednessCalculatorPostSumValueLimit = tmpd;
                        limitSemanticSimilarityAndRelatednessCalculatorPostSum = true;
                    } catch (NumberFormatException ex) {
                        //TODO: add exception logger line
                    }
                    break;
                case "-pre-only":
                    preProcessOnly = true;
                    break;
                case "-s":
                    if (loadConfig) {
                        throw new InvalidParameterException("Can't read AND write config file in the same run.");
                    }
                    saveConfig = true;
                    configFilename = args[++i];
                    break;
                case "-l":
                    if (saveConfig) {
                        throw new InvalidParameterException("Can't read AND write config file in the same run.");
                    }
                    loadConfig = true;
                    configFilename = args[++i];
                    break;
                default:
                    throw new InvalidParameterException(String.format("Invalid argument %s.", argument));
            }
        }

        if (loadConfig) {
            _loadConfig(configFilename);
        } else {
            setDefaultFeatureCalculators();
            setDefaultWekaModels();

            if (saveConfig) {
                _saveConfig(configFilename);
            }
        }
        java.lang.System.out.println("\tdone.");
    }

    private void _loadConfig(String filename) {
        //TODO: from file...
    }

    private void _saveConfig(String filename) {
        //TODO: to file...
    }

    /**
     *
     * @param args
     * @throws InvalidParameterException
     */
    public static void loadConfig(String[] args) throws InvalidParameterException {
        config._loadConfig(args);
    }

    //--------------------------------------------------------------------------
    //-         Getters                                                        -
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public static boolean needsPreProcessing() {
        return !inputTrainingFilenames.isEmpty() || !inputEvaluationFilenames.isEmpty();
    }

    /**
     *
     * @return
     */
    public static boolean needsPostProcessing() {
        return !preProcessOnly && (readSerializedModelFiles || generateSerializedModelFiles
                || predictionsOutputNewFormatFilename != null
                || predictionsOutputOldFormatFilename != null
                || needsDatasetSeparation);
    }

    /**
     *
     * @return
     */
    public static boolean needsModelBuildingAndTraining() {
        return (!extraTrainingFeaturesFilenames.isEmpty()
                || !inputTrainingFilenames.isEmpty())
                && !preProcessOnly;
    }

    /**
     *
     * @return
     */
    public static boolean needsModelLoading() {
        return readSerializedModelFiles && !preProcessOnly;
    }

    /**
     *
     * @return
     */
    public static boolean needsModelEvaluation() {
        return (!extraEvaluationFeaturesFilenames.isEmpty()
                || !inputEvaluationFilenames.isEmpty()
                || needsDatasetSeparation)
                && !preProcessOnly;
    }

    /**
     *
     * @return
     */
    public static boolean needsDatasetSeparation() {
        return needsDatasetSeparation;
    }

    /**
     *
     * @return
     */
    public static boolean useStanfordTokenizer() {
        return useStanfordTokenizer;
    }

    /**
     *
     * @return
     */
    public static boolean useStanfordPOSTagger() {
        return useStanfordPOSTagger;
    }

    /**
     *
     * @return
     */
    public static boolean useDBPediaLemmaLookup() {
        return useDBPediaLemmaLookup;
    }

    /**
     *
     * @return
     */
    public static String getChunkerModelFilename() {
        return chunkerModelFilename;
    }

    /**
     *
     * @return
     */
    public static String[] getNerModelFilenames() {
        return nerModelFilenames;
    }

    /**
     *
     * @return
     */
    public static List<String> getExtraTrainingFeaturesFilenames() {
        return Collections.unmodifiableList(extraTrainingFeaturesFilenames);
    }

    /**
     *
     * @return
     */
    public static List<String> getExtraEvaluationFeaturesFilenames() {
        return Collections.unmodifiableList(extraEvaluationFeaturesFilenames);
    }

    /**
     *
     * @return
     */
    public static List<FeatureCalculator> getFeatureCalculators() {
        return Collections.unmodifiableList(featureCalculators);
    }

    /**
     *
     * @return
     */
    public static int getPercentDatasetDivisionForTest() {
        return percentDatasetDivisionForTest;
    }

    /**
     *
     * @return
     */
    public static List<String> getInputEvaluationFilenames() {
        return Collections.unmodifiableList(inputEvaluationFilenames);
    }

    /**
     *
     * @return
     */
    public static List<String> getInputTrainingFilenames() {
        return Collections.unmodifiableList(inputTrainingFilenames);
    }

    /**
     *
     * @return
     */
    public static List<String> getLexicalCountWordsWordListFilenames() {
        return Collections.unmodifiableList(lexicalCountWordsWordListFilenames);
    }

    /**
     *
     * @return
     */
    public static int getLexicalOverlapMaxStopWordRemoval() {
        return lexicalOverlapMaxStopWordRemoval;
    }

    /**
     *
     * @return
     */
    public static int getLexicalOverlapMinStopWordRemoval() {
        return lexicalOverlapMinStopWordRemoval;
    }

    /**
     *
     * @return
     */
    public static String getLogGrammarCountersOutputFilename() {
        return logGrammarCountersOutputFilename;
    }

    /**
     *
     * @return
     */
    public static String getLogLemmasNotFoundOutputFilename() {
        return logLemmasNotFoundOutputFilename;
    }

    /**
     *
     * @return
     */
    public static String getLogTimingsOutputFilename() {
        return logTimingsOutputFilename;
    }

    /**
     *
     * @return
     */
    public static String getLogNamedEntitiesFoundOutputFilename() {
        return logNamedEntitiesFoundOutputFilename;
    }

    /**
     *
     * @return
     */
    public static String getLogLemmasInDBPediaOutputFilename() {
        return logLemmasInDBPediaOutputFilename;
    }

    /**
     *
     * @return
     */
    public static String getLogPredictionsErrorsOutputDir() {
        return logPredictionsErrorsOutputFilename;
    }

    /**
     *
     * @return
     */
    public static int getNumThreads() {
        return numThreads;
    }

    /**
     *
     * @return
     */
    public static String getOpenNlpModelsDirectory() {
        return openNlpModelsDirectory;
    }

    /**
     *
     * @return
     */
    public static String getPosTaggerModelFilename() {
        return posTaggerModelFilename;
    }

    /**
     *
     * @return
     */
    public static String getPredictionsOutputNewFormatFilename() {
        return predictionsOutputNewFormatFilename;
    }

    /**
     *
     * @return
     */
    public static String getPredictionsOutputOldFormatFilename() {
        return predictionsOutputOldFormatFilename;
    }

    /**
     *
     * @return
     */
    public static int getRepeatProcessingCount() {
        return repeatProcessingCount;
    }

    /**
     *
     * @return
     */
    public static String getSavePreprocessedTextOutputFilename() {
        return savePreprocessedTextOutputFilename;
    }

    /**
     *
     * @return
     */
    public static String getSaveTextFeaturesOutputFilename() {
        return saveTextFeaturesOutputFilename;
    }

    /**
     *
     * @return
     */
    public static String getSerializedModelFilesDirectory() {
        return serializedModelFilesDirectory;
    }

    /**
     *
     * @return
     */
    public static String getStanfordModelsDirectory() {
        return stanfordModelsDirectory;
    }

    /**
     *
     * @return
     */
    public static String getTokenizerModelFilename() {
        return tokenizerModelFilename;
    }

    /**
     *
     * @return
     */
    public static List<String> getWekaModelsCmd() {
        return Collections.unmodifiableList(wekaModelsCmd);
    }

    /**
     *
     * @return
     */
    public static boolean generateSerializedModelFiles() {
        return generateSerializedModelFiles;
    }

    /**
     *
     * @return
     */
    public static boolean logGrammarCounters() {
        return logGrammarCounters;
    }

    /**
     *
     * @return
     */
    public static boolean logLemmasNotFound() {
        return logLemmasNotFound;
    }

    /**
     *
     * @return
     */
    public static boolean logTimings() {
        return logTimings;
    }

    /**
     *
     * @return
     */
    public static boolean logNamedEntitiesFound() {
        return logNamedEntitiesFound;
    }

    /**
     *
     * @return
     */
    public static boolean logLemmasFoundInDBPedia() {
        return logLemmasInDBPedia;
    }

    /**
     *
     * @return
     */
    public static boolean logPredictionsErrors() {
        return logPredictionsErrors;
    }

    /**
     *
     * @return
     */
    public static boolean readSerializedModelFiles() {
        return readSerializedModelFiles;
    }

    /**
     *
     * @return
     */
    public static boolean repeatProcessing() {
        return repeatProcessing;
    }

    /**
     *
     * @return
     */
    public static boolean savePreprocessedText() {
        return savePreprocessedText;
    }

    /**
     *
     * @return
     */
    public static boolean saveTextFeatures() {
        return saveTextFeatures;
    }

    /**
     *
     * @return
     */
    public static boolean useMultithread() {
        return useMultithread;
    }

    /**
     *
     * @return
     */
    public static int getCrossValidationSeed() {
        return crossValidationSeed;
    }

    /**
     *
     * @return
     */
    public static int getCrossValidationFolds() {
        return crossValidationFolds;
    }

    /**
     *
     * @return
     */
    public static boolean outputPredictionsErrors() {
        return outputPredictionsErrors;
    }

    /**
     *
     * @return
     */
    public static String getOutputPredictionsErrorsOutputFilename() {
        return outputPredictionsErrorsOutputFilename;
    }

    //--------------------------------------------------------------------------
    //-         "Setters"                                                      -
    //--------------------------------------------------------------------------
    private void setDefaultFeatureCalculators() {
        //there's probably a better way here than instancing each calculator:
        Config.featureCalculators.add(new SemanticSimilarityAndRelatednessCalculator());
        lexicalCountWordsWordListFilenames.add("negative-stopword-list.txt");
        lexicalCountWordsWordListFilenames.add("stopword-list.txt");

        for (String filename : lexicalCountWordsWordListFilenames) {
            Config.featureCalculators.add(new LexicalCountWords(filename, filename));
        }

        Config.featureCalculators.add(new LexicalOverlapFeaturesCalculator(lexicalOverlapMinStopWordRemoval, lexicalOverlapMaxStopWordRemoval));
        Config.featureCalculators.add(new SyntacticCountChunkTypesFeatures());

        Config.featureCalculators.add(new FeatureCalculatorSentenceLengths());

        Config.featureCalculators.add(new FeatureCalculatorOverlappingLemmas());

        if (useStanfordNER) {
            Config.featureCalculators.add(new DummyFeatureCalculator(TextProcessNamedEntitiesStanford.getTextProcessNamedEntitiesStanford()));
        } else {
            Config.featureCalculators.add(new DummyFeatureCalculator(TextProcessNamedEntities.getTextProcessNamedEntities()));
        }
    }

    private void setDefaultWekaModels() {
        Config.wekaModelsCmd.add("weka.classifiers.meta.Stacking -X 10 -M \"weka.classifiers.trees.M5P -M 4.0\" -S 1 -num-slots 1 -B \"weka.classifiers.trees.M5P -R -M 4.0\" -B \"weka.classifiers.lazy.IBk -K 1 -W 0 -A \\\"weka.core.neighboursearch.LinearNNSearch -A \\\\\\\"weka.core.EuclideanDistance -R first-last\\\\\\\"\\\"\" -B \"weka.classifiers.functions.LinearRegression -S 0 -R 1.0E-8\"");
        Config.wekaModelsCmd.add("weka.classifiers.meta.Stacking -X 10 -M \"weka.classifiers.trees.M5P -M 4.0\" -S 1 -num-slots 1 -B \"weka.classifiers.trees.M5P -R -M 4.0\" -B \"weka.classifiers.lazy.IBk -K 1 -W 0 -A \\\"weka.core.neighboursearch.LinearNNSearch -A \\\\\\\"weka.core.EuclideanDistance -R first-last\\\\\\\"\\\"\" -B \"weka.classifiers.functions.LinearRegression -S 0 -R 1.0E-8\" -B \"weka.classifiers.rules.ZeroR \" -B \"weka.classifiers.functions.IsotonicRegression \"");
        Config.wekaModelsCmd.add("weka.classifiers.meta.Vote -S 1 -B \"weka.classifiers.trees.M5P -R -M 4.0\" -B \"weka.classifiers.lazy.IBk -K 1 -W 0 -A \\\"weka.core.neighboursearch.LinearNNSearch -A \\\\\\\"weka.core.EuclideanDistance -R first-last\\\\\\\"\\\"\" -B \"weka.classifiers.trees.M5P -R -M 10.0\" -B \"weka.classifiers.lazy.IBk -K 3 -W 0 -A \\\"weka.core.neighboursearch.LinearNNSearch -A \\\\\\\"weka.core.EuclideanDistance -R first-last\\\\\\\"\\\"\" -B \"weka.classifiers.trees.M5P -R -M 20.0\" -B \"weka.classifiers.lazy.IBk -K 5 -W 0 -A \\\"weka.core.neighboursearch.LinearNNSearch -A \\\\\\\"weka.core.EuclideanDistance -R first-last\\\\\\\"\\\"\" -B \"weka.classifiers.functions.LinearRegression -S 0 -R 1.0E-8\" -R AVG");
        Config.wekaModelsCmd.add("weka.classifiers.meta.Stacking -X 10 -M \"weka.classifiers.trees.M5P -M 4.0\" -S 1 -num-slots 1 -B \"weka.classifiers.trees.M5P -R -M 4.0\" -B \"weka.classifiers.lazy.IBk -K 1 -W 0 -A \\\"weka.core.neighboursearch.LinearNNSearch -A \\\\\\\"weka.core.EuclideanDistance -R first-last\\\\\\\"\\\"\" -B \"weka.classifiers.trees.M5P -R -M 10.0\" -B \"weka.classifiers.lazy.IBk -K 3 -W 0 -A \\\"weka.core.neighboursearch.LinearNNSearch -A \\\\\\\"weka.core.EuclideanDistance -R first-last\\\\\\\"\\\"\" -B \"weka.classifiers.trees.M5P -R -M 20.0\" -B \"weka.classifiers.lazy.IBk -K 5 -W 0 -A \\\"weka.core.neighboursearch.LinearNNSearch -A \\\\\\\"weka.core.EuclideanDistance -R first-last\\\\\\\"\\\"\" -B \"weka.classifiers.functions.LinearRegression -S 0 -R 1.0E-8\"");
    }

}
