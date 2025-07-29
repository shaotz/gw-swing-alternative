package sh.tze.gw_swing.UI.SuggestionAdapter;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Provider {
    public interface SuggestionProvider<C extends JComponent> {
        Point derivePopupLocation(C on);
        void updateWithSelectedSuggestion(C on, String selectedSuggestion);
        java.util.List<String> retrieveSuggestions(C on);
    }


    public static class TextSuggestionProvider implements SuggestionProvider<JTextComponent>{
        private final Function<String, java.util.List<String>> advisor;

        // enum for horizontal alignment
        public enum POPUP_ALIGNMENT {
            POPUP_ALIGNMENT_BORDER, // aligns with left border
            POPUP_ALIGNMENT_CURSOR //TODO aligns with cursor
        }
        public TextSuggestionProvider(Function<String, java.util.List<String>> advisor){
            this.advisor = advisor;
        }

        public Point derivePopupLocation(JTextComponent on, POPUP_ALIGNMENT alignment){
            if(alignment.equals(POPUP_ALIGNMENT.POPUP_ALIGNMENT_BORDER)) {
                Point p = on.getLocationOnScreen();                     //  (X=0,Y=0) --X-->
                Dimension d = on.getSize();                             //   Y
                p.setLocation(p.getX(),p.getY() + d.getHeight());   //   vvv
                return p;                                               //
            }
            return null;
        }

        //deprecated: incomplete, was used to calculate coordinate offset
        public Point derivePopupLocation(JTextComponent on){
            Container c = on;
            int xoff = 0;
            int yoff = 0;
            while (!c.getParent().getName().equals("RootPanel")) {
                Point p = c.getLocationOnScreen();
                c = c.getParent();
            }
            return new Point(on.getWidth(),on.getHeight()); // (0,0)=TopLef

        }
        public void updateWithSelectedSuggestion(JTextComponent on, String selectedSuggestion){
            on.setText(selectedSuggestion);
        }
        public java.util.List<String> retrieveSuggestions(JTextComponent on){
            return advisor.apply(on.getText().trim()); // if it's guarded, it IS unwanted
        }
    }


    public static class TextHistorySuggestionProvider extends TextSuggestionProvider {
        private final java.util.List<String> history;
        private final int maxHistorySize;

        public TextHistorySuggestionProvider() {
            super(null); // No need for advisor function
            this.history = new ArrayList<>();
            this.maxHistorySize = 50; // Configurable limit
        }

        public TextHistorySuggestionProvider(int maxHistorySize) {
            super(null);
            this.history = new ArrayList<>();
            this.maxHistorySize = maxHistorySize;
        }

        @Override
        public java.util.List<String> retrieveSuggestions(JTextComponent on) {
            return filterHistory(on.getText().trim());
        }

        // Fixed filtering logic
        public java.util.List<String> filterHistory(String input) {
            if (input == null || input.isEmpty()) {
                return new ArrayList<>(history);
            }

            java.util.List<String> startsWith = new ArrayList<>();
            java.util.List<String> contains = new ArrayList<>();

            for (String item : history) {
                if (item.startsWith(input)) {
                    startsWith.add(item);
                } else if (item.contains(input)) {
                    contains.add(item);
                }
            }

            // Priority: \b$PATTERN, then $PATTERN
            startsWith.addAll(contains);
            return startsWith;
        }

        // Add method to update history
        public void addToHistory(String entry) {
            if (entry == null || entry.trim().isEmpty()) return;

            entry = entry.trim();

            // Remove if already exists (move to front)
            history.remove(entry);

            // Add to front
            history.add(0, entry);

            // Maintain size limit
            while (history.size() > maxHistorySize) {
                history.remove(history.size() - 1);
            }
        }

        public List<String> getHistory() {
            return new ArrayList<>(history);
        }

        public void clearHistory() {
            history.clear();
        }
    }
}
