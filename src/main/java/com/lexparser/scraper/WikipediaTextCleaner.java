package com.lexparser.scraper;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Cleans and preprocesses Wikipedia text content for lexical analysis.
 * Removes footnotes, citations, and formatting artifacts while preserving meaningful content.
 */
public class WikipediaTextCleaner {
    
    // Patterns for various Wikipedia artifacts
    private static final Pattern FOOTNOTE_REFS = Pattern.compile("\\[\\d+\\]");
    private static final Pattern CITATION_NEEDED = Pattern.compile("\\[citation needed\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern PARENTHETICAL_REFS = Pattern.compile("\\([^)]*\\b\\d{4}\\b[^)]*\\)");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern UNICODE_QUOTES = Pattern.compile("[\\u201C\\u201D\\u2018\\u2019]");
    private static final Pattern DASH_VARIANTS = Pattern.compile("[\\u2013\\u2014]");
    private static final Pattern PRONUNCIATION_GUIDES = Pattern.compile("\\([^)]*pronunciation[^)]*\\)", Pattern.CASE_INSENSITIVE);
    
    /**
     * Performs comprehensive cleaning of Wikipedia text content.
     * 
     * @param rawText the raw text to clean
     * @return CleanedText object containing the processed content
     */
    public CleanedText cleanText(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return new CleanedText("", 0, 0);
        }
        
        String originalText = rawText;
        int originalLength = originalText.length();
        
        String cleaned = rawText;
        
        // Remove footnote references
        cleaned = FOOTNOTE_REFS.matcher(cleaned).replaceAll("");
        
        // Remove citation needed tags
        cleaned = CITATION_NEEDED.matcher(cleaned).replaceAll("");
        
        // Remove parenthetical year references (e.g., "(born 1985)")
        cleaned = PARENTHETICAL_REFS.matcher(cleaned).replaceAll("");
        
        // Remove pronunciation guides
        cleaned = PRONUNCIATION_GUIDES.matcher(cleaned).replaceAll("");
        
        // Normalize unicode characters
        cleaned = normalizeUnicodeCharacters(cleaned);
        
        // Clean up spacing and formatting
        cleaned = normalizeWhitespace(cleaned);
        
        // Remove empty sentences and clean up punctuation
        cleaned = cleanupPunctuation(cleaned);
        
        int finalLength = cleaned.length();
        
        return new CleanedText(cleaned, originalLength, finalLength);
    }
    
    /**
     * Cleans a list of text segments (paragraphs) and returns cleaned versions.
     * 
     * @param textSegments list of text segments to clean
     * @return list of cleaned text segments
     */
    public List<String> cleanTextSegments(List<String> textSegments) {
        List<String> cleanedSegments = new ArrayList<>();
        
        for (String segment : textSegments) {
            CleanedText cleaned = cleanText(segment);
            if (!cleaned.getCleanedText().trim().isEmpty()) {
                cleanedSegments.add(cleaned.getCleanedText());
            }
        }
        
        return cleanedSegments;
    }
    
    /**
     * Normalizes Unicode characters to their ASCII equivalents where appropriate.
     */
    private String normalizeUnicodeCharacters(String text) {
        String normalized = text;
        
        // Convert smart quotes to regular quotes
        normalized = UNICODE_QUOTES.matcher(normalized).replaceAll("\"");
        
        // Convert em dashes and en dashes to regular hyphens
        normalized = DASH_VARIANTS.matcher(normalized).replaceAll("-");
        
        // Handle other common Unicode normalizations
        normalized = normalized.replace("…", "...");
        normalized = normalized.replace("'", "'");
        normalized = normalized.replace("'", "'");
        
        return normalized;
    }
    
    /**
     * Normalizes whitespace throughout the text.
     */
    private String normalizeWhitespace(String text) {
        // Replace multiple spaces with single space
        String normalized = MULTIPLE_SPACES.matcher(text).replaceAll(" ");
        
        // Clean up line breaks and tabs
        normalized = normalized.replace("\t", " ");
        normalized = normalized.replace("\r\n", "\n");
        normalized = normalized.replace("\r", "\n");
        
        // Remove excessive line breaks
        normalized = normalized.replaceAll("\n\\s*\n\\s*\n", "\n\n");
        
        return normalized.trim();
    }
    
    /**
     * Cleans up punctuation and removes malformed sentences.
     */
    private String cleanupPunctuation(String text) {
        String cleaned = text;
        
        // Fix spacing around punctuation
        cleaned = cleaned.replaceAll("\\s+([.!?,:;])", "$1");
        cleaned = cleaned.replaceAll("([.!?])([A-Z])", "$1 $2");
        
        // Remove sentences that are too short or seem incomplete
        String[] sentences = cleaned.split("(?<=[.!?])\\s+");
        StringBuilder result = new StringBuilder();
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (isValidSentence(sentence)) {
                result.append(sentence);
                if (!sentence.endsWith(".") && !sentence.endsWith("!") && !sentence.endsWith("?")) {
                    result.append(".");
                }
                result.append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Validates whether a sentence should be included in the cleaned text.
     */
    private boolean isValidSentence(String sentence) {
        if (sentence.length() < 10) {
            return false;
        }
        
        // Check for reasonable word count
        String[] words = sentence.split("\\s+");
        if (words.length < 3) {
            return false;
        }
        
        // Avoid sentences that are mostly numbers or special characters
        long letterCount = sentence.chars().filter(Character::isLetter).count();
        return letterCount > sentence.length() * 0.5;
    }
    
    /**
     * Container class for cleaned text results.
     */
    public static class CleanedText {
        private final String cleanedText;
        private final int originalLength;
        private final int finalLength;
        
        public CleanedText(String cleanedText, int originalLength, int finalLength) {
            this.cleanedText = cleanedText;
            this.originalLength = originalLength;
            this.finalLength = finalLength;
        }
        
        public String getCleanedText() { return cleanedText; }
        public int getOriginalLength() { return originalLength; }
        public int getFinalLength() { return finalLength; }
        
        /**
         * Calculates the compression ratio of the cleaning process.
         */
        public double getCompressionRatio() {
            return originalLength > 0 ? (double) finalLength / originalLength : 0.0;
        }
        
        /**
         * Returns the number of characters removed during cleaning.
         */
        public int getCharactersRemoved() {
            return originalLength - finalLength;
        }
    }
} 