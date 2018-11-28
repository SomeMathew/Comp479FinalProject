package edu.comp479.search.indexer;

import java.util.Map;

import static com.google.common.base.Preconditions.*;

public class IndexBlockBuilderFactory {
    private final Map<String, Integer> sentimentDictionary;

    public IndexBlockBuilderFactory(Map<String, Integer> sentimentDictionary) {
        this.sentimentDictionary = checkNotNull(sentimentDictionary);
    }

    public IndexBlockBuilder createIndexBlockBuilder() {
        return new IndexBlockBuilder(sentimentDictionary);
    }

}
