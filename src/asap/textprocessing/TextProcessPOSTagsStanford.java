/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.textprocessing;

import asap.Config;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.io.File;
import java.io.StringReader;
import java.util.List;

/**
 * This processer takes care of tagging the part-of-speech of each part of the
 * sentences. processing input: Tokenized sentences (String[]) processing
 * output: The tags aligned with the tokenized sentences (String[]) as well as
 * inline sentence tagging (String)
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class TextProcessPOSTagsStanford extends TextProcessPOSTags {

    MaxentTagger tagger;

    /**
     *
     * @param t
     * @return
     */
    public static TextProcessPOSTags getTextProcessPOSTags(Thread t) {
        if (tppts.containsKey(t.getId())) {
            return tppts.get(t.getId());
        }
        TextProcessPOSTagsStanford tppt = new TextProcessPOSTagsStanford(t);
        tppts.put(t.getId(), tppt);
        return tppt;
    }

    /**
     *
     * @param modelsPath
     * @param t
     */
    protected TextProcessPOSTagsStanford(String modelsPath, Thread t) {

        tagger = new MaxentTagger(modelsPath + File.separator + Config.getPosTaggerModelFilename());

    }

    /**
     *
     * @param t
     */
    protected TextProcessPOSTagsStanford(Thread t) {
        this(Config.getStanfordModelsDirectory(), t);
    }

    /**
     *
     */
    protected TextProcessPOSTagsStanford() {
        this(Thread.currentThread());
    }

    /**
     * Uses loaded tagger model to calculate POS tags for the given sentence
     * tokens
     *
     * @param tokens
     * @return tags
     */
    @Override
    protected synchronized String[] getTags(String[] tokens) {
        String sentence = "";
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            sentence += token;
            if (i + 1 < tokens.length) {
                sentence += " ";
            }

        }

        List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(sentence));
        String tags[] = null;
        for (List<HasWord> sentenceL : sentences) {
            List<TaggedWord> taggedSentence = tagger.tagSentence(sentenceL);
            tags = new String[taggedSentence.size()];
            for (int j = 0; j < taggedSentence.size(); j++) {
                TaggedWord taggedWord = taggedSentence.get(j);
                tags[j] = taggedWord.tag();
            }
        }
        return tags;
    }

    /**
     * Temporary function to reconstruct sentence with POSTags. It'll become
     * obsolete after class refactor for proper use of OOP.
     *
     * @param tokens
     * @param tags
     * @return
     */
    @Override
    protected String tagSentence(String[] tokens, String[] tags) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            sb.append(tokens[i])
                    .append("_")
                    .append(tags[i]);
            if (i + 1 < tokens.length) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

}
