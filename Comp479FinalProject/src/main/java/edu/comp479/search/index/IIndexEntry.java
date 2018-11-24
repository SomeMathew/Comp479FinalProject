package edu.comp479.search.index;

import java.util.List;

public interface IIndexEntry {
    public String getTerm();

    public int getSentimentValue();

    public List<Posting> getPostingsList();
}
