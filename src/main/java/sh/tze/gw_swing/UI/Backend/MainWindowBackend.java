package sh.tze.gw_swing.UI.Backend;

import com.lexparser.scraper.WikipediaScraper;
import com.lexparser.scraper.nlp.AnnotatedToken;
import com.lexparser.scraper.nlp.NLPProcessing;
import sh.tze.gw_swing.UI.Backend.DataRepresentation.PresentableWord;
import sh.tze.gw_swing.UI.Backend.DataRepresentation.Word;
import sh.tze.gw_swing.UI.MainWindowView;

import com.lexparser.scraper.nlp.SearchResult;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;


public class MainWindowBackend {
    private MainWindowView mwView;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private String corpus;
    private boolean isCorpusNewlyInstalled;
    private NLPProcessing nlpres;

    private final List<String> urlhist = new ArrayList<>();
    private final List<NLPProcessing> corpushist = new ArrayList<>();
    private NLPProcessing current;

    public MainWindowBackend(MainWindowView view) {
        mwView = view;
        this.corpus = "";
        this.isCorpusNewlyInstalled = false;
        setup();
    }

    public void setup(){
        addPropertyChangeListener("corpus", evt -> {
            onCorpusChange(); // 320ms
        });
    }

    private void onCorpusChange(){
        //throwing out from the processing class is a bit annoying
        try{
            nlpres = new NLPProcessing(getCorpus()); // 310ms of 320ms lmao
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        doConversion();
        present(); // only 10ms. modern microarchitecture. wow.
        //.setText(corpus) so it doesn't consume the flag. Sounds like working on tensor.data to avoid recording grad_fn
    }

    public void onFilterClicked(){
        presentFilter();
    }

    public void onResetClicked(){
        if(corpus == null || nlpres == null ){
            return;
        }
        present();
    }
    private void doConversion(){
        corpushist.add(current);
        current = nlpres;
        var doc = nlpres.getWordSentences();
        List<List<PresentableWord>> w = new ArrayList<>(doc.size());
        for(var sentence : doc){
            List<PresentableWord> ws = new ArrayList<>(sentence.size());
            for(var word : sentence){
                ws.add(new PresentableWord(Word.fromAnnotatedToken(word)));
            }
            w.add(ws);
        }

    }



    // profiling shows the big-eater is still opennlp. good news.
    private void present(){
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style=\"white-space: nowrap;\">");
        for(int i = 0; i < nlpres.getWordSentences().size(); i++){
            sb.append(parseSentence(nlpres.getWordSentences().get(i)));
//            StringBuilder l1 = new StringBuilder();
//            StringBuilder l2 = new StringBuilder();
//            StringBuilder l3 = new StringBuilder();
//            for(int j = 0; j < nlpres.getWordSentences().get(i).size(); j++){
//                var w = nlpres.getWordSentences().get(i).get(j);
//                l1.append(w.getForm() + " ");
//                l2.append(w.getLemma() + " ");
//                l3.append(w.getPos() + " ");
//            }
//            sb.append(l1);
//            sb.append("<br>");
//            sb.append(l2);
//            sb.append("<br>");
//            sb.append(l3);
//            sb.append("<br>");
//            sb.append("<hr>");
        }
        sb.append("</body></html>");
        mwView.getTextDisplayPanel().setText(sb.toString());
    }

    private void presentFilter(){
        List<List<SearchResult>> results = doFilter(obtainFilterScheme());
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style=\"white-space: nowrap;\">");
        for(var identity : results){
            for(var occurrence : identity){
                // although the user is searching a for a certain criteria, the hits are standalone identities.
                // so I think highlighting(bolding) the whole identity(wf,lemma,pos) is justified and the best
                sb.append(parseSentence(occurrence));
            }
        }
        sb.append("</body></html>");
        mwView.getTextDisplayPanel().setText(sb.toString());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public String getCorpus() {
        isCorpusNewlyInstalled = false;
        return corpus;
    }
    public void setCorpus(String corpus) {
        String oldCorpus = this.corpus;
        this.corpus = corpus;
        isCorpusNewlyInstalled = true;
        pcs.firePropertyChange("corpus", oldCorpus, corpus);
    }

    public class URLOpenListener implements ActionListener {
        private JTextField urlTextField;

