package edu.comp479.search.index;

public class DictionaryEntry implements Comparable<DictionaryEntry> {
    private final String term;
    private final int docFreq;
    private final int sentiment;

    public DictionaryEntry(String term, int docFreq, int sentiment) {
        if (term == null || term.isEmpty() || docFreq < 0) {
            throw new IllegalArgumentException();
        }
        this.term = term;
        this.docFreq = docFreq;
        this.sentiment = sentiment;
    }

    public String getTerm() {
        return term;
    }

    public int getDocFreq() {
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
