package edu.comp479.search.indexer.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;

import edu.comp479.search.index.structure.DictionaryEntry;
import edu.comp479.search.index.structure.DictionaryEntryLinked;
import edu.comp479.search.index.structure.Posting;

import static com.google.common.base.Preconditions.*;

public class IndexReaderMemoryMapped extends IndexReader {
    private MappedByteBuffer postingsMappedByteBuffer;
    private MappedByteBuffer normsMappedByteBuffer;

    private ByteBufferInput postingsByteBufferInput;
    private ByteBufferInput normsByteBufferInput;

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

    public NormFileEntry readNormEntry(long docId) {
        long offset = IndexFileUtility.NORM_HEADER_SIZE + docId * IndexFileUtility.NORM_ENTRY_SIZE;

        normsByteBufferInput.setPosition((int) offset);

        NormFileEntry normEntry = decodeNormEntry(normsByteBufferInput);
        Verify.verify(normEntry.getDocId() == docId,
                "Error when reading the norm file, non-matching docId. Wanted: %s, Got: %s", docId,
                normEntry.getDocId());
        return normEntry;
    }

    public boolean open() throws IOException {
        try (FileChannel postingsChannel = FileChannel.open(postingsPath);
                FileChannel normsChannel = FileChannel.open(normsPath)) {
            postingsMappedByteBuffer = postingsChannel.map(MapMode.READ_ONLY, 0, postingsChannel.size());
            postingsByteBufferInput = new ByteBufferInput(postingsMappedByteBuffer);

            normsMappedByteBuffer = normsChannel.map(MapMode.READ_ONLY, 0, normsChannel.size());
            normsByteBufferInput = new ByteBufferInput(normsMappedByteBuffer);
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

    /**
     * Override the creation of the ByteBufferInput for norms with a custom object.
     * 
     * This is used mainly for testing the class with a mock.
     * 
     * @param normsByteBufferInput
     * @return true if successful.
     */
    public boolean openNorms(ByteBufferInput normsByteBufferInput) {
        this.normsByteBufferInput = checkNotNull(normsByteBufferInput);
        return true;
    }

    @Override
    public void close() throws IOException {
        postingsByteBufferInput.close();
        postingsByteBufferInput.reset();

        normsByteBufferInput.close();
        normsByteBufferInput.reset();
        // We want to make sure the MappedByteBuffer gets GC
        postingsByteBufferInput.setBuffer(ByteBuffer.allocate(1));
        postingsByteBufferInput = null;
        postingsMappedByteBuffer = null;

        normsByteBufferInput.setBuffer(ByteBuffer.allocate(1));
        normsByteBufferInput = null;
        normsMappedByteBuffer = null;
    }

}
