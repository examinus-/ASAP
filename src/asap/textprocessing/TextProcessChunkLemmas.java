/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asap.textprocessing;

import asap.Chunk;
import asap.Instance;
import asap.LemmasNotFound;
import asap.PerformanceCounters;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.pobj.Synset;
import edu.cmu.lti.jawjaw.pobj.Word;
import edu.mit.jwi.Dictionary;
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

public class TextProcessChunkLemmas implements TextProcesser, TextProcessedPartKeyConsts {

    private static final HashMap<Long, TextProcessChunkLemmas> tpcls
            = new HashMap<>();
    
    
    private final TextProcesser textProcesserDependency;
    
    //use jwi only for "lematisation"
    private edu.mit.jwi.IDictionary dict;
    private edu.mit.jwi.morph.WordnetStemmer stemmer;
    
    public static TextProcessChunkLemmas getTextProcessChunkLemmas(Thread t) {
        TextProcessChunkLemmas r;
        if (tpcls.containsKey(t.getId())) {
            return tpcls.get(t.getId());
        }
        r = new TextProcessChunkLemmas(t);
        tpcls.put(t.getId(), r);
        return r;
    }
    
    public static TextProcessChunkLemmas getTextProcessChunkLemmas() {
        return getTextProcessChunkLemmas(Thread.currentThread());
    }

    private TextProcessChunkLemmas(String wordnetDatabasePath, Thread t) {

        loadJWI(wordnetDatabasePath);
        
        textProcesserDependency = TextProcessChunks.getTextProcessChunks(t);
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
        dict = new Dictionary(url);
        try {
            dict.open();
        } catch (IOException ex) {
            Logger.getLogger(TextProcessChunkLemmas.class.getName()).log(Level.SEVERE, null, ex);
        }
        stemmer = new WordnetStemmer(dict);
        System.out.println("Loaded JWI Dictionary.");
    }

    private TextProcessChunkLemmas(Thread t) {
        this("/usr/share/wordnet", t);
    }
    
    private TextProcessChunkLemmas() {
        this(Thread.currentThread());
    }

    @Override
    public boolean textProcessingDependenciesMet(Instance i) {
        return i.isProcessed(textProcesserDependency);
    }

    @Override
    public void process(Instance i) {
        if (!textProcessingDependenciesMet(i)) {
            textProcesserDependency.process(i);
        }
        if (i.getProcessedTextPart(sentence1ChunkLemmas) != null) {
            System.out.println("Duplicate calculation detected in ..ChunkLemmas!");
        }
        PerformanceCounters.startTimer("process ChunkLemmas");

        Object o;
        Chunk[] chunks;
        String[] lemmas;
        int j;

        o = i.getProcessedTextPart(sentence1Chunks);
        if (!(o instanceof Chunk[])) {
            return;
        }
        chunks = (Chunk[]) o;
        lemmas = new String[chunks.length];
        j = 0;
        for (Chunk chunk : chunks) {
            chunk.setLemma(lemmatizeChunk(chunk));
            lemmas[j++] = chunk.getLemma();
            if (chunk.getLemma() == null) {
                LemmasNotFound.log(chunk, i.getSentence1());
            }
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
            chunk.setLemma(lemmatizeChunk(chunk));
            lemmas[j++] = chunk.getLemma();
            if (chunk.getLemma() == null) {
                LemmasNotFound.log(chunk, i.getSentence2());
            }
        }
        i.addProcessedTextPart(sentence2ChunkLemmas, lemmas);

        i.addProcessed(this);
        PerformanceCounters.stopTimer("process ChunkLemmas");
    }

    private String lemmatizeChunk(Chunk chunk) {
        return lemmatizeChunkMethodJWI(chunk);
    }
    
    private String lemmatizeChunkMethodJWI(Chunk chunk) {
        String chunkText = chunk.getChunkText().replaceAll("[^a-zA-Z0-9]", " ").trim();
        if (chunkText.isEmpty())
            return "";
        
        edu.mit.jwi.item.POS pos = edu.mit.jwi.item.POS.getPartOfSpeech(chunk.getChunkType().charAt(0));
        
        if (pos == null)
            return null;
        
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

//        System.out.println("\tChunk's (" + chunk.getChunkText()
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

        Word maxFreqWord = null;
        int maxFreq = 0;
        
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
        IIndexWord validLemma = getIndexWord(lemma, pos);
        while (validLemma == null) {
            int indexOfSpace = lemma.indexOf(" ");
            if (indexOfSpace < 0) {
                break;
            }
            lemma = lemma.substring(indexOfSpace + 1);

            validLemma = getIndexWord(lemma, pos);
        }

        if (validLemma != null) {
            return validLemma.getLemma();
        }

        return null;
    }
}
