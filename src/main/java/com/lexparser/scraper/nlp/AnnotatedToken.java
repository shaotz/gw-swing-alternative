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

    public boolean equalsSelective(AnnotatedToken other)
    {
        return this.form.equalsIgnoreCase(other.form) &&  this.pos.equalsIgnoreCase(other.pos) && this.lemma.equalsIgnoreCase(other.lemma);
    }

    public boolean equalsSelectiveCaseSensitive(AnnotatedToken other) {
        return this.form.equals(other.form)  && this.pos.equals(other.pos) && this.lemma.equals(other.lemma);
    }

    @Override
    public String toString() {
        return form + "/" + pos + "/" + lemma;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // same object
        if (!(obj instanceof AnnotatedToken)) return false;
        AnnotatedToken other = (AnnotatedToken) obj;
        return equalsSelective(other);
    }
}

/*
    WHAT is `this.lemma.<method>`.
    Why is so that only lemma be the deterministic identity of a whole word.
    For a lemma you might have different wf and thus different pos.
    The identity of a token should be WF, i.e. the attribute that carries maximum information.

    Anyway this is inadequate for proper filtering. Leute schau den UI an.

 */

