/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class LemmasInDBPedia {

    private static final LemmasInDBPedia logger = new LemmasInDBPedia();

    private File logFile;
    private FileOutputStream fos;

    private int totalLookedUpLemmas = 0;
    private int lemmasFound = 0;

    private HashMap<String, String> lookupResults;

    /**
     *
     */
    public static void incTotal() {
        logger.totalLookedUpLemmas++;
    }

    /**
     *
     */
    public static void incFound() {
        logger.lemmasFound++;
    }

    private LemmasInDBPedia() {
        lookupResults = new HashMap<>();

        logFile = new File(Config.getLogLemmasInDBPediaOutputFilename());
        logFile.getParentFile().mkdirs();
        try {
            fos = new FileOutputStream(logFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LemmasNotFound.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void _log(String lookup, String result) {
        lookupResults.put(lookup, result);
    }

    /**
     *
     * @param lookup
     * @param result
     */
    public static void log(String lookup, String result) {
        logger._log(lookup, result);
    }

    /**
     *
     */
    public static void finishLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("Lemmas found in DBPedia: ")
                .append(logger.lemmasFound)
                .append(" / ")
                .append(logger.totalLookedUpLemmas)
                .append("\n\n");

        for (String lookup : logger.lookupResults.keySet()) {
            String result = logger.lookupResults.get(lookup);

            sb.append(String.format("%s\t->\t%s\n", lookup, result));
        }

        try {
            logger.fos.write(sb.toString().getBytes());
            logger.fos.flush();
            logger.fos.close();
        } catch (IOException ex) {
            Logger.getLogger(LemmasNotFound.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
