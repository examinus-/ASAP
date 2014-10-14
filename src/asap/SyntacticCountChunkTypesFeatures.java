/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.chunker.ChunkerModelLoader;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

public class SyntacticCountChunkTypesFeatures implements FeatureCalculator {

    POSModel posModel;
    POSTaggerME tagger;
    ChunkerME chunker;
    ChunkerModel chunkerModel;

    public SyntacticCountChunkTypesFeatures() {

        String modelsPath = "src/opennlp-models";
        File modelFile = new File(modelsPath + "/en-pos-maxent.bin");
        System.out.println("loading POSModel from:" + modelFile.getAbsolutePath());
        posModel = new POSModelLoader().load(modelFile);
        tagger = new POSTaggerME(posModel);

        modelFile = new File(modelsPath + "/en-chunker.bin");
        System.out.println("loading ChunkerModel from:" + modelFile.getAbsolutePath());
        chunkerModel = new ChunkerModelLoader().load(modelFile);
        chunker = new ChunkerME(chunkerModel);

    }

    @Override
    public void calculate(Instance i) {
        System.out.println("calculating " + Arrays.toString(getFeatureNames())
                + " for instance " + i.getAttributeAt(0));

        int sentence1Counters[];
        int sentence2Counters[];

        i.setSentence1Tokenized(tokenizeSentence(i.getSentence1()));
        i.setSentence2Tokenized(tokenizeSentence(i.getSentence2()));

        i.setSentence1PosTags(getTags(i.getSentence1Tokenized()));
        i.setSentence2PosTags(getTags(i.getSentence2Tokenized()));

        i.setSentence1PosTagged(tagSentence(i.getSentence1Tokenized(), i.getSentence1PosTags()));
        i.setSentence2PosTagged(tagSentence(i.getSentence2Tokenized(), i.getSentence2PosTags()));
         
        i.setSentence1Chunks(chunkSentence(i.getSentence1Tokenized(), i.getSentence1PosTags()));
        i.setSentence2Chunks(chunkSentence(i.getSentence2Tokenized(), i.getSentence2PosTags()));

        sentence1Counters = countNpsVpsPPs(i.getSentence1Chunks());
        sentence2Counters = countNpsVpsPPs(i.getSentence2Chunks());

        double[] features = {Math.abs(sentence1Counters[0] - sentence2Counters[0]),
            Math.abs(sentence1Counters[1] - sentence2Counters[1]),
            Math.abs(sentence1Counters[2] - sentence2Counters[2])};

        i.addAtribute(features);
    }

    @Override
    public String[] getFeatureNames() {
        String r[] = {"syn_count_dif_NPs",
            "syn_count_dif_VPs",
            "syn_count_dif_PPs"};

        return r;
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
            Logger.getLogger(SyntacticCountChunkTypesFeatures.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private String[] getTags(String[] tokens) {
        return tagger.tag(tokens);
    }

    private String tagSentence(String[] tokens, String[] tags) {
        POSSample sample = new POSSample(tokens, tags);
        return sample.toString();
    }

    private String[] chunkSentence(String[] tokens, String[] tags) {
        String[] chunkTypes = chunker.chunk(tokens, tags);
        ArrayList<String> chunks = new ArrayList<>();

        for (int i = 0; i < chunkTypes.length; i++) {
            System.out.println("creating chunk " + i);
            String chunkType = chunkTypes[i];
            if (chunkType.length() < 2) {
                chunks.add(chunkType + " " + tokens[i]);
                continue;
            }
            
            StringBuilder chunkBuilder = new StringBuilder(chunkType.substring(2));
            chunkBuilder.append(" ");
            chunkBuilder.append(tokens[i]);

            String followUpType = "I" + chunkType.substring(1);
            if (i + 1 < chunkTypes.length) {
                for (; i + 1 < chunkTypes.length
                        && chunkTypes[i + 1].equals(followUpType); i++) {
                    chunkBuilder.append(" ");
                    chunkBuilder.append(tokens[i + 1]);
                }
            }
            if (chunkBuilder.length() > 0) {
                chunks.add(chunkBuilder.toString());
            }
        }

        System.out.println("chunking complete");
        String[] tmp = new String[chunks.size()];
        return chunks.toArray(tmp);
    }

    private int[] countNpsVpsPPs(String[] chunks) {
        int[] counters = new int[3];

        for (String chunk : chunks) {
            if (chunk.contains("NP")) {
                counters[0]++;
            } else if (chunk.contains("VP")) {
                counters[1]++;
            } else if (chunk.contains("PP")) {
                counters[2]++;
            }
        }
        return counters;
    }

}
