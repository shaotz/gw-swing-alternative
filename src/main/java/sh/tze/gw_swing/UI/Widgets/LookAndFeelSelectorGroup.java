package sh.tze.gw_swing.UI.Widgets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class LookAndFeelSelectorGroup extends JPanel {
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
