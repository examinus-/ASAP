/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.textprocessing;

import asap.Instance;
import asap.PerformanceCounters;
import java.io.File;
import java.util.HashMap;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;

public class TextProcessPOSTags implements TextProcesser, TextProcessedPartKeyConsts {

    private POSModel posModel;
    private POSTaggerME tagger;
    private final TextProcesser textProcesserDependency;

    private static final HashMap<Long, TextProcessPOSTags> tppts
            = new HashMap<>();
    
    public static TextProcessPOSTags getTextProcessPOSTags(Thread t) {
        TextProcessPOSTags r;
        if (tppts.containsKey(t.getId())) {
            return tppts.get(t.getId());
        }
        r = new TextProcessPOSTags(t);
        tppts.put(t.getId(), r);
        return r;
    }
    
    public static TextProcessPOSTags getTextProcessPOSTags() {
        return getTextProcessPOSTags(Thread.currentThread());
    }
    
    private TextProcessPOSTags(String modelsPath, Thread t) {
        File modelFile = new File(modelsPath + "/en-pos-maxent.bin");
        //System.out.println("loading POSModel from:" + modelFile.getAbsolutePath());
        posModel = new POSModelLoader().load(modelFile);
        tagger = new POSTaggerME(posModel);
        textProcesserDependency = TextProcessTokenizer.getTextProcessTokenizer(t);
    }

    private TextProcessPOSTags(Thread t) {
        this("opennlp-models", t);
    }

    private TextProcessPOSTags() {
        this(Thread.currentThread());
    }

    @Override
    public boolean textProcessingDependenciesMet(Instance i) {
        return i.isProcessed(textProcesserDependency);
    }

    @Override
    public void process(Instance i) {
        if (!textProcessingDependenciesMet(i)) {
            textProcesserDependency.process(i);
        }
        
        PerformanceCounters.startTimer("process POSTags");

        Object o;

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

        i.addProcessed(this);

        PerformanceCounters.stopTimer("process POSTags");
    }

    private synchronized String[] getTags(String[] tokens) {
        return tagger.tag(tokens);
    }

    private String tagSentence(String[] tokens, String[] tags) {
        POSSample sample = new POSSample(tokens, tags);
        return sample.toString();
    }

}
