package sh.tze.gw_swing.UI.SuggestionAdapter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class Decorator {
    /**
     *
     * Call to doDecorateOn(...) -> doDecorateOn(...) initializes an instance of TextSuggestionDecorator
     * TSD: contains PopupMenu itself, contains the information to display
     * PopupMenu: should be updated per change in the JTextComponent it hooks to:
     *      1. JTextComponent sends a documentEvent
     *      2. The suggestion result is fetched(generated) accordingly
     *      3. PopupMenu re-configure itself to accommodate the updated information
     * @param <C> Object type of the object to decorate, should be an inherited type ofJTextComponent
     */

    // C for Component; This is Decorator in Decorator/SuggestionAdapter/SuggestionProvider
    public static class TextSuggestionDecorator<C extends JTextComponent> {
        private final C invoker;
        private JPopupMenu popupMenu;
        Provider.TextSuggestionProvider suggestionProvider;

        private DefaultListModel<String> candidateListModel;
        // volatile buffer & flag
        private JList<String> candidateList;
        private boolean inhibitTextEvent;
        // (almost) static resources
        private static final int MAX_TEXT_WIDHTH = 20;
        private final LayoutManager layoutManager = new BorderLayout();
        private static final String TEXT_HISTORY_SUGGESTION_PROVIDER_KEY = "TextHistorySuggestionProvider";

        // listener pool
        private ArrayList<ActionListener> selectionListeners = new ArrayList<>();
        private ArrayList<DocumentListener> textChangeListeners = new ArrayList<>();

        public TextSuggestionDecorator(C invoker,
                                       Provider.TextSuggestionProvider suggestionProvider) {
            this.invoker = invoker;
            this.suggestionProvider = suggestionProvider;
        }

        public static <C extends JTextComponent> void doDecorationOn(C on,
                                                                     Provider.TextSuggestionProvider sp){
            TextSuggestionDecorator<C> decorator = new TextSuggestionDecorator<C>(on,sp);
            decorator.init();
        }

        private void init(){
            // to do are: init ui components, register listeners, hook arrow up/down key
            popupMenu = new JPopupMenu();
            popupMenu.setVisible(false);
            popupMenu.setBorder(BorderFactory.createEtchedBorder());
            popupMenu.setBorderPainted(true);
            candidateListModel = new DefaultListModel<>();
            candidateList = new JList<>(candidateListModel);
            popupMenu.add(candidateList);
            inhibitTextEvent = false;
            initListener();
            initHooks();

            if(suggestionProvider instanceof Provider.TextHistorySuggestionProvider){
                invoker.putClientProperty(TEXT_HISTORY_SUGGESTION_PROVIDER_KEY, suggestionProvider);
            }
        }

        private void initListener(){
            invoker.getDocument().addDocumentListener(new PopupDocumentListener());
        }
        // The focus should be retained on the decorated object,
        // and the suggestions should only be accessible with keyboard inputs(Up/Down, Enter/Esc)
        // (mouse clicks should be accepted as well but needs some research)
        // this means to create key listener on the decorated object
        private void initHooks(){

            invoker.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_ENTER:
                            if(!popupMenu.isShowing())break;
                            confirmSelection();
                            e.consume();
                            break;
                        case KeyEvent.VK_ESCAPE:
                            popupMenu.setVisible(false);
                            e.consume();
                            break;
                        // fallthrough to accepts Keyboard&Keypad arrow keys
                        case KeyEvent.VK_LEFT: case KeyEvent.VK_KP_LEFT: case KeyEvent.VK_RIGHT: case KeyEvent.VK_KP_RIGHT:
                            // in the case of left/right arrow, the caret was repositioned,
                            // and it is arguable whether to re-generate suggestions on the partial input
                            popupMenu.setVisible(true);
                            e.consume();
                            break;
                        case KeyEvent.VK_UP: case KeyEvent.VK_KP_UP:
                            if (candidateList.getSelectedIndex() - 1 >= 0)
                                // targetIndex mod size to prevent overflow: thanks autocompletion, you've finally provided something insightful
                                candidateList.setSelectedIndex(candidateList.getSelectedIndex() - 1 % candidateList.getModel().getSize());
                            e.consume();
                            break;
                        case KeyEvent.VK_DOWN: case KeyEvent.VK_KP_DOWN:
                            if(candidateList.getSelectedIndex() + 1 < candidateListModel.getSize())
                                candidateList.setSelectedIndex((candidateList.getSelectedIndex() + 1) % candidateListModel.getSize());
                            e.consume();
                            break;
                    }

                }
            });

            invoker.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    popupMenu.setVisible(false);
                }
            });
        }

        private class PopupDocumentListener implements DocumentListener{
            @Override
            public void insertUpdate(DocumentEvent e) {
                doCustomUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doCustomUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                doCustomUpdate(e);
            }

            private void doCustomUpdate(DocumentEvent e) {
                if(inhibitTextEvent) return;
                List<String> suggestions = suggestionProvider.retrieveSuggestions(invoker);
                // The non-null test based on performed on the generated suggestions
                // but can be later refactored to a "No suggestion" indicating typo,
                //  since this already infer no match
                if(suggestions != null && !suggestions.isEmpty()){
                    presentPopup(suggestions);
                }else{
                    popupMenu.setVisible(false); //nothing to display here, then just don't display anything
                }
            }
        }

        private void presentPopup(List<String> toDisplay){
            // ListModel<String> is the underlying container to operate on
            candidateListModel.clear(); // 1. clear the buffer
            for(String s : toDisplay){
                candidateListModel.addElement(s); // 2. (re)creates the buffer
            }
            // 3. reset states of the Popup Menu
//            popupMenu.setLocation(invoker.getLocationOnScreen());
            popupMenu.setLocation(suggestionProvider.derivePopupLocation(invoker,
                    Provider.TextSuggestionProvider.POPUP_ALIGNMENT.POPUP_ALIGNMENT_BORDER));
            candidateList.setSelectedIndex(0);
            // 4. present it
            popupMenu.pack(); //resize it to optimal
            popupMenu.setVisible(true);
        }

        private void confirmSelection(){
            popupMenu.setVisible(false); // 1. off you go
            if(candidateList.getSelectedIndex() != -1){
                inhibitTextEvent = true;
                String sel = candidateList.getSelectedValue();
                invoker.setText(sel);
                inhibitTextEvent = false;

                if (suggestionProvider instanceof Provider.TextHistorySuggestionProvider) {
                    ((Provider.TextHistorySuggestionProvider) suggestionProvider).addToHistory(sel);
                }
            }
        }

    }


}
