package com.lexparser.scraper.nlp;

public class AnnotatedToken {
    private final String form;
    private final String pos;
    private final String lemma;

    public AnnotatedToken(String form, String pos, String lemma) {
        this.form = form;
        this.pos = pos;
        this.lemma = lemma;
    }

    public String getForm() { return form; }
    public String getPos() { return pos; }
    public String getLemma() { return lemma; }

    public boolean equalsSelective(AnnotatedToken other) {
        return this.lemma.equalsIgnoreCase(other.lemma);
    }

    public boolean equalsSelectiveCaseSensitive(AnnotatedToken other) {
        return this.lemma.equals(other.lemma);
    }

    @Override
    public String toString() {
        return form + "/" + pos + "/" + lemma;
    }
}

