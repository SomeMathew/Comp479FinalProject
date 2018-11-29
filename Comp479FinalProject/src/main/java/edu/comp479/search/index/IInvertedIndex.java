package edu.comp479.search.index;

import edu.comp479.search.index.structure.IIndexEntry;

public interface IInvertedIndex {
	/**
	 * Retrieves the details about a specific term from the inverted index.
	 * 
	 * If a term is not found in the index, it will return an {@link IIndexEntry}
	 * which has an empty Postings list and a {@code docFreq} of 0.
	 * 
	 * @param term Search Term
	 * @return Entry details and postings for the term.
	 */
	public IIndexEntry getPostings(String term);

	/**
	 * Retrieves a document's length norm for normalizing the tf-idf cosine
	 * similarity score.
	 * 
	 * @param docId Document to retrieve.
	 * @return
	 */
	public float getDocumentLengthNorm(long docId);

	/**
	 * Get the number of documents in the inverted index.
	 * 
	 * @return the number of documents.
	 */
	public long getDocumentCount();
}
