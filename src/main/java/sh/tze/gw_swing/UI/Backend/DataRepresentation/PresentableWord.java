package sh.tze.gw_swing.UI.Backend.DataRepresentation;

public class PresentableWord extends Word {
    //        private boolean onFocus;
    private boolean bold;

    public PresentableWord(String word, String POS, String lemma, boolean bold) {
        super(word, POS, lemma);
        this.bold = bold;
    }

    public PresentableWord(String word, String POS, String lemma) {
        super(word, POS, lemma);
        bold = false;
    }
    public PresentableWord(Word w) {
        super(w.getWord(), w.getPOS(), w.getLemma());
        bold = false;
    }
    public void setBold(boolean bold) {
        this.bold = bold;
    }
    public boolean isBold() {
        return bold;
    }
    public String renderWord(){
        return bold? "<b>" + getWord() + "</b>" : getWord();
    }
    public String renderLemma(){ return bold? "<b>" + getLemma() + "</b>" : getLemma(); }
    public String renderPOS(){
        return bold? "<b>" + getPOS() + "</b>" : getPOS();
    }
}