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

        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (ProgramUI.UI_STYLE.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, fall back to default.
        }

        frame.setVisible(true);
    }


    private static JFrame createFrame(){
        JFrame frame = new JFrame(ProgramUI.DISPLAY_NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(ProgramUI.DEFAULT_WIDTH, ProgramUI.DEFAULT_HEIGHT);
        return frame;
    }


}
