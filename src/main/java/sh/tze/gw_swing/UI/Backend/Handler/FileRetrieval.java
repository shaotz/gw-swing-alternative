package sh.tze.gw_swing.UI.Backend.Handler;

import java.io.File;

public class FileRetrieval {
    private File f;
    public FileRetrieval(File file) {
        f = file;
    }

    public File getFile() {return f;}
}
