/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.io.File;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;

public class TextProcessPOSTags extends TextProcessTokenizer implements TextProcessedPartKeyConsts {

    private POSModel posModel;
    private POSTaggerME tagger;
    private static final TextProcesser textProcesserDependency = new TextProcessTokenizer();

    public TextProcessPOSTags(String modelsPath) {
        super(modelsPath);
        File modelFile = new File(modelsPath + "/en-pos-maxent.bin");
        //System.out.println("loading POSModel from:" + modelFile.getAbsolutePath());
        posModel = new POSModelLoader().load(modelFile);
        tagger = new POSTaggerME(posModel);
    }

    public TextProcessPOSTags() {
        this("opennlp-models");
    }

    @Override
    public boolean textProcessingDependenciesMet(Instance i) {
        return i.isProcessed(textProcesserDependency);
    }

    @Override
    public void process(Instance i) {
        if (!textProcessingDependenciesMet(i)) {
            super.process(i);
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

    private String[] getTags(String[] tokens) {
        return tagger.tag(tokens);
    }

    private String tagSentence(String[] tokens, String[] tags) {
        POSSample sample = new POSSample(tokens, tags);
        return sample.toString();
    }

}
