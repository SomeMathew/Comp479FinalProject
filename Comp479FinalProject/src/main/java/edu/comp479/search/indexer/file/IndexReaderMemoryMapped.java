package edu.comp479.search.indexer.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.google.common.collect.ImmutableList;

import edu.comp479.search.index.structure.DictionaryEntry;
import edu.comp479.search.index.structure.DictionaryEntryLinked;
import edu.comp479.search.index.structure.Posting;

import static com.google.common.base.Preconditions.*;

public class IndexReaderMemoryMapped extends IndexReader {
    private MappedByteBuffer postingsMappedByteBuffer;
    private ByteBufferInput postingsByteBufferInput;

    public IndexReaderMemoryMapped(String indexName, Path dir) throws IOException {
        super(indexName, dir);
    }

    public ImmutableList<Posting> readPostings(DictionaryEntryLinked dictionaryEntry) {
        checkNotNull(dictionaryEntry);
        long offset = dictionaryEntry.getPostingsOffset();
        assert offset <= Integer.MAX_VALUE : "The Current implementation does not support files addressable with 64bits offset";

        long docFreq = dictionaryEntry.getDocFreq();
        postingsByteBufferInput.setPosition((int) offset);
        return decodePostingsList(postingsByteBufferInput, docFreq);
    }

    public ImmutableList<Posting> readPostings(DictionaryEntry dictionaryEntry) {
        checkArgument(dictionaryEntry instanceof DictionaryEntryLinked,
                "The entry needs to be linked to Posting on disk.");
        return readPostings((DictionaryEntryLinked) dictionaryEntry);
    }

    @Override
    public boolean openDictionary() {
        // This shouldn't be used in this mode
        throw new UnsupportedOperationException("Dictionary Shouldn't be read in memory mapped mode.");
    }

    @Override
    public boolean openPostings() throws IOException {
        try (FileChannel fileChannel = FileChannel.open(postingsPath)) {
            postingsMappedByteBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
            postingsByteBufferInput = new ByteBufferInput(postingsMappedByteBuffer);
        }
        return true;
    }

    /**
     * Override the creation of the ByteBufferInput for postings with a custom
     * object.
     * 
     * This is used mainly for testing the class with a mock.
     * 
     * @param postingsByteBufferInput
     * @return true if successful.
     */
    public boolean openPostings(ByteBufferInput postingsByteBufferInput) {
        this.postingsByteBufferInput = checkNotNull(postingsByteBufferInput);
        return true;
    }

    @Override
    public void close() throws IOException {
        postingsByteBufferInput.close();
        postingsByteBufferInput.reset();
        // We want to make sure the MappedByteBuffer gets GC
        postingsByteBufferInput.setBuffer(ByteBuffer.allocate(1));
        postingsByteBufferInput = null;
        postingsMappedByteBuffer = null;
    }

}
