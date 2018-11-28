package edu.comp479.search.indexer.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import com.esotericsoftware.kryo.io.Input;
import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.*;

import edu.comp479.search.index.structure.DictionaryEntryLinked;
import edu.comp479.search.index.structure.IndexEntry;
import edu.comp479.search.index.structure.Posting;

public class IndexReaderStreamed extends IndexReader {
    private Input postingsInput;
    private Input dictionaryInput;

    private long currentPosition;
    private long lastPostingsListOffset;

    public IndexReaderStreamed(String indexName, Path dir) throws IOException {
        super(indexName, dir);
    }

    /**
     * Validates if the next call to {@link #readNextEntry()} will be valid and
     * return a result.
     * 
     * @return {@code true} if an entry can be read.
     * @throws IllegalStateException If the streams are unavailable.
     */
    public boolean hasNextEntry() {
        checkInputsState();
        return currentPosition < termCount;
    }

    /**
     * Returns the next entry of this Inverted Index that can be read from the file.
     * 
     * <b> Note:</b> This reader does not guarantee the state of the underlying
     * index file. While it should be ordered properly when written, this task is
     * not handled by the Data Mapper.
     * 
     * @return The next Index Entry
     * 
     * @throws IOException            If an error happens with the I/O.
     * @throws NoSuchElementException if the stream is finished.
     * @throws IllegalStateException  if the stream is closed or never opened.
     */
    public IndexEntry readNextEntry() throws IOException {
        checkInputsState();
        if (currentPosition >= termCount) {
            throw new NoSuchElementException("Reached the end of the disk file.");
        }

        currentPosition++;

        DictionaryEntryLinked dictEntry = decodeDictionaryEntry(dictionaryInput, lastPostingsListOffset);
        ImmutableList<Posting> postingsList = decodePostingsList(postingsInput, dictEntry.getDocFreq());
        lastPostingsListOffset = dictEntry.getPostingsOffset();

        return new IndexEntry(dictEntry, postingsList);
    }

    private void checkInputsState() {
        if (postingsInput == null || dictionaryInput == null) {
            throw new IllegalStateException("The inputs are not ready.");
        }
    }

    public boolean openDictionary() throws IOException {
        this.dictionaryInput = new Input(Files.newInputStream(dictionaryPath));
        return true;
    }

    /**
     * Open the streams for the postings and dictionary files.
     * 
     * @return {@code true} if successful
     * @throws IOException
     */
    public boolean open() throws IOException {
        return open(new Input(Files.newInputStream(postingsPath)), new Input(Files.newInputStream(dictionaryPath)));
    }

    /**
     * Open the streams for the postings and dictionary files with the given
     * bufferSize.
     * 
     * @param bufferSize
     * @return {@code true} if successful
     * @throws IOException
     */
    public boolean open(int bufferSize) throws IOException {
        checkArgument(bufferSize > 0, "The bufferSize must be greate than 0. Given: %s", bufferSize);
        return open(new Input(Files.newInputStream(postingsPath), bufferSize),
                new Input(Files.newInputStream(dictionaryPath), bufferSize));
    }

    /**
     * Override the creation of the {@link Input} for the dictionary and postings
     * with a custom object.
     * 
     * This is used mainly for testing the class with a mock.
     * 
     * @param postingsInput
     * @param dictionaryInput
     * @return {@code true} if successful.
     */
    public boolean open(Input postingsInput, Input dictionaryInput) {
        this.postingsInput = checkNotNull(postingsInput);
        this.dictionaryInput = checkNotNull(dictionaryInput);
        reset();
        return true;
    }

    @Override
    public void close() throws IOException {
        if (postingsInput != null) {
            postingsInput.close();
        }
        if (dictionaryInput != null) {
            dictionaryInput.close();
        }
        reset();
    }

    private void reset() {
        currentPosition = 0;
        lastPostingsListOffset = 0;
    }
}
