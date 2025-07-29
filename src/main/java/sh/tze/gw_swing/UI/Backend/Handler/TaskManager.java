package sh.tze.gw_swing.UI.Backend.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager { // that's an embarrassing typo
    private static TaskManager instance;
    private static HashMap<String, Tasks.RunnableTask> tasks = new HashMap<>();
    private final List<Thread> activeThreads = new ArrayList<>();

    private TaskManager() {}

    public static synchronized TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }


    public void addTask(Tasks.RunnableTask task) {
        String taskKey = task.getClass().getSimpleName() + "_" + System.currentTimeMillis();
        tasks.put(taskKey, task);

        // starting a new thread
        Thread taskThread = new Thread(task);
        taskThread.start();
        activeThreads.add(taskThread); // register thread
    }
    public Tasks.RunnableTask getTask(String taskName) {
        return tasks.get(taskName);
    }

    public void removeTask(String taskName) {
        tasks.remove(taskName);
    }

    public void shutdown() {
        // Interrupt all active threads (best-effort shutdown)
        for (Thread thread : activeThreads) {
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }
        activeThreads.clear();
    }
}
