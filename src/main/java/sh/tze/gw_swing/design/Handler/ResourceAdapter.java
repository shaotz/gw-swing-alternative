package sh.tze.gw_swing.design.Handler;

import java.util.Optional;
import java.util.UUID;

public class ResourceAdapter {
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
        protected boolean completed;
        public Crawler(String url) {
            taskID = UUID.randomUUID().toString();
            this.url = url;
            completed = false;
        }

    }

    private abstract class Analyzer implements RunnableTask {
        public final String toAnalyze;
        public String result;
        public Analyzer(String toAnalyze) {
            this.toAnalyze = toAnalyze;
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

    public abstract class NLPAnalyzer extends Analyzer { //TODO declared abstract to suppress warning
        public NLPAnalyzer(String toAnalyze) {
            super(toAnalyze);
        }
        @Override
        public void run() {
            //TODO
        }
    }

}
