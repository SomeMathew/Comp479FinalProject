package edu.comp479.search.indexer;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.util.stream.Collectors.*;
import static java.util.Comparator.*;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

import com.google.common.collect.HashMultiset;

import static com.google.common.collect.Comparators.*;

import edu.comp479.search.index.structure.DictionaryEntry;
import edu.comp479.search.index.structure.Posting;
import edu.comp479.search.indexer.file.IndexWriter;
import edu.comp479.search.util.SentimentDictionaryBuilder;

@ExtendWith(MockitoExtension.class)
class IndexBlockBuilderTest {
    private IndexBlockBuilder builder;

    private static String[] doc1 = { "singleword1", "doubleword1", "doubleword1", "fiveword1", "fiveword1", "fiveword1",
            "fiveword1", "fiveword1", "wordinbothsingle", "wordinbothdouble", "wordinbothdouble" };

    private static String[] doc2 = { "singleword2", "doubleword2", "doubleword2", "fiveword2", "fiveword2", "fiveword2",
            "fiveword2", "fiveword2", "wordinbothsingle", "wordinbothdouble", "wordinbothdouble" };

    private static String[] doc3 = { "wordinbothsingle", "wordinbothdouble", "wordinbothdouble" };

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
        builder = new IndexBlockBuilder(new SentimentDictionaryBuilder().loadSentimentDictionary());
    }

    @Test
    void testTermFrequencyInOnePosting() {
        for (String s : doc1) {
            builder.addPosting(s, 1);
        }
        Map<String, HashMultiset<Long>> block = builder.getBlock();
        assertAll(() -> assertEquals(1, block.get("singleword1").count(1l)),
                () -> assertEquals(2, block.get("doubleword1").count(1l)),
                () -> assertEquals(5, block.get("fiveword1").count(1l)));
    }

    @Test
    void testPostingCountForATerm() {
        for (String s : doc1) {
            builder.addPosting(s, 1);
        }
        for (String s : doc2) {
            builder.addPosting(s, 2);
        }
        Map<String, HashMultiset<Long>> block = builder.getBlock();

        assertAll(() -> assertThat(block.get("wordinbothsingle"), hasSize(2)),
                () -> assertThat(block.get("wordinbothsingle"), hasItems(1l, 2l)));
    }

    @Test
    void testPostingCountAndFrequency() {
        for (String s : doc1) {
            builder.addPosting(s, 1);
        }
        for (String s : doc2) {
            builder.addPosting(s, 2);
        }

        Map<String, HashMultiset<Long>> block = builder.getBlock();

        assertAll(() -> assertThat(block.get("wordinbothdouble"), hasSize(4)),
                () -> assertThat(block.get("wordinbothdouble"), hasItems(1l, 2l)),
                () -> assertEquals(2, block.get("wordinbothdouble").count(1l)),
                () -> assertEquals(2, block.get("wordinbothdouble").count(2l)));
    }

    @Test
    void testFailOnEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> builder.addPosting("", 42));
    }

    @Test
    void testFailOnNullString() {
        assertThrows(IllegalArgumentException.class, () -> builder.addPosting(null, 42));
    }

    @Test
    void testFailOnNegativeDocId() {
        assertThrows(IllegalArgumentException.class, () -> builder.addPosting("testterm", -42));
    }

    @Test
    void testWriteToDiskCalledForEachTerm() throws IOException {
        for (String s : doc1) {
            builder.addPosting(s, 1);
        }
        IndexWriter indexWriterMock = mock(IndexWriter.class);

        builder.writeToDisk(indexWriterMock);

        verify(indexWriterMock, times((int) Arrays.stream(doc1).distinct().count())).write(any(), any());
    }

    @Test
    void testWriteToDiskCalledInOrder() throws IOException {
        for (String s : doc1) {
            builder.addPosting(s, 1);
        }
        IndexWriter indexWriterMock = mock(IndexWriter.class);

        builder.writeToDisk(indexWriterMock);

        List<Matcher<? super DictionaryEntry>> termsInOrder = Arrays.stream(doc1).sorted().distinct()
                .map((term) -> hasProperty("term", equalTo(term))).collect(toList());

        ArgumentCaptor<DictionaryEntry> dictCaptor = ArgumentCaptor.forClass(DictionaryEntry.class);
        verify(indexWriterMock, times(termsInOrder.size())).write(dictCaptor.capture(), any());

        assertThat(dictCaptor.getAllValues(), contains(termsInOrder));
    }

    @Test
    void testWriteToDiskPostingsInOrder() throws IOException {
        for (String s : doc3) {
            builder.addPosting(s, 3);
        }

        for (String s : doc1) {
            builder.addPosting(s, 1);
        }

        for (String s : doc2) {
            builder.addPosting(s, 20);
        }

        for (String s : doc1) {
            builder.addPosting(s, 5);
        }

        for (String s : doc2) {
            builder.addPosting(s, 2);
        }

        IndexWriter indexWriterMock = mock(IndexWriter.class);

        builder.writeToDisk(indexWriterMock);

        Set<String> allWords = new TreeSet<>(Arrays.asList(doc1));
        allWords.addAll(Arrays.asList(doc2));
        allWords.addAll(Arrays.asList(doc3));

        verify(indexWriterMock, times(allWords.size())).write(any(),
                argThat((postings) -> isInStrictOrder(postings, comparing(Posting::getDocId))));
    }
}
