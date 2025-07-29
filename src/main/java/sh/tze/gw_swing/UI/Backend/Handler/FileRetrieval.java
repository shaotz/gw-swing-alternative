package sh.tze.gw_swing.UI.Backend.Handler;

import java.io.File;

@Deprecated
public class FileRetrieval {
    public static File open(String path){
        try{
            File file = new File(path);
        } catch (Exception e){

        }
        return null;
    }
}
