/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;


public class TextProcessTokenizer implements TextProcesser, TextProcessedPartKeyConsts {
    private static final TextProcesser textProcesserDependency = null;

    public TextProcessTokenizer(String modelsPath) {
        
    }
    public TextProcessTokenizer() {
        this("opennlp-models");
    }
    
    @Override
    public boolean textProcessingDependenciesMet(Instance i) {
        return i.isProcessed(textProcesserDependency);
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
