package edu.comp479.search.indexer.file;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Verify.*;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import com.esotericsoftware.kryo.io.Input;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.comp479.search.index.structure.DictionaryEntry;
import edu.comp479.search.index.structure.DictionaryEntryLinked;
import edu.comp479.search.index.structure.Posting;

public abstract class IndexReader implements Closeable {
    protected final String indexName;
    protected final Path directory;
    protected final long termCount;

    protected Path dictionaryPath;
    protected Path postingsPath;
    protected Path descriptorPath;

    /**
     * Creates a new IndexReader.
     * 
     * <p>
     * By default the only file read on construction is the descriptor. Streams are
     * not opened for reading until they are loaded by the implementing class.
     * 
     * <p>
     * The following files will be read for this index:<br>
     * {@value #DICTIONARY_EXTENSION} - Dictionary of terms<br>
     * {@value #POSTINGS_EXTENSION} - The Postings for each dictionary term<br>
     * {@value #DESCRIPTOR_EXTENSION} - The descriptor for the index
     * 
     * 
     * @param indexName The name for this index, used for the files name.
     * @param dir       Directory to create the index in.
     * @throws IOException
     */
    public IndexReader(String indexName, Path dir) throws IOException {
        this.indexName = checkNotNull(indexName);
        this.directory = checkNotNull(dir);

        checkArgument(Files.exists(dir), "The directory %s does not exists", dir);

        try {
            this.dictionaryPath = dir.resolve(indexName + IndexFileUtility.DICTIONARY_EXTENSION);
            this.postingsPath = dir.resolve(indexName + IndexFileUtility.POSTINGS_EXTENSION);
            this.descriptorPath = dir.resolve(indexName + IndexFileUtility.DESCRIPTOR_EXTENSION);
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException(
                    "The index files were not found: indexName: " + indexName + ", dir: " + directory, e);
        }

        try (Input inputPosting = new Input(Files.readAllBytes(descriptorPath))) {
            int fileVersion = inputPosting.readInt();
            verify(fileVersion == IndexFileUtility.FILE_VERSION,
                    "The version of the given index is not supported. Version found is %s.", fileVersion);
            this.termCount = inputPosting.readLong();
        }
    }

    public ImmutableMap<String, DictionaryEntry> readCompleteDictionary() throws IOException {
        ImmutableMap.Builder<String, DictionaryEntry> builder = ImmutableMap.builder();

        try (Input input = new Input(Files.newInputStream(dictionaryPath))) {

            long lastOffSet = 0;
            for (long i = 0; i < termCount; i++) {
                DictionaryEntryLinked dictEntry = decodeDictionaryEntry(input, lastOffSet);
                builder.put(dictEntry.getTerm(), dictEntry);
                lastOffSet = dictEntry.getPostingsOffset();
            }
        }
        return builder.build();
    }

    /**
     * Reads and decode the next dictionary entry from the dictionary file given by
     * the {@link Input}.
     * 
     * @see #decodePostingsList(Input, long)
     * @param input      Kryo Input to the dictionary file.
     * @param lastOffSet offset to the postings list of the last read dictionary
     *                   entry, 0 for the first.
     * 
     * @return A dictionary entry linked to a postings list offset
     */
    protected DictionaryEntryLinked decodeDictionaryEntry(Input input, long lastOffSet) {
        checkNotNull(input);
        checkArgument(lastOffSet >= 0, "Offset cannot be a negative number. Given offset: %s", lastOffSet);

        String term = input.readString();
        long docFreq = input.readVarLong(true);
        long freqDelta = input.readVarLong(true);
        int sentiment = input.readVarInt(false);

        return new DictionaryEntryLinked(term, docFreq, sentiment, freqDelta + lastOffSet);
    }

    /**
     * Reads and decode the next postings list from the posting file given by the
     * {@link Input}.
     * 
     * The docFrequency should be found from the dictionary file of the inverted
     * index.
     * 
     * 
     * @see #decodeDictionaryEntry(Input, long)
     * @param input        Kryo Input to the postings file.
     * @param docFrequency Number of posting to read from the {@link Input}.
     * @return A new immutable list of posting.
     */
    protected ImmutableList<Posting> decodePostingsList(Input input, long docFrequency) {
        checkNotNull(input);
        checkArgument(docFrequency > 0, "Document frequency should be greater than 0. Given docFreq: %s", docFrequency);

        ImmutableList.Builder<Posting> builder = ImmutableList.builder();

        long lastDocId = 0;
        for (long i = 0; i < docFrequency; i++) {
            Posting posting = decodePosting(input, lastDocId);
            builder.add(posting);
            lastDocId = posting.getDocId();
        }

        return builder.build();
    }

    /**
     * Reads and decode a single posting from the given {@link Input} to the
     * postings file.
     * 
     * @param input     Kryo Input to the postings file.
     * @param lastDocId The last document id for this postings list, 0 if this is
     *                  the first.
     * @return the next {@link Posting} read from file.
     */
    protected Posting decodePosting(Input input, long lastDocId) {
        checkNotNull(input);

        long docDelta = input.readVarLong(true);
        int termFreq = input.readVarInt(true);
        float tfIdf = input.readVarFloat(IndexFileUtility.TFIDF_VAR_FLOAT_PRECISION, true);

        return new Posting(lastDocId + docDelta, termFreq, tfIdf);
    }

    @Override
    public abstract void close() throws IOException;

}
