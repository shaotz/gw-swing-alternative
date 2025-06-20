package sh.tze.gw_swing.design;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.function.Function;
import java.util.List;

public class SuggestionAdapter {
    public interface SuggestionProvider<C extends JComponent> {
//        Point derivePopupLocation(C on);
        void updateWithSelectedSuggestion(C on, String selectedSuggestion);
        List<String> retrieveSuggestions(C on);
    }


    public static class TextSuggestionProvider implements SuggestionProvider<JTextComponent>{
        private final Function<String,List<String>> advisor;

        // enum for horizontal alignment
        public enum POPUP_ALIGNMENT {
            POPUP_ALIGNMENT_BORDER, // aligns with left border
            POPUP_ALIGNMENT_CARET //TODO aligns with caret
        }
        public TextSuggestionProvider(Function<String,List<String>> advisor){
            this.advisor = advisor;
        }

        public Point derivePopupLocation(JTextComponent on, POPUP_ALIGNMENT alignment){
            if(alignment.equals(POPUP_ALIGNMENT.POPUP_ALIGNMENT_BORDER)) {
                Point p = on.getLocationOnScreen();
                Dimension d = on.getSize();
                p.setLocation(p.getX(),p.getY() + d.getHeight());
                return p;
            } // (0,0)=TopLef
            return null;
        }

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
        public List<String> retrieveSuggestions(JTextComponent on){
            return advisor.apply(on.getText().trim()); // if it's guarded, it IS unwanted
        }
    }
    public static java.util.List<String> getSuggestions(String input){
        if (input.isEmpty()) {
            return null;
        }
        return List.of(input.split(" "));
    }
}
