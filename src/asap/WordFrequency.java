/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

/**
 *
 * @author exam
 */
public class WordFrequency implements Comparable<WordFrequency> {

    private final String word;
    private int frequency;

    public WordFrequency(String word) {
        this.word = word;
        this.frequency = 1;
    }

    public int getFrequency() {
        return frequency;
    }

    public void incFrequency() {
        this.frequency++;
    }

    @Override
    public int compareTo(WordFrequency o) {
        return Integer.compare(frequency, o.frequency);
    }

    public String getWord() {
        return word;
    }

}
