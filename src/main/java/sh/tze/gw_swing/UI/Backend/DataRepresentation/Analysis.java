package sh.tze.gw_swing.UI.Backend.DataRepresentation;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class Analysis {
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

