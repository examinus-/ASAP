/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.textprocessing;

import asap.Chunk;
import asap.Config;
import asap.Instance;
import asap.LemmasNotFound;
import java.io.Serializable;
import asap.PerformanceCounters;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.pobj.Synset;
import edu.cmu.lti.jawjaw.pobj.Word;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.morph.WordnetStemmer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class TextProcessChunkLemmas implements TextProcesser, TextProcessedPartKeyConsts, Serializable {

    /**
     *
     */
    protected static final HashMap<Long, TextProcessChunkLemmas> tpcls
            = new HashMap<>();

    private final TextProcesser textProcesserDependency;

    //use jwi only for "lematisation"
    private edu.mit.jwi.IDictionary dict;
    private edu.mit.jwi.morph.WordnetStemmer stemmer;

    /**
     *
     * @param t
     * @return
     */
    public static TextProcessChunkLemmas getTextProcessChunkLemmas(Thread t) {
        TextProcessChunkLemmas r;
        if (tpcls.containsKey(t.getId())) {
            return tpcls.get(t.getId());
        }
        r = new TextProcessChunkLemmas(t);
        tpcls.put(t.getId(), r);
        return r;
    }

    /**
     *
     * @return
     */
    public static TextProcessChunkLemmas getTextProcessChunkLemmas() {
        return getTextProcessChunkLemmas(Thread.currentThread());
    }

    /**
     *
     * @param wordnetDatabasePath
     * @param t
     */
    protected TextProcessChunkLemmas(String wordnetDatabasePath, Thread t) {

        loadJWI(wordnetDatabasePath);

        textProcesserDependency = TextProcessChunks.getTextProcessChunks(t);
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public TextProcesser getInstance(Thread t) {
        return getTextProcessChunkLemmas(t);
    }

    private void loadJWI(String wordnetDatabasePath) {
        String path = wordnetDatabasePath;
        URL url;
        try {
            url = new URL("file", null, path);
        } catch (MalformedURLException ex) {
            Logger.getLogger(TextProcessChunkLemmas.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        dict = new RAMDictionary(url, ILoadPolicy.NO_LOAD);
        try {
            dict.open();
        } catch (IOException ex) {
            Logger.getLogger(TextProcessChunkLemmas.class.getName()).log(Level.SEVERE, null, ex);
        }
        stemmer = new WordnetStemmer(dict);
        java.lang.System.out.println("Loaded JWI Dictionary.");
    }

    /**
     *
     * @param t
     */
    protected TextProcessChunkLemmas(Thread t) {
        this(System.getenv("WNSEARCHDIR"), t);
    }

    private TextProcessChunkLemmas() {
        this(Thread.currentThread());
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
     * @param i
     */
    @Override
    public void process(Instance i) {
        if (!textProcessingDependenciesMet(i)) {
            textProcesserDependency.process(i);
        }
        PerformanceCounters.startTimer("process ChunkLemmas");

        Object o;
        Chunk[] chunks;
        String[] lemmas;
        int j;

        if (Config.calculateWithoutCaseSensitivity()) {
            o = i.getProcessedTextPart(sentence1Chunks);
            if (!(o instanceof Chunk[])) {
                return;
            }
            chunks = (Chunk[]) o;
            lemmas = new String[chunks.length];
            j = 0;
            for (Chunk chunk : chunks) {
                String lemma = lemmatizeChunk(chunk);
                if (lemma == null) {
                    LemmasNotFound.log(chunk, i.getSentence1().toLowerCase(), i.getPair_ID());
                    chunk.setLemma(null);
                    continue;
                }
                lemma = lemma + chunk.toWNPos();
                chunk.setLemma(lemma);
                lemmas[j++] = lemma;
            }
            i.addProcessedTextPart(sentence1ChunkLemmas, lemmas);

            o = i.getProcessedTextPart(sentence2Chunks);
            if (!(o instanceof Chunk[])) {
                return;
            }
            chunks = (Chunk[]) o;
            lemmas = new String[chunks.length];
            j = 0;
            for (Chunk chunk : chunks) {
                String lemma = lemmatizeChunk(chunk);
                if (lemma == null) {
                    LemmasNotFound.log(chunk, i.getSentence2().toLowerCase(), i.getPair_ID());
                    chunk.setLemma(null);
                    continue;
                }
                lemma = lemma + chunk.toWNPos();
                chunk.setLemma(lemma);
                lemmas[j++] = lemma;
            }
            i.addProcessedTextPart(sentence2ChunkLemmas, lemmas);
        }
        
        
        
        
        if (Config.calculateWithCaseSensitivity()) {
            o = i.getProcessedTextPart(sentence1ChunksCaseSensitive);
            if (!(o instanceof Chunk[])) {
                return;
            }
            chunks = (Chunk[]) o;
            lemmas = new String[chunks.length];
            j = 0;
            for (Chunk chunk : chunks) {
                String lemma = lemmatizeChunk(chunk);
                if (lemma == null) {
                    LemmasNotFound.log(chunk, i.getSentence1(), i.getPair_ID());
                    chunk.setLemma(null);
                    continue;
                }
                lemma = lemma + chunk.toWNPos();
                chunk.setLemma(lemma);
                lemmas[j++] = lemma;
            }
            i.addProcessedTextPart(sentence1ChunkLemmasCaseSensitive, lemmas);

            o = i.getProcessedTextPart(sentence2ChunksCaseSensitive);
            if (!(o instanceof Chunk[])) {
                return;
            }
            chunks = (Chunk[]) o;
            lemmas = new String[chunks.length];
            j = 0;
            for (Chunk chunk : chunks) {
                String lemma = lemmatizeChunk(chunk);
                if (lemma == null) {
                    LemmasNotFound.log(chunk, i.getSentence2(), i.getPair_ID());
                    chunk.setLemma(null);
                    continue;
                }
                lemma = lemma + chunk.toWNPos();
                chunk.setLemma(lemma);
                lemmas[j++] = lemma;
            }
            i.addProcessedTextPart(sentence2ChunkLemmasCaseSensitive, lemmas);
        }
        
        i.addProcessed(this);
        PerformanceCounters.stopTimer("process ChunkLemmas");
    }

    private String changeChunkText(Chunk chunk) {
        String r = chunk.getChunkText();
        /* inflate have n't, do n't... to have not, do not... */

        r = r.replaceAll("ca n't", "can not");
        r = r.replaceAll("wo n't", "will not");

        r = r.replaceAll(" n't", " not");

        /* inflate verbal 's to is: */
        if (chunk.getChunkType().equalsIgnoreCase("VP")) {
            if (r.startsWith("'s ") || r.equalsIgnoreCase("'s")) {
                r = "is" + r.substring(2);
            }

        }

        //DEBUG only::
        /*
         if (!chunk.getChunkText().equalsIgnoreCase(r)) {
         java.lang.System.out.println("changed from \"" + chunk.getChunkText()
         + "\" to \"" + r + "\"");
         }
         */
        return r;
    }

    /**
     *
     * @param chunk
     * @return
     */
    protected String lemmatizeChunk(Chunk chunk) {
        LemmasNotFound.incTotal(chunk);
        return lemmatizeChunkMethodJWI(chunk);
    }

    /**
     *
     * @param chunk
     * @return
     */
    protected String lemmatizeChunkMethodJWI(Chunk chunk) {
        String chunkText = chunk.getChunkText().replaceAll("[^a-zA-Z0-9]", " ").trim();
//        Logger.getLogger("Lemmatizer").log(Level.INFO, "chunkText reduced from \"{0}\"\n\t to \"{1}\"", new Object[]{chunk.getChunkText(), chunkText});
        if (chunkText.isEmpty()) {
//            Logger.getLogger("Lemmatizer").log(Level.INFO, "\tlemma = null");
            return null;
        }

        edu.mit.jwi.item.POS pos = null;

        switch (chunk.getChunkType()) {
            case "NP":
                pos = edu.mit.jwi.item.POS.NOUN;
                break;
            case "VP":
                pos = edu.mit.jwi.item.POS.VERB;
        }

        if (pos == null) {
//            Logger.getLogger("Lemmatizer").log(Level.INFO, "\tlemma = null");
            return null;
        }
//        Logger.getLogger("Lemmatizer").log(Level.INFO, "\tlemma most likely POS = {0}", pos);

        return lemmatizeJWIMethod0(chunkText, pos);
    }
    /*    
     private String lemmatizeChunkMethodCombinedWs4jAndJaws(Chunk chunk) {
     String lemma = chunk.getChunkText().replaceAll("[^a-zA-Z0-9]", " ").trim();
     String validLemma = WNCheckValidMethod0(lemma.replace(" ", "_"), chunk.toPOS(), chunk.toSynsetType());
     while (validLemma == null) {
     int indexOfSpace = lemma.indexOf(" ");
     if (indexOfSpace < 0) {
     lemma = null;
     break;
     }
     lemma = lemma.substring(indexOfSpace + 1);

     validLemma = WNCheckValidMethod0(lemma.replace(" ", "_"), chunk.toPOS(), chunk.toSynsetType());
     }

     if (lemma == null) {
     lemma = chunk.getChunkText().replaceAll("[^a-zA-Z0-9]", " ").trim();
     validLemma = WNCheckValidMethod1(lemma, chunk.toPOS(), chunk.toSynsetType());
     while (validLemma == null) {
     int indexOfSpace = lemma.indexOf(" ");
     if (indexOfSpace < 0) {
     break;
     }
     lemma = lemma.substring(indexOfSpace + 1);

     validLemma = WNCheckValidMethod1(lemma, chunk.toPOS(), chunk.toSynsetType());
     }
     }

     //        java.lang.System.out.println("\tChunk's (" + chunk.getChunkText()
     //                + ") lemma is:" + validLemma);
     return validLemma;
     }
     */
    /*    
     private String WNCheckValidMethod1(String lemma, POS pos, SynsetType pos2) {
     //use JAWS method1:
     String[] wordCandidates = wndb.getBaseFormCandidates(lemma, pos2);
     if (wordCandidates == null) {
     return null;
     }
     if (wordCandidates.length == 0) {
     return null;
     }

     for (String wordCandidate : wordCandidates) {
     if (wordCandidate.equalsIgnoreCase(lemma)) {
     return lemma;
     }
     }

     String doubleCheck;
     for (String wordCandidate : wordCandidates) {
     doubleCheck = WNCheckValidMethod0(wordCandidate.replace(" ", "_"), pos, pos2);
     if (doubleCheck != null) {
     return doubleCheck;
     }
     }

     return null;

        
     //         //USE JAWS method1:
     //        
     //         Synset[] synsets;
     //         try {
     //         synsets = (Synset[]) wndb.getSynsets(lemma);
     //         } catch (WordNetException wne) {
     //         return null;
     //         }
     //         return Arrays.toString(synsets);
         
     }
     */

    private String WNCheckValidMethod0(String lemma, POS pos) {
        //USE ws4J:

        List<Synset> synsetList = edu.cmu.lti.jawjaw.util.WordNetUtil.wordToSynsets(lemma, pos);

        ArrayList<Word> words = new ArrayList<>();
        for (Synset synset : synsetList) {
            words.addAll(edu.cmu.lti.jawjaw.util.WordNetUtil.synsetToWords(synset.getSynset()));
        }
        Iterator<Word> it = words.iterator();

        while (it.hasNext()) {
            if (it.next().getLang().equals(Lang.jpn)) {
                it.remove();
            }
        }

        if (words.isEmpty()) {
            return null;
        }

        for (Word word : words) {
            if (word.getLemma().equalsIgnoreCase(lemma)) {
                return lemma;
            }
            /*TODO: how to check word frequency  */
        }
        return words.get(0).getLemma();

    }

    private IIndexWord getIndexWord(String wordForm, edu.mit.jwi.item.POS pos) {

        List<String> stems = stemmer.findStems(wordForm, pos);
        if (stems.isEmpty()) {
            return null;
        }

        return dict.getIndexWord(stems.get(0), pos);
    }

    private String lemmatizeJWIMethod0(String chunkText, edu.mit.jwi.item.POS pos) {
        String lemma = chunkText;
//        Logger.getLogger("Lemmatizer").log(Level.INFO, "\tgetIndexWord({0})", lemma);
        IIndexWord validLemma = getIndexWord(lemma, pos);
        while (validLemma == null) {
            validLemma = getIndexWord(lemma.toLowerCase(), pos);
            if (validLemma != null)
                break;
            
            int indexOfSpace = lemma.indexOf(" ");
            if (indexOfSpace < 0) {
                break;
            }
            lemma = lemma.substring(indexOfSpace + 1);
//            Logger.getLogger("Lemmatizer").log(Level.INFO, "\tgetIndexWord({0})", lemma);
            validLemma = getIndexWord(lemma, pos);
        }

        if (validLemma != null) {
//            Logger.getLogger("Lemmatizer").log(Level.INFO, "\tlemma = {0}", validLemma.getLemma());
            return validLemma.getLemma();
        }
//        Logger.getLogger("Lemmatizer").log(Level.INFO, "\tlemma = null");

        return null;
    }
}
