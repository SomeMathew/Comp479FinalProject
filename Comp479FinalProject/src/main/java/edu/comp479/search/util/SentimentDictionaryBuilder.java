package edu.comp479.search.util;

import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public final class SentimentDictionaryBuilder {
    private static final String SENTIMENT_DICTIONARY_RESOURCE = "AFINN/AFINN-111.txt";

    public SentimentDictionaryBuilder() {
    }

    public Map<String, Integer> loadSentimentDictionary() throws IOException {
        ClassLoader cl = getClass().getClassLoader();
        Path path = Paths.get(cl.getResource(SENTIMENT_DICTIONARY_RESOURCE).getFile());

        return Files.lines(path).map((line) -> line.split("\\t"))
                .collect(toMap(entry -> entry[0], entry -> Integer.parseInt(entry[1])));
    }

    public static void main(String[] args) throws IOException {
        SentimentDictionaryBuilder builder = new SentimentDictionaryBuilder();
        Map<String, Integer> testDict = builder.loadSentimentDictionary();

        testDict.forEach((word, val) -> System.out.println(word + " : " + val));
    }
}
