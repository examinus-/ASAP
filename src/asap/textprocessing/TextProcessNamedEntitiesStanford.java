/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.textprocessing;

import asap.Config;
import asap.NamedEntity;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This processer takes care of identifying named entities of each sentence.
 * processing input: Sentences (String) processing output: The named entities
 * found (NamedEntity[])
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class TextProcessNamedEntitiesStanford extends TextProcessNamedEntities {

    /**
     *
     * @param t
     * @return
     */
    public static TextProcessNamedEntitiesStanford getTextProcessNamedEntitiesStanford(Thread t) {
        TextProcessNamedEntitiesStanford r;
        if (tpnes.containsKey(t.getId())) {
            return (TextProcessNamedEntitiesStanford) tpnes.get(t.getId());
        }
        r = new TextProcessNamedEntitiesStanford(t);
        tpnes.put(t.getId(), r);
        return r;
    }

    /**
     *
     * @return
     */
    public static TextProcessNamedEntitiesStanford getTextProcessNamedEntitiesStanford() {
        return getTextProcessNamedEntitiesStanford(Thread.currentThread());
    }

    /**
     *
     * @param modelsPath
     * @param t
     */
    protected TextProcessNamedEntitiesStanford(String modelsPath, Thread t) {
        super(modelsPath, t);
        for (String nerModelFilename : Config.getNerModelFilenames()) {
            AbstractSequenceClassifier<CoreLabel> classifier;
            try {
                classifier = CRFClassifier.getClassifier(Config.getStanfordModelsDirectory() + File.separatorChar + nerModelFilename);
            } catch (IOException | ClassCastException | ClassNotFoundException ex) {
                Logger.getLogger(TextProcessNamedEntitiesStanford.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            ners.add(classifier);
        }
    }

    /**
     *
     */
    protected TextProcessNamedEntitiesStanford() {
        this(Thread.currentThread());
    }

    /**
     *
     * @param t
     */
    protected TextProcessNamedEntitiesStanford(Thread t) {
        this(Config.getStanfordModelsDirectory(), t);
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public TextProcesser getInstance(Thread t) {
        return getTextProcessNamedEntitiesStanford(t);
    }

    private NamedEntity[] tagSentenceNEStanfordNLP(CRFClassifier ner,
            String[] sentenceTokens, String sentence) {

        String classifiedCentence = ner.classifyWithInlineXML(sentence);

        LinkedList<NamedEntity> namedEntities = new LinkedList<>();

        int startIndex = classifiedCentence.indexOf("<");

        while (startIndex != -1) {
            int stopIndex = classifiedCentence.indexOf(">", startIndex + 1);
            String entityType = classifiedCentence.substring(startIndex + 1, stopIndex);

            int stop2Index = classifiedCentence.indexOf("</", stopIndex + 1);
            String entity = classifiedCentence.substring(stopIndex + 1, stop2Index);

            namedEntities.add(new NamedEntity(entity, entityType, sentence));

            startIndex = classifiedCentence.indexOf("<", stop2Index + 1);
        }

        return namedEntities.toArray(new NamedEntity[namedEntities.size()]);
    }

    /**
     *
     * @param ner
     * @param sentenceTokens
     * @param sentence
     * @return
     */
    @Override
    public NamedEntity[] tagSentenceNE(Object ner, String[] sentenceTokens,
            String sentence) {

        if (ner instanceof CRFClassifier) {
            return tagSentenceNEStanfordNLP((CRFClassifier) ner,
                    sentenceTokens, sentence);
        }

        return super.tagSentenceNE(ner, sentenceTokens, sentence);
    }

}
