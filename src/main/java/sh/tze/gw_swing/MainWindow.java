package sh.tze.gw_swing;

import sh.tze.gw_swing.UI.MainWindowView;
import sh.tze.gw_swing.UI.Widgets.LookAndFeelSelectorGroup;

import javax.swing.*;
import java.awt.*;


public class MainWindow {
    public static void main(String[] args) {
        runUIThread();
    }


    protected static void runUIThread(){
        JFrame frame = createFrame();

        MainWindowView view = new MainWindowView();
        frame.add(view.getMainPanel());
        attachLFSelector(frame);

        frame.getContentPane().setBackground(Color.LIGHT_GRAY);
        frame.setVisible(true);
    }
    private static JFrame createFrame(){
        JFrame frame = new JFrame(Manifest.DISPLAY_NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension d = new Dimension(Manifest.DEFAULT_WIDTH, Manifest.DEFAULT_HEIGHT);
        frame.setPreferredSize(d);
        return frame;
    }

    private static void attachLFSelector(JFrame frame){
        // toolbar for L&F
        LookAndFeelSelectorGroup lookAndFeelSelector = new LookAndFeelSelectorGroup(frame);
        // optional listener to handle L&F changes
        lookAndFeelSelector.addChangeListener(e -> {
            System.out.println("Look and Feel changed: " +
                    lookAndFeelSelector.getCurrentLookAndFeelName());
        });
        JToolBar toolbar = new JToolBar();
        toolbar.add(lookAndFeelSelector);
        frame.add(toolbar, BorderLayout.NORTH);
    }

}
