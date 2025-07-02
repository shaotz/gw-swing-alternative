package sh.tze.gw_swing.design.Handler;

import sh.tze.gw_swing.design.SuggestionAdapter.Provider;

import javax.swing.text.JTextComponent;
import java.util.List;
public class Suggestion {
    public static class HistoryUpdateHandler {
        private Provider.TextHistorySuggestionProvider hp;
        private static final String HISTORY_PROVIDER_KEY = "TextHistorySuggestionProvider";
        public static void recordOn(JTextComponent textField) {
            Provider.TextHistorySuggestionProvider provider =
                    (Provider.TextHistorySuggestionProvider) textField.getClientProperty(HISTORY_PROVIDER_KEY);

            if (provider != null) {
                String text = textField.getText().trim();
                if (!text.isEmpty()) {
                    provider.addToHistory(text);
                }
            }
        }
    }
    public static java.util.List<String> getSpaceSplitSuggestions(String input){
        if (input.isEmpty()) {
            return null;
        }
        return List.of(input.split(" "));
    }
}
