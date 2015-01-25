/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt)
 */
public class WordFrequency implements Comparable<WordFrequency> {

    private final String word;
    private int frequency;

    /**
     *
     * @param word
     */
    public WordFrequency(String word) {
        this.word = word;
        this.frequency = 1;
    }

    /**
     *
     * @return
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     *
     */
    public void incFrequency() {
        this.frequency++;
    }

    @Override
    public int compareTo(WordFrequency o) {
        return Integer.compare(frequency, o.frequency);
    }

    /**
     *
     * @return
     */
    public String getWord() {
        return word;
    }

}
