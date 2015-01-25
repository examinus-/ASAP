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
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David Simões
 */
public class NamedEntitiesFound {

    private static final NamedEntitiesFound logger = new NamedEntitiesFound();

    private File logFile;
    private FileOutputStream fos;

    private int totalEntitiesFound = 0;

    private HashSet<NamedEntity> loggedNamedEntities;

    private NamedEntitiesFound() {
        loggedNamedEntities = new HashSet<>();

        logFile = new File(Config.getLogNamedEntitiesFoundOutputFilename());
        logFile.getParentFile().mkdirs();
        try {
            fos = new FileOutputStream(logFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NamedEntitiesFound.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void _log(NamedEntity namedEntity, String sentence, int instanceId) {

        if (!loggedNamedEntities.contains(namedEntity)) {
            loggedNamedEntities.add(namedEntity);
        }else
        {
            //loggedNamedEntities.get.addsentence
        }
        totalEntitiesFound++;
    }

    /**
     *
     * @param namedEntity
     * @param sentence
     * @param instanceId
     */
    public synchronized static void log(NamedEntity namedEntity, String sentence, int instanceId) {
        logger._log(namedEntity, sentence, instanceId);
    }

    /**
     *
     * @param namedEntity
     */
    public synchronized static void incTotal(String namedEntity) {
        logger.totalEntitiesFound++;
    }

    /**
     *
     */
    public static void finishLog() {
        String tmp = "Named entities found: " + logger.totalEntitiesFound
                + "\n\n";

        try {
            logger.fos.write(tmp.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(NamedEntitiesFound.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (NamedEntity loggedNamedEntity : logger.loggedNamedEntities) {
            String out = loggedNamedEntity.toLog();
            try {
                logger.fos.write(out.getBytes());
            } catch (IOException ex) {
                Logger.getLogger(NamedEntitiesFound.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            logger.fos.flush();
            logger.fos.close();
        } catch (IOException ex) {
            Logger.getLogger(NamedEntitiesFound.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
