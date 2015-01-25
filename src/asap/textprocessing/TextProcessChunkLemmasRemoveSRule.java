/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.textprocessing;

import asap.Chunk;
import java.io.File;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class TextProcessChunkLemmasRemoveSRule extends TextProcessChunkLemmasWithDBPediaLookups {

    /**
     *
     */
    public TextProcessChunkLemmasRemoveSRule() {
        super(Thread.currentThread());
    }

    /**
     *
     * @param t
     * @param filename
     */
    public TextProcessChunkLemmasRemoveSRule(Thread t, String filename) {
        super(t, filename);
    }
    
    /**
     *
     * @param t
     */
    public TextProcessChunkLemmasRemoveSRule(Thread t) {
        super(t);
    }
    
    /**
     *
     * @param t
     * @return
     */
    public static TextProcessChunkLemmasRemoveSRule
            getTextProcessChunkLemmasRemoveSRule(Thread t) {
        if (tpcls.containsKey(t.getId())) {
            return (TextProcessChunkLemmasRemoveSRule) tpcls.get(t.getId());
        }
        TextProcessChunkLemmasRemoveSRule textProcessChunkLemmasRemoveSRule;
        if (new File(DEFAULT_LOOKUPS_CACHE_FILENAME).exists()) {
            textProcessChunkLemmasRemoveSRule = new TextProcessChunkLemmasRemoveSRule(t,
                    DEFAULT_LOOKUPS_CACHE_FILENAME);
        } else {
            textProcessChunkLemmasRemoveSRule = new TextProcessChunkLemmasRemoveSRule(t);
        }
        tpcls.put(t.getId(), textProcessChunkLemmasRemoveSRule);
        return textProcessChunkLemmasRemoveSRule;
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public TextProcesser getInstance(Thread t) {
        return getTextProcessChunkLemmasRemoveSRule(t);
    }
    
    /**
     *
     * @param chunk
     * @return
     */
    @Override
    protected String lemmatizeChunk(Chunk chunk) {
        String lemma = super.lemmatizeChunk(chunk);
        if (lemma == null ? true : lemma.isEmpty()) {
            Chunk editedChunk = new Chunk(chunk.getChunkType(), chunk.getChunkText().replaceAll("(\\S+)s", "$1"));
            lemma = super.lemmatizeChunk(editedChunk);
        }
        
        return lemma;
    }
}
