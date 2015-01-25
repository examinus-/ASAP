/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.featurecalculation;

import asap.Instance;
import asap.PerformanceCounters;
import java.util.HashMap;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class FeatureCalculatorSentenceLengths implements FeatureCalculator {

    private static final HashMap<Thread, FeatureCalculatorSentenceLengths> fcsls
            = new HashMap<>();
    
    /**
     *
     * @param t
     * @return
     */
    @Override
    public FeatureCalculator getInstance(Thread t) {
        return getFeatureCalculatorSentenceLengths(t);
    }

    /**
     *
     * @param i
     */
    @Override
    public void calculate(Instance i) {
        
        PerformanceCounters.startTimer("calculate FeatureCalculatorSentenceLengths");
        double features[] = {i.getSentence1().length(),i.getSentence2().length(),0d};
        
        features[2] = Math.abs(features[0] - features[1]);
        
        i.addAtribute(features);
        PerformanceCounters.stopTimer("calculate FeatureCalculatorSentenceLengths");
        
    }

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
     * @return
     */
    @Override
    public String[] getFeatureNames() {
        String r[] = {
            "s1_len",
            "s2_len",
            "dif_len"
        };
        
        return r;
    }

    /**
     *
     * @param t
     * @return
     */
    public static FeatureCalculator getFeatureCalculatorSentenceLengths(Thread t) {
        if (fcsls.containsKey(t)) {
            return fcsls.get(t);
        }
        
        FeatureCalculatorSentenceLengths fcsl = new FeatureCalculatorSentenceLengths();
        fcsls.put(t, fcsl);
        return fcsl;
    }
    
    
    @Override
    public String toString() {
        return "FeatureCalculatorSentenceLengths (3 features)";
    }
}
