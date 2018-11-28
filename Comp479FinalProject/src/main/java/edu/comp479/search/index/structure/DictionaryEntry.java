package edu.comp479.search.index.structure;

import static com.google.common.base.Preconditions.*;

public class DictionaryEntry implements Comparable<DictionaryEntry> {
    private final String term;
    private final long docFreq;
    private final int sentiment;

    public DictionaryEntry(String term, long docFreq, int sentiment) {
        checkNotNull(term);
        checkArgument(!term.isEmpty(), "Term cannot be empty.");
        checkArgument(docFreq >= 0, "The doc frequency must be non-negative. Given: %s", docFreq);

        this.term = term;
        this.docFreq = docFreq;
        this.sentiment = sentiment;
    }

    public String getTerm() {
        return term;
    }

    public long getDocFreq() {
        return docFreq;
    }

    public int getSentiment() {
        return sentiment;
    }

    @Override
    public int compareTo(DictionaryEntry dictEntry) {
        return this.term.compareTo(dictEntry.getTerm());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((term == null) ? 0 : term.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DictionaryEntry other = (DictionaryEntry) obj;
        if (term == null) {
            if (other.term != null)
                return false;
        } else if (!term.equals(other.term))
            return false;
        return true;
    }
}
