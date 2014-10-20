/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.textprocessing;

import asap.Instance;
import asap.PerformanceCounters;
import java.util.ArrayList;
import java.util.HashMap;


public class TextProcessHashWords implements TextProcesser, TextProcessedPartKeyConsts {

    private static final HashMap<Long, TextProcessHashWords> tphws
            = new HashMap<>();
    
    public static TextProcessHashWords getTextProcessHashWords(Thread t) {
        TextProcessHashWords r;
        if (tphws.containsKey(t.getId())) {
            return tphws.get(t.getId());
        }
        r = new TextProcessHashWords();
        tphws.put(t.getId(), r);
        return r;
    }
    
    public static TextProcessHashWords getTextProcessHashWords() {
        return getTextProcessHashWords(Thread.currentThread());
    }

    protected TextProcessHashWords() {
    }
    
    @Override
    public boolean textProcessingDependenciesMet(Instance i) {
        return true;
    }

    @Override
    public void process(Instance i) {
        if (i.getProcessedTextPart(sentence1Words) != null) {
            System.out.println("Duplicate calculation detected in ..HashWords!");
        }
        PerformanceCounters.startTimer("process HashDiferentWords");
        i.addProcessedTextPart(sentence1Words, getDiferentWords(i.getSentence1()));
        i.addProcessedTextPart(sentence2Words, getDiferentWords(i.getSentence2()));
        
        i.addProcessed(this);
        PerformanceCounters.stopTimer("process HashDiferentWords");
    }
    
    private String[] getDiferentWords(String sentence) {
        //TODO: HashSet is probably only faster for bigger sentences...
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
