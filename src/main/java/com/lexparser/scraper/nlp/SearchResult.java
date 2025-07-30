package com.lexparser.scraper.nlp;

import java.util.List;

public class SearchResult {
    private final int index;
    private final List<AnnotatedToken> sentence;

    public SearchResult(int index, List<AnnotatedToken> sentence) {
        this.index = index;
        this.sentence = sentence;
    }

    public int getIndex() { return index; }
    public List<AnnotatedToken> getSentence() { return sentence; }
}