package sh.tze.gw_swing.UI.Widgets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class FilterWidgetGroup extends JPanel {
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
