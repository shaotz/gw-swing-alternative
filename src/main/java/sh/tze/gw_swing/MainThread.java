package sh.tze.gw_swing;

import sh.tze.gw_swing.design.Handler.ResourceManager;
import sh.tze.gw_swing.design.Handler.ResourceAdapter;

import javax.swing.*;

public class MainThread {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow.runUIThread();
        });

        SwingUtilities.invokeLater(() -> {
            ResourceManager manager = ResourceManager.getInstance();
            ResourceAdapter adapter = new ResourceAdapter();
            ResourceAdapter.WikipediaCrawler crawler = adapter.new WikipediaCrawler("https://en.wikipedia.org/wiki/Java_(programming_language)");
            manager.addHandler(crawler);
        });
    }
}
