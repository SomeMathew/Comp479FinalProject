package edu.comp479.search.indexer.file;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.esotericsoftware.kryo.io.Output;

import edu.comp479.search.index.DictionaryEntry;
import edu.comp479.search.index.Posting;

import static com.google.common.base.Preconditions.*;

public class IndexWriter implements Closeable {
    public static final String DICTIONARY_EXTENSION = ".dic";
    public static final String POSTINGS_EXTENSION = ".pst";
    public static final String DESCRIPTOR_EXTENSION = ".desc";

    private static final int FILE_VERSION = 0xAB01;

    private static final float TFIDF_VAR_FLOAT_PRECISION = 1000.0f;

    private String indexName;
    private Path directory;
    private WriterMode mode;

    private Output dictionaryOutput;
    private Output postingsOutput;
    private Output descriptorOutput;

    private Path dictionaryPath;
    private Path postingsPath;
    private Path descriptorPath;

    private long lastPostingListOffset = 0;
    private long termCount = 0;

    public static enum WriterMode {
        STREAM, DUMP;
    }

    /**
     * Creates a new IndexWriter.
     * 
     * <p>
     * The file created by this writer will be {@code indexName} appended with the
     * extensions for each files of the index on disk.
     * 
     * <p>
     * <b>Usage:</b>
     * <ol>
     * <li>Each individual (dictionaryTerm, postingsList) tuple must be sent with
     * the {@link #write(DictionaryEntry, List)} method ordered by term and posting
     * docId.
     * <li>{@link #writeDescriptor()} must be called to complete the index.
     * <li>{@link #close()} to flush the buffer and close the streams.
     * </ol>
     * 
     * <p>
     * The following files will be created by this index:<br>
     * {@value #DICTIONARY_EXTENSION} - Dictionary of terms<br>
     * {@value #POSTINGS_EXTENSION} - The Postings for each dictionary term<br>
     * {@value #DESCRIPTOR_EXTENSION} - The descriptor for the index
     * 
     * 
     * @param indexName The name for this index, used for the files name.
     * @param dir       Directory to create the index in.
     * @param mode      Mode of operation, currently only supports Stream.
     * @throws IOException
     */
    public IndexWriter(String indexName, Path dir, WriterMode mode) throws IOException {
        this.indexName = checkNotNull(indexName);
        this.directory = checkNotNull(dir);
        this.mode = checkNotNull(mode);

        if (mode == WriterMode.STREAM) {

        } else {
            throw new UnsupportedOperationException("Mode Not Implemented yet");
        }

        Files.createDirectories(dir);

        this.dictionaryPath = dir.resolve(indexName + DICTIONARY_EXTENSION);
        this.postingsPath = dir.resolve(indexName + POSTINGS_EXTENSION);
        this.descriptorPath = dir.resolve(indexName + DESCRIPTOR_EXTENSION);

        this.dictionaryOutput = new Output(Files.newOutputStream(dictionaryPath));
        this.postingsOutput = new Output(Files.newOutputStream(postingsPath));
        this.descriptorOutput = new Output(Files.newOutputStream(descriptorPath));
    }

    /**
     * @see IndexWriter#IndexWriter(String, Path, WriterMode)
     */
    public IndexWriter(String indexName, String directory, WriterMode mode) throws IOException {
        this(indexName, Paths.get(directory), mode);
    }

    public Path getDictionaryPath() {
        return dictionaryPath;
    }

    public Path getPostingsPath() {
        return postingsPath;
    }

    public Path getDescriptorPath() {
        return descriptorPath;
    }

    /**
     * Writes a single dictionary term and postings list entry.
     * 
     * @param dictEntry
     * @param postingsList
     */
    public void write(DictionaryEntry dictEntry, List<Posting> postingsList) {
        checkState(mode == WriterMode.STREAM, "This method is reserved for the Stream Mode. Current: %s", mode);
        checkNotNull(dictEntry);
        checkNotNull(postingsList);
        checkArgument(!postingsList.isEmpty(), "The postings list cannot be empty.");

        long postingListOffset = writePostingsList(postingsList);
        writeDictionary(dictEntry, postingListOffset);
        termCount++;

        lastPostingListOffset = postingListOffset;
    }

