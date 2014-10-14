/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LexicalCountWords implements FeatureCalculator {

    HashSet<String> wordSet;
    String wordListName;

    /**
     *
     * @param wordListFilename
     * @param wordListName
     */
    public LexicalCountWords(String wordListFilename, String wordListName) {
        this.wordListName = wordListName;
        wordSet = new HashSet<>();

        FileInputStream fis = null;
        Scanner scanner = null;

        try {
            fis = new FileInputStream(wordListFilename);
            scanner = new Scanner(fis);

            while (scanner.hasNext()) {
                wordSet.add(scanner.next());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LexicalCountWords.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(LexicalCountWords.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public LexicalCountWords(Collection<String> words, String wordListName) {
        this.wordListName = wordListName;
        wordSet = new HashSet<>(words);
    }

    @Override
    public void calculate(Instance i) {
        System.out.println("calculating " + Arrays.toString(getFeatureNames())
                + " for instance " + i.getAttributeAt(0));
        double features[] = {countWords(i.getSentence1()),
            countWords(i.getSentence2()),
            0};

        features[2] = Math.abs(features[0] - features[1]);

        i.addAtribute(features);
    }

    private int countWords(String sentence) {
        int negativeStopWords = 0;

        for (String word : sentence.split(" ")) {
            if (isNegativeWord(word)) {
                negativeStopWords++;
            }
        }

        return negativeStopWords;
    }

    private boolean isNegativeWord(String word) {
        return wordSet.contains(word);
    }

    @Override
    public String toString() {
        return "LexicalCountWords Features(3#) with " + wordSet.size() + " words.";
    }

    @Override
    public String[] getFeatureNames() {
        String r[] = {"lex_count_" + wordListName + "_s1",
            "lex_count_" + wordListName + "_s2",
            "lex_count_" + wordListName + "_dif"
        };

        return r;
    }

}
