/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class NamedEntity {

    private final HashSet<String> sentences;
    private final String entity;
    private final String type;
    private String wordnetType;

    /**
     *
     * @param entity
     * @param type
     * @param sentence
     */
    public NamedEntity(String entity, String type, String sentence) {
        this.entity = entity;
        this.type = type;
        this.sentences = new HashSet<>();
    }

    /**
     *
     * @return
     */
    public String getKey() {
        return entity + type;
    }

    /**
     *
     * @return
     */
    public String getEntity() {
        return entity;
    }

    /**
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @return
     */
    public String getWordnetType() {
        return wordnetType;
    }

    /**
     *
     * @param wordnetType
     */
    public void setWordnetType(String wordnetType) {
        this.wordnetType = wordnetType;
    }

    /**
     *
     * @return
     */
    public Set<String> getSentences() {
        return Collections.unmodifiableSet(sentences);
    }

    /**
     *
     * @param sentence
     */
    public void addSentence(String sentence) {
        this.sentences.add(sentence);
    }

    /**
     *
     * @return
     */
    public String toLog() {
        StringBuilder sb = new StringBuilder();

        sb.append(
                String.format("entity: %s\n  type: %s\n\tin sentences:",
                        entity, type)
        );

        for (String sentence : sentences) {
            sb.append("\n\t\t").append(sentence);
        }

        return sb.append("\n").toString();
    }
}
