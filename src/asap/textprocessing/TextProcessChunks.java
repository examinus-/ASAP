/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.textprocessing;

import asap.Chunk;
import asap.Instance;
import asap.PerformanceCounters;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.chunker.ChunkerModelLoader;

public class TextProcessChunks implements TextProcesser, TextProcessedPartKeyConsts {
    
    private static final HashMap<Long, TextProcessChunks> tpcs
            = new HashMap<>();
    
    private ChunkerME chunker;
    private ChunkerModel chunkerModel;
    private final TextProcesser textProcesserDependency;

    public static TextProcessChunks getTextProcessChunks(Thread t) {
        TextProcessChunks r;
        if (tpcs.containsKey(t.getId())) {
            return tpcs.get(t.getId());
        }
        r = new TextProcessChunks(t);
        tpcs.put(t.getId(), r);
        return r;
    }
    
    public static TextProcessChunks getTextProcessChunks() {
        return getTextProcessChunks(Thread.currentThread());
    }


    private TextProcessChunks(String modelsPath, Thread t) {
        File modelFile = new File(modelsPath + "/en-chunker.bin");
        //System.out.println("loading ChunkerModel from:" + modelFile.getAbsolutePath());
        chunkerModel = new ChunkerModelLoader().load(modelFile);
        chunker = new ChunkerME(chunkerModel);
        
        textProcesserDependency = TextProcessPOSTags.getTextProcessPOSTags(t);
    }

    private TextProcessChunks(Thread t) {
        this("opennlp-models", t);
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
        if (i.getProcessedTextPart(sentence1Chunks) != null) {
            System.out.println("Duplicate calculation detected in ..Chunks!");
        }
        PerformanceCounters.startTimer("process Chunks");
        Object o;

        o = i.getProcessedTextPart(sentence1Tokens);
        if (!(o instanceof String[])) {
            return;
        }
        String[] tokens = (String[]) o;

        o = i.getProcessedTextPart(sentence1POSTags);
        if (!(o instanceof String[])) {
            return;
        }
        String[] tags = (String[]) o;
        StringBuilder chunkedSentence = new StringBuilder();
        Chunk[] chunks = chunkSentence(tokens, tags, chunkedSentence);
        
        i.addProcessedTextPart(sentence1Chunks, chunks);
        i.addProcessedTextPart(sentence1Chunkd, chunkedSentence.toString());

        o = i.getProcessedTextPart(sentence2Tokens);
        if (!(o instanceof String[])) {
            return;
        }
        tokens = (String[]) o;

        o = i.getProcessedTextPart(sentence2POSTags);
        if (!(o instanceof String[])) {
            return;
        }
        tags = (String[]) o;

        chunks = chunkSentence(tokens, tags, chunkedSentence);

        i.addProcessedTextPart(sentence2Chunks, chunks);
        i.addProcessedTextPart(sentence2Chunkd, chunkedSentence.toString());

        i.addProcessed(this);
        PerformanceCounters.stopTimer("process Chunks");
    }

    private Chunk[] chunkSentence(String[] tokens, String[] tags, StringBuilder chunkedSentence) {
        chunkedSentence.delete(0, chunkedSentence.length());
        String[] chunkTypes = chunker.chunk(tokens, tags);
        ArrayList<Chunk> chunks = new ArrayList<>();
        Chunk chunk;
        //TODO: faster way?
        for (int i = 0; i < chunkTypes.length; i++) {
            //System.out.println("creating chunk " + i);
            if (chunkTypes[i].length() < 2) {
                chunk = new Chunk(chunkTypes[i], tokens[i]);
                chunks.add(chunk);
                chunkedSentence.append(chunk.toString());
                continue;
            }
            StringBuilder chunkBuilder = new StringBuilder(tokens[i]);

            String followUpType = "I" + chunkTypes[i].substring(1);
            if (i + 1 < chunkTypes.length) {
                for (; i + 1 < chunkTypes.length
                        && chunkTypes[i + 1].equals(followUpType); i++) {
                    chunkBuilder.append(" ");
                    chunkBuilder.append(tokens[i + 1]);
                }
            }
            if (chunkBuilder.length() > 0) {
                chunk = new Chunk(chunkTypes[i].substring(2), chunkBuilder.toString());
            } else {
                chunk = new Chunk(chunkTypes[i].substring(2), "");
            }
            chunks.add(chunk);
            chunkedSentence.append(chunk.toString());

        }

        //System.out.println("chunking complete");
        Chunk[] tmp = new Chunk[chunks.size()];
        return chunks.toArray(tmp);
    }
}
