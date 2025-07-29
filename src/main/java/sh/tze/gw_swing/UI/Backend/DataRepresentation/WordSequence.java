package sh.tze.gw_swing.UI.Backend.DataRepresentation;

import java.util.ArrayList;
import java.util.List;

public class WordSequence {
    private final List<String> poss = new ArrayList<>();
    private final List<String> lemmas =  new ArrayList<>();
    private final List<String> wordForms  = new ArrayList<>();

    public WordSequence(List<String> poss, List<String> lemmas, List<String> wordForms){
        this.poss.addAll(poss);
        this.lemmas.addAll(lemmas);
        this.wordForms.addAll(wordForms);
    }

    public WordSequence(List<Word> words){
        for(int i = 0; i < words.size(); i++){ // thanks C, still thinking about memory adjacency to boost efficiency
            poss.add(words.get(i).getPOS());
            lemmas.add(words.get(i).getLemma());
            wordForms.add(words.get(i).getWord());
        }
    }

    public WordSequence(WordSequence wseq){
        this.poss.addAll(wseq.getPoss());
        this.lemmas.addAll(wseq.getLemmas());
        this.wordForms.addAll(wseq.getWordForms());
    }
    public List<String> getPoss() {
        return poss;
    }

    public List<String> getLemmas() {
        return lemmas;
    }

    public List<String> getWordForms() {
        return wordForms;
    }
}
