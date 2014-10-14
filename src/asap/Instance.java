/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

/**
 *  classe auxiliar ao preprocessamento
 * @author exam
 */
public class Instance {

    private final LinkedList<Object> values;
    private final String sentence1;
    private final String sentence2;
    private String sentence1PosTagged;
    private String sentence2PosTagged;
    private String[] sentence1Tokenized;
    private String[] sentence2Tokenized;
    private String[] sentence1PosTags;
    private String[] sentence2PosTags;
    private String[] sentence1Chunks;
    private String[] sentence2Chunks;
    private String[] sentence1ChunkLemmas;
    private String[] sentence2ChunkLemmas;
    
    private HashSet<String> sentence1Words;
    private HashSet<String> sentence2Words;
    
    
    private final double relatedness_groundtruth;

    
    public Instance(String sentence1, String sentence2, int pairId,
            double relatedness_groundtruth, HashSet<String> sentence1Words,
            HashSet<String> sentence2Words) {
        values = new LinkedList<>();
        this.sentence1 = sentence1;
        this.sentence2 = sentence2;
        this.values.add(pairId);
        this.sentence1Words = sentence1Words;
        this.sentence2Words = sentence2Words;
        this.relatedness_groundtruth = relatedness_groundtruth;
    }

    public String getSentence1() {
        return sentence1;
    }

    public String getSentence2() {
        return sentence2;
    }

    public String getSentence1PosTagged() {
        return sentence1PosTagged;
    }

    public String getSentence2PosTagged() {
        return sentence2PosTagged;
    }

    public HashSet<String> getSentence1Words() {
        return sentence1Words;
    }

    public HashSet<String> getSentence2Words() {
        return sentence2Words;
    }

    public String[] getSentence1Tokenized() {
        return sentence1Tokenized;
    }

    public String[] getSentence2Tokenized() {
        return sentence2Tokenized;
    }

    public String[] getSentence1PosTags() {
        return sentence1PosTags;
    }

    public String[] getSentence2PosTags() {
        return sentence2PosTags;
    }

    public String[] getSentence1Chunks() {
        return sentence1Chunks;
    }

    public String[] getSentence2Chunks() {
        return sentence2Chunks;
    }

    public String[] getSentence1ChunkLemmas() {
        return sentence1ChunkLemmas;
    }

    public String[] getSentence2ChunkLemmas() {
        return sentence2ChunkLemmas;
    }

    public Object getAttributeAt(int index) {
        return values.get(index);
    }
    
    public double getRelatedness_groundtruth() {
        return relatedness_groundtruth;
    }   
    
    @Override
    public String toString() {
        String r = "";

        Iterator<Object> it = values.listIterator();
        DecimalFormat df = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.US));
        df.setMaximumFractionDigits(3);
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Double) {
                r += df.format(o);
            } else {
                r += o;
            }
            r += "\t";
        }
        r += relatedness_groundtruth;

        System.out.println("Wrote " + (values.size() + 1) + " values");
        
        return r;
    }

    public String toStringFull() {
        String s1 = sentence1;
        while (s1.length() < 140) {
            s1 += " ";
        }
        String s2 = sentence2;
        while (s2.length() < 140) {
            s2 += " ";
        }
        String ps1 = sentence1PosTagged;
        while (ps1.length() < 140) {
            ps1 += " ";
        }
        String ps2 = sentence2PosTagged;
        while (ps2.length() < 140) {
            ps2 += " ";
        }

        return toString() + "\t" + s1 + "\t" + s2 + "\t" + ps1 + "\t" + ps2;
    }
 
    public String valuesToArffCsvFormat() {
        String r = "";

        Iterator<Object> it = values.listIterator();
        DecimalFormat df = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.US));
        df.setMaximumFractionDigits(3);
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Double) {
                r += df.format(o);
            } else {
                r += o;
            }
            r += ",";
        }
        
        r += relatedness_groundtruth;
        
        System.out.println("Wrote " + (values.size() + 1) + " values");
        System.out.println(r);
        
        return r;
    }

    

    public void setSentence1Words(HashSet<String> sentence1Words) {
        this.sentence1Words = sentence1Words;
    }

    public void setSentence2Words(HashSet<String> sentence2Words) {
        this.sentence2Words = sentence2Words;
    }

    public void setSentence1PosTagged(String sentence1PosTagged) {
        this.sentence1PosTagged = sentence1PosTagged;
    }

    public void setSentence2PosTagged(String sentence2PosTagged) {
        this.sentence2PosTagged = sentence2PosTagged;
    }
    
    public void setSentence1Tokenized(String[] sentence1Tokenized) {
        this.sentence1Tokenized = sentence1Tokenized;
    }

    public void setSentence2Tokenized(String[] sentence2Tokenized) {
        this.sentence2Tokenized = sentence2Tokenized;
    }

    public void setSentence1PosTags(String[] sentence1PosTags) {
        this.sentence1PosTags = sentence1PosTags;
    }

    public void setSentence2PosTags(String[] sentence2PosTags) {
        this.sentence2PosTags = sentence2PosTags;
    }

    public void setSentence1Chunks(String[] sentence1Chunks) {
        this.sentence1Chunks = sentence1Chunks;
    }

    public void setSentence2Chunks(String[] sentence2Chunks) {
        this.sentence2Chunks = sentence2Chunks;
    }

    public void setSentence1ChunkLemmas(String[] sentence1ChunkLemmas) {
        this.sentence1ChunkLemmas = sentence1ChunkLemmas;
    }

    public void setSentence2ChunkLemmas(String[] sentence2ChunkLemmas) {
        this.sentence2ChunkLemmas = sentence2ChunkLemmas;
    }
    
    
    public void addAtribute(Object o) {
        
        if (o instanceof double[]) {
            double[] doubleArray = (double[]) o;
            for (int i = 0; i < doubleArray.length; i++) {
                double value = doubleArray[i];
                values.add(value);
            }
            System.out.println("Added " + doubleArray.length + " attribute to " + getAttributeAt(0));
        } else if (o instanceof int[]) {
            
            int[] intArray = (int[]) o;
            for (int i = 0; i < intArray.length; i++) {
                double value = intArray[i];
                values.add(value);
            }
            System.out.println("Added " + intArray.length + " attribute to " + getAttributeAt(0));
        } else {
            values.add(o);
            System.out.println("Added 1 attribute to " + getAttributeAt(0));
        }
    }
    
}
