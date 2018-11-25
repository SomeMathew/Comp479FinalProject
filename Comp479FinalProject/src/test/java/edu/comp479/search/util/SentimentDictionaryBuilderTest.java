package edu.comp479.search.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SentimentDictionaryBuilderTest {

    private SentimentDictionaryBuilder builder;

    @BeforeEach
    void setUp() throws Exception {
        builder = new SentimentDictionaryBuilder();
    }

    @Test
    public void testDictionaryHasAllEntries() throws IOException {
        Map<String, Short> dict = builder.loadSentimentDictionary();
        assertEquals(2477, dict.size());
    }

    @Test
    public void testDictionaryNoEmptyWord() throws IOException {
        Map<String, Short> dict = builder.loadSentimentDictionary();
        Matcher<Set<String>> allOf = allOf(not(empty()));
        assertThat(dict.keySet(), allOf);
    }

    @Test
    public void testValuesNotNull() throws IOException {
        Map<String, Short> dict = builder.loadSentimentDictionary();
        Matcher<Collection<Short>> allOf = allOf(not(nullValue()));
        assertThat(dict.values(), allOf);
    }
}
