/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.textprocessing;

import asap.Chunk;
import asap.LemmasInDBPedia;
import asap.PerformanceCounters;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class TextProcessChunkLemmasWithDBPediaLookups extends TextProcessChunkLemmas {

    /**
     *
     */
    protected static final String DEFAULT_LOOKUPS_CACHE_FILENAME = "DBPediaLookups.cache";

    //lets avoid spamming the interwebz:
    private Map<Chunk, String> lemmas;
    private static final String DBPEDIA_LOOKUP_URL_PREFIX = "http://dbpedia.org/resource/";
    private static final String PAGE_LOOKUP_STRING = "rel=\"dbpprop:wordnet_type nofollow\" href=\"http://www.w3.org/2006/03/wn/wn20/instances/synset-";

    /**
     *
     * @param t
     * @return
     */
    @Override
    public TextProcesser getInstance(Thread t) {
        return getTextProcessChunkLemmas(t);
    }

    /**
     *
     * @return
     */
    public static TextProcessChunkLemmasWithDBPediaLookups getTextProcessChunkLemmas() {
        return getTextProcessChunkLemmas(Thread.currentThread());
    }

    /**
     *
     * @param t
     * @return
     */
    public static TextProcessChunkLemmasWithDBPediaLookups getTextProcessChunkLemmas(Thread t) {
        TextProcessChunkLemmasWithDBPediaLookups r;
        if (tpcls.containsKey(t.getId())) {
            return (TextProcessChunkLemmasWithDBPediaLookups) tpcls.get(t.getId());
        }
        if (new File(DEFAULT_LOOKUPS_CACHE_FILENAME).exists()) {
            r = new TextProcessChunkLemmasWithDBPediaLookups(t,
                    DEFAULT_LOOKUPS_CACHE_FILENAME);
        } else {
            r = new TextProcessChunkLemmasWithDBPediaLookups(t);
        }
        tpcls.put(t.getId(), r);
        return r;
    }

    /**
     *
     */
    protected TextProcessChunkLemmasWithDBPediaLookups() {
        this(Thread.currentThread());
    }

    /**
     *
     * @param savedLookups
     */
    protected TextProcessChunkLemmasWithDBPediaLookups(String savedLookups) {
        this(Thread.currentThread(), savedLookups);
    }

    /**
     *
     * @param t
     * @param savedLookups
     */
    protected TextProcessChunkLemmasWithDBPediaLookups(Thread t, String savedLookups) {
        this(t);
        if (lemmas.isEmpty()) {
            try (FileInputStream fis = new FileInputStream(savedLookups)) {
                Scanner sc = new Scanner(fis);

                while (sc.hasNextLine()) {
                    String line = sc.nextLine();

                    if (line.startsWith("#")) {
                        continue;
                    }

                    String[] lineElements = line.split(":");
                    if (lineElements.length != 3) {
                        continue;
                    }

                    lemmas.put(new Chunk(lineElements[1], lineElements[0]), lineElements[2]);
                }

            } catch (IOException ex) {
                Logger.getLogger(TextProcessChunkLemmasWithDBPediaLookups.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        java.lang.System.out.println("Loaded TextProcessChunkLemmasWithDBPediaLookups with " + lemmas.size() + " cached lookups.");
    }

    /**
     *
     * @param t
     */
    protected TextProcessChunkLemmasWithDBPediaLookups(Thread t) {
        super(t);
        if (lemmas == null) {
            lemmas = Collections.synchronizedMap(new HashMap<Chunk, String>());
        }
    }

    /**
     *
     * @param chunk
     * @return
     */
    @Override
    protected String lemmatizeChunk(Chunk chunk) {
        String lemma = super.lemmatizeChunk(chunk);
        if (lemma == null && chunk.getChunkType().equalsIgnoreCase("NP")) {
            lemma = dbPediaLookup(chunk);
        }
        return lemma;
    }

    /**
     *
     * @param chunk
     * @return
     */
    protected String dbPediaLookup(Chunk chunk) {
        PerformanceCounters.startTimer("dbPediaLookup");
        //avoid spamming the interwebz
        LemmasInDBPedia.incTotal();

        if (lemmas.containsKey(chunk)) {
//            Logger.getLogger(TextProcessChunkLemmasWithDBPediaLookups.class.getName())
//                    .log(Level.INFO, "found in HashMap...");
            chunk.setLemma(lemmas.get(chunk));
            if (chunk.getLemma() != null) {
                LemmasInDBPedia.incFound();
            }
            PerformanceCounters.stopTimer("dbPediaLookup");
            return chunk.getLemma();
        }

        String chunkText = chunk.getChunkText();
        String[] wnTypes = getWordNetTypes(chunkText);
        String lemma = null;
        while (wnTypes.length == 0) {
            wnTypes = getWordNetTypes(chunkText.toLowerCase());
            if (wnTypes.length > 0) {
                break;
            }

            int indexOfSpace = chunkText.indexOf(" ");
            if (indexOfSpace < 0) {
                break;
            }
            chunkText = chunkText.substring(indexOfSpace + 1);
//            Logger.getLogger("Lemmatizer").log(Level.INFO, "\tgetIndexWord({0})", lemma);
            wnTypes = getWordNetTypes(chunkText);
        }

        if (wnTypes.length > 0) {
//            Logger.getLogger(TextProcessChunkLemmasWithDBPediaLookups.class.getName())
//                    .log(Level.INFO, "found in DBPedia...");
            lemma = wnTypes[0];
            LemmasInDBPedia.incFound();
            LemmasInDBPedia.log(chunk.getChunkText(), lemma);
        }

        lemmas.put(chunk, lemma);
        chunk.setLemma(lemma);
        PerformanceCounters.stopTimer("dbPediaLookup");
        return lemma;
    }

    private String getPage(String query) {
        //TODO: don't rely on the exception::
        String content = null;
        URLConnection connection;
        try {
            connection = new URL(DBPEDIA_LOOKUP_URL_PREFIX + query).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            content = scanner.next();
        } catch (IOException ex) {
            //Logger.getLogger(TextProcessChunkLemmasWithDBPediaLookups.class.getName()).log(Level.SEVERE, null, ex);
        }
        return content;
    }

    private String[] getWordNetTypes(String text) {
        PerformanceCounters.startTimer("getWordNetTypes");
        String pageContent = getPage(text);
        if (pageContent == null) {
        PerformanceCounters.stopTimer("getWordNetTypes");
            return new String[0];
        }

        int startIndex = pageContent.indexOf(
                PAGE_LOOKUP_STRING
        ) + PAGE_LOOKUP_STRING
                .length();

        LinkedList<String> types = new LinkedList<>();

        while (startIndex != PAGE_LOOKUP_STRING
                .length() - 1) {
            int stopIndex = pageContent.indexOf("-", startIndex);

            types.add(pageContent.substring(startIndex, stopIndex));

            startIndex = pageContent.indexOf(
                    PAGE_LOOKUP_STRING, stopIndex) + PAGE_LOOKUP_STRING
                    .length();
        }

        PerformanceCounters.stopTimer("getWordNetTypes");
        return types.toArray(new String[types.size()]);
    }

    /**
     *
     */
    public static void saveLookups() {
        StringBuilder sb = new StringBuilder();
        for (TextProcessChunkLemmas textProcessChunkLemmas : tpcls.values()) {
            TextProcessChunkLemmasWithDBPediaLookups tpclwdbpl
                    = (TextProcessChunkLemmasWithDBPediaLookups) textProcessChunkLemmas;

            if (tpclwdbpl.lemmas == null) {
                return;
            }

            for (Chunk lemmaChunk : tpclwdbpl.lemmas.keySet()) {
                String lemma = tpclwdbpl.lemmas.get(lemmaChunk);

                sb.append(lemmaChunk.getChunkText())
                        .append(":")
                        .append(lemmaChunk.getChunkType())
                        .append(":")
                        .append(lemma)
                        .append("\n");
            }
        }

        try (FileOutputStream fos = new FileOutputStream(DEFAULT_LOOKUPS_CACHE_FILENAME)) {
            fos.write(sb.toString().getBytes());
        } catch (IOException ex) {
            Logger.getLogger(TextProcessChunkLemmasWithDBPediaLookups.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
