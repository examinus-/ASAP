/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import asap.LemmasNotFound;
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
            System.out.println("You need to provide [normal,test], an input and an output file.");
            return;
        }
        if (args.length < 2) {
            System.out.println("You still need to provide an input and an output file.");
            return;
        }
        if (args.length < 3) {
            System.out.println("You need to provide an output file aswell.");
            return;
        }

        if (args[0].equalsIgnoreCase("test")) {
            runTests(args);
        }

        preprocessedFilename = args[1] + ".ppout.arff";

        PreProcess pp = new PreProcess();
        Process p = new Process();
        
        
        
        if (args.length < 4) {
            pp.preProcessFile(args[1], preprocessedFilename);
            p.processFile(preprocessedFilename, args[2]);
        } else
        {
            pp.preProcessFile(args[1], args[2], preprocessedFilename);
            p.processFile(preprocessedFilename, args[3]);
        }

        LemmasNotFound.finishLog();

        System.out.println("\n\n\n-----------------------------------");
        PerformanceCounters.printStats();
    }

    private static void runTests(String[] args) {
        String preprocessedFilename = args[1] + ".ppout.arff";

        PreProcess pp = new PreProcess();
        Process p = new Process();
        
        
        
        if (args.length < 4) {
            pp.runTests(args[1], preprocessedFilename);

            p.runTests(preprocessedFilename);
        } else
        {
            pp.runTests(args[1], args[2], preprocessedFilename);
            p.runTests(preprocessedFilename);
        }

        LemmasNotFound.finishLog();

        System.out.println("\n\n\n-----------------------------------");
        PerformanceCounters.printStats();
    }
}
