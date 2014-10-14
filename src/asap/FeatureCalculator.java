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
public interface FeatureCalculator {
    public void calculate(Instance i);
    
    public String[] getFeatureNames();
}
