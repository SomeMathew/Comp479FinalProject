package edu.comp479.search.index;

import static com.google.common.base.Preconditions.*;

import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.comp479.search.index.structure.DictionaryEntry;
import edu.comp479.search.index.structure.IIndexEntry;
import edu.comp479.search.index.structure.IndexEntry;
import edu.comp479.search.index.structure.Posting;
import edu.comp479.search.indexer.file.IndexReaderMemoryMappedPostings;

public class InvertedIndex implements IInvertedIndex {
    private final IndexReaderMemoryMappedPostings indexReader;
    private final ImmutableMap<String, DictionaryEntry> dictionary;
    private final Map<String, Integer> sentimentDictionary;

    public InvertedIndex(ImmutableMap<String, DictionaryEntry> dictionary, IndexReaderMemoryMappedPostings indexReader,
            Map<String, Integer> sentimentDictionary) {
        this.indexReader = checkNotNull(indexReader);
        this.sentimentDictionary = checkNotNull(sentimentDictionary);
        this.dictionary = checkNotNull(dictionary);
    }

    @Override
    public IIndexEntry getPostings(String term) {
        checkNotNull(term);
        checkArgument(!term.isEmpty(), "The term must not be empty.");

        DictionaryEntry dictEntry = dictionary.get(term);
        if (dictEntry == null) {
            return new IndexEntry(new DictionaryEntry(term, 0, sentimentDictionary.getOrDefault(term, 0)),
                    ImmutableList.of());
        } else {
            ImmutableList<Posting> postings = indexReader.readPostings(dictEntry);
            return new IndexEntry(dictEntry, postings);
        }
    }

    @Override
    public float getDocumentLengthNorm(long docId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Method Stub");
    }

}
