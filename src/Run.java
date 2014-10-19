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

        if (args[0].equalsIgnoreCase("benchmark")) {
            runBenchmark(args);
            return;
        }
        
        if (args[0].equalsIgnoreCase("train")) {
            trainAndBuildModels(args);
            return;
        }
        
        if (args[0].equalsIgnoreCase("test")) {
            loadAndTestModels(args);
            return;
        }

        System.out.println("First argument must be either \"benchmark\", \"train\" or \"test\".");
    }

    private static void runBenchmark(String[] args) {
        String preprocessedFilename = args[1] + ".ppout.arff";

        PreProcess pp = new PreProcess();
        Process p = new Process();
        
        
        
        if (args.length < 4) {
            pp.runBenchmark(args[1], preprocessedFilename);

            p.runBenchmark(preprocessedFilename);
        } else
        {
            pp.runBenchmark(args[1], args[2], preprocessedFilename);
            p.runBenchmark(preprocessedFilename);
        }

        LemmasNotFound.finishLog();

        System.out.println("\n\n\n-----------------------------------");
        PerformanceCounters.printStats();
    }

    private static void trainAndBuildModels(String[] args) {
        
        String preprocessedFilename = args[1] + ".ppout.arff";

        PreProcess pp = new PreProcess();
        Process p = new Process();
        
        
        
        if (args.length < 4) {
            pp.preProcessFileWithGoldStandard(args[1], preprocessedFilename);
            p.buildModelsFromFile(preprocessedFilename, args[2]);
        } else
        {
            pp.preProcessFileWithGoldStandard(args[1], args[2], preprocessedFilename);
            p.buildModelsFromFile(preprocessedFilename, args[3]);
        }

        LemmasNotFound.finishLog();

        System.out.println("\n\n\n-----------------------------------");
        PerformanceCounters.printStats();
    }

    private static void loadAndTestModels(String[] args) {
        
        String preprocessedFilename = args[1] + ".ppout.arff";

        PreProcess pp = new PreProcess();
        Process p = new Process();
        
        if (args.length < 4) {
            pp.preProcessFileWithoutGoldStandards(args[1], preprocessedFilename);
            p.loadModelsAndTestFile(preprocessedFilename, args[2]);
        } else
        {
            pp.preProcessFileWithoutGoldStandards(args[1], preprocessedFilename);
            p.loadModelsAndTestFile(preprocessedFilename, args[2], args[3]);
        }

        LemmasNotFound.finishLog();

        System.out.println("\n\n\n-----------------------------------");
        PerformanceCounters.printStats();
    }
}
