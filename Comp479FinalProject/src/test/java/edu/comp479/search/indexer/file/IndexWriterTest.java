package edu.comp479.search.indexer.file;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.kryo.io.Input;

import edu.comp479.search.index.DictionaryEntry;
import edu.comp479.search.index.Posting;
import edu.comp479.search.indexer.file.IndexWriter.WriterMode;

//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.*;

class IndexWriterTest {
    private IndexWriter writer;
    private Path descriptorPath;
    private Path dictionaryPath;
    private Path postingsPath;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
        writer = new IndexWriter("testIndex", "./testIndex/", WriterMode.STREAM);
        descriptorPath = writer.getDescriptorPath();
        dictionaryPath = writer.getDictionaryPath();
        postingsPath = writer.getPostingsPath();
    }

    @AfterEach
    void tearDown() throws Exception {
        writer.getDictionaryPath().toFile().delete();
        writer.getDescriptorPath().toFile().delete();
        writer.getPostingsPath().toFile().delete();
    }

    @Test
    void testPostingsCanBeRead() throws IOException {
        List<Posting> postings = new ArrayList<>();
        List<Object> expected = new ArrayList<>();
        postings.add(new Posting(4, 1, 3.25f));
        expected.add(new Long(4));
        expected.add(new Integer(1));
        expected.add(new Float(3.25f));

        postings.add(new Posting(5, 2, 1.1f));
        expected.add(new Long(1));
        expected.add(new Integer(2));
        expected.add(new Float(1.1f));

        postings.add(new Posting(42, 3, 0));
        expected.add(new Long(37));
        expected.add(new Integer(3));
        expected.add(new Float(0));

        writer.write(new DictionaryEntry("test", 3, 0), postings);
        writer.writeDescriptor();
        writer.close();

        Input input = new Input(Files.newInputStream(postingsPath));

        List<Object> actual = new ArrayList<>();

        actual.add(input.readVarLong(true));
        actual.add(input.readVarInt(true));
        actual.add(input.readVarFloat(1000.0f, true));

        actual.add(input.readVarLong(true));
        actual.add(input.readVarInt(true));
        actual.add(input.readVarFloat(1000.0f, true));

        actual.add(input.readVarLong(true));
        actual.add(input.readVarInt(true));
        actual.add(input.readVarFloat(1000.0f, true));
        input.close();

        assertIterableEquals(expected, actual);
    }

    @Test
    void testPostingsDeltaAreValid() throws IOException {
        List<Posting> postings = new ArrayList<>();
        postings.add(new Posting(4, 1, 0));
        postings.add(new Posting(5, 1, 0));
        postings.add(new Posting(42, 1, 0));

        writer.write(new DictionaryEntry("test", 3, 0), postings);
        writer.writeDescriptor();
        writer.close();

        Input input = new Input(Files.newInputStream(postingsPath));

        long docDelta1 = input.readVarLong(true);
        input.readVarInt(true);
        input.readVarFloat(1000.0f, true);

        long docDelta2 = input.readVarLong(true);
        input.readVarInt(true);
        input.readVarFloat(1000.0f, true);

        long docDelta3 = input.readVarLong(true);
        input.readVarInt(true);
        input.readVarFloat(1000.0f, true);

        input.close();

        assertAll(() -> assertEquals(4, docDelta1), () -> assertEquals(1, docDelta2),
                () -> assertEquals(37, docDelta3));
    }

    @Test
    void testDictionaryCanBeRead() throws IOException {
        List<Posting> postings = new ArrayList<>();
        postings.add(new Posting(4, 1, 0));
        postings.add(new Posting(5, 1, 0));
        postings.add(new Posting(42, 1, 0));

        writer.write(new DictionaryEntry("test1", 3, 0), postings);

        postings = new ArrayList<>();
        postings.add(new Posting(4, 1, 0));
        postings.add(new Posting(5, 1, 0));

        writer.write(new DictionaryEntry("test2", 2, -2), postings);
        writer.writeDescriptor();
        writer.close();

        Input input = new Input(Files.newInputStream(dictionaryPath));

        String term1 = input.readString();
        int docFreq1 = input.readVarInt(true);
        long freqDelta1 = input.readVarLong(true);
        int sentiment1 = input.readVarInt(false);

        String term2 = input.readString();
        int docFreq2 = input.readVarInt(true);
        long freqDelta2 = input.readVarLong(true);
        int sentiment2 = input.readVarInt(false);

        input.close();

        assertAll(() -> assertEquals("test1", term1), () -> assertEquals(3, docFreq1),
                () -> assertEquals(0, sentiment1), () -> assertEquals("test2", term2), () -> assertEquals(2, docFreq2),
                () -> assertEquals(-2, sentiment2));

    }

    @Test
    void testDictionaryLinksToRightPostings() throws IOException {
        List<Posting> postings = new ArrayList<>();
        postings.add(new Posting(4, 1, 0));
        postings.add(new Posting(5, 1, 0));
        postings.add(new Posting(42, 1, 0));

        writer.write(new DictionaryEntry("test1", 3, 0), postings);

        postings = new ArrayList<>();
        postings.add(new Posting(4, 1, 0));
        postings.add(new Posting(5, 1, 0));
        postings.add(new Posting(42, 1, 0));

        writer.write(new DictionaryEntry("test2", 3, 0), postings);

        List<Object> expectedPostings = new ArrayList<>();
        postings = new ArrayList<>();
        postings.add(new Posting(4, 1, 3.25f));
        expectedPostings.add(new Long(4));
        expectedPostings.add(new Integer(1));
        expectedPostings.add(new Float(3.25f));

        postings.add(new Posting(5, 2, 1.1f));
        expectedPostings.add(new Long(1));
        expectedPostings.add(new Integer(2));
        expectedPostings.add(new Float(1.1f));

        writer.write(new DictionaryEntry("test3", 2, -2), postings);
        writer.writeDescriptor();
        writer.close();

        Input inputDict = new Input(Files.newInputStream(dictionaryPath));

        String term1 = inputDict.readString();
        int docFreq1 = inputDict.readVarInt(true);
        long freqDelta1 = inputDict.readVarLong(true);
        int sentiment1 = inputDict.readVarInt(false);

        String term2 = inputDict.readString();
        int docFreq2 = inputDict.readVarInt(true);
        long freqDelta2 = inputDict.readVarLong(true);
        int sentiment2 = inputDict.readVarInt(false);

        String term3 = inputDict.readString();
        int docFreq3 = inputDict.readVarInt(true);
        long freqDelta3 = inputDict.readVarLong(true);
        int sentiment3 = inputDict.readVarInt(false);

        inputDict.close();

        Input inputPosting = new Input(Files.readAllBytes(postingsPath));
        inputPosting.setPosition((int) (freqDelta1 + freqDelta2 + freqDelta3));

        List<Object> actualPostings = new ArrayList<>();

        actualPostings.add(inputPosting.readVarLong(true));
        actualPostings.add(inputPosting.readVarInt(true));
        actualPostings.add(inputPosting.readVarFloat(1000.0f, true));

        actualPostings.add(inputPosting.readVarLong(true));
        actualPostings.add(inputPosting.readVarInt(true));
        actualPostings.add(inputPosting.readVarFloat(1000.0f, true));

        inputPosting.close();

        assertIterableEquals(expectedPostings, actualPostings);
    }

    @Test
    void testDescriptorIsValid() throws IOException {
        long expectedTermCount = 10;

        for (int i = 0; i < expectedTermCount; i++) {
            List<Posting> postings = new ArrayList<>();
            postings.add(new Posting(4, 1, 0));
            postings.add(new Posting(5, 1, 0));
            postings.add(new Posting(42, 1, 0));

            writer.write(new DictionaryEntry("test" + i, 3, 0), postings);
        }
        writer.writeDescriptor();
        writer.close();

        Input inputDescriptor = new Input(Files.readAllBytes(descriptorPath));

        int fileVersion = inputDescriptor.readInt();
        long actualTermCount = inputDescriptor.readLong();

        inputDescriptor.close();

        assertEquals(expectedTermCount, actualTermCount);
    }
}
