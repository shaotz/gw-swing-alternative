package sh.tze.gw_swing.UI.Backend.DataRepresentation;

import java.util.ArrayList;
import java.util.List;

public class PresentableWordSequence{
    private final List<String> poss = new ArrayList<>();
    private final List<String> lemmas =  new ArrayList<>();
    private final List<String> wordForms  = new ArrayList<>();


    public PresentableWordSequence(List<PresentableWord> words) {
        for(int i = 0; i < words.size(); i++){ // thanks C, still thinking about memory adjacency to boost efficiency
            poss.add(words.get(i).getPOS());
            lemmas.add(words.get(i).getLemma());
            wordForms.add(words.get(i).getWord());
        }
    }

    public String renderSequence(){
        StringBuilder sb = new StringBuilder();
        wordForms.forEach((wordForm)->{sb.append(wordForm + " ");});
        sb.append("\n");
        lemmas.forEach((lemma)->{sb.append(lemma + " ");});
        sb.append('\n');
        poss.forEach((pos)->{sb.append(pos + " ");});
        sb.append('\n');
        return sb.toString();
    }
}
