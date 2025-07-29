package com.lexparser.scraper;

import java.net.URL;
import java.util.List;

/**
 * Main orchestrator for Wikipedia web scraping operations.
 * Coordinates URL validation, content fetching, extraction, and cleaning for GUI integration.
 */
public class WikipediaScraperOld {
    
    private final WikipediaURLValidator urlValidator;
    private final WikipediaContentFetcher contentFetcher;
    private final WikipediaContentExtractor contentExtractor;
    private final WikipediaTextCleaner textCleaner;

    /**
     * Initializes the scraper with all necessary components.
     */
    public WikipediaScraperOld() {
        this.urlValidator = new WikipediaURLValidator();
        this.contentFetcher = new WikipediaContentFetcher();
        this.contentExtractor = new WikipediaContentExtractor();
        this.textCleaner = new WikipediaTextCleaner();
    }

    /**
     * Performs complete scraping operation from URL to cleaned content.
     * 
     * @param urlString the Wikipedia URL to scrape
     * @return ScrapingResult containing all extracted and processed content
     */
    public ScrapingResult scrapeWikipediaPage(String urlString) {
        
        // Step 1: Validate URL
        WikipediaURLValidator.ValidationResult validation = urlValidator.validateURL(urlString);
        if (!validation.isValid()) {
            return new ScrapingResult(false, validation.getMessage(), null);
        }

        URL normalizedURL = validation.getURL();
        String finalURL = urlValidator.normalizeURL(normalizedURL);

        // Step 2: Fetch content
        WikipediaContentFetcher.FetchResult fetchResult = contentFetcher.fetchPage(normalizedURL);
        if (!fetchResult.isSuccess()) {
            return new ScrapingResult(false, fetchResult.getMessage(), null);
        }

        // Step 3: Validate fetched page
        if (!contentFetcher.isValidWikipediaPage(fetchResult.getDocument())) {
            return new ScrapingResult(false, "Page does not appear to be a valid Wikipedia article", null);
        }

        // Step 4: Extract structured content
        WikipediaContentExtractor.ExtractedContent extractedContent = 
            contentExtractor.extractContent(fetchResult.getDocument());

        // Step 5: Clean text content
        List<String> cleanedParagraphs = textCleaner.cleanTextSegments(extractedContent.getParagraphs());
        WikipediaTextCleaner.CleanedText cleanedCombinedText = 
            textCleaner.cleanText(extractedContent.getCombinedText());

        // Step 6: Extract metadata
        WikipediaContentFetcher.PageMetadata metadata = 
            contentFetcher.extractMetadata(fetchResult.getDocument());

        // Step 7: Compile results
        ProcessedContent processedContent = new ProcessedContent(
            extractedContent.getTitle(),
            extractedContent.getSections(),
            cleanedParagraphs,
            cleanedCombinedText.getCleanedText(),
            extractedContent.getInfoboxData(),
            metadata,
            finalURL
        );

        return new ScrapingResult(true, "Successfully scraped and processed content", processedContent);
    }

    /**
     * Quick validation method for GUI input validation.
     * 
     * @param urlString the URL to validate
     * @return true if the URL is valid for scraping
     */
    public boolean isValidWikipediaURL(String urlString) {
        return urlValidator.validateURL(urlString).isValid();
    }

    /**
     * Gets validation message for GUI error display.
     * 
     * @param urlString the URL to validate
     * @return validation message
     */
    public String getValidationMessage(String urlString) {
        return urlValidator.validateURL(urlString).getMessage();
    }

    /**
     * Container class for complete scraping results.
     */
    public static class ScrapingResult {
        private final boolean success;
        private final String message;
        private final ProcessedContent content;

        public ScrapingResult(boolean success, String message, ProcessedContent content) {
            this.success = success;
            this.message = message;
            this.content = content;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public ProcessedContent getContent() { return content; }
    }

    /**
     * Container class for processed Wikipedia content ready for analysis.
     */
    public static class ProcessedContent {
        private final String title;
        private final List<String> sections;
        private final List<String> cleanedParagraphs;
        private final String combinedCleanText;
        private final List<String> infoboxData;
        private final WikipediaContentFetcher.PageMetadata metadata;
        private final String sourceURL;

        public ProcessedContent(String title, List<String> sections, List<String> cleanedParagraphs,
                              String combinedCleanText, List<String> infoboxData,
                              WikipediaContentFetcher.PageMetadata metadata, String sourceURL) {
            this.title = title;
            this.sections = sections;
            this.cleanedParagraphs = cleanedParagraphs;
            this.combinedCleanText = combinedCleanText;
            this.infoboxData = infoboxData;
            this.metadata = metadata;
            this.sourceURL = sourceURL;
        }

        // Getters for GUI and lexical parser integration
        public String getTitle() { return title; }
        public List<String> getSections() { return sections; }
        public List<String> getCleanedParagraphs() { return cleanedParagraphs; }
        public String getCombinedCleanText() { return combinedCleanText; }
        public List<String> getInfoboxData() { return infoboxData; }
        public WikipediaContentFetcher.PageMetadata getMetadata() { return metadata; }
        public String getSourceURL() { return sourceURL; }

        /**
         * Returns text suitable for lexical analysis.
         */
        public String getTextForLexicalAnalysis() {
            return combinedCleanText;
        }

        /**
         * Returns a summary for GUI display.
         */
        public String getSummary() {
            return String.format("Title: %s\nSections: %d\nParagraphs: %d\nWord Count: ~%d\nLanguage: %s",
                title, sections.size(), cleanedParagraphs.size(),
                combinedCleanText.split("\\s+").length, metadata.getLanguage());
        }
    }
}
