package edu.comp479.search.index;

public class Posting {
    private final long docId;
    private final float tfIdf;

    public Posting(long docId, float tfIdf) {
        if (docId < 0) {
            throw new IllegalArgumentException("docId must be a non-negative integer");
        }
        if (tfIdf < 0) {
            throw new IllegalArgumentException("The tf-idf should not be negative");
        }
        this.docId = docId;
        this.tfIdf = tfIdf;
    }

    public long getDocId() {
        return docId;
    }

    public float getTfIdf() {
        return tfIdf;
    }

}
