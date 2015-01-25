/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.featurecalculation;

import asap.Instance;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt)
 */
public interface FeatureCalculator {

    /**
     *
     * @param t
     * @return
     */
    public FeatureCalculator getInstance(Thread t);

    /**
     *
     * @param i
     */
    public void calculate(Instance i);

    /**
     *
     * @param i
     * @return
     */
    public boolean textProcessingDependenciesMet(Instance i);

    /**
     *
     * @return
     */
    public String[] getFeatureNames();
}
