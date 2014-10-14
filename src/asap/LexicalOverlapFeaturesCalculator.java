/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

public class LexicalOverlapFeaturesCalculator implements FeatureCalculator {

    private final int minStopWords;
    private final int maxStopWords;
    private TreeSet<String> topWords;

    public LexicalOverlapFeaturesCalculator(int minStopWords, int maxStopWords) {
        this.minStopWords = minStopWords;
        this.maxStopWords = maxStopWords;
        this.topWords = PreProcess.getTopWords();
    }

    @Override
    public void calculate(Instance i) {
        System.out.println("calculating " + Arrays.toString(getFeatureNames())
                + " for instance " + i.getAttributeAt(0));
        
        HashSet<String> intersection, s1, s2;
        s1 = i.getSentence1Words();
        s2 = i.getSentence2Words();
        double overlap;

        Iterator<String> it = topWords.iterator();

        for (int k = 0; it.hasNext() && k < minStopWords; k++) {
            String word = it.next();
            s1.remove(word);
            s2.remove(word);
        }
        for (int j = minStopWords; j <= maxStopWords; j++) {
            intersection = new HashSet<>(s1);
            intersection.retainAll(s2);

            if (Math.max(s1.size(), s2.size()) == 0) {
                overlap = 1;
            } else {
                overlap = ((double) intersection.size())
                        / ((double) Math.max(s1.size(), s2.size()));
            }

            i.addAtribute(overlap);

            if (it.hasNext()) {
                String word = it.next();
                s1.remove(word);
                s2.remove(word);
            } else {
                //no more stop words to remove, speed it up:
            }
        }
    }

    @Override
    public String[] getFeatureNames() {
        String r[] = new String[maxStopWords+1-minStopWords];

        for (int i = minStopWords; i <= maxStopWords; i++) {
            r [i] = "lex_overlap_rem_" + i + "sw";
        }

        return r;
    }

}
