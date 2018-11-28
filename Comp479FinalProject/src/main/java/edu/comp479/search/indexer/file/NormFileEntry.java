package edu.comp479.search.indexer.file;

public class NormFileEntry {
    private long docId;
    private float norm;
    private float emoVal;

    public NormFileEntry(long docId, float norm, float emoVal) {
        this.docId = docId;
        this.norm = norm;
        this.emoVal = emoVal;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (docId ^ (docId >>> 32));
        result = prime * result + Float.floatToIntBits(emoVal);
        result = prime * result + Float.floatToIntBits(norm);
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
        NormFileEntry other = (NormFileEntry) obj;
        if (docId != other.docId)
            return false;
        if (Float.floatToIntBits(emoVal) != Float.floatToIntBits(other.emoVal))
            return false;
        if (Float.floatToIntBits(norm) != Float.floatToIntBits(other.norm))
            return false;
        return true;
    }

    public long getDocId() {
        return docId;
    }

    public void setDocId(long docId) {
        this.docId = docId;
    }

    public float getNorm() {
        return norm;
    }

    public void setNorm(float norm) {
        this.norm = norm;
    }

    public float getEmoVal() {
        return emoVal;
    }

    public void setEmoVal(float emoVal) {
        this.emoVal = emoVal;
    }

}
