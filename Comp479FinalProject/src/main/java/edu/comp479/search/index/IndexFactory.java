package edu.comp479.search.index;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

import edu.comp479.search.indexer.file.IndexReaderMemoryMapped;
import edu.comp479.search.util.SentimentDictionaryBuilder;

public class IndexFactory {

    public IndexFactory() {
    }

    /**
     * Retrieves an existing index pointed to by the given
     * {@link IndexReaderMemoryMapped}.
     * 
     * <p>
     * <b>Note:</B> The {@link IndexReaderMemoryMapped} must have been completely
     * initialized by the user by {@link IndexReaderMemoryMapped#open()}.
     * 
     * @param indexReader Initialized and opened indexReader.
     * @return An {@link IInvertedIndex} backed by the given
     *         {@link IndexReaderMemoryMapped}.
     * @throws IOException
     */
    public IInvertedIndex getIndex(IndexReaderMemoryMapped indexReader) throws IOException {
        checkNotNull(indexReader);
        Map<String, Integer> sentimentDict = new SentimentDictionaryBuilder().loadSentimentDictionary();
        return getIndex(indexReader, sentimentDict);
    }

    public IInvertedIndex getIndex(IndexReaderMemoryMapped indexReader, Map<String, Integer> sentimentDict)
            throws IOException {
        checkNotNull(indexReader);
        checkNotNull(sentimentDict);

        InvertedIndex index = new InvertedIndex(indexReader.readCompleteDictionary(), indexReader, sentimentDict);
        return index;
    }
}
