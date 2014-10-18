/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

/**
 *
 * @author skit
 */
public interface TextProcesser {
    public void process(Instance i);
    public boolean textProcessingDependenciesMet(Instance i);
}
