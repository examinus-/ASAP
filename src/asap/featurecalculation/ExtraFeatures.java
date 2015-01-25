/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.featurecalculation;

import asap.Instance;
import asap.PerformanceCounters;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class ExtraFeatures implements FeatureCalculator {

    private String[] columnNames;
    private FileInputStream fis;
    private Scanner sc;

    /**
     *
     * @param featuresFilename
     * @param instances
     */
    public ExtraFeatures(String featuresFilename, List<Instance> instances) {
        PerformanceCounters.startTimer("ExtraFeaturesConstructor");
        System.out.println(String.format("Adding features from %s", featuresFilename));
        File f = new File(featuresFilename);
        int lineNo = 0;
        try {
            fis = new FileInputStream(f);
            sc = new Scanner(fis);
            String line1 = sc.nextLine();

            columnNames = line1.split("\t");

            Iterator<Instance> it = instances.iterator();
            List<Instance> addedInstances = new LinkedList<>();
            Instance i;
            List<Integer> toRemove = new LinkedList<>();
            List<String> columnNamesList = new LinkedList<>();

            for (int j = 0; j < columnNames.length; j++) {
                String columnName = columnNames[j];
                //TODO: check for repeated features
                if (columnName.equalsIgnoreCase("pair_ID")) {
                    toRemove.add(j);
                    continue;
                }

                columnNamesList.add(columnName);
            }
            columnNames = columnNamesList
                    .toArray(new String[columnNamesList.size()]);

            while (sc.hasNextLine()) {
                String featuresLine = sc.nextLine();
                lineNo++;
                if (it.hasNext()) {
                    i = it.next();
                } else {
                    //TODO: fix this:
                    i = new Instance(featuresFilename, "", "", 0);
                    addedInstances.add(i);
                    System.out.println("Not enough instances!!");
                }
                String[] features = featuresLine.split("\t");

                if (features.length - toRemove.size() < columnNames.length) {
                    throw new IOException("only " + (features.length - toRemove.size()) + " features found in line " + lineNo + ".");
                }

                for (int j = 0; j < features.length; j++) {
                    String feature = features[j];
                    if (toRemove.contains(j)) {
                        continue;
                    }
                    i.addAtribute(Double.parseDouble(feature));
                }
            }
            if (it.hasNext()) {
                //TODO: fix somehow?
                System.out.println("Not all instances had features added!!");
            }

            sc.close();

            for (Instance addedInstance : addedInstances) {
                instances.add(addedInstance);
            }

        } catch (IOException ex) {
            columnNames = new String[0];
            Logger.getLogger(ExtraFeatures.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("\tAdded " + columnNames.length + " features.");
        PerformanceCounters.stopTimer("ExtraFeaturesConstructor");
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
        return columnNames;
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public FeatureCalculator getInstance(Thread t) {
        return this;
    }

    @Override
    public String toString() {
        return "ExtraFeatures (" + (columnNames.length) + " features)";
    }
}
