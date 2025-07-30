package sh.tze.gw_swing;

import javax.swing.*;

public class MainThread {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow.runUIThread();
        });

    }

}

/*
    An article is guaranteed to be present at the end of a file if something worth noting was found,
     or when some terrible implementation has occurred in a previous version. Pure personal opinions.

     Hey, you found the program entry point.

     - Why do I need a program entry point?

     Generally, it is better to separate UI thread with other threads.
     If the UI thread had called a routine that will presumably take time to complete, it gets stuck (you have to wait for the routine to return).
     (This is why sometimes you are seeing 'beach ball' on MacOS and the rolling ring on Windows)
     Specifically with Swing, UI-related is expected to be run in the EDT, or the Event Dispatcher Thread.
     The main thread, that is, where the entry point is loaded onto, is guaranteed NOT to be the EDT.

 */
