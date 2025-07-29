package com.lexparser.scraper;

/**
 * Simple interface for web scraping functionality.
 * Provides clean methods for other teams to integrate with the web scraper.
 */
public class WebScrapingInterface {
    
    private final WikipediaScraperOld scraper;
    
    public WebScrapingInterface() {
        this.scraper = new WikipediaScraperOld();
    }
    
    /**
     * Validates if a URL is a valid Wikipedia article URL.
     * 
     * @param url the URL to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidWikipediaURL(String url) {
        return scraper.isValidWikipediaURL(url);
    }
    
    /**
     * Gets validation error message for invalid URLs.
     * 
     * @param url the URL to validate
     * @return validation message
     */
    public String getValidationMessage(String url) {
        return scraper.getValidationMessage(url);
    }
    
    /**
     * Scrapes content from a Wikipedia URL and returns cleaned text.
     * 
     * @param url the Wikipedia URL to scrape
     * @return ScrapedContent containing the extracted and cleaned text
     */
    public ScrapedContent scrapeWikipediaContent(String url) {
        WikipediaScraperOld.ScrapingResult result = scraper.scrapeWikipediaPage(url);
        
        if (result.isSuccess()) {
            WikipediaScraperOld.ProcessedContent content = result.getContent();
            return new ScrapedContent(
                true,
                content.getTitle(),
                content.getCombinedCleanText(),
                content.getSourceURL(),
                null
            );
        } else {
            return new ScrapedContent(false, "", "", "", result.getMessage());
        }
    }
    
    /**
     * Container class for scraped content results.
     */
    public static class ScrapedContent {
        private final boolean success;
        private final String title;
        private final String cleanedText;
        private final String sourceURL;
        private final String errorMessage;
        
        public ScrapedContent(boolean success, String title, String cleanedText, 
                            String sourceURL, String errorMessage) {
            this.success = success;
            this.title = title;
            this.cleanedText = cleanedText;
            this.sourceURL = sourceURL;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() { return success; }
        public String getTitle() { return title; }
        public String getCleanedText() { return cleanedText; }
        public String getSourceURL() { return sourceURL; }
        public String getErrorMessage() { return errorMessage; }
    }
} 