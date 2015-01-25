/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.textprocessing;

import asap.Instance;

/**
 * A text processor represents the methods that transform text into information
 * rich string-like objects. Feature extraction techniques can require one of
 * these processors. Some TextProcessers require others to run first
 * (ex:POSTagger needs Tokenizer)
 *
 * @author David Simões
 */
public interface TextProcesser {

    /**
     *  This is the function called by any requiring feature extractor/calculator
     * @param i
     */
    public void process(Instance i);

    /**
     * Checks if we have the data required to process
     * @param i
     * @return
     */
    public boolean textProcessingDependenciesMet(Instance i);

    /**
     * Gets the unique instance for the given Thread instance.
     * @param t
     * @return
     */
    public TextProcesser getInstance(Thread t);
}
