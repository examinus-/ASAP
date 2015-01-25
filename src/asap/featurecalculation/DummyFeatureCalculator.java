/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.featurecalculation;

import asap.Instance;
import asap.textprocessing.TextProcesser;
import java.util.HashMap;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class DummyFeatureCalculator implements FeatureCalculator {

    private final TextProcesser textProcesserDependency;
    private static final HashMap<Long, HashMap<String, DummyFeatureCalculator>> dummyFeatureCalculators = new HashMap();

    /**
     *
     * @param t
     * @return
     */
    @Override
    public FeatureCalculator getInstance(Thread t) {
        HashMap<String, DummyFeatureCalculator> threadDummyFeatureCalculators;

        if (dummyFeatureCalculators.containsKey(t.getId())) {
            threadDummyFeatureCalculators = dummyFeatureCalculators.get(t.getId());

            if (threadDummyFeatureCalculators.containsKey(textProcesserDependency.getClass().getCanonicalName())) {
                return threadDummyFeatureCalculators.get(textProcesserDependency.getClass().getCanonicalName());
            }
        } else {
            threadDummyFeatureCalculators = new HashMap<>();
            dummyFeatureCalculators.put(t.getId(), threadDummyFeatureCalculators);
        }

        DummyFeatureCalculator dfc = new DummyFeatureCalculator(textProcesserDependency.getInstance(t));

        threadDummyFeatureCalculators.put(textProcesserDependency.getClass().getCanonicalName(), dfc);

        return dfc;
    }

    /**
     *
     * @param processorDependency
     */
    public DummyFeatureCalculator(TextProcesser processorDependency) {
        textProcesserDependency = processorDependency;
    }

    /**
     *
     * @param i
     */
    @Override
    public void calculate(Instance i) {
        if (!textProcessingDependenciesMet(i)) {
            textProcesserDependency.process(i);
        }
    }

    /**
     *
     * @param i
     * @return
     */
    @Override
    public boolean textProcessingDependenciesMet(Instance i) {
        return i.isProcessed(textProcesserDependency);
    }

    /**
     *
     * @return
     */
    @Override
    public String[] getFeatureNames() {
        return new String[0];
    }

    @Override
    public String toString() {
        return "DummyFeatureCalculator (0 features)";
    }

}
