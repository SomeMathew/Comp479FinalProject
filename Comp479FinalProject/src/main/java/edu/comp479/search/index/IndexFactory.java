package edu.comp479.search.index;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

import edu.comp479.search.indexer.file.IndexReaderMemoryMappedPostings;
import edu.comp479.search.util.SentimentDictionaryBuilder;

public class IndexFactory {

    public IndexFactory() {
    }

    public IInvertedIndex getIndex(IndexReaderMemoryMappedPostings indexReader) throws IOException {
        checkNotNull(indexReader);
        Map<String, Integer> sentimentDict = new SentimentDictionaryBuilder().loadSentimentDictionary();
        return getIndex(indexReader, sentimentDict);
    }

    public IInvertedIndex getIndex(IndexReaderMemoryMappedPostings indexReader, Map<String, Integer> sentimentDict)
            throws IOException {
        checkNotNull(indexReader);
        checkNotNull(sentimentDict);

        InvertedIndex index = new InvertedIndex(indexReader.readCompleteDictionary(), indexReader, sentimentDict);
        return index;
    }
}
