package edu.comp479.search.tokenizer;

import static com.google.common.base.Preconditions.*;

public class Token implements IToken {
    private final String term;
    private final long docId;

    public Token(String term, long docId) {
        this.term = checkNotNull(term);
        checkArgument(docId >= 0, "DocId must be non-negative. Given: %s", docId);
        this.docId = docId;
    }

    @Override
    public String getTerm() {
        return term;
    }

    @Override
    public long getDocId() {
        return docId;
    }

}
