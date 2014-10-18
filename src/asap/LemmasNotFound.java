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
 * @author skit
 */
public class LemmasNotFound {

    private static LemmasNotFound logger = new LemmasNotFound();
    
    private File logFile;
    private FileOutputStream fos;
    
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
        logFile = new File("outputs/wordsNotFoundInWN.log");
        try {
            fos = new FileOutputStream(logFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LemmasNotFound.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void _log(Chunk chunk, String sentence) {
        ChunkWithSentence cws = new ChunkWithSentence(chunk, sentence);
        if (!loggedChunks.containsKey(cws.getKey())) {
            loggedChunks.put(cws.getKey(), cws);
        }
    }
    
    public static void log(Chunk chunk, String sentence) {
        logger._log(chunk, sentence);
    }
    
    public static void finishLog() {
        
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
