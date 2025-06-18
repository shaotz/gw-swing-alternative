package sh.tze.gw_swing.design;

import javax.swing.*;
import java.awt.*;

public class MainWindowWidgets {
    public static JPanel initMainWindowPanel() {
        JPanel MWPanel = new JPanel(new BorderLayout());

        JPanel MWPanelSectionA = initMWPanelSectA();
        JPanel MWPanelSectionB = initMWPanelSectB();

        MWPanel.add(MWPanelSectionA,BorderLayout.NORTH);
        MWPanel.add(MWPanelSectionB,BorderLayout.CENTER);

        return MWPanel;
    }

    //<editor-fold desc="Flattened tree definition Side L: Section Above(A)">
    public static JPanel initMWPanelSectA(){
        JPanel sect = new JPanel(new GridLayout(3,1));

//        GridLayout gl = new GridLayout(3,1);

        sect.add(initURLSelectorPanel());
        return sect;
    }

    public static JPanel initURLSelectorPanel(){
        JPanel container = new JPanel(new BorderLayout());

        // Def Enclosed Items
        JTextField urlTextField = new JTextField(10);
        JButton urlActionButton = new JButton("Open");

        JRadioButton urlDestSelLo = new JRadioButton("Local");
        JRadioButton urlDestSelRe = new JRadioButton("Remote");
        ButtonGroup urlDestSelGroup = new ButtonGroup();
        urlDestSelGroup.add(urlDestSelLo);
        urlDestSelGroup.add(urlDestSelRe);
        JPanel urlDestSelContainer = new JPanel(new GridLayout(1,2));
        urlDestSelContainer.add(urlDestSelLo);
        urlDestSelContainer.add(urlDestSelRe);


        // Registering enclosed items
        container.add(urlDestSelContainer,BorderLayout.WEST);
        container.add(urlTextField,BorderLayout.CENTER);
        container.add(urlActionButton,BorderLayout.EAST);
        // applying BorderLayout on atom items to instruct positioning

        return container;
    }

    //</editor-fold>


    //<editor-fold desc="Flattened tree definition Side R: Section Below(B)">
    public static JPanel initMWPanelSectB() {
        JPanel sect = new JPanel(new BorderLayout());

        sect.add(initPrimaryTextDisplay(), BorderLayout.CENTER);
        sect.add(initFunctionControlPanel(), BorderLayout.EAST);
        return sect;
    }

    public static JPanel initPrimaryTextDisplay(){
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea mainTextArea = new JTextArea();
        JScrollPane encapsulator = new JScrollPane(mainTextArea);
        mainTextArea.setLineWrap(true);
        mainTextArea.setWrapStyleWord(true);

        panel.add(encapsulator,BorderLayout.CENTER);
        return panel;
    }

    public static JPanel initFunctionControlPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(); // Iseri Nina wrote this

//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton filterActionButton = new JButton("Filter");
        JButton filterResetButton = new JButton("Reset");
        JButton masterSaveButton = new JButton("Save");

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0; // Don't expand the button panel
        buttonPanel.add(filterActionButton);
        buttonPanel.add(filterResetButton);

        // child widgets/widget-groups
        JPanel filterPanel = initFilterSection();
        JPanel infoPanel = new JPanel();

        // add components with optional spacing
        panel.add(filterPanel);
//        panel.add(Box.createVerticalStrut(5));
        panel.add(buttonPanel, gbc);
//        panel.add(Box.createVerticalStrut(5));
        panel.add(infoPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(masterSaveButton);

        return panel;
    }

    public static JPanel initFilterSection() {
        JPanel section = new JPanel(new BorderLayout());

        JPanel filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));

        WidgetUtilities.FilterWidgetGroup searchFilter = new WidgetUtilities.FilterWidgetGroup("Search", "Enter search terms...");
        WidgetUtilities.FilterWidgetGroup categoryFilter = new WidgetUtilities.FilterWidgetGroup("Category", "Select category...");

        filtersPanel.add(searchFilter);
        filtersPanel.add(categoryFilter);

        section.add(filtersPanel, BorderLayout.NORTH);
        return section;
    }

    //</editor-fold>

}
