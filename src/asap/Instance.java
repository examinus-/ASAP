/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import asap.textprocessing.TextProcesser;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

/**
 * classe auxiliar ao preprocessamento
 *
 * @author exam
 */
public class Instance {

    private final LinkedList<Object> values;
    private final String sentence1;
    private final String sentence2;
//    private String sentence1PosTagged;
//    private String sentence2PosTagged;
//    private String[] sentence1Tokenized;
//    private String[] sentence2Tokenized;
//    private String[] sentence1PosTags;
//    private String[] sentence2PosTags;
//    private String[] sentence1Chunks;
//    private String[] sentence2Chunks;
//    private String[] sentence1ChunkLemmas;
//    private String[] sentence2ChunkLemmas;
//    
//    private HashSet<String> sentence1Words;
//    private HashSet<String> sentence2Words;

    private final HashMap<String, Object> processedText;
    private final int pair_ID;
    private double goldStandard;
    private final LinkedList<TextProcesser> processed;

    public Instance(String sentence1, String sentence2, int pairId) {
        values = new LinkedList<>();
        this.sentence1 = sentence1;
        this.sentence2 = sentence2;
        this.values.add(pairId);
        this.pair_ID = pairId;
        this.processed = new LinkedList<>();
        this.processedText = new HashMap<>();

        this.goldStandard = -1;
    }

    public Instance(String sentence1, String sentence2, int pairId,
            double relatedness_groundtruth) {
        this(sentence1, sentence2, pairId);
        this.goldStandard = relatedness_groundtruth;
    }

    public String getSentence1() {
        return sentence1;
    }

    public String getSentence2() {
        return sentence2;
    }

    public synchronized Object getProcessedTextPart(String objectKey) {
        if (!processedText.containsKey(objectKey)) {
            return null;
        }
        return processedText.get(objectKey);
    }

    public synchronized Object getAttributeAt(int index) {
        return values.get(index);
    }

    public double getRelatedness_groundtruth() {
        return goldStandard;
    }

    public synchronized boolean isProcessed(TextProcesser textProcesser) {
        if (textProcesser == null) {
            return true;
        }
        Iterator<TextProcesser> it = processed.iterator();
        while (it.hasNext()) {
            //if (it.next().getClass().isInstance(textProcesser)) {
            if (textProcesser.getClass().isInstance(it.next())) {
                return true;
            }
        }
        return false;
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
        r += goldStandard;

//        System.out.println("Wrote " + (values.size() + 1) + " values");
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

        return toString() + "\t" + s1 + "\t" + s2;
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
            if (it.hasNext() || hasGoldStandard()) {
                r += ",";
            }
        }
        
        if (hasGoldStandard()) {
            r += goldStandard;
        }

//        System.out.println("Wrote " + (values.size() + 1) + " values");
//        System.out.println(r);
        return r;
    }

    public synchronized void addProcessedTextPart(String key, Object processedTextPart) {
        processedText.put(key, processedTextPart);
    }

    public synchronized void addProcessed(TextProcesser textProcesser) {
        this.processed.add(textProcesser);
    }

    public synchronized void addAtribute(Object o) {

        if (o instanceof double[]) {
            double[] doubleArray = (double[]) o;
            for (int i = 0; i < doubleArray.length; i++) {
                double value = doubleArray[i];
                values.add(value);
            }
//            System.out.println("Added " + doubleArray.length + " attribute to " + getAttributeAt(0));
        } else if (o instanceof int[]) {

            int[] intArray = (int[]) o;
            for (int i = 0; i < intArray.length; i++) {
                double value = intArray[i];
                values.add(value);
            }
//            System.out.println("Added " + intArray.length + " attribute to " + getAttributeAt(0));
        } else {
            values.add(o);
//            System.out.println("Added 1 attribute to " + getAttributeAt(0));
        }
    }

    public boolean hasGoldStandard() {
        return goldStandard != -1;
    }

}
