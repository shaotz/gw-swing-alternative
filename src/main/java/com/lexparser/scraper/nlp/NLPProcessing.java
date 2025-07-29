package com.lexparser.scraper.nlp;

import opennlp.tools.sentdetect.*;
import opennlp.tools.tokenize.*;
import opennlp.tools.postag.*;
import opennlp.tools.lemmatizer.*;
import sh.tze.gw_swing.UI.Backend.DataRepresentation.Word;

import java.io.*;
import java.util.*;

public class NLPProcessing {

    private final String text;
    private String[] sentences;
    private final List<List<String>> tokens = new ArrayList<>();
    private final List<List<String>> posTags = new ArrayList<>();
    private final List<List<String>> lemmas = new ArrayList<>();
    private final List<List<AnnotatedToken>> wordSentences = new ArrayList<>();

    // Cache models for better performance
    private static SentenceModel sentenceModel;
    private static TokenizerModel tokenizerModel;
    private static POSModel posModel;
    private static LemmatizerModel lemmaModel;

    record ModelPaths(String tk, String lm, String pos, String sd){}
    private static ModelPaths mp_maven = new ModelPaths(
            "opennlp-en-ud-ewt-tokens-1.3-2.5.4.bin",
            "opennlp-en-ud-ewt-lemmas-1.3-2.5.4.bin",
            "opennlp-en-ud-ewt-pos-1.3-2.5.4.bin",
            "opennlp-en-ud-ewt-sentence-1.3-2.5.4.bin"
    );
    private static ModelPaths mp_legacy = new ModelPaths(
            "en-token.bin",
            "en-lemmatizer.bin",
            "en-pos-maxent.bin",
            "en-sent.bin"
    );
    public NLPProcessing(String text) throws IOException {
        this.text = text;
        loadModels();
        process();
    }



    // Load all models first
    private static void loadModels() throws IOException {
        ModelPaths mp = mp_maven;
        if (sentenceModel == null) {
            try (InputStream modelIn = new FileInputStream(mp.sd)) {
                sentenceModel = new SentenceModel(modelIn);
            }
        }
        if (tokenizerModel == null) {
            try (InputStream modelIn = new FileInputStream(mp.tk)) {
                tokenizerModel = new TokenizerModel(modelIn);
            }
        }
        if (posModel == null) {
            try (InputStream modelIn = new FileInputStream(mp.pos)) {
                posModel = new POSModel(modelIn);
            }
        }
        if (lemmaModel == null) {
            try (InputStream modelIn = new FileInputStream(mp.lm)) {
                lemmaModel = new LemmatizerModel(modelIn);
            }
        }
    }

    // Main processing pipeline
    private void process() {
        detectSentences();
        tokenizeSentences();
        tagPOS();
        lemmatize();
    }

    // Uses OpenNLP sentence model to split the input text into sentences
    private void detectSentences() {
        SentenceDetectorME detector = new SentenceDetectorME(sentenceModel);
        this.sentences = detector.sentDetect(text);
    }

    // Tokenizes each sentence into individual words using OpenNLP tokenizer model
    private void tokenizeSentences() {
        TokenizerME tokenizer = new TokenizerME(tokenizerModel);
        for (String sentence : sentences) {
            String[] tokenArray = tokenizer.tokenize(sentence);
            tokens.add(Arrays.asList(tokenArray));
        }
    }

    // Tags each token with its corresponding POS tag using OpenNLP POS tagger
    private void tagPOS() {
        POSTaggerME tagger = new POSTaggerME(posModel);
        for (List<String> tokenList : tokens) {
            String[] tokenArray = tokenList.toArray(new String[0]);
            String[] posArray = tagger.tag(tokenArray);
            posTags.add(Arrays.asList(posArray));
        }
    }

    // Lemmatizes each token using the corresponding POS tag
    private void lemmatize() {
        LemmatizerME lemmatizer = new LemmatizerME(lemmaModel);
        for (int i = 0; i < tokens.size(); i++) {
            List<String> tokenList = tokens.get(i);
            List<String> posList = posTags.get(i);
            String[] lemmaArray = lemmatizer.lemmatize(
                    tokenList.toArray(new String[0]),
                    posList.toArray(new String[0])
            );
            lemmas.add(Arrays.asList(lemmaArray));

            // Construct full sentences with annotations
            List<AnnotatedToken> sentence = new ArrayList<>();
            for (int j = 0; j < tokenList.size(); j++) {
                sentence.add(new AnnotatedToken(tokenList.get(j), posList.get(j), lemmaArray[j]));
            }
            wordSentences.add(sentence);
        }
    }


    public List<SearchResult> find(AnnotatedToken targetWord) {
        List<SearchResult> matches = new ArrayList<>();
        for (List<AnnotatedToken> sentence : wordSentences) {
            for (int i = 0; i < sentence.size(); i++) {
                if (sentence.get(i).equalsSelective(targetWord)) {
                    matches.add(new SearchResult(i, sentence));
                }
            }
        }
        return matches;
    }

    public List<SearchResult> findCaseSensitive(AnnotatedToken targetWord) {
        List<SearchResult> matches = new ArrayList<>();
        for (List<AnnotatedToken> sentence : wordSentences) {
            for (int i = 0; i < sentence.size(); i++) {
                if (sentence.get(i).equalsSelectiveCaseSensitive(targetWord)) {
                    matches.add(new SearchResult(i, sentence));
                }
            }
        }
        return matches;
    }

    public List<SearchResult> findMatchesWithNeighbors(AnnotatedToken targetWord, int leftNumber, int rightNumber) {
        List<SearchResult> result = new ArrayList<>();
        List<AnnotatedToken> allWords = new ArrayList<>();
        for (List<AnnotatedToken> sentence : wordSentences) {
            allWords.addAll(sentence);
        }
        for (int i = 0; i < allWords.size(); i++) {
            if (allWords.get(i).equalsSelective(targetWord)) {
                int start = Math.max(i - leftNumber, 0);
                int end = Math.min(i + rightNumber + 1, allWords.size());
                result.add(new SearchResult(leftNumber, allWords.subList(start, end)));
            }
        }
        return result;
    }

    public List<SearchResult> showNeighborsCaseSensitive(AnnotatedToken targetWord, int leftNumber, int rightNumber) {
        List<SearchResult> result = new ArrayList<>();
        List<AnnotatedToken> allWords = new ArrayList<>();
        for (List<AnnotatedToken> sentence : wordSentences) {
            allWords.addAll(sentence);
        }
        for (int i = 0; i < allWords.size(); i++) {
            if (allWords.get(i).equalsSelectiveCaseSensitive(targetWord)) {
                int start = Math.max(i - leftNumber, 0);
                int end = Math.min(i + rightNumber + 1, allWords.size());
                result.add(new SearchResult(leftNumber, allWords.subList(start, end)));
            }
        }
        return result;
    }

    public void printSearchResults(List<SearchResult> matches) {
        for (SearchResult match : matches) {
            System.out.println("Context:");
            for (AnnotatedToken word : match.getSentence()) {
                System.out.print(word + " ");
            }
            System.out.println("\n---");
        }
    }


    // Getters
    public String[] getSentences() { return sentences; }
    public List<List<String>> getTokens() { return tokens; }
    public List<List<String>> getPosTags() { return posTags; }
    public List<List<String>> getLemmas() { return lemmas; }
    public List<List<AnnotatedToken>> getWordSentences() { return wordSentences; }
}





