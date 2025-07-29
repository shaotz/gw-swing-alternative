package sh.tze.gw_swing.UI.Backend;

import com.lexparser.scraper.WikipediaScraper;
import com.lexparser.scraper.nlp.AnnotatedToken;
import com.lexparser.scraper.nlp.NLPProcessing;
import sh.tze.gw_swing.UI.Backend.DataRepresentation.PresentableWord;
import sh.tze.gw_swing.UI.Backend.DataRepresentation.PresentableWordSequence;
import sh.tze.gw_swing.UI.Backend.DataRepresentation.Word;
import sh.tze.gw_swing.UI.MainWindowView;

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
    private final List<NLPProcessing> hist = new ArrayList<>();
    private NLPProcessing current;

    public MainWindowBackend(MainWindowView view) {
        mwView = view;
        this.corpus = "";
        this.isCorpusNewlyInstalled = false;
        setup();
    }

    public void setup(){
        addPropertyChangeListener("corpus", evt -> {
            onCorpusChange();
        });
    }

    private void onCorpusChange(){
        //throwing out from the processing class is a bit annoying
        try{
            nlpres = new NLPProcessing(getCorpus());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        doConversion();
        present();
        //.setText(corpus) so it doesn't consume the flag. Sounds like working on tensor.data to avoid recording grad_fn
    }

    private void doConversion(){
        hist.add(current);
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

    private void present(){
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        for(int i = 0; i < nlpres.getWordSentences().size(); i++){
            StringBuilder l1 = new StringBuilder();
            StringBuilder l2 = new StringBuilder();
            StringBuilder l3 = new StringBuilder();
            for(int j = 0; j < nlpres.getWordSentences().get(i).size(); j++){
                var w = nlpres.getWordSentences().get(i).get(j);
                l1.append(w.getForm() + " ");
                l2.append(w.getLemma() + " ");
                l3.append(w.getPos());
            }
            sb.append(l1);
            sb.append("<br>");
            sb.append(l2);
            sb.append("<br>");
            sb.append(l3);
            sb.append("<br>");
            sb.append("<hr>");
        }
        sb.append("</html>");
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
    private static final String LocalPolicy = "^(/|(/([a-zA-Z0-9_-]+))+$)";

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




}
