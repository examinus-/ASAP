/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.textprocessing;

import asap.Config;
import asap.Instance;
import asap.PerformanceCounters;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This processer takes care of separating different words of each sentence.
 * processing input: Sentences (String) processing output: Each unique word (String[])
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class TextProcessHashWords implements TextProcesser, TextProcessedPartKeyConsts, Serializable {

    private static final HashMap<Long, TextProcessHashWords> tphws
            = new HashMap<>();

    /**
     *
     * @param t
     * @return
     */
    public static TextProcessHashWords getTextProcessHashWords(Thread t) {
        TextProcessHashWords r;
        if (tphws.containsKey(t.getId())) {
            return tphws.get(t.getId());
        }
        r = new TextProcessHashWords();
        tphws.put(t.getId(), r);
        return r;
    }

    /**
     *
     * @return
     */
    public static TextProcessHashWords getTextProcessHashWords() {
        return getTextProcessHashWords(Thread.currentThread());
    }

    /**
     *
     */
    protected TextProcessHashWords() {
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public TextProcesser getInstance(Thread t) {
        return getTextProcessHashWords(t);
    }

    /**
     *
     * @param i
     * @return
     */
    @Override
    public boolean textProcessingDependenciesMet(Instance i) {
        return true;
    }

    /**
     *
     * @param i
     */
    @Override
    public void process(Instance i) {
        
        PerformanceCounters.startTimer("process HashDiferentWords");
        if (Config.calculateWithoutCaseSensitivity()) {
            i.addProcessedTextPart(sentence1Words, getDiferentWords(i.getSentence1().toLowerCase()));
            i.addProcessedTextPart(sentence2Words, getDiferentWords(i.getSentence2().toLowerCase()));
        }
        if (Config.calculateWithCaseSensitivity()) {
            i.addProcessedTextPart(sentence1WordsCaseSensitive, getDiferentWords(i.getSentence1()));
            i.addProcessedTextPart(sentence2WordsCaseSensitive, getDiferentWords(i.getSentence2()));
        }
        
        i.addProcessed(this);
        PerformanceCounters.stopTimer("process HashDiferentWords");
    }

    private String[] getDiferentWords(String sentence) {
        //TODO: HashSet is probably only faster for bigger sentences?
        String[] r;
        ArrayList<String> rAux = new ArrayList<>();

        for (String word : sentence.split(" ")) {
            if (!rAux.contains(word)) {
                rAux.add(word);
            }
        }

        r = new String[rAux.size()];
        r = rAux.toArray(r);
        return r;
    }

}
