/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.featurecalculation;

import asap.Instance;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class ExternalFeatureCalculator implements FeatureCalculator, Serializable {

    private String []featureNames;
    
    /**
     *
     * @param t
     * @return
     */
    @Override
    public FeatureCalculator getInstance(Thread t) {
        return this;
    }

    /**
     *
     * @param i
     */
    @Override
    public void calculate(Instance i) {
        //nothing to do.
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
        return featureNames;
    }

    /**
     *
     * @param instances
     */
    public ExternalFeatureCalculator(List<Instance> instances) {
        this.featureNames = new String[0];
        
        //TODO: prepare external program's input
        //TODO: call whatever external binary to calculate extra features
        //TODO: parse output into extra features
    }

    
    
}
