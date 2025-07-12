package sh.tze.gw_swing.design.Handler;

import java.util.HashMap;

public class ResourceManager { // that's an embarrassing typo
    private static ResourceManager instance;
    private static HashMap<String, Runnable> handlers = new HashMap<>();

    private ResourceManager() {

    }

    public static synchronized ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }


    public Runnable getHandler(String handlerName) {
        return handlers.get(handlerName);
    }
    public void addHandler(Runnable r) {
        handlers.put(r.getClass().getSimpleName(), r);
    }
    public void removeHandler(Runnable r) {
        handlers.remove(r.getClass().getSimpleName());
    }


}
