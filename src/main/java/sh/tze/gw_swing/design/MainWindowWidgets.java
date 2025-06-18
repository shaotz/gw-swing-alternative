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
    private static JPanel initMWPanelSectA(){
        JPanel sect = new JPanel(new GridLayout(3,1));

//        GridLayout gl = new GridLayout(3,1);

        sect.add(initURLSelectorPanel());
        return sect;
    }

    private static JPanel initURLSelectorPanel(){
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
    private static JPanel initMWPanelSectB() {
        JPanel sect = new JPanel(new BorderLayout());

        sect.add(initPrimaryTextDisplay(), BorderLayout.CENTER);
        sect.add(initFunctionControlPanel(), BorderLayout.EAST);
        return sect;
    }

    private static JPanel initPrimaryTextDisplay(){
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea mainTextArea = new JTextArea();
        JScrollPane encapsulator = new JScrollPane(mainTextArea);
        mainTextArea.setLineWrap(true);
        mainTextArea.setWrapStyleWord(true);

        panel.add(encapsulator,BorderLayout.CENTER);
        return panel;
    }

    private static JPanel initFunctionControlPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(); // Iseri Nina wrote this

//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton filterActionButton = new JButton("Filter");
        JButton filterResetButton = new JButton("Reset");
        JButton masterSaveButton = new JButton("Save");

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0; // not allowing buttons to v-expand
        buttonPanel.add(filterActionButton);
        buttonPanel.add(filterResetButton);

        // child widgets/widget-groups
        JPanel filterPanel = initFilterSection();
        JPanel infoPanel = initInfoPanel();

        // Starting adding components
        panel.add(buttonPanel, gbc);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0; // Don't expand vertically
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(filterPanel,gbc); //omitting constraints is not okay now
        // Info panel
        gbc.gridx = 0;
        gbc.gridy = 2; // 3rd row
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; // Takes extra vertical space
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(infoPanel, gbc);
        // Master Save Button
        gbc.gridx = 0;
        gbc.gridy = 3; // 4th row
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(masterSaveButton, gbc);

        return panel;
    }

    private static JPanel initFilterSection() {
        JPanel section = new JPanel(); // removed a confusing declaration
        JPanel keywordFilterPanel = new JPanel();
        JPanel adjustmentPanel = new JPanel(); //TODO

        keywordFilterPanel.setLayout(new BoxLayout(keywordFilterPanel, BoxLayout.Y_AXIS));

        WidgetUtilities.FilterWidgetGroup fulltextFilter = new WidgetUtilities.FilterWidgetGroup("Search", "Search in full text...");
        WidgetUtilities.FilterWidgetGroup posFilter = new WidgetUtilities.FilterWidgetGroup("POS Tag", "Filter for POS tags...");
        WidgetUtilities.FilterWidgetGroup lemmaFilter = new WidgetUtilities.FilterWidgetGroup("Lemma", "Filter for lemma...");

        keywordFilterPanel.add(fulltextFilter);
        keywordFilterPanel.add(posFilter);
        keywordFilterPanel.add(lemmaFilter);

        section.add(keywordFilterPanel); // also fixed correspondingly
        return section;
    }

    private static JPanel initInfoPanel(){
        JPanel infoPanel = new JPanel();

        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        String[] loremipsum = {"This is a placeholder header", "Lorem ipsum dolor sit amet, ...", ""};
        JScrollPane listSP1 = new JScrollPane(new WidgetUtilities.TextListDisplay<>(loremipsum));
        JScrollPane listSP2 = new JScrollPane(new WidgetUtilities.TextListDisplay<>(loremipsum.clone()));
        infoPanel.add(listSP1);
        infoPanel.add(listSP2);

        return infoPanel;
    }

    //</editor-fold>

}
