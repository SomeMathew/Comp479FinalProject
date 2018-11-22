package edu.comp479.search.index;

public interface IInvertedIndex {
    public IIndexEntry getPostings(String term);
}
