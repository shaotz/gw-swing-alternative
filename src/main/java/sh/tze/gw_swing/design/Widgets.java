package sh.tze.gw_swing.design;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
// to prevent awt.* from masking util.List with awt.List
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;

public class Widgets {
    public static class FilterWidgetGroup extends JPanel {
        private JCheckBox filterCheckBox;
        private JTextField filterTextField;
        private final String placeholder;

        private static final int DEFAULT_TEXT_FIELD_WIDTH = 15;
        //Color Scheme
        private final double placeholderOpacity = 0.75;
        private final double textOpacity = 1.0;
        private final Color placeholderColor = Color.BLACK;
        private final Color textColor = Color.BLACK;

        private final LayoutManager layoutManager = new FlowLayout(FlowLayout.LEFT, 5, 0);
        //runtime flag
        private boolean isPlaceholderVisible = true;

        //Action handlers storage
        private ArrayList<ActionListener> checkboxListeners = new ArrayList<>();

        /**
         * Creates a FilterWidgetGroup object with a checkbox
         * @param checkboxText The checkbox's display name
         * @param placeholderText The textfield's placeholder text
         * @param textFieldColumns The textfield's display width, in characters.
         */
        public FilterWidgetGroup(String checkboxText, String placeholderText, int textFieldColumns) {
            this.placeholder = placeholderText;
            initializeComponents(checkboxText, textFieldColumns);
            registerComponents();
            setLayout(layoutManager);
            setupHandlers();
            initState();
        }

        public FilterWidgetGroup(String checkboxText, String placeholderText) {
            this(checkboxText, placeholderText, DEFAULT_TEXT_FIELD_WIDTH);
        }

        private void initializeComponents(String checkboxText, int textFieldColumns) {
            filterCheckBox = new JCheckBox(checkboxText);
            filterTextField = new JTextField(textFieldColumns);


            filterTextField.setText(placeholder);
            filterTextField.setForeground(placeholderColor);
            // make italic to indicate hint message
            filterTextField.setFont(filterTextField.getFont().deriveFont(Font.ITALIC));
        }

        private void registerComponents() {
            add(filterCheckBox);
            add(filterTextField);
        }

        private void setupHandlers() {
            // Checkbox event handling
            filterCheckBox.addActionListener(e -> {
                boolean isSelected = filterCheckBox.isSelected();
                filterTextField.setEnabled(isSelected);

                // Notify registered listeners
                ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "checkbox_changed");
                for (ActionListener listener : checkboxListeners) {
                    listener.actionPerformed(event);
                }
            });

