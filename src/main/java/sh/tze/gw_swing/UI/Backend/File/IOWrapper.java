package sh.tze.gw_swing.UI.Backend.File;

import com.lexparser.scraper.nlp.AnnotatedToken;
import com.lexparser.scraper.nlp.NLPProcessing;
import com.lexparser.scraper.nlp.SearchResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IOWrapper {

    public static void saveToXML(List<List<List<AnnotatedToken>>> documents,
                                 List<String> urls,
                                 List<String> filterSchemes,
                                 String filePath) throws IOException {

        List<String> dates = new ArrayList<>();
        dates.add(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        String xmlContent = XML.toXML(documents, urls, dates, filterSchemes);
        Files.writeString(Paths.get(filePath), xmlContent, StandardCharsets.UTF_8);
    }

    public static void saveToXML(NLPProcessing nlpProcessing,
                                 String url,
                                 String filterScheme,
                                 String filePath) throws IOException {

        // sort of "upcast"
        List<List<List<AnnotatedToken>>> documents = new ArrayList<>();
        documents.add(nlpProcessing.getWordSentences());

        List<String> urls = new ArrayList<>();
        urls.add(url);


        List<String> filterSchemes = new ArrayList<>();
        filterSchemes.add(filterScheme);

        saveToXML(documents, urls, filterSchemes, filePath);
    }

    public static void saveSearchResultsToXML(List<List<SearchResult>> searchResults,
                                              String url,
                                              String filterScheme,
                                              String filePath) throws IOException {
        String date = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String xmlContent = XML.toXMLFromSearchResults(searchResults, url, date, filterScheme);
        Files.writeString(Paths.get(filePath), xmlContent, StandardCharsets.UTF_8);
    }

    public static void saveCorpusMapToXML(Map<String, NLPProcessing> corpusMap,
                                          Map<String, String> filterSchemes,
                                          String filePath) throws IOException {
        List<List<List<AnnotatedToken>>> documents = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        List<String> schemesList = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        String currentDate = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        for (Map.Entry<String, NLPProcessing> entry : corpusMap.entrySet()) {
            String url = entry.getKey();
            NLPProcessing nlpProcessing = entry.getValue();

            documents.add(nlpProcessing.getWordSentences());
            urls.add(url);
            dates.add(currentDate);

            // Add filter scheme if available, otherwise empty string
            String scheme = filterSchemes != null && filterSchemes.containsKey(url) ?
                    filterSchemes.get(url) : "";
            schemesList.add(scheme);
        }

        saveToXML(documents, urls, schemesList, filePath);
    }
    public static void saveMultipleSearchResultsToXML(
            Map<String, List<List<SearchResult>>> urlToResultsMap,
            Map<String, String> urlToDateMap,
            Map<String, String> urlToFilterSchemeMap,
            String filePath) throws IOException {

        String xmlContent = XML.toXMLFromMultipleSearchResults(
                urlToResultsMap, urlToDateMap, urlToFilterSchemeMap);

        try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
            writer.write(xmlContent);
        }
    }
}