/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.textprocessing;

import asap.Chunk;
import asap.Config;
import asap.GrammarCounters;
import java.io.Serializable;
import asap.Instance;
import asap.PerformanceCounters;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.chunker.ChunkerModelLoader;

/**
 * This processer takes care of separating sentences by phrasal parts
 * processing input: Sentences (String) processing output: identified chunks (Chunk[])
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class TextProcessChunks implements TextProcesser, TextProcessedPartKeyConsts, Serializable {

    private static final HashMap<Long, TextProcessChunks> tpcs
            = new HashMap<>();

    private ChunkerME chunker;
    private static ChunkerModel chunkerModel;
    private final TextProcesser textProcesserDependency;

    /**
     *
     * @param t
     * @return
     */
    public static TextProcessChunks getTextProcessChunks(Thread t) {
        TextProcessChunks r;
        if (tpcs.containsKey(t.getId())) {
            return tpcs.get(t.getId());
        }
        r = new TextProcessChunks(t);
        tpcs.put(t.getId(), r);
        return r;
    }

    /**
     *
     * @return
     */
    public static TextProcessChunks getTextProcessChunks() {
        return getTextProcessChunks(Thread.currentThread());
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public TextProcesser getInstance(Thread t) {
        return getTextProcessChunks(t);
    }

    private TextProcessChunks(String modelsPath, Thread t) {
        if (chunkerModel == null) {
            File modelFile = new File(Config.getOpenNlpModelsDirectory() + File.separator + Config.getChunkerModelFilename());
            //java.lang.System.out.println("loading ChunkerModel from:" + modelFile.getAbsolutePath());
            chunkerModel = new ChunkerModelLoader().load(modelFile);
        }
        chunker = new ChunkerME(chunkerModel);

        if (Config.useStanfordPOSTagger()) {
            textProcesserDependency = TextProcessPOSTagsStanford.getTextProcessPOSTags(t);
        } else {
            textProcesserDependency = TextProcessPOSTags.getTextProcessPOSTags(t);
        }
    }

    private TextProcessChunks(Thread t) {
        this(Config.getOpenNlpModelsDirectory(), t);
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
        PerformanceCounters.startTimer("process Chunks");
        
        Object o;

        
        if (Config.calculateWithoutCaseSensitivity()) {
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
            Chunk[] chunks = chunkSentence(tokens, tags, chunkedSentence, i.getSentence1().toLowerCase(),
                    i.getPair_ID());

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

            chunks = chunkSentence(tokens, tags, chunkedSentence, i.getSentence2().toLowerCase(),
                    i.getPair_ID());

            i.addProcessedTextPart(sentence2Chunks, chunks);
            i.addProcessedTextPart(sentence2Chunkd, chunkedSentence.toString());
        }
        if (Config.calculateWithCaseSensitivity()) {
            o = i.getProcessedTextPart(sentence1TokensCaseSensitive);
            if (!(o instanceof String[])) {
                return;
            }
            String[] tokens = (String[]) o;

            o = i.getProcessedTextPart(sentence1POSTagsCaseSensitive);
            if (!(o instanceof String[])) {
                return;
            }
            String[] tags = (String[]) o;
            StringBuilder chunkedSentence = new StringBuilder();
            Chunk[] chunks = chunkSentence(tokens, tags, chunkedSentence, i.getSentence1(),
                    i.getPair_ID());

            i.addProcessedTextPart(sentence1ChunksCaseSensitive, chunks);
            i.addProcessedTextPart(sentence1ChunkdCaseSensitive, chunkedSentence.toString());

            o = i.getProcessedTextPart(sentence2TokensCaseSensitive);
            if (!(o instanceof String[])) {
                return;
            }
            tokens = (String[]) o;

            o = i.getProcessedTextPart(sentence2POSTagsCaseSensitive);
            if (!(o instanceof String[])) {
                return;
            }
            tags = (String[]) o;

            chunks = chunkSentence(tokens, tags, chunkedSentence, i.getSentence2(),
                    i.getPair_ID());

            i.addProcessedTextPart(sentence2ChunksCaseSensitive, chunks);
            i.addProcessedTextPart(sentence2ChunkdCaseSensitive, chunkedSentence.toString());
        }
        
        
        
        i.addProcessed(this);
        PerformanceCounters.stopTimer("process Chunks");
    }

    private Chunk[] chunkSentence(String[] tokens, String[] tags,
            StringBuilder chunkedSentence, String sentence, int instanceId) {
        chunkedSentence.delete(0, chunkedSentence.length());
        String[] chunkTypes = chunker.chunk(tokens, tags);
        ArrayList<Chunk> chunks = new ArrayList<>();
        Chunk chunk;
        //TODO: faster way?
        for (int i = 0; i < chunkTypes.length; i++) {
            int j = i;
            //java.lang.System.out.println("creating chunk " + i);
            if (chunkTypes[i].length() < 2) {
                chunk = new Chunk(chunkTypes[i], tokens[i]);
                chunks.add(chunk);
                chunkedSentence.append(chunk.toString());
                GrammarCounters.incTotal(chunk);
                if (tags[i].contains("VB") && !chunkTypes[i].contains("VP")) {
                    if (!tags[i].contains("VBG")) {
                        GrammarCounters.logVerbOutsideVP(chunk, sentence, instanceId);
                    }
                }

                if (tags[i].contains("NN") && !chunkTypes[i].contains("NP")) {
                    GrammarCounters.logNounOutsideNP(chunk, sentence, instanceId);
                }

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

                //check these tokens for possible grammatically "misplacement"
                for (int k = j; k <= i && k + 1 < chunkTypes.length; k++) {
                    if (chunkTypes[k] == null) {
                        continue;
                    }
                    if (tags[k].contains("VB") && !chunkTypes[k].contains("VP")) {
                        if (!tags[i].contains("VBG")) {
                            GrammarCounters.logVerbOutsideVP(chunk, sentence, instanceId);
                        }
                    }
                    if (tags[k].contains("NN") && !chunkTypes[k].contains("NP")) {
                        GrammarCounters.logNounOutsideNP(chunk, sentence, instanceId);
                    }
                }
                //----
            } else {
                chunk = new Chunk(chunkTypes[i].substring(2), "");
            }
            GrammarCounters.incTotal(chunk);
            chunks.add(chunk);
            chunkedSentence.append(chunk.toString());

        }

        //java.lang.System.out.println("chunking complete");
        Chunk[] tmp = new Chunk[chunks.size()];
        return chunks.toArray(tmp);
    }
}
