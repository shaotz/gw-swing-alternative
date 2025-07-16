package sh.tze.gw_swing.design;

import sh.tze.gw_swing.design.Handler.Tasks;
import sh.tze.gw_swing.design.Handler.TaskManager;
import sh.tze.gw_swing.design.DataRepresentation.*;
import sh.tze.gw_swing.design.Handler.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class WorkflowController {
    private static WorkflowController instance;
    private TaskManager taskManager;
    private List<WorkflowListener> listeners;

    private WorkflowController() {
        this.taskManager = TaskManager.getInstance();
        this.listeners = new ArrayList<>();
    }

    public static synchronized WorkflowController getInstance() {
        if (instance == null) {
            instance = new WorkflowController();
        }
        return instance;
    }

    public interface WorkflowListener {
        void onCrawlingStarted(String url);
        void onCrawlingCompleted(String content);
        void onAnalysisStarted(String content);
        void onAnalysisCompleted(Analysis analysis);
        void onError(String error);
    }

    public void addListener(WorkflowListener listener) {
        listeners.add(listener);
    }

    public void removeListener(WorkflowListener listener) {
        listeners.remove(listener);
    }

    public void processUrl(String url) {
        // Determine if it's a remote URL or local file
        if (isRemoteUrl(url)) {
            processCrawling(url);
        } else {
            processLocalFile(url);
        }
    }

    private void processCrawling(String url) {
        notifyListeners(l -> l.onCrawlingStarted(url));

        Tasks adapter = new Tasks();
        Tasks.WikipediaCrawler crawler = adapter.new WikipediaCrawler(url);

        taskManager.addTask(crawler);

        // Monitor crawler completion
        CompletableFuture.runAsync(() -> {
            while (!crawler.hasCompleted()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            Optional<String> result = crawler.getResult();
            if (result.isPresent()) {
                String content = result.get();
                notifyListeners(l -> l.onCrawlingCompleted(content));
                processAnalysis(content, url);
            } else {
                notifyListeners(l -> l.onError("Failed to crawl content from: " + url));
            }
        });
    }

    private void processLocalFile(String filePath) {
        // TODO: Implement local file reading
//        String content = "Local file content from: " + filePath;
        String content = "";

        notifyListeners(l -> l.onCrawlingCompleted(content));
        processAnalysis(content, filePath);
    }

    private void processAnalysis(String content, String sourceUrl) {
        notifyListeners(l -> l.onAnalysisStarted(content));

        var analyzer = new Tasks.NLPAnalyzer(sourceUrl);

        taskManager.addTask(analyzer);

        CompletableFuture.runAsync(() -> {
            while (!analyzer.hasCompleted()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            Analysis analysis = analyzer.getAnalysis();
            notifyListeners(l -> l.onAnalysisCompleted(analysis));
        });
    }

    private boolean isRemoteUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private void notifyListeners(java.util.function.Consumer<WorkflowListener> action) {
        listeners.forEach(action);
    }

}
