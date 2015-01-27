/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.util.Objects;

/**
 *
 * @author David Simões
 */
public class Chunk {

    private final String chunkType;
    private String chunkText;
    private String lemma;

    /**
     *
     * @param chunkType
     * @param chunkText
     */
    public Chunk(String chunkType, String chunkText) {
        this.chunkType = chunkType;
        this.chunkText = chunkText;
    }

    /**
     *
     * @param chunkText
     */
    public void setChunkText(String chunkText) {
        this.chunkText = chunkText;
    }

    /**
     *
     * @param lemma
     */
    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    /**
     *
     * @return
     */
    public String getChunkType() {
        return chunkType;
    }

    /**
     *
     * @return
     */
    public String getChunkText() {
        return chunkText;
    }

    /**
     *
     * @return
     */
    public String getLemma() {
        return lemma;
    }

    @Override
    public String toString() {
        return "[" + chunkType + " " + chunkText + "]";
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Chunk other = (Chunk) obj;
        if (!Objects.equals(this.chunkType, other.chunkType)) {
            return false;
        }
        return Objects.equals(this.chunkText, other.chunkText);
    }


    /**
     *
     * @return
     */
    public String toWNPos() {

        switch (chunkType) {
            case "NP":
                return "#n";
            case "VP":
                return "#v";
        }

        return "";
    }
}
