package edu.comp479.search.index.structure;

public class Posting {
    private final long docId;
    private final int termFreq;
    private final float tfIdf;

    public Posting(long docId, int termFreq, float tfIdf) {
        if (docId < 0) {
            throw new IllegalArgumentException("docId must be a non-negative integer");
        }
        if (termFreq < 1) {
            throw new IllegalArgumentException("TermFreq must be >= 1");
        }
        if (tfIdf < 0) {
            throw new IllegalArgumentException("The tf-idf should not be negative");
        }
        this.docId = docId;
        this.tfIdf = tfIdf;
        this.termFreq = termFreq;
    }

    public long getDocId() {
        return docId;
    }

    public float getTfIdf() {
        return tfIdf;
    }

    public int getTermFreq() {
        return termFreq;
    }

}
