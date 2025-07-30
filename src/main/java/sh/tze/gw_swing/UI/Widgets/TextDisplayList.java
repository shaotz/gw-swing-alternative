package sh.tze.gw_swing.UI.Widgets;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

// E for Element (Type)
public class TextDisplayList<E> extends JList {
    // Default styling constants
    private static final Color HEADER_BACKGROUND = Color.LIGHT_GRAY;
    private static final Color HEADER_FOREGROUND = Color.DARK_GRAY;
    private static final int HEADER_STYLE = Font.BOLD;

    /**
     * Creates an empty TextListDisplay with protected header functionality
     */
    public TextDisplayList() {
        super();
        initializeComponent();

    }

    /**
     * Creates a TextListDisplay with legacy array data
     * @param listData array of data items to display
     */
    public TextDisplayList(E[] listData) {
        super(listData);
        initializeComponent();
    }

    /**
     * Creates a TextListDisplay with Vector data
     * @param listData Vector of data items to display
     */
    public TextDisplayList(Vector<? extends E> listData) {
        super(listData);
        initializeComponent();
    }

    /**
     * Creates a TextListDisplay with custom ListModel
     * @param dataModel the ListModel to use
     */
    public TextDisplayList(ListModel<E> dataModel) {
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