    /**
     * Call this method to write the descriptor with the number of entries once the
     * postings have all been written.
     * 
     * This should be called right before {@link #close()}.
     * 
     * <p>
     * IndexDescriptorFile → FileVersion, TermCount <br>
     * FileVersion → Int <br>
     * TermCount → Long
     * 
     */
    public void writeDescriptor() {
        descriptorOutput.writeInt(FILE_VERSION);
        descriptorOutput.writeLong(termCount);
    }

    /**
     * Encodes and writes the given {@code postingsList} to the postings file.
     * 
     * <p>
     * PostingsFile → (Posting) <sup>TermCount</sup> <br>
     * Posting → (DocDelta, TermFreq, TFIDF) <sup>DocFreq</sup> <br>
     * DocDelta → VarLong (unsigned) <br>
     * TermFreq → VarInt (unsigned) <br>
     * TFIDF → VarFloat (unsigned) precision 1000.0
     * </p>
     * 
     * @param postingsList Non empty postingsList.
     * @return The starting position in the file for this postings list.
     */
    private long writePostingsList(List<Posting> postingsList) {
        long startPosition = postingsOutput.total();

        long lastDocId = -1;
        for (Posting posting : postingsList) {
            lastDocId = writeDeltaVarLong(postingsOutput, lastDocId, posting.getDocId(), lastDocId == -1, true);
            postingsOutput.writeVarInt(posting.getTermFreq(), true);
            postingsOutput.writeVarFloat(posting.getTfIdf(), TFIDF_VAR_FLOAT_PRECISION, true);
        }
        return startPosition;
    }

    /**
     * Encodes and writes the given {@code dictEntry} to the dictionary file with a
     * pointer to the associated postings list.
     * 
     * <p>
     * DictionaryFile → (TermInfo)<sup>TermCount</sup> <br>
     * TermInfo → (Term, DocFreq, FreqDelta, Sentiment) <br>
     * Term → String <br>
     * DocFreq → VarInt <br>
     * FreqDelta → VarLong <br>
     * Sentiment → VarInt (signed)
     * 
     * @param dictEntry         Dictionary entry to write
     * @param postingListOffset
     */
    private void writeDictionary(DictionaryEntry dictEntry, long postingListOffset) {
        dictionaryOutput.writeString(dictEntry.getTerm());
        dictionaryOutput.writeVarInt(dictEntry.getDocFreq(), true);
        // No use for isFirst since the first will always initialize at 0
        writeDeltaVarLong(dictionaryOutput, lastPostingListOffset, postingListOffset, false, true);
        dictionaryOutput.writeVarInt(dictEntry.getSentiment(), false);
    }

    /**
     * Writes the delta between {@code lastValue} and the {@code currentValue} as a
     * varLong.
     * 
     * @param output           The {@link Output} to write to
     * @param lastValue        The last Value written
     * @param currentValue     The Current value
     * @param isFirst          If true, then the first value will be the actual
     *                         currentValue.
     * @param optimizePositive See: {@link Output#writeVarLong(long, boolean)}
     * @return {@code currentValue}
     */
    private long writeDeltaVarLong(Output output, long lastValue, long currentValue, boolean isFirst,
            boolean optimizePositive) {
        checkArgument(lastValue <= currentValue, "The delta must be positive. (last: %s, current: %s", lastValue,
                currentValue);
        if (isFirst) {
            // This is the first docId in the list write its actual DocId
            output.writeVarLong(currentValue, true);
        } else {
            output.writeVarLong(currentValue - lastValue, true);
        }
        return currentValue;
    }

    /**
     * Close and flush the file output stream.
     */
    @Override
    public void close() throws IOException {
        dictionaryOutput.close();
        postingsOutput.close();
        descriptorOutput.close();
    }

}
