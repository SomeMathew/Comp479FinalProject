package edu.comp479.search.util;

import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SentimentDictionaryBuilder {
    private static Logger LOGGER = Logger.getLogger(SentimentDictionaryBuilder.class.getName());

    private static final String SENTIMENT_DICTIONARY_RESOURCE = "AFINN/AFINN-111.txt";

    public SentimentDictionaryBuilder() {
    }

    public Map<String, Integer> loadSentimentDictionary() throws IOException {
        URI uri = null;
        try {
            uri = ClassLoader.getSystemResource(SENTIMENT_DICTIONARY_RESOURCE).toURI();
        } catch (URISyntaxException e) {
            LOGGER.log(Level.WARNING, "Problem loading the AFINN dictionary", e);
            return null;
        }
        Path path = Paths.get(uri);

        return Files.lines(path).map((line) -> line.split("\\t"))
                .collect(toMap(entry -> entry[0], entry -> Integer.parseInt(entry[1])));
    }

    public static void main(String[] args) throws IOException {
        SentimentDictionaryBuilder builder = new SentimentDictionaryBuilder();
        Map<String, Integer> testDict = builder.loadSentimentDictionary();

        testDict.forEach((word, val) -> System.out.println(word + " : " + val));
    }
}
