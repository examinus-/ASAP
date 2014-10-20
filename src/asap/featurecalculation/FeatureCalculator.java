/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.featurecalculation;

import asap.Instance;

/**
 *
 * @author exam
 */
public interface FeatureCalculator {
    public void calculate(Instance i);
    public boolean textProcessingDependenciesMet(Instance i);
    
    public String[] getFeatureNames();
}
