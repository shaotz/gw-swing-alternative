package sh.tze.gw_swing.design;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainWindowView {
    public static JPanel initMainWindowPanel() {
        JPanel MWPanel = new JPanel(new BorderLayout());
        MWPanel.setName("RootPanel");
        JPanel MWPanelSectionA = initMWPanelSectA();
        JPanel MWPanelSectionB = initMWPanelSectB();

        MWPanel.add(MWPanelSectionA,BorderLayout.NORTH);
        MWPanel.add(MWPanelSectionB,BorderLayout.CENTER);

        return MWPanel;
    }

    //<editor-fold desc="Flattened tree definition Side L: Section Above(A)">
    private static JPanel initMWPanelSectA(){

//      JPanel sect = new JPanel(new GridLayout(2,3));
        Color color = new Color(0xF6, 0xC9, 0xCC);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(color);

        JRadioButton selectCaseButton = new JRadioButton("Case Sensitive");
        JRadioButton wholeSentenceButton = new JRadioButton("Whole Sentence");
        JRadioButton numOfNeighbors = new JRadioButton("# of Neighbor Words");

        JPanel neighborPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        neighborPanel.setBackground(color);
        // Left input
        JLabel leftNum = new JLabel("Left:");
        JTextField leftTextField = new JTextField(5); // 5 columns width

        // Right input
        JLabel rightNum = new JLabel("Right:");
        JTextField rightTextField = new JTextField(5); // 5 columns width

        // Add components to neighbor panel
        neighborPanel.add(numOfNeighbors);
        neighborPanel.add(Box.createHorizontalStrut(5)); // Add spacing
        neighborPanel.add(leftNum);
        neighborPanel.add(leftTextField);
        neighborPanel.add(Box.createHorizontalStrut(5)); // Add spacing
        neighborPanel.add(rightNum);
        neighborPanel.add(rightTextField);

//        GridLayout gl = new GridLayout(1,1);
//        panel for the center components
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        centerPanel.add(wholeSentenceButton);
        centerPanel.add(numOfNeighbors);
        centerPanel.add(neighborPanel);

        container.add(initURLSelectorPanel(), BorderLayout.NORTH);
        container.add(selectCaseButton, BorderLayout.WEST);
        container.add(centerPanel, BorderLayout.CENTER);

        return container;
    }

    private static JPanel initURLSelectorPanel(){
        JPanel container = new JPanel(new BorderLayout());

        // Def Enclosed Items
        JTextField urlTextField = new JTextField(10);
        JButton urlActionButton = new JButton("Open");

        JRadioButton urlDestSelLo = new JRadioButton("Local File");
        JRadioButton urlDestSelRe = new JRadioButton("Wiki URL");
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
        // Will just operate on constraint buffer `gbc`
        GridBagConstraints gbc = new GridBagConstraints(); // Iseri Nina wrote this LOL

//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton filterActionButton = new JButton("Filter");
        JButton filterResetButton = new JButton("Reset");
        JButton masterSaveButton = new JButton("Save as XML");

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0; // not allowing buttons to v-expand
        gbc.insets = new Insets(5, 5, 5, 5);
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

        Widgets.FilterWidgetGroup wordFilter = new Widgets.FilterWidgetGroup("Word", "Search word in full text...");
        Widgets.FilterWidgetGroup posFilter = new Widgets.FilterWidgetGroup("POS Tag", "Filter for POS tags...");
        Widgets.FilterWidgetGroup lemmaFilter = new Widgets.FilterWidgetGroup("Lemma", "Filter for lemma...");

        Widgets.TextSuggestionDecorator.doDecorationOn(wordFilter.getTextField(),
                new SuggestionAdapter.TextSuggestionProvider(SuggestionAdapter::getSuggestions));
        keywordFilterPanel.add(wordFilter);
        keywordFilterPanel.add(posFilter);
        keywordFilterPanel.add(lemmaFilter);

        section.add(keywordFilterPanel); // also fixed correspondingly
        return section;
    }

    private static JPanel initInfoPanel(){
        JPanel infoPanel = new JPanel();

        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        String[] loremipsum = {"This is a placeholder header", "Lorem ipsum dolor sit amet, ...", ""};
        JScrollPane listSP1 = new JScrollPane(new Widgets.TextListDisplay<>(loremipsum));
        JScrollPane listSP2 = new JScrollPane(new Widgets.TextListDisplay<>(loremipsum.clone()));
        infoPanel.add(listSP1);
        infoPanel.add(listSP2);

        return infoPanel;
    }

    //</editor-fold>

}
