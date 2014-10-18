/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.smu.tspell.wordnet.SynsetType;

/**
 *
 * @author skit
 */
public class Chunk {

    private final String chunkType;
    private final String chunkText;
    private String lemma;

    public Chunk(String chunkType, String chunkText) {
        this.chunkType = chunkType;
        this.chunkText = chunkText;
    }
    
    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public String getChunkType() {
        return chunkType;
    }

    public String getChunkText() {
        return chunkText;
    }
    
    public String getLemma() {
        return lemma;
    }

    @Override
    public String toString() {
        return "[" + chunkType + " " + chunkText + "]";
    }

    public POS toPOS() {
        switch (chunkType) {
            case "NP":
                return POS.n;
            case "VP":
                return POS.v;
        }
        return POS.a;
    }

    public SynsetType toSynsetType() {

        switch (chunkType) {
            case "NP":
                return SynsetType.NOUN;
            case "VP":
                return SynsetType.VERB;
        }
        return SynsetType.ADJECTIVE;
    }
}
