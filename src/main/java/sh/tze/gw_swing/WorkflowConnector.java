package sh.tze.gw_swing;

import com.lexparser.scraper.WebScrapingInterface;
import sh.tze.gw_swing.UI.Backend.Handler.Tasks;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * SwingWorker-based connector for handling long-running web scraping operations.
 * Performs Wikipedia scraping off the EDT and delivers results back safely.
 * 
 * This class maintains UI/worker separation while providing progress updates
 * through PropertyChangeListener support.
 */
public class WorkflowConnector extends SwingWorker<String, Void> {
    
    // Progress constants
    public static final String PROGRESS_PROPERTY = "progress";
    public static final String STATE_PROPERTY = "state";
    public static final String MESSAGE_PROPERTY = "message";
    
    // State constants for tracking workflow progress
    public enum WorkflowState {
        IDLE("Idle"),
        VALIDATING("Validating URL..."),
        SCRAPING("Scraping Wikipedia content..."),
        PROCESSING("Processing scraped content..."),
        COMPLETED("Completed"),
        FAILED("Failed");
        
        private final String description;
        
        WorkflowState(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private final String url;
    private final WebScrapingInterface webScrapingInterface;
    private final PropertyChangeSupport propertySupport;
    
    private WorkflowState currentState = WorkflowState.IDLE;
    private String statusMessage = "";
    private String scrapedTitle = "";
    
    // Future extension: additional options can be added here
    private boolean includeMetadata = false;
    private boolean cleanText = true;
    
    /**
     * Creates a new WorkflowConnector for the specified URL.
     * 
     * @param url the Wikipedia URL to scrape
     */
    public WorkflowConnector(String url) {
        this.url = url;
        this.webScrapingInterface = new WebScrapingInterface();
        this.propertySupport = new PropertyChangeSupport(this);
        
        // Initialize with IDLE state
        updateState(WorkflowState.IDLE, "Ready to start");
    }
    
    /**
     * Creates a new WorkflowConnector with additional options.
     * 
     * @param url the Wikipedia URL to scrape
     * @param includeMetadata whether to include metadata in results
     * @param cleanText whether to clean the scraped text
     */
    public WorkflowConnector(String url, boolean includeMetadata, boolean cleanText) {
        this(url);
        this.includeMetadata = includeMetadata;
        this.cleanText = cleanText;
    }
    
    /**
     * Performs the background scraping operation.
     * This method runs on a worker thread, not the EDT.
     */
    @Override
    protected String doInBackground() throws Exception {
        try {
            // Step 1: Validate URL
            updateState(WorkflowState.VALIDATING, "Validating Wikipedia URL...");
            setProgress(10);
            
            if (!webScrapingInterface.isValidWikipediaURL(url)) {
                String validationMessage = webScrapingInterface.getValidationMessage(url);
                throw new IllegalArgumentException("Invalid URL: " + validationMessage);
            }
            
            // Step 2: Perform web scraping
            updateState(WorkflowState.SCRAPING, "Connecting to Wikipedia and fetching content...");
            setProgress(30);
            
            // Option 1: Use WebScrapingInterface directly
            WebScrapingInterface.ScrapedContent scrapedContent = 
                webScrapingInterface.scrapeWikipediaContent(url);
            
            if (!scrapedContent.isSuccess()) {
                throw new RuntimeException("Scraping failed: " + scrapedContent.getErrorMessage());
            }
            
            setProgress(70);
            
            // Step 3: Process the content
            updateState(WorkflowState.PROCESSING, "Processing scraped content...");
            
            scrapedTitle = scrapedContent.getTitle();
            String cleanedText = scrapedContent.getCleanedText();
            
            // Option 2 (alternative): Use Tasks.WikipediaCrawler if needed
            // This approach would require modifying WikipediaCrawler to return results differently
            /*
            Tasks tasks = new Tasks();
            Tasks.WikipediaCrawler crawler = tasks.new WikipediaCrawler(url);
            crawler.run();
            if (crawler.hasCompleted()) {
                cleanedText = crawler.getResult().orElse("");
            }
            */
            
            setProgress(90);
            
            // Format the final result
            StringBuilder result = new StringBuilder();
            result.append("=== Wikipedia Content ===\n");
            result.append("Title: ").append(scrapedTitle).append("\n");
            result.append("Source: ").append(scrapedContent.getSourceURL()).append("\n");
            result.append("\n--- Content ---\n");
            result.append(cleanedText);
            
            if (includeMetadata) {
                result.append("\n\n--- Metadata ---\n");
                result.append("Scraped at: ").append(new java.util.Date()).append("\n");
                result.append("Text length: ").append(cleanedText.length()).append(" characters\n");
            }
            
            setProgress(100);
            updateState(WorkflowState.COMPLETED, "Successfully scraped content from Wikipedia");
            
            return result.toString();
            
        } catch (Exception e) {
            updateState(WorkflowState.FAILED, "Error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Called on the EDT when the background task completes.
     * Subclasses can override this to handle the result.
     */
    @Override
    protected void done() {
        try {
            String result = get();
            // Result is available, listeners will be notified through property changes
        } catch (Exception e) {
            // Error handling - state already updated in doInBackground
            statusMessage = "Task failed: " + e.getMessage();
        }
    }
    
    /**
     * Updates the current workflow state and notifies listeners.
     * 
     * @param newState the new state
     * @param message status message for the state
     */
    private void updateState(WorkflowState newState, String message) {
        WorkflowState oldState = this.currentState;
        this.currentState = newState;
        this.statusMessage = message;
        
        // Fire property change events
        firePropertyChange(STATE_PROPERTY, oldState, newState);
        firePropertyChange(MESSAGE_PROPERTY, null, message);
    }
    
    /**
     * Gets the current workflow state.
     * 
     * @return the current state
     */
    public WorkflowState getCurrentState() {
        return currentState;
    }
    
    /**
     * Gets the current status message.
     * 
     * @return the status message
     */
    public String getStatusMessage() {
        return statusMessage;
    }
    
    /**
     * Gets the scraped page title (available after successful scraping).
     * 
     * @return the page title, or empty string if not yet scraped
     */
    public String getScrapedTitle() {
        return scrapedTitle;
    }
    
    /**
     * Adds a custom PropertyChangeListener for monitoring workflow-specific changes.
     * Note: Also use the standard addPropertyChangeListener() for SwingWorker events.
     * 
     * @param listener the listener to add
     */
    public void addWorkflowListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    /**
     * Removes a custom PropertyChangeListener.
     * 
     * @param listener the listener to remove
     */
    public void removeWorkflowListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    
    /**
     * Adds a custom PropertyChangeListener for a specific workflow property.
     * 
     * @param propertyName the property to listen for
     * @param listener the listener to add
     */
    public void addWorkflowListener(String propertyName, PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(propertyName, listener);
    }
    
    /**
     * Removes a custom PropertyChangeListener for a specific property.
     * 
     * @param propertyName the property name
     * @param listener the listener to remove
     */
    public void removeWorkflowListener(String propertyName, PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(propertyName, listener);
    }
}
