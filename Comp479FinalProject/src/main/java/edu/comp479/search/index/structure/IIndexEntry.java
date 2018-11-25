package edu.comp479.search.index.structure;

import java.util.List;

public interface IIndexEntry {
    public String getTerm();
    
    public long getDocumentFrequency();

    public int getSentimentValue();

    public List<Posting> getPostingsList();
}
