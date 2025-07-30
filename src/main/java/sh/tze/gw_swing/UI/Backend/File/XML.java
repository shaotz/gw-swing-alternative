package sh.tze.gw_swing.UI.Backend.File;

import com.lexparser.scraper.nlp.AnnotatedToken;
import com.lexparser.scraper.nlp.SearchResult;

import javax.xml.stream.*;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class XML {

    /**
     * Converts a list of document data (List<List<List<AnnotatedToken>>>) to XML string representation
     */
    public static String toXML(List<List<List<AnnotatedToken>>> documents,
                               List<String> urls,
                               List<String> dates,
                               List<String> filterSchemes) {
        StringWriter stringWriter = new StringWriter();
        try {
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(stringWriter);

            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement("corpus-collection");

            // List<List<AnnotatedToken>> is a document
            for (int docIndex = 0; docIndex < documents.size(); docIndex++) {
                List<List<AnnotatedToken>> document = documents.get(docIndex);

                writer.writeStartElement("document");
                writer.writeAttribute("id", String.valueOf(docIndex));

                writer.writeStartElement("metadata");

                // URL
                if (urls != null && docIndex < urls.size()) {
                    writer.writeStartElement("url");
                    writer.writeCharacters(urls.get(docIndex));
                    writer.writeEndElement();
                }

                // Date, actually not sure if to add date
                // because the page retrieval date is not recorded,
                // and as for the XML file creation date, file system have it
                if (dates != null && docIndex < dates.size()) {
                    writer.writeStartElement("date");
                    writer.writeCharacters(dates.get(docIndex));
                    writer.writeEndElement();
                }

                // FilterScheme
                if (filterSchemes != null && docIndex < filterSchemes.size()) {
                    writer.writeStartElement("filter-scheme");
                    writer.writeCharacters(filterSchemes.get(docIndex));
                    writer.writeEndElement();
                }

                writer.writeEndElement(); // end metadata

                // processe sentence
                writer.writeStartElement("sentences");

                for (List<AnnotatedToken> sentence : document) {
                    writer.writeStartElement("sentence");

                    for (AnnotatedToken token : sentence) {
                        writer.writeStartElement("token");

                        // WordForm
                        writer.writeStartElement("form");
                        writer.writeCharacters(token.getForm());
                        writer.writeEndElement();

                        // POS tag
                        writer.writeStartElement("pos");
                        writer.writeCharacters(token.getPos());
                        writer.writeEndElement();

                        // Lemma
                        writer.writeStartElement("lemma");
                        writer.writeCharacters(token.getLemma());
                        writer.writeEndElement();

                        writer.writeEndElement(); // end token
                    }

                    writer.writeEndElement(); // end sentence
                }

                writer.writeEndElement(); // end sentences
                writer.writeEndElement(); // end document
            }

            writer.writeEndElement(); // end corpus-collection
            writer.writeEndDocument();

            writer.flush();
            writer.close();

            return stringWriter.toString();

        } catch (XMLStreamException e) {
            throw new RuntimeException("Error creating XML: " + e.getMessage(), e);
        }
    }

    public static String toXMLFromMultipleSearchResults(
            Map<String, List<List<SearchResult>>> urlToResultsMap,
            Map<String, String> urlToDateMap,
            Map<String, String> urlToFilterSchemeMap) {

        StringWriter stringWriter = new StringWriter();
        try {
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(stringWriter);

            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement("corpus-collection");

            // Process each URL and its search results
            int docId = 0;
            for (Map.Entry<String, List<List<SearchResult>>> entry : urlToResultsMap.entrySet()) {
                String url = entry.getKey();
                List<List<SearchResult>> searchResults = entry.getValue();

                if (searchResults.isEmpty()) continue;

                writer.writeStartElement("document");
                writer.writeAttribute("id", String.valueOf(docId++));

                // Write metadata
                writer.writeStartElement("metadata");

                // URL
                writer.writeStartElement("url");
                writer.writeCharacters(url != null ? url : "");
                writer.writeEndElement();

                // Date
                String date = urlToDateMap.get(url);
                writer.writeStartElement("date");
                writer.writeCharacters(date != null ? date : "");
                writer.writeEndElement();

                // FilterScheme
                String filterScheme = urlToFilterSchemeMap.get(url);
                writer.writeStartElement("filter-scheme");
                writer.writeCharacters(filterScheme != null ? filterScheme : "");
                writer.writeEndElement();

                writer.writeEndElement(); // end metadata

                // Write sentences
                writer.writeStartElement("sentences");

                for (List<SearchResult> resultGroup : searchResults) {
                    for (SearchResult result : resultGroup) {
                        writer.writeStartElement("sentence");

                        List<AnnotatedToken> sentence = result.getSentence();
                        int matchIndex = result.getIndex();

                        for (int i = 0; i < sentence.size(); i++) {
                            AnnotatedToken token = sentence.get(i);
                            writer.writeStartElement("token");

                            if (i == matchIndex) {
                                writer.writeAttribute("matched", "true");
                            }

                            writer.writeStartElement("form");
                            writer.writeCharacters(token.getForm());
                            writer.writeEndElement();

                            writer.writeStartElement("pos");
                            writer.writeCharacters(token.getPos());
                            writer.writeEndElement();

                            writer.writeStartElement("lemma");
                            writer.writeCharacters(token.getLemma());
                            writer.writeEndElement();

                            writer.writeEndElement(); // end token
                        }

                        writer.writeEndElement(); // end sentence
                    }
                }

                writer.writeEndElement(); // end sentences
                writer.writeEndElement(); // end document
            }

            writer.writeEndElement(); // end corpus-collection
            writer.writeEndDocument();

            writer.flush();
            writer.close();

            return stringWriter.toString();

        } catch (XMLStreamException e) {
            throw new RuntimeException("Error creating XML: " + e.getMessage(), e);
        }
    }
}