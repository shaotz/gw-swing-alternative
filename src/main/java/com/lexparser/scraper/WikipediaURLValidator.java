package com.lexparser.scraper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Validates and processes Wikipedia URLs for web scraping operations.
 * Ensures URLs conform to Wikipedia's structure before attempting to fetch content.
 */
public class WikipediaURLValidator {
    
    private static final Pattern WIKIPEDIA_PATTERN = Pattern.compile(
        "^https?://[a-z]{2,3}\\.wikipedia\\.org/wiki/.+$"
    );
    
    private static final String[] BLOCKED_PAGES = {
        "Main_Page", "Special:", "Category:", "File:", "Template:", "Help:", "Wikipedia:"
    };

    /**
     * Validates if the provided URL is a valid Wikipedia article URL.
     * 
     * @param urlString the URL to validate
     * @return ValidationResult containing validation status and processed URL
     */
    public ValidationResult validateURL(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return new ValidationResult(false, "URL cannot be empty", null);
        }

        String trimmedURL = urlString.trim();
        
        try {
            URL url = new URL(trimmedURL);
            
            if (!WIKIPEDIA_PATTERN.matcher(trimmedURL).matches()) {
                return new ValidationResult(false, 
                    "URL must be a Wikipedia article (e.g., https://en.wikipedia.org/wiki/Article_Name)", 
                    null);
            }
            
            if (isBlockedPage(trimmedURL)) {
                return new ValidationResult(false, 
                    "Cannot scrape Wikipedia system pages (Main, Special, Category, etc.)", 
                    null);
            }
            
            return new ValidationResult(true, "Valid Wikipedia URL", url);
            
        } catch (MalformedURLException e) {
            return new ValidationResult(false, "Invalid URL format: " + e.getMessage(), null);
        }
    }

    /**
     * Checks if the URL points to a blocked Wikipedia page type.
     */
    private boolean isBlockedPage(String url) {
        for (String blockedPage : BLOCKED_PAGES) {
            if (url.contains("/wiki/" + blockedPage)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Normalizes Wikipedia URLs to ensure consistent format.
     * 
     * @param url the URL to normalize
     * @return normalized URL string
     */
    public String normalizeURL(URL url) {
        String urlString = url.toString();
        
        // Remove fragment identifiers
        if (urlString.contains("#")) {
            urlString = urlString.substring(0, urlString.indexOf("#"));
        }
        
        // Ensure HTTPS
        if (urlString.startsWith("http://")) {
            urlString = urlString.replace("http://", "https://");
        }
        
        return urlString;
    }

    /**
     * Container class for URL validation results.
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String message;
        private final URL url;

        public ValidationResult(boolean isValid, String message, URL url) {
            this.isValid = isValid;
            this.message = message;
            this.url = url;
        }

        public boolean isValid() { return isValid; }
        public String getMessage() { return message; }
        public URL getURL() { return url; }
    }
} 