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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

/**
 *  This processer takes care of tokenizing text.
 * processing input: original sentences
 * processing output: sentences split by words (String[])
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class TextProcessTokenizer implements TextProcesser, TextProcessedPartKeyConsts, Serializable {

    private Tokenizer tokenizer;

    private static TokenizerModel model;

    private static final HashMap<Long, TextProcessTokenizer> tpts
            = new HashMap<>();

    /**
     *
     * @param t
     * @return
     */
    public static TextProcessTokenizer getTextProcessTokenizer(Thread t) {
        TextProcessTokenizer r;
        if (tpts.containsKey(t.getId())) {
            return tpts.get(t.getId());
        }
        r = new TextProcessTokenizer();
        tpts.put(t.getId(), r);
        return r;
    }

    /**
     *
     * @return
     */
    public static TextProcessTokenizer getTextProcessTokenizer() {
        return getTextProcessTokenizer(Thread.currentThread());
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public TextProcesser getInstance(Thread t) {
        return getTextProcessTokenizer(t);
    }

    private TextProcessTokenizer(String modelsPath) {
        boolean modelLoaded = false;
        InputStream modelIn = null;
        try {
            if (model == null) {
                modelIn = new FileInputStream(Config.getOpenNlpModelsDirectory() + File.separator + Config.getTokenizerModelFilename());
                model = new TokenizerModel(modelIn);
                modelLoaded = true;
            }
            tokenizer = new TokenizerME(model);
        } catch (IOException ex) {
            Logger.getLogger(TextProcessTokenizer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (modelLoaded && modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException ex) {
                    Logger.getLogger(TextProcessTokenizer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private TextProcessTokenizer() {
        this("opennlp-models");
    }

    /**
     *
     * @param i
     * @return
     */
    @Override
    public boolean textProcessingDependenciesMet(Instance i) {
        return true;
    }

    /**
     *
     * @param i
     */
    @Override
    public void process(Instance i) {
        PerformanceCounters.startTimer("process TextProcessTokenizer");
        if (Config.calculateWithoutCaseSensitivity()) {
            i.addProcessedTextPart(sentence1Tokens, tokenizeSentence(i.getSentence1().toLowerCase()));
            i.addProcessedTextPart(sentence2Tokens, tokenizeSentence(i.getSentence2().toLowerCase()));
        }
        if (Config.calculateWithCaseSensitivity()) {
            i.addProcessedTextPart(sentence1TokensCaseSensitive, tokenizeSentence(i.getSentence1()));
            i.addProcessedTextPart(sentence2TokensCaseSensitive, tokenizeSentence(i.getSentence2()));
        }
        i.addProcessed(this);
        PerformanceCounters.stopTimer("process TextProcessTokenizer");
    }

    /**
     *
     * @param sentence
     * @return
     */
    protected String[] tokenizeSentence(String sentence) {
        return tokenizeSentenceWithModel(sentence);
    }

    private synchronized String[] tokenizeSentenceWithModel(String sentence) {
        return tokenizer.tokenize(sentence);
    }

    private String[] tokenizeSentenceWithWhiteSpace(String sentence) {
        ObjectStream<String> lineStream
                = new PlainTextByLineStream(new StringReader(sentence));

        String line;
        try {
            while ((line = lineStream.read()) != null) {

                return WhitespaceTokenizer.INSTANCE.tokenize(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(TextProcessTokenizer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

}
