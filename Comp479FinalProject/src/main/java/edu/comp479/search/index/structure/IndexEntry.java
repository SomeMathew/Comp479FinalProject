package edu.comp479.search.index.structure;

import java.util.List;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Strings.lenientFormat;
import static com.google.common.base.Preconditions.*;

public class IndexEntry implements IIndexEntry {
    private final DictionaryEntry dictionaryEntry;
    private final List<Posting> postings;

    public IndexEntry(DictionaryEntry dictionaryEntry, ImmutableList<Posting> postings) {
        this.dictionaryEntry = checkNotNull(dictionaryEntry);
        this.postings = checkNotNull(postings);
        assert dictionaryEntry.getDocFreq() == postings.size() : lenientFormat("docFreq: %s, postingsSize: %s",
                dictionaryEntry.getDocFreq(), postings.size());
    }

    @Override
    public String getTerm() {
        return dictionaryEntry.getTerm();
    }

    @Override
    public long getDocumentFrequency() {
        return dictionaryEntry.getDocFreq();
    }

    @Override
    public int getSentimentValue() {
        return dictionaryEntry.getSentiment();
    }

    @Override
    public List<Posting> getPostingsList() {
        return postings;
    }

}