        public URLOpenListener(JTextField urlTextField) {
            this.urlTextField = urlTextField;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String url = urlTextField.getText().trim();
            if (url.isEmpty()) {
                JOptionPane.showMessageDialog((JButton) e.getSource(), "Please enter a URL", "Error", JOptionPane.ERROR_MESSAGE);
            }
            else dispatchURL(url);
        }
    }

    private static final String RemotePolicy = "^(http|https|ftp)://.*$";
    private static final String FQDNShapedPolicy = "([a-zA-Z0-9].)+[a-zA-Z0-9]"; // sry modern non-latin tLDs
    // now only allowing valid absolute path; relying on 'working directory' for a gui program is anyway less intuitive
    private static final String LocalPolicy = "^(/|(/(.+))+$)"; //forgot about unicode chars

    //https://developer.mozilla.org/en-US/docs/Learn_web_development/Howto/Web_mechanics/What_is_a_URL
    public void dispatchURL(String url) { // oof, debate over whether to lazy-load content
        if(url.matches(RemotePolicy)) {
            setCorpus(WikipediaScraper.scrapeContent(url));
            urlhist.add(url);
        }
        else if(url.matches(LocalPolicy)){ // Sorry but only '/' as path separator is allowed, bye NT/Win32, welcome POSIX
            try {
                setCorpus(java.nio.file.Files.readString(java.nio.file.Path.of(url)));
                urlhist.add(url);
            } catch (java.io.IOException ex) { // sad to emit an orphan dialog
                JOptionPane.showMessageDialog(null, "Failed to read file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (url.matches(".*" + FQDNShapedPolicy)) {
            JOptionPane.showMessageDialog(null, "Malformed input that looks like an URL. Are you missing the url scheme?", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public java.util.List<String> getWFSuggestionFromNLPResult(String input){
        if (input.isEmpty() || nlpres == null) {
            return null;
        }
        return nlpres.getTokens().stream()
                .flatMap(List::stream)
                .filter(token -> token.startsWith(input))
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    public java.util.List<String> getPOSSuggestionFromNLPResult(String input){
        if (input.isEmpty() || nlpres == null) {
            return null;
        }
        // since pos can be 'exhaustively' listed in a simple dict, this is a bit overly performance-hungry
        return nlpres.getPosTags().stream()
                .flatMap(List::stream)
                .filter(token -> token.startsWith(input))
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    public java.util.List<String> getLemmaSuggestionFromNLPResult(String input){
        if (input.isEmpty() || nlpres == null) {
            return null;
        }
        return nlpres.getLemmas().stream()
                .flatMap(List::stream)
                .filter(token -> token.startsWith(input))
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    enum _filter_range_scheme {
        whole_sentence,
        of_neighbour,
        _reserved
    }
    record filterScheme(boolean caseSensitive, _filter_range_scheme rs, int l, int r, String wf, String pos, String lemma){};

    public filterScheme obtainFilterScheme(){
        boolean caseSensitive = false;
        _filter_range_scheme rs;
        int l = 0, r = 0;
        if(mwView.getSel_cs().isSelected()){
            caseSensitive = true;
        }
        if(mwView.getSel_ws().isSelected()){
            rs = _filter_range_scheme.whole_sentence;
        }else if(mwView.getSel_nw().isSelected()){
            rs = _filter_range_scheme.of_neighbour;
            l = Integer.parseInt(mwView.getTf_range_l().getText());
            r = Integer.parseInt(mwView.getTf_range_r().getText());
        }else rs = _filter_range_scheme._reserved;
        String wf = mwView.getSel_wf().isSelected() ? mwView.getTf_wf().getText() : "";
        String pos = mwView.getSel_pos().isSelected() ? mwView.getTf_pos().getText() : "";
        String lemma = mwView.getSel_lemma().isSelected() ? mwView.getTf_lemma().getText() : "";

        return new filterScheme(caseSensitive,rs,l,r,wf,pos,lemma);
    }

    public List<List<SearchResult>> doFilter(filterScheme fs){
        /*
            precedence seems fine, you have f(g(x)) = g(f(x))
            3 pass filtering
         */

        if (nlpres == null) {
            //whether to break the chain to indicate problems:
            // an empty but compatible data structure handles it quitely
            // so just stop returning null or std::nullptr or anything similar
            // because you don't really have to, you just need to write something that's mathematically,formally proofed correct(i can't).
            return new ArrayList<>();
        }

        List<AnnotatedToken> intermediate1 = new ArrayList<>();
        // pass 1 to collect hit from corpus with the highest,
        // also must handle case-specificity here, otherwise words of diff. cases will be collected and contaminate the intermediate set
        if (!fs.wf().isBlank()) {
            for(var s : nlpres.getWordSentences()) {
                for(var w : s){
                    boolean matches = fs.caseSensitive() ?
                            fs.wf().equals(w.getForm()) :
                            fs.wf().equalsIgnoreCase(w.getForm());
                    if(matches) intermediate1.add(w);
                }
            }
        } else if (!fs.lemma().isBlank()) {
            for(var s : nlpres.getWordSentences()) {
                for(var w : s){
                    boolean matches = fs.caseSensitive() ?
                            fs.lemma().equals(w.getLemma()) :
                            fs.lemma().equalsIgnoreCase(w.getLemma());
                    if(matches) intermediate1.add(w);
                }
            }
        } else if (!fs.pos().isBlank()) {
            for(var s : nlpres.getWordSentences()) {
                for(var w : s){
                    if(fs.pos().equals(w.getPos())) intermediate1.add(w);
                }
            }
        } else {
            return new ArrayList<>();
        }

        //pass 2 to apply lower precedence keys
        List<AnnotatedToken> intermediate2 = new ArrayList<>();
        if(!fs.lemma().isBlank()) {
            for(var w: intermediate1){
                boolean matches = fs.caseSensitive() ?
                        fs.lemma().equals(w.getLemma()) :
                        fs.lemma().equalsIgnoreCase(w.getLemma());
                if(matches) intermediate2.add(w);
            }
        } else if(!fs.pos().isBlank()) {
            for(var w: intermediate1){
                if(fs.pos().equals(w.getPos())) intermediate2.add(w);
            }
        } else intermediate2.addAll(intermediate1);


        // pass 3, range
        List<List<SearchResult>> results = new ArrayList<>();

        for(var w : intermediate2){
            List<SearchResult> searchResults;
            switch (fs.rs()) {
                case whole_sentence:
                    if (fs.caseSensitive()) {
                        searchResults = nlpres.findCaseSensitive(w);
                    } else {
                        searchResults = nlpres.find(w);
                    }
                    break;
                case of_neighbour:
                    if (fs.caseSensitive()) {
                        searchResults = nlpres.showNeighborsCaseSensitive(w, fs.l(), fs.r());
                    } else {
                        searchResults = nlpres.findMatchesWithNeighbors(w, fs.l(), fs.r());
                    }
                    break;
                default:
                    searchResults = new ArrayList<>();
            }
            if (!searchResults.isEmpty()) {
                results.add(searchResults);
            }
        }

        return results;

    }

    private String parseSentence(List<AnnotatedToken> sentence){
        StringBuilder sb = new StringBuilder();
        StringBuilder l1 = new StringBuilder();
        StringBuilder l2 = new StringBuilder();
        StringBuilder l3 = new StringBuilder();
        for(int j = 0; j < sentence.size(); j++){
            var w = sentence.get(j);
            l1.append(w.getForm() + " ");
            l2.append(w.getLemma() + " ");
            l3.append(w.getPos() + " ");
        }
        sb.append(l1);
        sb.append("<br>");
        sb.append(l2);
        sb.append("<br>");
        sb.append(l3);
        sb.append("<br>");
        sb.append("<hr>");

        return sb.toString();
    }
    private String parseSentence(List<AnnotatedToken> sentence, int boldAt){
        StringBuilder sb = new StringBuilder();
        StringBuilder l1 = new StringBuilder();
        StringBuilder l2 = new StringBuilder();
        StringBuilder l3 = new StringBuilder();
        for(int j = 0; j < sentence.size(); j++){
            var w = sentence.get(j);
            if(j == boldAt){
                l1.append("<b>" + w.getForm() + "</b> ");
                l2.append("<b>" + w.getLemma() + "</b> ");
                l3.append("<b>" + w.getPos() + "</b> ");
                continue;
            }
            l1.append(w.getForm() + " ");
            l2.append(w.getLemma() + " ");
            l3.append(w.getPos() + " ");
        }
        sb.append(l1);
        sb.append("<br>");
        sb.append(l2);
        sb.append("<br>");
        sb.append(l3);
        sb.append("<br>");
        sb.append("<hr>");

        return sb.toString();
    }
    private String parseSentence(SearchResult sr){
        return parseSentence(sr.getSentence(),sr.getIndex());
    }


}
