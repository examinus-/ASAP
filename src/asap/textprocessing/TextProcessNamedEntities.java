/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.textprocessing;

import asap.Config;
import asap.Instance;
import asap.NamedEntitiesFound;
import asap.NamedEntity;
import asap.PerformanceCounters;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

/**
 * This processer takes care of identifying named entities of each sentence.
 * processing input: Sentences (String) processing output: The named entities
 * found (NamedEntity[])
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class TextProcessNamedEntities implements TextProcesser, TextProcessedPartKeyConsts {

    /**
     *
     */
    protected static final HashMap<Long, TextProcessNamedEntities> tpnes
            = new HashMap<>();

    /**
     *
     */
    protected List<Object> ners;
    private static List<TokenNameFinderModel> models;

    /**
     *
     */
    protected final TextProcesser textProcesserDependency;

    /**
     *
     * @param t
     * @return
     */
    public static TextProcessNamedEntities getTextProcessNamedEntities(Thread t) {
        TextProcessNamedEntities r;
        if (tpnes.containsKey(t.getId())) {
            return tpnes.get(t.getId());
        }
        r = new TextProcessNamedEntities(t);
        tpnes.put(t.getId(), r);
        return r;
    }

    /**
     *
     * @return
     */
    public static TextProcessNamedEntities getTextProcessNamedEntities() {
        return getTextProcessNamedEntities(Thread.currentThread());
    }

    /**
     *
     * @param modelsPath
     * @param t
     */
    protected TextProcessNamedEntities(String modelsPath, Thread t) {
        ners = new LinkedList<>();
        if (models == null) {
            TokenNameFinderModel model;
            models = new LinkedList<>();
            for (String nerModelsFilename : Config.getNerModelFilenames()) {
                File modelFile = new File(Config.getOpenNlpModelsDirectory() + File.separator + nerModelsFilename);
                try {
                    System.out.println("loading TokenNameFinderModel from:" + modelFile.getAbsolutePath());
                    model = new TokenNameFinderModel(modelFile);
                    models.add(model);
                } catch (IOException ex) {
                    Logger.getLogger(TextProcessNamedEntities.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        for (TokenNameFinderModel model : models) {
            ners.add(new NameFinderME(model));
        }

        textProcesserDependency = TextProcessTokenizer.getTextProcessTokenizer(t);

    }

    /**
     *
     * @param t
     */
    protected TextProcessNamedEntities(Thread t) {
        this(Config.getOpenNlpModelsDirectory(), t);
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public TextProcesser getInstance(Thread t) {
        return getTextProcessNamedEntities(t);
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

        PerformanceCounters.startTimer("process NER");

        Object o;
        NamedEntity[] nes = null;

        if (Config.calculateWithoutCaseSensitivity()) {
            o = i.getProcessedTextPart(sentence1Tokens);
            if (!(o instanceof String[])) {
                return;
            }

            for (Object ner : ners) {
                nes = tagSentenceNE(ner, (String[]) o, i.getSentence1().toLowerCase());
                for (NamedEntity ne : nes) {
                    NamedEntitiesFound.log(ne, i.getSentence1().toLowerCase(), i.getPair_ID());
                }

            }
            i.addProcessedTextPart(sentence1NER, nes);

            o = i.getProcessedTextPart(sentence2Tokens);
            if (!(o instanceof String[])) {
                return;
            }
            for (Object ner : ners) {
                nes = tagSentenceNE(ner, (String[]) o, i.getSentence2().toLowerCase());
                for (NamedEntity ne : nes) {
                    NamedEntitiesFound.log(ne, i.getSentence2().toLowerCase(), i.getPair_ID());
                }

            }
            i.addProcessedTextPart(sentence2NER, nes);
        }

        if (Config.calculateWithCaseSensitivity()) {
            o = i.getProcessedTextPart(sentence1TokensCaseSensitive);
            if (!(o instanceof String[])) {
                return;
            }

            for (Object ner : ners) {
                nes = tagSentenceNE(ner, (String[]) o, i.getSentence1());
                for (NamedEntity ne : nes) {
                    NamedEntitiesFound.log(ne, i.getSentence1(), i.getPair_ID());
                }

            }
            i.addProcessedTextPart(sentence1NERCaseSensitive, nes);

            o = i.getProcessedTextPart(sentence2TokensCaseSensitive);
            if (!(o instanceof String[])) {
                return;
            }
            for (Object ner : ners) {
                nes = tagSentenceNE(ner, (String[]) o, i.getSentence2());
                for (NamedEntity ne : nes) {
                    NamedEntitiesFound.log(ne, i.getSentence2(), i.getPair_ID());
                }

            }
            i.addProcessedTextPart(sentence2NERCaseSensitive, nes);
        }

        i.addProcessed(this);

        PerformanceCounters.stopTimer("process NER");
    }

    private NamedEntity[] tagSentenceNEOpenNLP(NameFinderME ner, String[] sentenceTokens,
            String sentence) {

        LinkedList<NamedEntity> namedEntities = new LinkedList<>();

        Span nameSpans[] = ner.find(sentenceTokens);
        //ner.clearAdaptiveData();

        StringBuilder sb;

        for (Span nameSpan : nameSpans) {
            sb = new StringBuilder();
            for (int j = nameSpan.getStart(); j < nameSpan.getEnd(); j++) {
                sb.append(sentenceTokens[j])
                        .append(" ");
            }
            namedEntities.add(new NamedEntity(sb.toString(), nameSpan.getType(), sentence));
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
    public NamedEntity[] tagSentenceNE(Object ner, String[] sentenceTokens,
            String sentence) {
        if (ner instanceof NameFinderME) {
            return tagSentenceNEOpenNLP((NameFinderME) ner, sentenceTokens, sentence);
        }
        return new NamedEntity[0];
    }

}
