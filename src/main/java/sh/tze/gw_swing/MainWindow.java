package sh.tze.gw_swing;

import sh.tze.gw_swing.design.UI.MainWindowView;
import sh.tze.gw_swing.design.UI.Widgets;

import javax.swing.*;
import java.awt.*;


public class MainWindow {
    public static void main(String[] args) {
        runUIThread();
    }


    protected static void runUIThread(){
        JFrame frame = createFrame();
        frame.add(MainWindowView.initMainWindowPanel());

        frame.getContentPane().setBackground(Color.LIGHT_GRAY);

        // toolbar for L&F
        Widgets.LookAndFeelSelectorGroup lookAndFeelSelector = new Widgets.LookAndFeelSelectorGroup(frame);
        // optional listener to handle L&F changes
        // TODO: maybe don't hook selector change, or somehow filter the garbage output
        lookAndFeelSelector.addChangeListener(e -> {
            System.out.println("Look and Feel changed: " +
                    lookAndFeelSelector.getCurrentLookAndFeelName());
        });
        JToolBar toolbar = new JToolBar();
        toolbar.add(lookAndFeelSelector);
        frame.add(toolbar, BorderLayout.NORTH);


        frame.setVisible(true);
    }
    private static JFrame createFrame(){
        JFrame frame = new JFrame(ProgramUI.DISPLAY_NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension d = new Dimension(ProgramUI.DEFAULT_WIDTH, ProgramUI.DEFAULT_HEIGHT);
        frame.setPreferredSize(d);
        return frame;
    }


}
