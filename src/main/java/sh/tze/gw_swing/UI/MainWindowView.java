package sh.tze.gw_swing.UI;

import sh.tze.gw_swing.UI.Backend.MainWindowBackend;
import sh.tze.gw_swing.UI.SuggestionAdapter.Decorator;
import sh.tze.gw_swing.UI.SuggestionAdapter.Provider;
import sh.tze.gw_swing.UI.Widgets.FilterWidgetGroup;
import sh.tze.gw_swing.UI.Widgets.TextDisplayPanel;
import sh.tze.gw_swing.UI.Widgets.TextDisplayList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainWindowView {
    private final JPanel mainPanel;
    private JTextField urlTextField;
    private TextDisplayPanel textDisplayPanel;

    private JLabel statusLabel;
    private JProgressBar progressBar;

    private JCheckBox sel_cs;
    private JRadioButton sel_ws;
    private JRadioButton sel_nw;
    private JTextField tf_range_l;
    private JTextField tf_range_r;

    private JTextField tf_wf;
    private JTextField tf_pos;
    private JTextField tf_lemma;

    private JCheckBox sel_wf;
    private JCheckBox sel_pos;
    private JCheckBox sel_lemma;

    private TextDisplayList urlHistoryList;
    private TextDisplayList filterSchemeHistoryList;

    private DefaultListModel<String> urlHistoryListModel;
    private DefaultListModel<String> filterSchemeHistoryListModel;

    private  MainWindowBackend backend;

    public MainWindowView() {
        backend = new MainWindowBackend(this);
        mainPanel = initMainWindowPanel();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private JPanel initMainWindowPanel() {
        JPanel MWPanel = new JPanel(new BorderLayout());
        MWPanel.setName("RootPanel");

        JPanel MWPanelSectionA = createMWPanelSectA();
        JPanel MWPanelSectionB = createMWPanelSectB();

        MWPanel.add(MWPanelSectionA,BorderLayout.NORTH);
        MWPanel.add(MWPanelSectionB,BorderLayout.CENTER);

        return MWPanel;
    }

    //<editor-fold desc="Flattened tree definition Side L: Section Above(A)">
    private JPanel createMWPanelSectA(){

        JPanel container = new JPanel(new BorderLayout());

        JCheckBox selectCaseButton = new JCheckBox("Case Sensitive");
        JRadioButton wholeSentenceButton = new JRadioButton("Whole Sentence");
        JRadioButton numOfNeighbors = new JRadioButton("# of Neighbor Words");

        ButtonGroup filterRangeGroup = new ButtonGroup();
        filterRangeGroup.add(numOfNeighbors);
        filterRangeGroup.add(wholeSentenceButton);
        filterRangeGroup.setSelected(wholeSentenceButton.getModel(), true);

        JPanel neighborPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        // Left input
        JLabel leftNum = new JLabel("Left:");
        JTextField leftTextField = new JTextField(5); // 5 columns width

        // Right input
        JLabel rightNum = new JLabel("Right:");
        JTextField rightTextField = new JTextField(5); // 5 columns width

        leftTextField.setText("1");
        rightTextField.setText("1");

        // Add components to neighbor panel
        neighborPanel.add(numOfNeighbors);
        neighborPanel.add(Box.createHorizontalStrut(5)); // Add spacing
        neighborPanel.add(leftNum);
        neighborPanel.add(leftTextField);
        neighborPanel.add(Box.createHorizontalStrut(5)); // Add spacing
        neighborPanel.add(rightNum);
        neighborPanel.add(rightTextField);

//        panel for the center components
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        centerPanel.add(wholeSentenceButton);
        centerPanel.add(numOfNeighbors);
        centerPanel.add(neighborPanel);

        container.add(createURLSelectorPanel(), BorderLayout.NORTH);
        container.add(selectCaseButton, BorderLayout.WEST);
        container.add(centerPanel, BorderLayout.CENTER);

        sel_cs = selectCaseButton;
        sel_ws = wholeSentenceButton;
        sel_nw = numOfNeighbors;

        tf_range_l = leftTextField;
        tf_range_r = rightTextField;

        return container;
    }

    private JPanel createURLSelectorPanel(){
        JPanel container = new JPanel(new BorderLayout());

        // Def Enclosed Items
        urlTextField = new JTextField(10);
        JButton urlActionButton = new JButton("Open");
        urlActionButton.addActionListener(backend.new URLOpenListener(urlTextField)); // this static and non-static shit

//        var historyProvider = new Provider.TextHistorySuggestionProvider();
//        Decorator.TextSuggestionDecorator.doDecorationOn(urlTextField,
//                historyProvider);



        // Registering enclosed items

        container.add(urlTextField,BorderLayout.CENTER);
        container.add(urlActionButton,BorderLayout.EAST);
        // applying BorderLayout on atom items to instruct positioning

        return container;
    }

    //</editor-fold>


    //<editor-fold desc="Flattened tree definition Side R: Section Below(B)">
    private JPanel createMWPanelSectB() {
        JPanel sect = new JPanel(new BorderLayout());

        sect.add(createPrimaryTextDisplay(), BorderLayout.CENTER);
        sect.add(createFunctionControlPanel(), BorderLayout.EAST);
        return sect;
    }

    private JPanel createPrimaryTextDisplay(){
        TextDisplayPanel tdp = new TextDisplayPanel();
        textDisplayPanel = tdp;
        return tdp;
    }

    private JPanel createFunctionControlPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        // Will just operate on constraint buffer `gbc`
        GridBagConstraints gbc = new GridBagConstraints(); // Iseri Nina wrote this LOL

        JButton filterActionButton = new JButton("Filter");
        JButton filterResetButton = new JButton("Reset");
        filterActionButton.addActionListener(e -> {
            backend.onFilterClicked();
        });
        filterResetButton.addActionListener(e -> {
            backend.onResetClicked();
        });
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

        // Add file selector to masterSaveButton
        masterSaveButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save as XML");
            int userSelection = fileChooser.showSaveDialog(panel);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                backend.onSaveClicked(fileToSave);
            }
        });
        return panel;
    }

    private JPanel initFilterSection() {
        JPanel section = new JPanel(); // removed a confusing declaration
        JPanel keywordFilterPanel = new JPanel();
//        JPanel adjustmentPanel = new JPanel(); //TODO

        keywordFilterPanel.setLayout(new BoxLayout(keywordFilterPanel, BoxLayout.Y_AXIS));

        FilterWidgetGroup wordFilter = new FilterWidgetGroup("Word", "Search word in full text...");
        FilterWidgetGroup posFilter = new FilterWidgetGroup("POS Tag", "Filter for POS tags...");
        FilterWidgetGroup lemmaFilter = new FilterWidgetGroup("Lemma", "Filter for lemma...");

        sel_wf = wordFilter.getCheckBox();
        sel_pos = posFilter.getCheckBox();
        sel_lemma = lemmaFilter.getCheckBox();

        tf_wf = wordFilter.getTextField();
        tf_pos = posFilter.getTextField();
        tf_lemma = lemmaFilter.getTextField();

        Decorator.TextSuggestionDecorator.doDecorationOn(wordFilter.getTextField(),
                new Provider.TextSuggestionProvider(backend::getWFSuggestionFromNLPResult));
        Decorator.TextSuggestionDecorator.doDecorationOn(posFilter.getTextField(),
                new Provider.TextSuggestionProvider(backend::getPOSSuggestionFromNLPResult));
        Decorator.TextSuggestionDecorator.doDecorationOn(lemmaFilter.getTextField(),
                new Provider.TextSuggestionProvider(backend::getLemmaSuggestionFromNLPResult));

        keywordFilterPanel.add(wordFilter);
        keywordFilterPanel.add(posFilter);
        keywordFilterPanel.add(lemmaFilter);

        section.add(keywordFilterPanel); // also fixed correspondingly
        return section;
    }

    private JPanel initInfoPanel(){
        JPanel infoPanel = new JPanel();

        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        String[] loremipsum = {"This is a placeholder header", "Lorem ipsum dolor sit amet","...", "", "Lorem ipsum dolor sit amet","Lorem ipsum dolor sit amet","Lorem ipsum dolor sit amet","Lorem ipsum dolor sit amet","Lorem ipsum dolor sit amet","Lorem ipsum dolor sit amet","Lorem ipsum dolor sit amet","Lorem ipsum dolor sit amet",};

        DefaultListModel<String> lm1 = new DefaultListModel<>();
        DefaultListModel<String> lm2 = new DefaultListModel<>();
        var tdl1 = new TextDisplayList<>(lm1);
        var tdl2 = new TextDisplayList<>(lm2);
        JScrollPane corpusHistoryPane = new JScrollPane(tdl1);
        JScrollPane filterSchemeHistoryPane = new JScrollPane(tdl2);

        tdl1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click
                    backend.onURLListEntryActivated();
                }
            }
        });
        tdl2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click
                    backend.onFilterSchemeListEntryActivated();
                }
            }
        });

        lm1.addElement("URL History");
        lm2.addElement("Filter Scheme History");

        urlHistoryList = tdl1;
        filterSchemeHistoryList = tdl2;
        urlHistoryListModel = lm1;
        filterSchemeHistoryListModel = lm2;


        infoPanel.add(corpusHistoryPane);
        infoPanel.add(filterSchemeHistoryPane);

        return infoPanel;
    }

    //</editor-fold>


    public JTextField getUrlTextField() {
        return urlTextField;
    }

    public TextDisplayPanel getTextDisplayPanel() {
        return textDisplayPanel;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public MainWindowBackend getBackend() {
        return backend;
    }

    public JCheckBox getSel_cs() {
        return sel_cs;
    }

    public JRadioButton getSel_ws() {
        return sel_ws;
    }

    public JRadioButton getSel_nw() {
        return sel_nw;
    }

    public JTextField getTf_range_l() {
        return tf_range_l;
    }

    public JTextField getTf_range_r() {
        return tf_range_r;
    }

    public JTextField getTf_wf() {
        return tf_wf;
    }

    public JTextField getTf_pos() {
        return tf_pos;
    }

    public JTextField getTf_lemma() {
        return tf_lemma;
    }

    public JCheckBox getSel_wf() {
        return sel_wf;
    }

    public JCheckBox getSel_pos() {
        return sel_pos;
    }

    public JCheckBox getSel_lemma() {
        return sel_lemma;
    }

    public TextDisplayList getUrlHistoryList() {
        return urlHistoryList;
    }

    public TextDisplayList getFilterSchemeHistoryList() {
        return filterSchemeHistoryList;
    }

    public DefaultListModel<String> getFilterSchemeHistoryListModel() {
        return filterSchemeHistoryListModel;
    }

    public DefaultListModel<String> getUrlHistoryListModel() {
        return urlHistoryListModel;
    }
}
