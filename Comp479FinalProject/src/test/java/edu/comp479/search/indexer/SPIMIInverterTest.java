package edu.comp479.search.indexer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import edu.comp479.search.indexer.file.IndexDataMapperFactory;
import edu.comp479.search.indexer.file.IndexWriter;
import edu.comp479.search.tokenizer.IToken;
import edu.comp479.search.tokenizer.ITokenStream;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Paths;

@ExtendWith(MockitoExtension.class)
class SPIMIInverterTest {

    SPIMIInverter spimiInverter;

    @Mock
    private ITokenStream tokenStreamMock;

    @Mock
    private IndexBlockBuilderFactory blockBuilderFactoryMock;

    @Mock
    private IndexDataMapperFactory indexDataMapperFactoryMock;

    @Mock
    private IndexWriter indexWriterMock;

    @Mock
    private IndexBlockBuilder indexBlockBuilderMock;

    @Mock
    private Runtime runtime;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        spimiInverter = new SPIMIInverter("test", tokenStreamMock, Paths.get("./TestIndex/"), 128l,
                blockBuilderFactoryMock, indexDataMapperFactoryMock, runtime);
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testPostingsAreAddedToTheBlockBuilder() {
        when(blockBuilderFactoryMock.createIndexBlockBuilder()).thenReturn(indexBlockBuilderMock);
        when(runtime.totalMemory()).thenReturn(1l);

        when(tokenStreamMock.hasNext()).then(returnTrueTimes(3));
        when(tokenStreamMock.next()).thenReturn(new Token("term1", 0), new Token("term2", 1), new Token("term3", 2));

        spimiInverter.invert();

        verify(indexBlockBuilderMock, times(1)).addPosting("term1", 0l);
        verify(indexBlockBuilderMock, times(1)).addPosting("term2", 1l);
        verify(indexBlockBuilderMock, times(1)).addPosting("term3", 2l);
        verify(indexBlockBuilderMock, times(3)).addPosting(anyString(), anyLong());
    }

    @Test
    void testBuildIndexCalledWhenComplete() throws IOException {
        when(blockBuilderFactoryMock.createIndexBlockBuilder()).thenReturn(indexBlockBuilderMock);
        when(indexDataMapperFactoryMock.createIndexWriter(any(), any())).thenReturn(indexWriterMock);

        when(runtime.totalMemory()).thenReturn(1l);

        when(tokenStreamMock.hasNext()).then(returnTrueTimes(3));
        when(tokenStreamMock.next()).thenReturn(new Token("term1", 0), new Token("term2", 1), new Token("term3", 2));

        when(indexBlockBuilderMock.getSize()).thenReturn(3);

        spimiInverter.invert();

        verify(indexBlockBuilderMock, times(1)).writeToDisk(eq(indexWriterMock));
    }

    @Test
    void testReturnANewblockWhenMemoryLimitIsHit() throws IOException {
        when(blockBuilderFactoryMock.createIndexBlockBuilder()).thenReturn(indexBlockBuilderMock);
        when(indexDataMapperFactoryMock.createIndexWriter(any(), any())).thenReturn(indexWriterMock);

        when(runtime.totalMemory()).thenReturn(1l, 1l, 250l * 1024 * 1024);

        when(tokenStreamMock.hasNext()).then(returnTrueTimes(3));
        when(tokenStreamMock.next()).thenReturn(new Token("term1", 0), new Token("term2", 1), new Token("term3", 2));

        when(indexBlockBuilderMock.getSize()).thenReturn(2).thenReturn(1);

        String blockName = spimiInverter.invert();

        verify(blockBuilderFactoryMock, times(1)).createIndexBlockBuilder();
        verify(tokenStreamMock, times(2)).next();
        assertEquals("test_0.blk", blockName);
    }

    @Test
    void testCreateANewblockWhenStreamIsFinished() throws IOException {
        when(blockBuilderFactoryMock.createIndexBlockBuilder()).thenReturn(indexBlockBuilderMock);
        when(indexDataMapperFactoryMock.createIndexWriter(any(), any())).thenReturn(indexWriterMock);

        when(runtime.totalMemory()).thenReturn(1l, 128l, 1l);

        when(tokenStreamMock.hasNext()).then(returnTrueTimes(3));
        when(tokenStreamMock.next()).thenReturn(new Token("term1", 0), new Token("term2", 1), new Token("term3", 2));
        when(indexBlockBuilderMock.getSize()).thenReturn(3);

        String block1Name = spimiInverter.invert();
        String block2Name = spimiInverter.invert();

        verify(blockBuilderFactoryMock, times(2)).createIndexBlockBuilder();
        verify(indexDataMapperFactoryMock, times(2)).createIndexWriter(any(), any());
        verify(indexBlockBuilderMock, times(2)).writeToDisk(eq(indexWriterMock));
        assertTrue(!block1Name.equals(block2Name), "Block1: " + block1Name + ", Block2: " + block2Name);
    }

    @Test
    void testCreateNewBlockWhenRestartedAfterMemLimitHit() throws IOException {
        when(blockBuilderFactoryMock.createIndexBlockBuilder()).thenReturn(indexBlockBuilderMock);
        when(indexDataMapperFactoryMock.createIndexWriter(any(), any())).thenReturn(indexWriterMock);

        when(runtime.totalMemory()).thenReturn(1l);
        when(tokenStreamMock.hasNext()).then(returnTrueTimes(3));

        when(tokenStreamMock.next()).thenReturn(new Token("term1", 0), new Token("term2", 1), new Token("term3", 2));
        when(indexBlockBuilderMock.getSize()).thenReturn(3);

        spimiInverter.invert();

        verify(blockBuilderFactoryMock, times(1)).createIndexBlockBuilder();
        verify(indexDataMapperFactoryMock, times(1)).createIndexWriter(any(), any());
        verify(indexBlockBuilderMock, times(1)).writeToDisk(eq(indexWriterMock));
        verify(tokenStreamMock, times(3)).next();
    }

    @Test
    void testReturnsNullOnIOException() throws IOException {
        when(blockBuilderFactoryMock.createIndexBlockBuilder()).thenReturn(indexBlockBuilderMock);
        when(indexDataMapperFactoryMock.createIndexWriter(any(), any())).thenThrow(IOException.class);

        when(runtime.totalMemory()).thenReturn(1l);
        when(tokenStreamMock.hasNext()).then(returnTrueTimes(3));
        when(tokenStreamMock.next()).thenReturn(new Token("term1", 0), new Token("term2", 1), new Token("term3", 2));
        when(indexBlockBuilderMock.getSize()).thenReturn(3);

        String blockName = spimiInverter.invert();

        assertNull(blockName, "Given: " + blockName);
    }

    private Answer<Boolean> returnTrueTimes(int times) {
        return new Answer<Boolean>() {
            int hasNextCalledTime = 0;

            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return hasNextCalledTime++ < times;
            }
        };
    }

    private static class Token implements IToken {
        private String term;
        private long docId;

        public Token(String term, long docId) {
            this.term = term;
            this.docId = docId;
        }

        @Override
        public String getTerm() {
            return term;
        }

        @Override
        public long getDocId() {
            return docId;
        }

    }

}
