package sh.tze.gw_swing.design;

import sh.tze.gw_swing.ProgramUI;

import javax.swing.*;
import java.awt.*;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class MainWindow {
    public static void main(String[] args) {
        JFrame frame = createFrame();
        frame.add(MainWindowWidgets.initMainWindowPanel());

        frame.getContentPane().setBackground(Color.LIGHT_GRAY);

        // toolbar for L&F
        WidgetUtilities.LookAndFeelSelectorGroup lookAndFeelSelector = new WidgetUtilities.LookAndFeelSelectorGroup(frame);
        // optional listener to handle L&F changes
        lookAndFeelSelector.addChangeListener(e -> {
            System.out.println("Look and Feel changed: " +
                    lookAndFeelSelector.getCurrentLookAndFeelName());
        });
        JToolBar toolbar = new JToolBar();
        toolbar.add(lookAndFeelSelector);
        frame.add(toolbar, BorderLayout.NORTH);

//        try {
//            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                if (ProgramUI.UI_STYLE.equals(info.getName())) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            // If desired LookAndFeel is not available, fall back to default.
//        }

        frame.setVisible(true);
    }


    private static JFrame createFrame(){
        JFrame frame = new JFrame(ProgramUI.DISPLAY_NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(ProgramUI.DEFAULT_WIDTH, ProgramUI.DEFAULT_HEIGHT);
        return frame;
    }


}
