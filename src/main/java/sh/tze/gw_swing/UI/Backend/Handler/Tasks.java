package sh.tze.gw_swing.UI.Backend.Handler;

import sh.tze.gw_swing.UI.Backend.DataRepresentation.Analysis;

import java.util.Optional;
import java.util.UUID;

public class Tasks {
    public static final String CRAWLER_TYPE = "CRAWLER";
    public static final String ANALYZER_TYPE = "ANALYZER";

    private static final String CLASS_NAME_WIKIPEDIA_SCRAPER = "com.lexparser.scraper.WikipediaScraper";
    private static final String CLASS_NAME_NLP_PROCESSOR = "NLPProcessor";

    public interface RunnableTask extends Runnable {
        String getTaskType();
        boolean hasCompleted();
        Optional<String> getResult();
    }
    private abstract class Crawler implements RunnableTask {
        protected final String taskType = CRAWLER_TYPE;
        protected String taskID;
        protected final String url;
        protected String result;
        protected boolean completed = false;

        public Crawler(String url) {
            taskID = UUID.randomUUID().toString();
            this.url = url;
            completed = false;
        }

    }

    private static abstract class Analyzer implements RunnableTask {
        protected final String taskType = ANALYZER_TYPE;
        protected String taskId;
        protected final String toAnalyze;
        protected String result;
        protected boolean completed;
        protected Analysis analysis; // To store processed results

        public Analyzer(String toAnalyze) {
            this.taskId = UUID.randomUUID().toString();
            this.toAnalyze = toAnalyze;
            this.completed = false;
            this.result = "";
            this.analysis = new Analysis(""); // Initialize with empty source (set later if needed)
        }

        @Override
        public String getTaskType() {
            return taskType;
        }

        @Override
        public boolean hasCompleted() {
            return completed;
        }

        @Override
        public Optional<String> getResult() {
            return completed ? Optional.of(result) : Optional.empty();
        }

        public Analysis getAnalysis() {
            return analysis;
        }
    }

    public class WikipediaCrawler extends Crawler {
        static {
            try {
                c = Class.forName(CLASS_NAME_WIKIPEDIA_SCRAPER);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException();
            }
        }

        static Class<?> c;
            @Override
        public String getTaskType() {
            return "CRAWLER";
        }

        public WikipediaCrawler(String url) {
            super(url);
        }

        @Override
        public void run(){
            try{
                var cc = c.getDeclaredConstructor(String.class).newInstance(url);
                result = (String) c.getMethod("scrapeContent").invoke(cc);
                completed = true;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException();
            }
        }
        @Override
        public boolean hasCompleted() {
            return completed;
        }
        @Override
        public Optional<String> getResult() {
            return Optional.of(result);
        }
    }

    public static class NLPAnalyzer extends Analyzer {
        public NLPAnalyzer(String toAnalyze) {
            super(toAnalyze);
        }
        @Override
        public void run() {
            try {
                for (int i = 0; i < 10; i++) { // Simulate loop
                    if (Thread.currentThread().isInterrupted()) {
                        result = "Task interrupted";
                        completed = true;
                        return;
                    }
                    Thread.sleep(100); // Smaller sleeps to check interruption more frequently
                }
                completed = true;
            } catch (InterruptedException e) {
                result = "Error during NLP analysis: " + e.getMessage();
                completed = true;
                Thread.currentThread().interrupt(); // Restore interrupt flag
            }
        }

    }

}
