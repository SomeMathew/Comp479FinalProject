package edu.comp479.search.index.structure;

import java.util.List;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Strings.lenientFormat;
import static com.google.common.base.Preconditions.*;

public class IndexEntry implements IIndexEntry {
    private final DictionaryEntry dictionaryEntry;
    private final ImmutableList<Posting> postings;

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
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IndexEntry [dictionaryEntry=");
		builder.append(dictionaryEntry);
		builder.append(", postings=");
		builder.append(postings);
		builder.append("]");
		return builder.toString();
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

    public DictionaryEntry getDictionaryEntry() {
        return dictionaryEntry;
    }
}
