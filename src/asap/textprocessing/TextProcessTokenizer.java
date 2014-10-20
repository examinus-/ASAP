/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.textprocessing;

import asap.Instance;
import asap.PerformanceCounters;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

public class TextProcessTokenizer implements TextProcesser, TextProcessedPartKeyConsts {

    private Tokenizer tokenizer;

    private static final HashMap<Long, TextProcessTokenizer> tpts
            = new HashMap<>();
    
    public static TextProcessTokenizer getTextProcessTokenizer(Thread t) {
        TextProcessTokenizer r;
        if (tpts.containsKey(t.getId())) {
            return tpts.get(t.getId());
        }
        r = new TextProcessTokenizer();
        tpts.put(t.getId(), r);
        return r;
    }
    
    public static TextProcessTokenizer getTextProcessTokenizer() {
        return getTextProcessTokenizer(Thread.currentThread());
    }
    private TextProcessTokenizer(String modelsPath) {

        InputStream modelIn = null;
        try {

            modelIn = new FileInputStream(modelsPath + "/en-token.bin");
            try {

                TokenizerModel model = new TokenizerModel(modelIn);
                tokenizer = new TokenizerME(model);
            } catch (IOException e) {
            } finally {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TextProcessTokenizer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                modelIn.close();
            } catch (IOException ex) {
                Logger.getLogger(TextProcessTokenizer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private TextProcessTokenizer() {
        this("opennlp-models");
    }

    @Override
    public boolean textProcessingDependenciesMet(Instance i) {
        return true;
    }

    @Override
    public void process(Instance i) {
        PerformanceCounters.startTimer("process Tokens");
        i.addProcessedTextPart(sentence1Tokens, tokenizeSentence(i.getSentence1()));
        i.addProcessedTextPart(sentence2Tokens, tokenizeSentence(i.getSentence2()));

        i.addProcessed(this);
        PerformanceCounters.stopTimer("process Tokens");
    }

    private String[] tokenizeSentence(String sentence) {
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
