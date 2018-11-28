package edu.comp479.search.indexer.file;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.Output;

import edu.comp479.search.index.structure.DictionaryEntryLinked;
import edu.comp479.search.index.structure.Posting;

import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class IndexReaderMemoryMappedPostingsTest {
    @Mock
    ByteBufferInput byteBufferInputMock;

    IndexReaderMemoryMapped reader;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        Files.createDirectories(Paths.get("./testIndex/"));
        Paths.get("./testIndex/testIndex.desc").toFile().createNewFile();
        Paths.get("./testIndex/testIndex.dic").toFile().createNewFile();
        Paths.get("./testIndex/testIndex.pst").toFile().createNewFile();

        try (Output output = new Output(Files.newOutputStream(Paths.get("./testIndex/testIndex.desc")))) {
            output.writeInt(IndexFileUtility.FILE_VERSION);
            output.writeLong(42);
        }
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
        Paths.get("./testIndex/testIndex.desc").toFile().delete();
        Paths.get("./testIndex/testIndex.dic").toFile().delete();
        Paths.get("./testIndex/testIndex.pst").toFile().delete();
    }

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        reader = new IndexReaderMemoryMapped("testIndex", Paths.get("./testIndex/"));
        reader.open(byteBufferInputMock);
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testReadTheRightOrderOfField() {
        when(byteBufferInputMock.readVarLong(true)).thenReturn(42l);
        when(byteBufferInputMock.readVarInt(true)).thenReturn(2);
        when(byteBufferInputMock.readVarFloat(IndexFileUtility.TFIDF_VAR_FLOAT_PRECISION, true)).thenReturn(1.25f);

        reader.readPostings(new DictionaryEntryLinked("test", 1, 2, 50));

        InOrder orderVerifier = inOrder(byteBufferInputMock);
        orderVerifier.verify(byteBufferInputMock).setPosition(50);
        orderVerifier.verify(byteBufferInputMock).readVarLong(true);
        orderVerifier.verify(byteBufferInputMock).readVarInt(true);
        orderVerifier.verify(byteBufferInputMock).readVarFloat(IndexFileUtility.TFIDF_VAR_FLOAT_PRECISION, true);
    }

    @Test
    void testReadTheCorrectAmountOfPostings() {
        when(byteBufferInputMock.readVarLong(true)).thenReturn(42l);
        when(byteBufferInputMock.readVarInt(true)).thenReturn(2);
        when(byteBufferInputMock.readVarFloat(IndexFileUtility.TFIDF_VAR_FLOAT_PRECISION, true)).thenReturn(1.25f);

        int docFreq = 25;
        reader.readPostings(new DictionaryEntryLinked("test", docFreq, 2, 50));

        verify(byteBufferInputMock, times(1)).setPosition(50);
        verify(byteBufferInputMock, times(docFreq)).readVarLong(true);
        verify(byteBufferInputMock, times(docFreq)).readVarInt(true);
        verify(byteBufferInputMock, times(docFreq)).readVarFloat(IndexFileUtility.TFIDF_VAR_FLOAT_PRECISION, true);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testReadDecodesValidDocDeltas() {
        when(byteBufferInputMock.readVarLong(true)).thenReturn(7l, 11l, 45l);
        when(byteBufferInputMock.readVarInt(true)).thenReturn(24);
        when(byteBufferInputMock.readVarFloat(IndexFileUtility.TFIDF_VAR_FLOAT_PRECISION, true)).thenReturn(1f);

        int docFreq = 3;
        List<Posting> postings = reader.readPostings(new DictionaryEntryLinked("test", docFreq, 3, 50));

        assertThat(postings, contains(hasProperty("docId", equalTo(7l)), hasProperty("docId", equalTo(18l)),
                hasProperty("docId", equalTo(63l))));
    }

    @Test
    void testReadDecodesValidPosting() {
        when(byteBufferInputMock.readVarLong(true)).thenReturn(7l, 11l, 45l);
        when(byteBufferInputMock.readVarInt(true)).thenReturn(24);
        when(byteBufferInputMock.readVarFloat(IndexFileUtility.TFIDF_VAR_FLOAT_PRECISION, true)).thenReturn(1.10f);

        Posting actualPosting = reader.readPostings(new DictionaryEntryLinked("test", 1, 3, 50)).get(0);

        Posting expectedPosting = new Posting(7l, 24, 1.10f);

        assertThat(actualPosting, samePropertyValuesAs(expectedPosting));
    }

    @Test
    void testReadSendRightOffset() {
        when(byteBufferInputMock.readVarLong(true)).thenReturn(1l);
        when(byteBufferInputMock.readVarInt(true)).thenReturn(24);
        when(byteBufferInputMock.readVarFloat(IndexFileUtility.TFIDF_VAR_FLOAT_PRECISION, true)).thenReturn(1.10f);

        List<Integer> offSets = Arrays.asList(50, 100, 150, 200, 250);
        for (int i : offSets) {
            reader.readPostings(new DictionaryEntryLinked("test", 1, 3, i));
        }

        ArgumentCaptor<Integer> offsetCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(byteBufferInputMock, times(offSets.size())).setPosition(offsetCaptor.capture());

        assertEquals(offSets, offsetCaptor.getAllValues());
    }
}
