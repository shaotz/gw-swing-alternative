package com.lexparser.scraper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts and structures content from Wikipedia HTML documents.
 * Focuses on main article content while filtering out navigation and metadata elements.
 */
public class WikipediaContentExtractor {

    /**
     * Extracts structured content from a Wikipedia document.
     * 
     * @param document the Wikipedia HTML document
     * @return ExtractedContent containing organized page content
     */
    public ExtractedContent extractContent(Document document) {
        if (document == null) {
            return new ExtractedContent("", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        String title = extractTitle(document);
        List<String> sections = extractSections(document);
        List<String> paragraphs = extractParagraphs(document);
        List<String> infoboxData = extractInfoboxData(document);

        return new ExtractedContent(title, sections, paragraphs, infoboxData);
    }

    /**
     * Extracts the main article title from the document.
     */
    private String extractTitle(Document document) {
        Element titleElement = document.select(".mw-page-title-main").first();
        if (titleElement != null) {
            return titleElement.text().trim();
        }
        
        // Fallback to h1 element
        titleElement = document.select("h1").first();
        return titleElement != null ? titleElement.text().trim() : "Unknown Title";
    }

    /**
     * Extracts section headers from the Wikipedia article.
     */
    private List<String> extractSections(Document document) {
        List<String> sections = new ArrayList<>();
        Elements sectionHeaders = document.select("h2 .mw-headline, h3 .mw-headline");
        
        for (Element header : sectionHeaders) {
            String sectionText = header.text().trim();
            if (!sectionText.isEmpty() && !isUnwantedSection(sectionText)) {
                sections.add(sectionText);
            }
        }
        
        return sections;
    }

    /**
     * Extracts main content paragraphs from the article body.
     */
    private List<String> extractParagraphs(Document document) {
        List<String> paragraphs = new ArrayList<>();
        Element contentDiv = document.getElementById("mw-content-text");
        
        if (contentDiv == null) {
            return paragraphs;
        }

        Elements paragraphElements = contentDiv.select(".mw-parser-output > p");
        
        for (Element paragraph : paragraphElements) {
            String text = paragraph.text().trim();
            if (!text.isEmpty() && text.length() > 20) { // Filter out very short paragraphs
                paragraphs.add(text);
            }
        }
        
        return paragraphs;
    }

    /**
     * Extracts key-value data from Wikipedia infoboxes.
     */
    private List<String> extractInfoboxData(Document document) {
        List<String> infoboxData = new ArrayList<>();
        Elements infoboxes = document.select(".infobox");
        
        for (Element infobox : infoboxes) {
            Elements rows = infobox.select("tr");
            for (Element row : rows) {
                Elements cells = row.select("th, td");
                if (cells.size() >= 2) {
                    String label = cells.get(0).text().trim();
                    String value = cells.get(1).text().trim();
                    if (!label.isEmpty() && !value.isEmpty()) {
                        infoboxData.add(label + ": " + value);
                    }
                }
            }
        }
        
        return infoboxData;
    }

    /**
     * Checks if a section should be excluded from extraction.
     */
    private boolean isUnwantedSection(String sectionName) {
        String lowerSection = sectionName.toLowerCase();
        return lowerSection.contains("references") ||
               lowerSection.contains("external links") ||
               lowerSection.contains("see also") ||
               lowerSection.contains("notes") ||
               lowerSection.contains("bibliography");
    }

    /**
     * Container class for extracted Wikipedia content.
     */
    public static class ExtractedContent {
        private final String title;
        private final List<String> sections;
        private final List<String> paragraphs;
        private final List<String> infoboxData;

        public ExtractedContent(String title, List<String> sections, 
                              List<String> paragraphs, List<String> infoboxData) {
            this.title = title;
            this.sections = new ArrayList<>(sections);
            this.paragraphs = new ArrayList<>(paragraphs);
            this.infoboxData = new ArrayList<>(infoboxData);
        }

        public String getTitle() { return title; }
        public List<String> getSections() { return new ArrayList<>(sections); }
        public List<String> getParagraphs() { return new ArrayList<>(paragraphs); }
        public List<String> getInfoboxData() { return new ArrayList<>(infoboxData); }
        
        /**
         * Returns the total word count of all paragraphs.
         */
        public int getWordCount() {
            return paragraphs.stream()
                    .mapToInt(p -> p.split("\\s+").length)
                    .sum();
        }
        
        /**
         * Combines all paragraph text into a single string.
         */
        public String getCombinedText() {
            return String.join(" ", paragraphs);
        }
    }
} 