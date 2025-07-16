package sh.tze.gw_swing;

import sh.tze.gw_swing.design.Handler.TaskManager;
import sh.tze.gw_swing.design.Handler.Tasks;

import javax.swing.*;

public class MainThread {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow.runUIThread();
        });

        SwingUtilities.invokeLater(() -> {
            TaskManager manager = TaskManager.getInstance();
            Tasks adapter = new Tasks();
            Tasks.WikipediaCrawler crawler = adapter.new WikipediaCrawler("https://en.wikipedia.org/wiki/Java_(programming_language)");
            manager.addTask(crawler);
        });
    }
}
