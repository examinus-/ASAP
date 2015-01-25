/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.featurecalculation;

import asap.Instance;
import asap.PerformanceCounters;
import asap.textprocessing.TextProcessedPartKeyConsts;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO: change text processing part to a textprocessor!

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class LexicalCountWords implements FeatureCalculator, TextProcessedPartKeyConsts {

    private static HashSet<String> fullStopWordSet;
    private static final HashMap<Long, HashMap<String, LexicalCountWords>> perThreadInstances = new HashMap<>();
    HashSet<String> stopWordSet;
    String wordListName;

    /**
     *
     * @param wordListFilename
     * @param wordListName
     */
    public LexicalCountWords(String wordListFilename, String wordListName) {
        this(wordListFilename, wordListName, Thread.currentThread());
    }

    /**
     *
     * @param wordListFilename
     * @param wordListName
     * @param t
     */
    public LexicalCountWords(String wordListFilename, String wordListName, Thread t) {
        
        PerformanceCounters.startTimer("LexicalCountWordsConstructor");
        this.wordListName = wordListName;
        stopWordSet = new HashSet<>();

        FileInputStream fis = null;
        Scanner scanner = null;

        try {
            fis = new FileInputStream(wordListFilename);
            scanner = new Scanner(fis);

            while (scanner.hasNext()) {
                stopWordSet.add(scanner.next());
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
        
        PerformanceCounters.stopTimer("LexicalCountWordsConstructor");
    }

    /**
     *
     * @param stopWordSet
     * @param wordListName
     * @param t
     */
    public LexicalCountWords(HashSet<String> stopWordSet, String wordListName, Thread t) {
        
        PerformanceCounters.startTimer("LexicalCountWordsConstructor");
        this.wordListName = wordListName;
        this.stopWordSet = new HashSet<>();
        this.stopWordSet.addAll(stopWordSet);
        PerformanceCounters.stopTimer("LexicalCountWordsConstructor");
    }

    /**
     *
     * @param words
     * @param wordListName
     */
    public LexicalCountWords(Collection<String> words, String wordListName) {
        PerformanceCounters.startTimer("LexicalCountWordsConstructor");
        this.wordListName = wordListName;
        stopWordSet = new HashSet<>(words);
        PerformanceCounters.stopTimer("LexicalCountWordsConstructor");
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public FeatureCalculator getInstance(Thread t) {
        HashMap<String, LexicalCountWords> aux;
        LexicalCountWords lcw;

        if (perThreadInstances.containsKey(t.getId())) {
            aux = perThreadInstances.get(t.getId());
        } else {
            aux = new HashMap<>();
            perThreadInstances.put(t.getId(), aux);
        }

        if (aux.containsKey(wordListName)) {
            return aux.get(wordListName);
        }
        lcw = new LexicalCountWords(stopWordSet, wordListName, t);
        aux.put(wordListName, lcw);

        return lcw;
    }

    /*
     public static HashSet<String> getMergedWordList() {
     if (fullStopWordSet == null) {
     fullStopWordSet = new HashSet<>();

     for (HashMap<String, LexicalCountWords> perWordListInstances : perThreadInstances.values()) {
     for (LexicalCountWords lexicalCountWordsInstance : perWordListInstances.values()) {
     fullStopWordSet.addAll(lexicalCountWordsInstance.stopWordSet);
     }
     }
     }
        
     return fullStopWordSet;
     }
     */

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
    public void calculate(Instance i) {
        PerformanceCounters.startTimer("calculate LexicalCountWords");
//        System.out.println("calculating " + Arrays.toString(getFeatureNames())
//                + " for instance " + i.getAttributeAt(0));
        double features[] = new double[3];
        StringBuilder sb1 = new StringBuilder(), sb2 = new StringBuilder();
        StringBuilder sb1swr = new StringBuilder(), sb2swr = new StringBuilder();

        Object o = i.getProcessedTextPart(sentence1WithoutStopWords);
        if (o != null) {
            features[0] = countWords((String) o, sb1, sb1swr);

            features[1] = countWords((String) i.getProcessedTextPart(sentence2WithoutStopWords), sb2, sb2swr);

            String[] sb1swra = (String[]) i.getProcessedTextPart(sentence1StopWordsFound);
            String[] sb2swra = (String[]) i.getProcessedTextPart(sentence2StopWordsFound);
            for (String word : sb1swra) {
                sb1swr.append(" ").append(word);
            }
            for (String word : sb2swra) {
                sb2swr.append(" ").append(word);
            }
        } else {
            features[0] = countWords(i.getSentence1(), sb1, sb1swr);

            features[1] = countWords(i.getSentence2(), sb2, sb2swr);
        }

        features[2] = Math.abs(features[0] - features[1]);

        i.addProcessedTextPart(sentence1WithoutStopWords, sb1.toString().trim());
        i.addProcessedTextPart(sentence2WithoutStopWords, sb2.toString().trim());

        i.addProcessedTextPart(sentence1StopWordsFound, sb1swr.toString().trim().split(" "));
        i.addProcessedTextPart(sentence2StopWordsFound, sb2swr.toString().trim().split(" "));

        i.addAtribute(features);
//        System.out.println("Completed adding " + Arrays.toString(getFeatureNames()));
        PerformanceCounters.stopTimer("calculate LexicalCountWords");
    }

    /**
     *
     * @param sentence
     * @param sb
     * @param sbswf
     * @return
     */
    public int countWords(String sentence, StringBuilder sb, StringBuilder sbswf) {
        PerformanceCounters.startTimer("LexicalCountWords countWords()");
        //TODO: tweak this to allow more than one list...
        int stopWords = 0;
        sb.delete(0, sb.length());

        String[] words = sentence.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            if (_isStopWord(word)) {
                stopWords++;
                sbswf.append(" ").append(word);
                continue;
            }
            sb.append(word);
            if (i + 1 < words.length) {
                sb.append(" ");
            }
        }

        PerformanceCounters.stopTimer("LexicalCountWords countWords()");
        return stopWords;
    }

    private boolean _isStopWord(String word) {
        return stopWordSet.contains(word);
    }

    /**
     *
     * @param word
     * @return
     */
    public static boolean isStopWord(String word) {
        return fullStopWordSet.contains(word);
    }

    @Override
    public String toString() {
        return String.format("LexicalCountWords Features(3 features) with %s words (%s).", stopWordSet.size(), wordListName);
    }

    /**
     *
     * @return
     */
    @Override
    public String[] getFeatureNames() {
        String r[] = {
            String.format("lex_count_%s_s1", wordListName),
            String.format("lex_count_%s_s2", wordListName),
            String.format("lex_count_%s_dif", wordListName)
        };

        return r;
    }

}
