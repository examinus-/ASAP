/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class PerlCorrelation {

    /**
     *
     * @param goldStandardFile
     * @param predictionsFile
     * @return
     */
    public static double getCorrelation(String goldStandardFile, String predictionsFile) {
        PerformanceCounters.startTimer("PerlPearsonsCorrelation");
        myjava.util.function.Process perlProcess;
        String[] params = {"correlation-noconfidence.pl", goldStandardFile, predictionsFile};
        perlProcess = new myjava.util.function.Process("perl", params);
        perlProcess.run();
        String out = perlProcess.getSdtoutOutput();
        int aux = out.indexOf("Pearson: ");
        if (aux == -1) {
            throw new Error("Pearson correlation could not be determined!\n\t" + out);
        }
        out = out.substring(aux + "Pearson: ".length());
        double r;
        try {
            r = Double.parseDouble(out);
        } catch (NumberFormatException ex) {
            Logger.getLogger(PerlCorrelation.class.getName()).log(Level.SEVERE, null, ex);
            return -1d;
        }
        PerformanceCounters.stopTimer("PerlPearsonsCorrelation");
        return r;
    }

}
