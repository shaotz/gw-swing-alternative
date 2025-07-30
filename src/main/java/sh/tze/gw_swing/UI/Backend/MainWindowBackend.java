package sh.tze.gw_swing.UI.Backend;

import com.lexparser.scraper.WikipediaScraper;
import com.lexparser.scraper.nlp.AnnotatedToken;
import com.lexparser.scraper.nlp.NLPProcessing;
import sh.tze.gw_swing.UI.Backend.DataRepresentation.PresentableWord;
import sh.tze.gw_swing.UI.Backend.DataRepresentation.Word;
import sh.tze.gw_swing.UI.Backend.File.IOWrapper;
import sh.tze.gw_swing.UI.MainWindowView;

import com.lexparser.scraper.nlp.SearchResult;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class MainWindowBackend {
    private MainWindowView mwView;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private String corpus;
    private boolean isCorpusNewlyInstalled;
    private NLPProcessing nlpres;

    private final List<String> urlHistory = new ArrayList<>();
    // L<L<AT>> as a document. NLPProcessing contains a L<L<AT>>. L<L<L<AT>>> as set of document
//    private final List<NLPProcessing> corpusHistory = new ArrayList<>();
    private final HashMap<String,NLPProcessing> corpusHistory = new HashMap<>(); // now linking them together as well
    // and don't ever think to remove repetition here. It's web content.
    // corpushist is updated on new corpus load, schemehist is updated every new search.
    // to align corpushist with schemehist what about using dictionary
    private final HashMap<NLPProcessing,List<filterScheme>> schemeHistory = new HashMap<>(); // updated is aligned with `corpusHistory`
    private final List<filterScheme> currentSchemeHistory = new ArrayList<>(); // to recreate the search results

    private boolean f_first; // although just realized it is possible to use isCorpusNewlyInstalled to lock schemeHistory, but iCNI is hooked rather weirdly(my bad) so use a dedicated one.

    public MainWindowBackend(MainWindowView view) {
        mwView = view;
        this.corpus = "";
        this.isCorpusNewlyInstalled = false;
        f_first = true;
        setup();
    }

    public void setup(){
        addPropertyChangeListener("corpus", evt -> {
            onCorpusChange(); // 320ms
        });
    }

    private void onCorpusChange(){
        //throwing out from the processing class is a bit annoying
        var previous = nlpres;
        try{
            nlpres = new NLPProcessing(getCorpus());

            // Only save schemes if there was a previous corpus AND current schemes exist
            if(previous != null && !currentSchemeHistory.isEmpty()){
                var hist = schemeHistory.get(previous);
                if(hist == null || hist.isEmpty()){
                    schemeHistory.put(previous, new ArrayList<>(currentSchemeHistory));
                }else {
                    hist.addAll(currentSchemeHistory);
                }
                currentSchemeHistory.clear();
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        doConversion();
        present(); // only 10ms. modern microarchitecture. wow.
        //.setText(corpus) so it doesn't consume the flag. Sounds like working on tensor.data to avoid recording grad_fn  <no more relevant>
    }
    public void onFilterClicked(){
        if( mwView.getSel_wf() == null && mwView.getTf_pos() == null && mwView.getSel_lemma() == null) {
            JOptionPane.showMessageDialog(mwView.getTextDisplayPanel(), "Please select at least one filter criterion.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        presentFiltering();
        updateFilterSchemePaneOnNewScheme();
    }
    public void onResetClicked(){
        if(corpus == null || nlpres == null ){
            return;
        }
        present();
    }
    /*
    using:
    public static void saveToXML(List<List<List<AnnotatedToken>>> documents,
                                 List<String> urls,
                                 List<String> filterSchemes,
                                 String filePath)

     */
    public void onSaveClicked(File file){
        if(corpus == null || nlpres == null ){
            JOptionPane.showMessageDialog(mwView.getTextDisplayPanel(), "No corpus loaded or processed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        // ITERATE OVER HISTORY CHAIN
        // URL -> CorpusHistory -> FilterSchemeHistory
        // **almost forgot currentSchemeHistory**
        // and then apply filterScheme to their own corpus
        try {
            // Maps to store all results
            Map<String, List<List<SearchResult>>> urlToResultsMap = new HashMap<>();
            Map<String, String> urlToDateMap = new HashMap<>();
            Map<String, String> urlToFilterSchemeMap = new HashMap<>();

            // First add current filter scheme results if it exists
            if (!currentSchemeHistory.isEmpty()) {
                filterScheme currentScheme = currentSchemeHistory.get(currentSchemeHistory.size() - 1);
                List<List<SearchResult>> results = doFilter(currentScheme);

                if (!results.isEmpty() && !urlHistory.isEmpty()) {
                    String url = urlHistory.get(urlHistory.size() - 1);
                    urlToResultsMap.put(url, results);
                    urlToDateMap.put(url, ""); // No date available
                    urlToFilterSchemeMap.put(url, currentScheme.toStringAsListEntry());
                }
            }
            for (int i = 0; i < urlHistory.size(); i++) {
                String url = urlHistory.get(i);
                NLPProcessing atCorpus = corpusHistory.get(url);

                if (atCorpus != null) {
                    List<filterScheme> filterSchemes = schemeHistory.get(atCorpus);

                    if (filterSchemes != null && !filterSchemes.isEmpty()) {
                        for (filterScheme scheme : filterSchemes) {
                            // Temporarily set nlpres to the historical corpus to filter correctly
                            NLPProcessing tempNlpres = nlpres;
                            nlpres = atCorpus;
                            List<List<SearchResult>> results = doFilter(scheme);
                            nlpres = tempNlpres; // Restore current nlpres

                            if (!results.isEmpty()) {
                                // If this URL is already in the map, append results
                                if (urlToResultsMap.containsKey(url)) {
                                    urlToResultsMap.get(url).addAll(results);
                                } else {
                                    urlToResultsMap.put(url, results);
                                }

                                urlToDateMap.put(url, ""); // No date available
                                urlToFilterSchemeMap.put(url, scheme.toStringAsListEntry());
                            }
                        }
                    }
                }
            }
            if (!urlToResultsMap.isEmpty()) {
                IOWrapper.saveMultipleSearchResultsToXML(
                        urlToResultsMap, urlToDateMap, urlToFilterSchemeMap,
                        file.getAbsolutePath());

                JOptionPane.showMessageDialog(mwView.getTextDisplayPanel(),
                        "Saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mwView.getTextDisplayPanel(),
                        "No search results to save.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            // ^^^^^ don't get distracted by popup lines
        }catch (RuntimeException e){
          JOptionPane.showMessageDialog(mwView.getTextDisplayPanel(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e){
            JOptionPane.showMessageDialog(mwView.getTextDisplayPanel(), "Failed to save: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void onURLListEntryActivated(){
        var corpusHistoryList = mwView.getUrlHistoryList();
        var listModel = mwView.getUrlHistoryListModel();
        var filterSchemeHistoryList = mwView.getFilterSchemeHistoryList();
        var filterSchemeListModel = mwView.getFilterSchemeHistoryListModel();

        if(nlpres != null && !currentSchemeHistory.isEmpty()){
            var hist = schemeHistory.get(nlpres);
            if(hist == null || hist.isEmpty()){
                schemeHistory.put(nlpres, new ArrayList<>(currentSchemeHistory));
            }else {
                hist.addAll(currentSchemeHistory);
            }
            currentSchemeHistory.clear();
        }

        String selectedURL = (String) corpusHistoryList.getSelectedData();
        NLPProcessing referencedDoc = corpusHistory.get(selectedURL);
        nlpres = referencedDoc;

        filterSchemeListModel.clear();
        filterSchemeListModel.addElement("Filter Scheme History");

        List<filterScheme> schemes = schemeHistory.get(nlpres);
        if (schemes != null && !schemes.isEmpty()) {
            List<String> filterSchemeHistory = schemes.stream()
                    .map(filterScheme::toStringAsListEntry)
                    .collect(Collectors.toList());
            filterSchemeListModel.addAll(filterSchemeHistory);
        }

        present();
    }
    public void onFilterSchemeListEntryActivated(){
        var filterSchemeList = mwView.getFilterSchemeHistoryList();
        var listModel = mwView.getFilterSchemeHistoryListModel();

        String selectedScheme = (String) filterSchemeList.getSelectedData();
        filterScheme fs = fromStringAsListEntry(selectedScheme);
        presentFiltering(fs);
    }
    private void updateFilterSchemePaneOnNewScheme(){ //idealy should be triggered on new_corpus, new_filterScheme
        if(currentSchemeHistory.isEmpty()) {
            return; // Nothing to update
        }

        var filterSchemeHistoryList = mwView.getFilterSchemeHistoryList();
        var listModel = mwView.getFilterSchemeHistoryListModel();

        var fs = currentSchemeHistory.get(currentSchemeHistory.size()-1);
        listModel.addElement(fs.toStringAsListEntry());
        filterSchemeHistoryList.setSelectedIndex(listModel.getSize() - 1);

    }
    private void updateURLPaneOnCorpusChange(){
        var urlHistoryList =  mwView.getUrlHistoryList();
        var listModel = mwView.getUrlHistoryListModel();

        listModel.addElement(urlHistory.get(urlHistory.size()-1));
        urlHistoryList.setSelectedIndex(listModel.getSize() - 1);

        var filterSchemeHistoryList = mwView.getFilterSchemeHistoryList();
        var filterSchemeListModel = mwView.getFilterSchemeHistoryListModel();
        // clear the filter scheme history, since it is not relevant to the new corpus
        filterSchemeListModel.clear();
        filterSchemeListModel.addElement("Filter Scheme History");
        filterSchemeHistoryList.setSelectedIndex(-1); // deselect the list

        // is not relevant to the new corpus, and the user should not see any previous filter schemes
    }
    private void doConversion(){
        if(f_first){
            corpusHistory.put(urlHistory.get(0),nlpres); //i know, i know
            f_first = false;
        }else{
            corpusHistory.put(urlHistory.get(urlHistory.size()-1),nlpres);
        }

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
        }
        sb.append("</body></html>");
        mwView.getTextDisplayPanel().setText(sb.toString());
    }

    private void presentFiltering(){
        filterScheme fs = obtainFilterScheme();
//        currentSchemeHistory.add(fs); // moved to obtainFilterScheme(), this makes sure that I can use present() and presentFilter() as standalone
        List<List<SearchResult>> results = doFilter(fs);
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
    private void presentFiltering(filterScheme fs){
        List<List<SearchResult>> results = doFilter(fs);
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style=\"white-space: nowrap;\">");
        for(var identity : results){
            for(var occurrence : identity){
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
    @Deprecated
    public void setCorpus(String corpus) {
        String oldCorpus = this.corpus;
        this.corpus = corpus;
        isCorpusNewlyInstalled = true;
        pcs.firePropertyChange("corpus", oldCorpus, corpus);
    }
    public void setCorpusWithURL(String corpus, String url) {
        String oldCorpus = this.corpus;
        this.corpus = corpus;
        isCorpusNewlyInstalled = true;
        urlHistory.add(url);
        updateURLPaneOnCorpusChange();
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
            setCorpusWithURL(WikipediaScraper.scrapeContent(url),url);
//            if(isCorpusNewlyInstalled) urlHistory.add(url); //  to ensure atomic alignment with corpusHistory ('scraper' can throw, and this might be a failed url)
        }
        else if(url.matches(LocalPolicy)){ // Sorry but only '/' as path separator is allowed, bye NT/Win32, welcome POSIX
            try {
                setCorpusWithURL(java.nio.file.Files.readString(java.nio.file.Path.of(url)),url);
//                if(isCorpusNewlyInstalled) urlHistory.add(url);
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
    record filterScheme(boolean caseSensitive, _filter_range_scheme rs, int l, int r, String wf, String pos, String lemma){
        public String toStringAsListEntry() {
            StringBuilder sb = new StringBuilder();
            sb.append("WF=\"" + wf + "\"");
            sb.append(",Lemma=\"" + lemma + "\"");
            sb.append(",POS=\"" + pos + "\"");
            sb.append(switch(rs){
                case whole_sentence ->  ",whole_sentence";
                case of_neighbour ->  ",of_neighbour=\"" + l + ":" + r + "\"";
                case _reserved ->  ",(invalid)";
            });
            if(caseSensitive) sb.append(",case-sensitive");
            return sb.toString();
        }
    };

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

        var fs = new filterScheme(caseSensitive,rs,l,r,wf,pos,lemma);
        currentSchemeHistory.add(fs);
        return fs;
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
        if((!Objects.equals(fs.wf(), "") || !Objects.equals(fs.lemma(), "") || !Objects.equals(fs.pos(), ""))){
            // pass 1 to collect hit from corpus with the highest,
            // also must handle case-specificity here, otherwise words of diff. cases will be collected and contaminate the intermediate set
            if (!fs.wf().isBlank()) {
                for (var s : nlpres.getWordSentences()) {
                    for (var w : s) {
                        boolean matches = fs.caseSensitive() ?
                                fs.wf().equals(w.getForm()) :
                                fs.wf().equalsIgnoreCase(w.getForm());
                        if (matches) intermediate1.add(w);
                    }
                }
            } else if (!fs.lemma().isBlank()) {
                for (var s : nlpres.getWordSentences()) {
                    for (var w : s) {
                        boolean matches = fs.caseSensitive() ?
                                fs.lemma().equals(w.getLemma()) :
                                fs.lemma().equalsIgnoreCase(w.getLemma());
                        if (matches) intermediate1.add(w);
                    }
                }
            } else if (!fs.pos().isBlank()) {
                for (var s : nlpres.getWordSentences()) {
                    for (var w : s) {
                        if (fs.pos().equals(w.getPos())) intermediate1.add(w);
                    }
                }
            } else {
                return new ArrayList<>();
            }
        }else intermediate1 = nlpres.getWordSentences().stream().flatMap(List::stream).collect(Collectors.toList());

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

    // some dirty works here
    public static filterScheme fromStringAsListEntry(String listEntry) {
        boolean caseSensitive = false;
        _filter_range_scheme rs = _filter_range_scheme.whole_sentence; // adjusted to whole_sentence for better compatibility with XML writes
        int l = 0, r = 0;
        String wf = "", pos = "", lemma = "";
        // csv hoooray
        String[] parts = listEntry.split(",");

        for (String part : parts) {
            part = part.trim();

            if (part.startsWith("WF=\"") && part.endsWith("\"")) {
                wf = part.substring(4, part.length() - 1);
            } else if (part.startsWith("Lemma=\"") && part.endsWith("\"")) {
                lemma = part.substring(7, part.length() - 1);
            } else if (part.startsWith("POS=\"") && part.endsWith("\"")) {
                pos = part.substring(5, part.length() - 1);
            } else if (part.equals("whole_sentence")) {
                rs = _filter_range_scheme.whole_sentence;
            } else if (part.startsWith("of_neighbour=\"") && part.endsWith("\"")) {
                String rangeStr = part.substring(14, part.length() - 1);
                String[] rangeParts = rangeStr.split(":");
                if (rangeParts.length == 2) {
                    l = Integer.parseInt(rangeParts[0]);
                    r = Integer.parseInt(rangeParts[1]);
                }
                rs = _filter_range_scheme.of_neighbour;
            } else if (part.equals("case-sensitive")) {
                caseSensitive = true;
            }
        }

        return new filterScheme(caseSensitive, rs, l, r, wf, pos, lemma);
    }

}