            // Text field placeholder handling
            filterTextField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (isPlaceholderVisible) {
                        filterTextField.setText("");
                        filterTextField.setForeground(textColor);
                        filterTextField.setFont(filterTextField.getFont().deriveFont(Font.PLAIN));
                        isPlaceholderVisible = false;
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (filterTextField.getText().trim().isEmpty()) {
                        showPlaceholder();
                    }
                }
            });

            // Handle placeholder when typing
            filterTextField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (isPlaceholderVisible) {
                        filterTextField.setText("");
                        filterTextField.setForeground(textColor);
                        filterTextField.setFont(filterTextField.getFont().deriveFont(Font.PLAIN));
                        isPlaceholderVisible = false;
                    }
                }
            });
        }

        private void initState() {
            filterTextField.setEnabled(false);
            isPlaceholderVisible = true;
        }
        private void showPlaceholder() {
            filterTextField.setText(placeholder); // clears last input
            filterTextField.setForeground(placeholderColor);
            filterTextField.setFont(filterTextField.getFont().deriveFont(Font.ITALIC));
            isPlaceholderVisible = true;
        }

        //<editor-fold desc="API Definitions of FilterWidgetGroups">
        public String getFilterText() {
            if (isPlaceholderVisible) {
                return "";
            }
            return filterTextField.getText().trim();
        }

        // maybe useful when an invalid input was keyed in
        public void setFilterText(String text) {
            if (text == null || text.trim().isEmpty()) {
                showPlaceholder();
            } else {
                filterTextField.setText(text);
                filterTextField.setForeground(textColor);
                filterTextField.setFont(filterTextField.getFont().deriveFont(Font.PLAIN));
                isPlaceholderVisible = false;
            }
        }

        public boolean isFilterActive() {
            return filterCheckBox.isSelected();
        }

        public void addCheckboxActionListener(ActionListener listener) {
            checkboxListeners.add(listener);
        }

        public void removeCheckboxActionListener(ActionListener listener) {
            checkboxListeners.remove(listener);
        }

        public void addTextFieldDocumentListener(javax.swing.event.DocumentListener listener) {
            filterTextField.getDocument().addDocumentListener(listener);
        }

        public JCheckBox getCheckBox() {
            return filterCheckBox;
        }
        public JTextField getTextField() {
            return filterTextField;
        }

        /**
         * Clears the filter text and resets to placeholder state
         */
        public void clearFilter() {
            showPlaceholder();
        }
        //</editor-fold>


    }

    public static class LookAndFeelSelectorGroup extends JPanel {
        private JLabel lookAndFeelLabel;
        private JComboBox<UIManager.LookAndFeelInfo> lookAndFeelComboBox;
        private JFrame parentFrame;

        private static final String DEFAULT_LABEL_TEXT = "Look & Feel: ";

        // Layout configuration
        private final LayoutManager layoutManager = new FlowLayout(FlowLayout.LEFT, 5, 0);

        // Action handlers storage
        private ArrayList<ActionListener> changeListeners = new ArrayList<>();

        /**
         * Creates a LookAndFeelSelectorGroup with custom label and parent frame reference
         * @param labelText the label text to display
         * @param parentFrame the parent frame to update when L&F changes
         */
        public LookAndFeelSelectorGroup(String labelText, JFrame parentFrame) {
            this.parentFrame = parentFrame;
            initializeComponents(labelText);
            registerComponents();
            setLayout(layoutManager);
            setupHandlers();
            initState();
        }

        /**
         * Creates a LookAndFeelSelectorGroup with default label text
         * @param parentFrame the parent frame to update when L&F changes
         */
        public LookAndFeelSelectorGroup(JFrame parentFrame) {
            this(DEFAULT_LABEL_TEXT, parentFrame);
        }

        private void initializeComponents(String labelText) {
            lookAndFeelLabel = new JLabel(labelText);

            // obtain all installed look and feels from UIManager
            UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
            lookAndFeelComboBox = new JComboBox<>(lookAndFeels);
            lookAndFeelComboBox.setRenderer(new LookAndFeelRenderer());
        }

        private void registerComponents() {
            add(lookAndFeelLabel);
            add(lookAndFeelComboBox);
        }

        private void setupHandlers() {
            // event handling on change of l&f
            lookAndFeelComboBox.addActionListener(e -> {
                UIManager.LookAndFeelInfo selectedLAF =
                        (UIManager.LookAndFeelInfo) lookAndFeelComboBox.getSelectedItem();

                if (selectedLAF != null) {
                    changeLookAndFeel(selectedLAF.getClassName());

                    // Notify registered listeners
                    ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                            "lookandfeel_changed:" + selectedLAF.getName());
                    for (ActionListener listener : changeListeners) {
                        listener.actionPerformed(event);
                    }
                }
            });
        }

        private void initState() {
            // Set current l&f as selected
            String currentLAF = UIManager.getLookAndFeel().getClass().getName();
            UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();

            for (UIManager.LookAndFeelInfo info : lookAndFeels) {
                if (info.getClassName().equals(currentLAF)) {
                    lookAndFeelComboBox.setSelectedItem(info);
                    break;
                }
            }
        }

        private void changeLookAndFeel(String className) {
            try {
                // Set the new L&F
                UIManager.setLookAndFeel(className);

                // Update parent frame if there is, in our case it's always applied to root frame
                if (parentFrame != null) {
                    SwingUtilities.updateComponentTreeUI(parentFrame);
                    parentFrame.pack();
                }

                System.out.println("Look and Feel changed to: " + className);

            } catch (Exception ex) {
                if (parentFrame != null) {
                    JOptionPane.showMessageDialog(parentFrame,
                            "Error changing Look and Feel: " + ex.getMessage(),
                            "Look and Feel Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                ex.printStackTrace();
            }
        }

        //<editor-fold desc="API Definition for LookAndFeelSelectorGroup">
        // Public API methods below
        public String getCurrentLookAndFeelName() {
            UIManager.LookAndFeelInfo selected =
                    (UIManager.LookAndFeelInfo) lookAndFeelComboBox.getSelectedItem();
            return selected != null ? selected.getName() : "";
        }


        public void setSelectedLookAndFeel(String className) {
            UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
            for (UIManager.LookAndFeelInfo info : lookAndFeels) {
                if (info.getClassName().equals(className)) {
                    lookAndFeelComboBox.setSelectedItem(info);
                    break;
                }
            }
        }

        public void addChangeListener(ActionListener listener) {
            changeListeners.add(listener);
        }

        public void removeChangeListener(ActionListener listener) {
            changeListeners.remove(listener);
        }

        public JComboBox<UIManager.LookAndFeelInfo> getComboBox() {
            return lookAndFeelComboBox;
        }

        public JLabel getLabel() {
            return lookAndFeelLabel;
        }

        /**
         * Updates the UI components when the look and feel changes externally
         */
        public void refreshUI() {
            SwingUtilities.updateComponentTreeUI(this);
            initState(); // sort of like "confirms" the selection
        }



        /**
         *  ComboBox rendering L&F names would be terrible if stays with default
         *  It overrides the default renderer, and hooks the function to text rendering,
         *  and essentially trim the text
         */
        private static class LookAndFeelRenderer extends DefaultListCellRenderer {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof UIManager.LookAndFeelInfo) {
                    // stays the old-fashion way, pattern var is only supported 14+
                    //noinspection PatternVariableCanBeUsed
                    UIManager.LookAndFeelInfo info = (UIManager.LookAndFeelInfo) value;
                    setText(info.getName());
                }
                return this;
            }
        }
        //</editor-fold>
    }

    // E for Element (Type)
    public static class TextListDisplay<E> extends JList {
        // Default styling constants
        private static final Color HEADER_BACKGROUND = Color.LIGHT_GRAY;
        private static final Color HEADER_FOREGROUND = Color.DARK_GRAY;
        private static final int HEADER_STYLE = Font.BOLD;

        /**
         * Creates an empty TextListDisplay with protected header functionality
         */
        public TextListDisplay() {
            super();
            initializeComponent();
        }

        /**
         * Creates a TextListDisplay with legacy array data
         * @param listData array of data items to display
         */
        public TextListDisplay(E[] listData) {
            super(listData);
            initializeComponent();
        }

        /**
         * Creates a TextListDisplay with Vector data
         * @param listData Vector of data items to display
         */
        public TextListDisplay(Vector<? extends E> listData) {
            super(listData);
            initializeComponent();
        }

        /**
         * Creates a TextListDisplay with custom ListModel
         * @param dataModel the ListModel to use
         */
        public TextListDisplay(ListModel<E> dataModel) {
            super(dataModel);
            initializeComponent();
        }


        /**
         * Custom SelectionModel to hook api calls, preventing selection of the 1st entry(header)
         */
        private static class HeaderProtectedSelectionModel extends DefaultListSelectionModel {

            @Override
            public void setSelectionInterval(int index0, int index1) {
                // "Selection Interval": the list of selected items denoted with start and end
                if (index0 == 0 || index1 == 0) {
                    return; // ignore user input/api call involving index 0
                }
                super.setSelectionInterval(index0, index1);
            }

            @Override
            public void addSelectionInterval(int index0, int index1) {
                // Prevent adding selection to index 0
                if (index0 == 0 || index1 == 0) {
                    return;
                }
                super.addSelectionInterval(index0, index1);
            }

            @Override
            public void removeSelectionInterval(int index0, int index1) {
                // Allow removal but skip header row
                if (index0 == 0) index0 = 1;
                if (index1 == 0) return;
                if (index0 <= index1) {
                    super.removeSelectionInterval(index0, index1);
                }
            }
        }

        private void initializeComponent() {
            setSelectionModel(new HeaderProtectedSelectionModel());

            setCellRenderer(new HeaderAwareRenderer());

            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            setEnabled(true);
        }

        /**
         * Custom cell renderer to style the header row differently
         */
        private static class HeaderAwareRenderer extends DefaultListCellRenderer {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                // Apply custom style for header
                if (index == 0) {
                    setFont(getFont().deriveFont(HEADER_STYLE));
                    setBackground(HEADER_BACKGROUND);
                    setForeground(HEADER_FOREGROUND);

                    // Create border to distinguish from normal entries
                    setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
                } else {
                    // Reset normal styling for non-header rows
                    setBorder(isSelected ? BorderFactory.createEmptyBorder(2, 4, 2, 4) : null);
                }

                return this;
            }
        }

        //<editor-fold desc="API Definition for TextListDisplay">
        // Public API methods for enhanced functionality

        /**
         * Gets the selected data item (excluding header i.e. 0th elem)
         *
         * @return selected item or null if no selection
         */
        public E getSelectedData() {
            int selectedIndex = getSelectedIndex();
            return (selectedIndex > 0) ? (E) getModel().getElementAt(selectedIndex) : null;
        }

        public E getHeaderText() {
            return (getModel().getSize() > 0) ? (E) getModel().getElementAt(0) : null;
        }

        /**
         * Selects an item by value (excluding header)
         * @param value the value to select
         */
        public void setSelectedData(E value) {
            if (value == null) {
                clearSelection();
                return;
            }

            ListModel<E> model = getModel();
            for (int i = 1; i < model.getSize(); i++) { //skipping header
                if (value.equals(model.getElementAt(i))) {
                    setSelectedIndex(i);
                    return;
                }
            }
        }

        /**
         * Gets the count of selectable items (excluding header)
         * @return count of selectable items
         */
        public int getSelectableItemCount() {
            return Math.max(0, getModel().getSize() - 1);
        }

        //</editor-fold>

    }

    /**
     *
     * Step-in: Call to doDecorateOn(...) -> doDecorateOn(...) initializes an instance of TSD
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
        SuggestionAdapter.TextSuggestionProvider suggestionProvider;

        private DefaultListModel<String> candidateListModel;
        // volatile buffer & flag
        private JList<String> candidateList;
        private boolean inhibitTextEvent;
        // (almost) static resources
        private static final int MAX_TEXT_WIDHTH = 20;
        private final LayoutManager layoutManager = new BorderLayout();

        // listener pool
        private ArrayList<ActionListener> selectionListeners = new ArrayList<>();
        private ArrayList<DocumentListener> textChangeListeners = new ArrayList<>();

        public TextSuggestionDecorator(C invoker,
                                       SuggestionAdapter.TextSuggestionProvider suggestionProvider) {
            this.invoker = invoker;
            this.suggestionProvider = suggestionProvider;
        }

        public static <C extends JTextComponent> void doDecorationOn(C on,
                                                                     SuggestionAdapter.TextSuggestionProvider sp){
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
        }

        private void initListener(){
            invoker.getDocument().addDocumentListener(new PopupDocumentListener());
        }
        // The focus should be retained on the decorated object,
        // and the suggestions should only be accessible with keyboard inputs(Up/Down, Enter/Esc)
        // (mouse access should be accepted as well but needs some research)
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
                            if (candidateList.getSelectedIndex() - 1 > 0)
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
                    SuggestionAdapter.TextSuggestionProvider.POPUP_ALIGNMENT.POPUP_ALIGNMENT_BORDER));
            candidateList.setSelectedIndex(0);
            // 4. present it
            popupMenu.pack(); //resize it to optimal
            popupMenu.setVisible(true);
        }

        private void confirmSelection(){
            popupMenu.setVisible(false); // 1. off you go
            if(candidateList.getSelectedIndex() != -1){
                inhibitTextEvent = true;
                invoker.setText(candidateList.getSelectedValue());
                inhibitTextEvent = false;
            }
        }

    }

    /* End of WidgetUtilities class definition*/
}
