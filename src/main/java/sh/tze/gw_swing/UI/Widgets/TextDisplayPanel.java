package sh.tze.gw_swing.UI.Widgets;

import sh.tze.gw_swing.UI.Backend.DataRepresentation.PresentableWord;

import javax.swing.*;
import java.awt.*;
import java.util.List;
public class TextDisplayPanel extends JPanel {
    private JEditorPane editorPane;
    private JScrollPane scrollPane;

    public TextDisplayPanel() {
        setLayout(new BorderLayout());
        editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        scrollPane = new JScrollPane(editorPane);
        add(scrollPane, BorderLayout.CENTER);
    }


    public void setTextFromWord(List<PresentableWord> words) {
        StringBuilder html = new StringBuilder("<html><body>");
        for (PresentableWord word : words) {
            html.append(word.renderWord()).append(" ");
        }
        html.append("</body></html>");
        editorPane.setText(html.toString());
    }

    public void setText(String text) { editorPane.setText(text);}
    public void clearDisplay(){
        editorPane.setText("");
    }
}

