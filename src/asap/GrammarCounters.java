/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David Simões
 */
public class GrammarCounters {

    private static final GrammarCounters logger = new GrammarCounters();

    /**
     *
     * @param chunk
     */
    public static void incTotal(Chunk chunk) {
        if (chunk.getChunkType().equalsIgnoreCase("VP")) {
            logger.totalVPChunks++;
        }
        if (chunk.getChunkType().equalsIgnoreCase("NP")) {
            logger.totalNPChunks++;
        }

        logger.totalChunks++;
    }

    private File logFile;
    private FileOutputStream fos;

    private int totalChunks = 0;
    private int totalVPChunks = 0;
    private int totalNPChunks = 0;
    private int verbsOutsideVps = 0;
    private int nounsOutsideNps = 0;

    private final HashMap<String, ChunkWithExtraInfo> chunks;

    private class ChunkWithExtraInfo {

        private final Chunk chunk;
        private final HashSet<String> sentences;
        private final boolean isVerbOutsideVp;    //false ---> noun outside NP

        public ChunkWithExtraInfo(Chunk chunk, String sentence, int instanceId, boolean isVerbOutsideVp) {
            this.chunk = chunk;
            this.sentences = new HashSet<>();
            this.sentences.add(String.format("%5s %s", instanceId, sentence));
            this.isVerbOutsideVp = isVerbOutsideVp;
        }

        public void addSentence(String sentence, int instanceId) {
            this.sentences.add(String.format("%5s %s", instanceId, sentence));
        }

        @Override
        public String toString() {
            String r;
            if (isVerbOutsideVp) {
                r = "verb outside VP in chunk:\t" + chunk
                        + "\n\tin sentences:\n\t\t";
            } else {
                r = "noun outside NP in chunk:\t" + chunk
                        + "\n\tin sentences:\n\t\t";
            }

            Iterator<String> it = sentences.iterator();

            while (it.hasNext()) {
                String sentence = it.next();
                r += sentence + "\n";
                if (it.hasNext()) {
                    r += "\t\t";
                }
            }

            return r + "\n\n";
        }
    }

    private GrammarCounters() {
        logFile = new File(Config.getLogGrammarCountersOutputFilename());
        logFile.getParentFile().mkdirs();
        try {
            fos = new FileOutputStream(logFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LemmasNotFound.class.getName()).log(Level.SEVERE, null, ex);
        }
        chunks = new HashMap<>();
    }

    private void _logVerbOutsideVP(Chunk chunk, String sentence, int instanceId) {
        if (!chunks.containsKey(chunk.toString())) {
            ChunkWithExtraInfo cwei = new ChunkWithExtraInfo(chunk, sentence, instanceId, true);
            chunks.put(chunk.toString(), cwei);
        } else {
            chunks.get(chunk.toString()).addSentence(sentence, instanceId);
        }
        verbsOutsideVps++;
    }

    private void _logNounOutsideNP(Chunk chunk, String sentence, int instanceId) {
        if (!chunks.containsKey(chunk.toString())) {
            ChunkWithExtraInfo cwei = new ChunkWithExtraInfo(chunk, sentence, instanceId, false);
            chunks.put(chunk.toString(), cwei);
        } else {
            chunks.get(chunk.toString()).addSentence(sentence, instanceId);
        }
        nounsOutsideNps++;
    }

    /**
     *
     * @param chunk
     * @param sentence
     * @param instanceId
     */
    public synchronized static void logVerbOutsideVP(Chunk chunk, String sentence, int instanceId) {
        logger._logVerbOutsideVP(chunk, sentence, instanceId);
    }

    /**
     *
     * @param chunk
     * @param sentence
     * @param instanceId
     */
    public synchronized static void logNounOutsideNP(Chunk chunk, String sentence, int instanceId) {
        logger._logNounOutsideNP(chunk, sentence, instanceId);
    }

    /**
     *
     */
    public static void finishLog() {
        String tmp = "total chunks processed: " + logger.totalChunks
                + "\nverbs outside VPs:" + logger.verbsOutsideVps
                + " / " + logger.totalVPChunks
                + "\nnouns outside NPs:" + logger.nounsOutsideNps
                + " / " + logger.totalNPChunks
                + "\n\n";
        try {
            logger.fos.write(tmp.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(GrammarCounters.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (ChunkWithExtraInfo loggedChunk : logger.chunks.values()) {
            String out = loggedChunk.toString();
            try {
                logger.fos.write(out.getBytes());
            } catch (IOException ex) {
                Logger.getLogger(LemmasNotFound.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            logger.fos.flush();
            logger.fos.close();
        } catch (IOException ex) {
            Logger.getLogger(LemmasNotFound.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
