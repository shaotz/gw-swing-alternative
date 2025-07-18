package sh.tze.gw_swing.design;

import java.util.ArrayList;
import java.util.List;

public class DataRepresentation {
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

    }

    class ex{
        List<List<PresentableWord>> some;
    }
    public class PresentableWord extends Word {
//        private boolean onFocus;
        private boolean bold;
        public PresentableWord(String word, String POS, String lemma) {
            super(word, POS, lemma);
        }
        public PresentableWord(Word w){
            super(w.getWord(), w.getPOS(), w.getLemma());
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

    public static class Analysis {
        private List<List<Word>> corpus;
        private String originalText;
        private String sourceUrl;
        private long timestamp;

        public Analysis(String sourceUrl) {
            this.sourceUrl = sourceUrl;
            this.corpus = new ArrayList<>();
            this.timestamp = System.currentTimeMillis();
        }

        public void setOriginalText(String originalText) {
            this.originalText = originalText;
        }

        public void setCorpus(List<List<Word>> corpus) {
            this.corpus = corpus;
        }

        public void addSentence(List<Word> sentence) {
            this.corpus.add(sentence);
        }

        public List<List<Word>> getCorpus() { return corpus; }
        public String getOriginalText() { return originalText; }
        public String getSourceUrl() { return sourceUrl; }
        public long getTimestamp() { return timestamp; }

        public boolean isEmpty() {
            return corpus.isEmpty();
        }

        public int getSentenceCount() {
            return corpus.size();
        }

        public int getTotalWordCount() {
            return corpus.stream().mapToInt(List::size).sum();
        }
    }
}
