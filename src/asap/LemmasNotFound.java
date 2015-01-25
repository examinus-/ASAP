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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David Simões
 */
public class LemmasNotFound {

    private static final LemmasNotFound logger = new LemmasNotFound();

    private File logFile;
    private FileOutputStream fos;

    private int totalLemmas = 0;
    private int lemmasNotFound = 0;
    private int lemmasNotFoundInNPChunks = 0;
    private int totalNPChunks = 0;
    private int lemmasNotFoundInVPChunks = 0;
    private int totalVPChunks = 0;

    private HashMap<String, ChunkWithSentence> loggedChunks;

    class ChunkWithSentence {

        private final Chunk chunk;
        private final String sentence;

        public ChunkWithSentence(Chunk chunk, String sentence) {
            this.chunk = chunk;
            this.sentence = sentence;
        }

        public Chunk getChunk() {
            return chunk;
        }

        public String getSentence() {
            return sentence;
        }

        public String getKey() {
            return chunk.getChunkText() + chunk.getChunkType();
        }

        public String log() {
            return "\"" + chunk.getChunkText() + "\" (POS=" + chunk.getChunkType() + ")\n\t in sentence \"" + sentence + "\"\n\n";
        }
    }

    private LemmasNotFound() {
        loggedChunks = new HashMap<>();

        logFile = new File(Config.getLogLemmasNotFoundOutputFilename());
        logFile.getParentFile().mkdirs();
        try {
            fos = new FileOutputStream(logFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LemmasNotFound.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void _log(Chunk chunk, String sentence, int instanceId) {
        ChunkWithSentence cws = new ChunkWithSentence(chunk, String.format("%5s %s", instanceId, sentence));
        if (!loggedChunks.containsKey(cws.getKey())) {
            loggedChunks.put(cws.getKey(), cws);
            if (cws.getChunk().getChunkType().equalsIgnoreCase("VP")) {
                lemmasNotFoundInVPChunks++;
            }
            if (cws.getChunk().getChunkType().equalsIgnoreCase("NP")) {
                lemmasNotFoundInNPChunks++;
            }
        }
        lemmasNotFound++;
    }

    /**
     *
     * @param chunk
     * @param sentence
     * @param instanceId
     */
    public synchronized static void log(Chunk chunk, String sentence, int instanceId) {
        logger._log(chunk, sentence, instanceId);
    }

    /**
     *
     * @param chunk
     */
    public synchronized static void incTotal(Chunk chunk) {
        logger.totalLemmas++;
        if (chunk.getChunkType().equalsIgnoreCase("VP")) {
            logger.totalVPChunks++;
        }
        if (chunk.getChunkType().equalsIgnoreCase("NP")) {
            logger.totalNPChunks++;
        }
    }

    /**
     *
     */
    public static void finishLog() {
        String tmp = "Lemmas not found: " + logger.lemmasNotFound
                + " / " + logger.totalLemmas
                + "\n\tVP chunk lemmas not found: " + logger.lemmasNotFoundInVPChunks
                + " / " + logger.totalVPChunks
                + "\n\tNP chunk lemmas not found: " + logger.lemmasNotFoundInNPChunks
                + " / " + logger.totalNPChunks
                + "\n\n";

        try {
            logger.fos.write(tmp.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(LemmasNotFound.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (ChunkWithSentence loggedChunk : logger.loggedChunks.values()) {
            String out = loggedChunk.log();
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
