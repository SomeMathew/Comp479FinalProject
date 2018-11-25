package edu.comp479.search.index.structure;

import static com.google.common.base.Preconditions.*;

public class DictionaryEntryLinked extends DictionaryEntry {
    private final long postingsOffset;

    public DictionaryEntryLinked(String term, long docFreq, int sentiment, long postingsOffset) {
        super(term, docFreq, sentiment);
        checkArgument(postingsOffset >= 0, "Offset to the postings list cannot be null. Value: %s", postingsOffset);
        this.postingsOffset = postingsOffset;
    }

    public long getPostingsOffset() {
        return postingsOffset;
    }
}
