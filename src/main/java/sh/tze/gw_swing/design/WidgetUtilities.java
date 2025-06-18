package sh.tze.gw_swing.design;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class WidgetUtilities {
    public static class FilterWidgetGroup extends JPanel {
        private JCheckBox filterCheckBox;
        private JTextField filterTextField;
        private String placeholder;

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
         * @param checkboxText
         * @param placeholderText
         * @param textFieldColumns
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
                    UIManager.LookAndFeelInfo info = (UIManager.LookAndFeelInfo) value;
                    setText(info.getName());
                }
                return this;
            }
        }
    }

}
