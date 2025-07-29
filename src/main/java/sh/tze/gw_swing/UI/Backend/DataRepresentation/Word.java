package sh.tze.gw_swing.UI.Backend.DataRepresentation;


import com.lexparser.scraper.nlp.AnnotatedToken;

public class Word {
    private final String word;
    private final String POS;
    private final String lemma;
    public Word(String word, String POS, String lemma) {
        this.word = word;
        this.POS = POS;
        this.lemma = lemma;
    }
    public String getWord() {
        return word;
    }
    public String getPOS() {
        return POS;
    }
    public String getLemma() {
        return lemma;
    }

    // adapts to Word, and then converts to PresentableWord
    public static Word fromAnnotatedToken(AnnotatedToken token){
        return new Word(token.getForm(),token.getPos(),token.getLemma());
    }


}
