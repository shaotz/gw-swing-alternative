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

    // Flattened tree definition Side L: Section Above(A)
    public static JPanel initMWPanelSectA(){
        JPanel sect = new JPanel(new GridLayout(3,1));

//        GridLayout gl = new GridLayout(3,1);

        sect.add(initURLSelectorPanel());
        return sect;
    }
    public static JPanel initURLSelectorPanel(){
        JPanel container = new JPanel(new BorderLayout());

        // Def Layout Manager
//        GridLayout gl = new GridLayout(1,2);
//        container.setLayout(gl);

        // Def Enclosed Items
        JTextField urlTextField = new JTextField(10);
        JButton urlActionButton = new JButton("Open");


        // Registering enclosed items
        container.add(urlTextField,BorderLayout.CENTER);
        container.add(urlActionButton,BorderLayout.EAST);
        // applying BorderLayout on atom items to instruct positioning

        return container;
    }

    // Flattened tree definition Side R: Section Below(B)
    public static JPanel initMWPanelSectB() {
        JPanel sect = new JPanel(new BorderLayout());

        sect.add(initPrimaryTextDisplay(), BorderLayout.CENTER);
        return sect;
    }
    public static JPanel initPrimaryTextDisplay(){
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea mainTextArea = new JTextArea();
        JScrollPane encapsulator = new JScrollPane(mainTextArea);
        mainTextArea.setLineWrap(true);
        mainTextArea.setWrapStyleWord(true);
//        JScrollBar scrollBar = new JScrollBar();
//
//        encapsulator.add(mainTextArea, BorderLayout.CENTER);
//        encapsulator.add(scrollBar, BorderLayout.EAST
//        );

        panel.add(encapsulator,BorderLayout.CENTER);
        return panel;
    }

}
