package com.lexparser.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.net.URL;

/**
 * Handles HTTP connections and content fetching from Wikipedia pages.
 * Implements proper connection management and error handling for reliable web scraping.
 */
public class WikipediaContentFetcher {
    
    private static final int CONNECTION_TIMEOUT = 15000; // 15 seconds
    private static final int READ_TIMEOUT = 30000; // 30 seconds
    private static final String USER_AGENT = "Mozilla/5.0 (Educational Research Bot 1.0)";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY = 2000; // 2 seconds

    /**
     * Fetches the HTML document from a Wikipedia URL with retry logic.
     * 
     * @param url the Wikipedia URL to fetch
     * @return FetchResult containing the document or error information
     */
    public FetchResult fetchPage(URL url) {
        return fetchPage(url, 0);
    }

    /**
     * Internal method that handles the actual fetching with retry mechanism.
     */
    private FetchResult fetchPage(URL url, int attemptNumber) {
        try {
            Document document = Jsoup.connect(url.toString())
                    .userAgent(USER_AGENT)
                    .timeout(CONNECTION_TIMEOUT)
                    .followRedirects(true)
                    .maxBodySize(0) // No limit on body size
                    .get();
            
            return new FetchResult(true, document, null, "Successfully fetched page");
            
        } catch (IOException e) {
            if (attemptNumber < MAX_RETRIES - 1) {
                try {
                    Thread.sleep(RETRY_DELAY * (attemptNumber + 1)); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return new FetchResult(false, null, e, "Fetch interrupted");
                }
                return fetchPage(url, attemptNumber + 1);
            }
            
            return new FetchResult(false, null, e, 
                "Failed to fetch page after " + MAX_RETRIES + " attempts: " + e.getMessage());
        }
    }

    /**
     * Validates that the fetched document is a proper Wikipedia article page.
     * 
     * @param document the document to validate
     * @return true if the document appears to be a valid Wikipedia article
     */
    public boolean isValidWikipediaPage(Document document) {
        if (document == null) {
            return false;
        }
        
        // Check for essential Wikipedia elements
        return document.getElementById("mw-content-text") != null &&
               document.select(".mw-parser-output").size() > 0 &&
               !document.select("p").isEmpty();
    }

    /**
     * Extracts basic metadata from the Wikipedia page.
     * 
     * @param document the Wikipedia document
     * @return PageMetadata containing basic page information
     */
    public PageMetadata extractMetadata(Document document) {
        if (document == null) {
            return new PageMetadata("Unknown", "Unknown", 0);
        }
        
        String title = document.title().replace(" - Wikipedia", "");
        String language = extractLanguageCode(document);
        int contentLength = document.text().length();
        
        return new PageMetadata(title, language, contentLength);
    }

    /**
     * Extracts the language code from the Wikipedia document.
     */
    private String extractLanguageCode(Document document) {
        String lang = document.select("html").attr("lang");
        return lang.isEmpty() ? "unknown" : lang;
    }

    /**
     * Container class for fetch operation results.
     */
    public static class FetchResult {
        private final boolean success;
        private final Document document;
        private final Exception error;
        private final String message;

        public FetchResult(boolean success, Document document, Exception error, String message) {
            this.success = success;
            this.document = document;
            this.error = error;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public Document getDocument() { return document; }
        public Exception getError() { return error; }
        public String getMessage() { return message; }
    }

    /**
     * Container class for page metadata.
     */
    public static class PageMetadata {
        private final String title;
        private final String language;
        private final int contentLength;

        public PageMetadata(String title, String language, int contentLength) {
            this.title = title;
            this.language = language;
            this.contentLength = contentLength;
        }

        public String getTitle() { return title; }
        public String getLanguage() { return language; }
        public int getContentLength() { return contentLength; }
    }
} 