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