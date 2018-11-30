package edu.comp479.search.index.structure;

import static com.google.common.base.Preconditions.*;

import java.util.function.Function;

public class Posting {
	private final long docId;
	private final int termFreq;
	private final float tfIdf;

	public Posting(long docId, int termFreq, float tfIdf) {
		checkArgument(docId >= 0, "The document Id should be a non-negative value. Given: %s", docId);
		checkArgument(termFreq >= 1, "The term Frequency should be greater than 0. Given: %s", termFreq);
		checkArgument(tfIdf >= 0, "The tf-idf for a posting should be a non-negative value. Given: %s", tfIdf);

		this.docId = docId;
		this.tfIdf = tfIdf;
		this.termFreq = termFreq;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(docId=");
		builder.append(docId);
		builder.append(", termFreq=");
		builder.append(termFreq);
		builder.append(", tfIdf=");
		builder.append(tfIdf);
		builder.append(")");
		return builder.toString();
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

	public Posting withWeight(Function<Posting, Float> weightComputer) {
		Float newWeight = weightComputer.apply(this);
		return new Posting(docId, termFreq, newWeight);
	}
}
