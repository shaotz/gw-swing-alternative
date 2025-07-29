package sh.tze.gw_swing.UI.Backend.DataRepresentation;

import java.util.List;

@Deprecated
public class PresentableWordSequence extends WordSequence{
    public PresentableWordSequence(List<String> poss, List<String> lemmas, List<String> wordForms) {
        super(poss, lemmas, wordForms);
    }

    public PresentableWordSequence(List<Word> words) {
        super(words);
    }

    // at this point just realized java doesn't provide default copy-constructor
    public PresentableWordSequence(WordSequence wseq){
        super(wseq);
    }
}
