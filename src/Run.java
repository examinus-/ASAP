/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import asap.PerformanceCounters;
import asap.PreProcess;
import asap.Process;
import java.util.Arrays;

/**
 * classe auxiliar que representa o pipeline completo do programa.
 *
 * @author exam
 */
public class Run {

    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        String preprocessedFilename;
        if (args.length < 1) {
            System.out.println("You need to provide an input and an output file.");
        }
        if (args.length < 2) {
            System.out.println("You need to provide an output file aswell.");
        }

        preprocessedFilename = args[0] + ".ppout.arff";

        PreProcess pp = new PreProcess();
        pp.preProcessFile(args[0], preprocessedFilename);

        Process p = new Process();

        if (args.length < 3) {
            p.processFile(preprocessedFilename, args[1]);
        } else if (args[2].equalsIgnoreCase("test")) {
            p.runTests(preprocessedFilename);
        }

        System.out.println("\n\n\n-----------------------------------");
        PerformanceCounters.printStats();
    }
}
