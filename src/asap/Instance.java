/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import asap.textprocessing.TextProcessedPartKeyConsts;
import asap.textprocessing.TextProcesser;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * classe auxiliar ao preprocessamento
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt)
 */
public class Instance implements Comparable<Instance> {

    private final LinkedList<Object> values;
    private final String sentence1;
    private final String sentence2;

    private final HashMap<String, Object> processedText;
    private final int pair_ID;
    private final String sourceFile;
    private double goldStandard;
    private final LinkedList<TextProcesser> processed;

    /**
     *
     * @param sourceFile
     * @param sentence1
     * @param sentence2
     * @param pairId
     */
    public Instance(String sourceFile, String sentence1, String sentence2, int pairId) {
        values = new LinkedList<>();
        this.sentence1 = sentence1;
        this.sentence2 = sentence2;
        this.values.add(pairId);
        this.pair_ID = pairId;
        this.processed = new LinkedList<>();
        this.processedText = new HashMap<>();
        this.sourceFile = sourceFile;
        this.goldStandard = -1;
    }

    /**
     *
     * @param sourceFile
     * @param sentence1
     * @param sentence2
     * @param pairId
     * @param goldStandard
     */
    public Instance(String sourceFile, String sentence1, String sentence2, int pairId,
            double goldStandard) {
        this(sourceFile, sentence1, sentence2, pairId);
        this.goldStandard = goldStandard;
    }

    /**
     *
     * @return
     */
    public int getPair_ID() {
        return pair_ID;
    }

    /**
     *
     * @return
     */
    public String getSentence1() {
        return sentence1;
    }

    /**
     *
     * @return
     */
    public String getSentence2() {
        return sentence2;
    }

    /**
     *
     * @param objectKey
     * @return
     */
    public synchronized Object getProcessedTextPart(String objectKey) {
        if (!processedText.containsKey(objectKey)) {
            return null;
        }
        return processedText.get(objectKey);
    }

    /**
     *
     * @param index
     * @return
     */
    public synchronized Object getAttributeAt(int index) {
        return values.get(index);
    }

    /**
     *
     * @return
     */
    public double getGoldStandard() {
        return goldStandard;
    }

    /**
     *
     * @param textProcesser
     * @return
     */
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

    /**
     *
     * @return
     */
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

    /**
     *
     * @return
     */
    public String valuesToArffCsvFormat() {
        String r = "";

        Iterator<Object> it = values.listIterator();
        DecimalFormat df = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.US));
        df.setMaximumFractionDigits(3);

        r += sourceFile;
        
        Object o;
        while (it.hasNext()) {
            o = it.next();
            r += ",";
            if (o instanceof Double) {
                r += df.format(o);
            } else {
                r += o;
            }
        }
        r += ",";
            
        if (hasGoldStandard()) {
            r += goldStandard;
        }else
        {
            r += "0";
        }

//        System.out.println("Wrote " + (values.size() + 1) + " values");
//        System.out.println(r);
        return r;
    }

    /**
     *
     * @return
     */
    public synchronized String getPreprocessedText() {
        StringBuilder sb = new StringBuilder();
        Object o;

        o = getProcessedTextPart(TextProcessedPartKeyConsts.sentence1StopWordsFound);
        if (!(o instanceof String[])) {
            return null;
        }
        String[] stopWords = (String[]) o;

        o = getProcessedTextPart(TextProcessedPartKeyConsts.sentence1Chunks);
        if (!(o instanceof Chunk[])) {
            return null;
        }
        Chunk[] chunks = (Chunk[]) o;
        for (Chunk chunk : chunks) {
            String lemma = chunk.getLemma();
            if (lemma != null) {
                if (!lemma.isEmpty()) {
                    boolean isStopWord = false;
                    for (String stopWord : stopWords) {
                        if (lemma.equalsIgnoreCase(stopWord)) {
                            isStopWord = true;
                            break;
                        }
                    }
                    if (!isStopWord) {
                        sb.append(" ").append(lemma);
                    }
                }
            }
        }

        sb.append("\t");

        o = getProcessedTextPart(TextProcessedPartKeyConsts.sentence2StopWordsFound);
        if (!(o instanceof String[])) {
            return null;
        }
        stopWords = (String[]) o;

        o = getProcessedTextPart(TextProcessedPartKeyConsts.sentence2Chunks);
        if (!(o instanceof Chunk[])) {
            return null;
        }

        chunks = (Chunk[]) o;
        for (Chunk chunk : chunks) {
            String lemma = chunk.getLemma();
            if (lemma != null) {
                if (!lemma.isEmpty()) {
                    boolean isStopWord = false;
                    for (String stopWord : stopWords) {
                        if (lemma.equalsIgnoreCase(stopWord)) {
                            isStopWord = true;
                            break;
                        }
                    }
                    if (!isStopWord) {
                        sb.append(" ").append(lemma);
                    }
                }
            }
        }

        return sb.toString();
    }

    /**
     *
     * @param key
     * @param processedTextPart
     */
    public synchronized void addProcessedTextPart(String key, Object processedTextPart) {
        processedText.put(key, processedTextPart);
    }

    /**
     *
     * @param textProcesser
     */
    public synchronized void addProcessed(TextProcesser textProcesser) {
        this.processed.add(textProcesser);
    }

    /**
     *
     * @param o
     */
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

    /**
     *
     * @return
     */
    public boolean hasGoldStandard() {
        return goldStandard != -1;
    }

    @Override
    public int compareTo(Instance o) {
        return pair_ID - o.pair_ID;
    }

    /**
     *
     * @return
     */
    public Set<Map.Entry<String, Object>> getProcessedTextParts() {
        return Collections.unmodifiableSet(processedText.entrySet());
    }

}
