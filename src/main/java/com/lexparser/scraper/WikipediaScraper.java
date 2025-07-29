package com.lexparser.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WikipediaScraper {
    private String url;

    public WikipediaScraper(String url) {
        this.url = url;
    }

    public String scrapeContent() {
        try {
            // Connect to the URL and get the HTML document
            Document doc = Jsoup.connect(url).get();
            
            // Get the main content div (this is specific to Wikipedia's structure)
            Element content = doc.getElementById("mw-content-text");
            
            // Get all paragraphs from the content
            Elements paragraphs = content.select("p");
            
            // StringBuilder to store the cleaned text
            StringBuilder cleanedText = new StringBuilder();
            
            // Process each paragraph
            for (Element paragraph : paragraphs) {
                // Get the text and clean it
                String text = paragraph.text();
                if (!text.isEmpty()) {
                    cleanedText.append(text).append("\n\n");
                }
            }
            
            return cleanedText.toString();
            
        } catch (Exception e) {
            return "Error scraping the page: " + e.getMessage();
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        // Example usage
        String wikipediaUrl = "https://en.wikipedia.org/wiki/Java_(programming_language)";
        WikipediaScraper scraper = new WikipediaScraper(wikipediaUrl);
        String content = scraper.scrapeContent();
        System.out.println(content);
    }
} 