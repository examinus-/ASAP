/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.textprocessing;

import asap.Config;
import asap.Instance;
import asap.PerformanceCounters;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import java.io.Serializable;
import opennlp.tools.postag.POSTaggerME;

/**
 * This processer takes care of tagging the part-of-speech of each part of the
 * sentences. processing input: Tokenized sentences (String[]) processing
 * output: The tags aligned with the tokenized sentences (String[]) as well as
 * inline sentence tagging (String)
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class TextProcessPOSTags implements TextProcesser, TextProcessedPartKeyConsts, Serializable {

    private static POSModel posModel;
    private POSTaggerME tagger;
    private final TextProcesser textProcesserDependency;

    /**
     *
     */
    protected static final HashMap<Long, TextProcessPOSTags> tppts
            = new HashMap<>();

    /**
     *
     * @param t
     * @return
     */
    public static TextProcessPOSTags getTextProcessPOSTags(Thread t) {
        TextProcessPOSTags r;
        if (tppts.containsKey(t.getId())) {
            return tppts.get(t.getId());
        }
        r = new TextProcessPOSTags(t);
        tppts.put(t.getId(), r);
        return r;
    }

    /**
     *
     * @return
     */
    public static TextProcessPOSTags getTextProcessPOSTags() {
        return getTextProcessPOSTags(Thread.currentThread());
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public TextProcesser getInstance(Thread t) {
        return getTextProcessPOSTags(t);
    }

    /**
     *
     * @param modelsPath
     * @param t
     */
    protected TextProcessPOSTags(String modelsPath, Thread t) {
        if (posModel == null) {
            File modelFile = new File(Config.getOpenNlpModelsDirectory() + File.separator + Config.getPosTaggerModelFilename());
            //File modelFile = new File(modelsPath + "/en-pos-perceptron.bin");
            //java.lang.System.out.println("loading POSModel from:" + modelFile.getAbsolutePath());
            posModel = new POSModelLoader().load(modelFile);
        }
        tagger = new POSTaggerME(posModel);
        textProcesserDependency = TextProcessTokenizer.getTextProcessTokenizer(t);
    }

    /**
     *
     * @param t
     */
    protected TextProcessPOSTags(Thread t) {
        this("opennlp-models", t);
    }

    /**
     *
     */
    protected TextProcessPOSTags() {
        this(Thread.currentThread());
    }

    /**
     *
     * @param i
     * @return
     */
    @Override
    public boolean textProcessingDependenciesMet(Instance i) {
        return i.isProcessed(textProcesserDependency);
    }

    /**
     *
     * @param i
     */
    @Override
    public void process(Instance i) {
        if (!textProcessingDependenciesMet(i)) {
            textProcesserDependency.process(i);
        }

        PerformanceCounters.startTimer("process POSTags");

        Object o;
        if (Config.calculateWithoutCaseSensitivity()) {
            o = i.getProcessedTextPart(sentence1Tokens);
            if (!(o instanceof String[])) {
                return;
            }

            String[] tokens = (String[]) o;
            String[] tags = getTags(tokens);
            i.addProcessedTextPart(sentence1POSTags, tags);
            i.addProcessedTextPart(sentence1POSTagd, tagSentence(tokens, tags));

            o = i.getProcessedTextPart(sentence2Tokens);
            if (!(o instanceof String[])) {
                return;
            }

            tokens = (String[]) o;
            tags = getTags(tokens);
            i.addProcessedTextPart(sentence2POSTags, tags);
            i.addProcessedTextPart(sentence2POSTagd, tagSentence(tokens, tags));
        }

        if (Config.calculateWithCaseSensitivity()) {
            o = i.getProcessedTextPart(sentence1TokensCaseSensitive);
            if (!(o instanceof String[])) {
                return;
            }

            String[] tokens = (String[]) o;
            String[] tags = getTags(tokens);
            i.addProcessedTextPart(sentence1POSTagsCaseSensitive, tags);
            i.addProcessedTextPart(sentence1POSTagdCaseSensitive, tagSentence(tokens, tags));

            o = i.getProcessedTextPart(sentence2TokensCaseSensitive);
            if (!(o instanceof String[])) {
                return;
            }

            tokens = (String[]) o;
            tags = getTags(tokens);
            i.addProcessedTextPart(sentence2POSTagsCaseSensitive, tags);
            i.addProcessedTextPart(sentence2POSTagdCaseSensitive, tagSentence(tokens, tags));
        }

        i.addProcessed(this);

        PerformanceCounters.stopTimer("process POSTags");
    }

    /**
     *
     * @param tokens
     * @return
     */
    protected synchronized String[] getTags(String[] tokens) {
        return tagger.tag(tokens);
    }

    /**
     *
     * @param tokens
     * @param tags
     * @return
     */
    protected String tagSentence(String[] tokens, String[] tags) {
        POSSample sample = new POSSample(tokens, tags);
        return sample.toString();
    }

}
