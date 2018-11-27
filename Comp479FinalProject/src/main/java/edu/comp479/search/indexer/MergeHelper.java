package edu.comp479.search.indexer;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Comparator.comparing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import edu.comp479.search.index.structure.IndexEntry;
import edu.comp479.search.indexer.file.IndexReaderStreamed;
import edu.comp479.search.util.Pair;

public class MergeHelper {
	private PriorityQueue<Pair<IndexEntry, Integer>> pq;
	private IndexReaderStreamed[] indexReaders;

	/**
	 * Helper class for {@link IndexBlockMerger} to manage retrieving the inverted
	 * index entries for which the postings can be merged from multiple index
	 * blocks.
	 * 
	 * @param indexReaders Opened index reader streams.
	 * @throws IOException
	 */
	public MergeHelper(IndexReaderStreamed[] indexReaders) throws IOException {
		checkNotNull(indexReaders);
		this.pq = new PriorityQueue<>(comparing((pair) -> pair.x.getTerm()));
		this.indexReaders = indexReaders;

		// Fill the priority queue with the next entries from each readers
		for (int i = 0; i < indexReaders.length; i++) {
			IndexReaderStreamed indexReader = indexReaders[i];
			checkNotNull(indexReader);
			prepareNext(i);
		}
	}

	/**
	 * Get the bag of entries with the same terms from each block that can be
	 * merged.
	 * 
	 * @return The list of entries that are next to merge. Will be empty if none are
	 *         left.
	 * @throws IOException
	 */
	public List<IndexEntry> getNextEntriesWithSameTerm() throws IOException {
		List<IndexEntry> entries = new ArrayList<>();
		if (hasNextEntry()) {
			IndexEntry nextEntry = getNextEntryAndAdvance();
			entries.add(nextEntry);
			String nextTerm = nextEntry.getTerm();

			while (nextEntryHasTerm(nextTerm)) {
				entries.add(getNextEntryAndAdvance());
			}
		}
		return entries;
	}

	/**
	 * Returns {@code true} if there are entries left to be fetched from any of the
	 * input blocks.
	 * 
	 * @return {@code true} if new index entries are available
	 */
	public boolean hasNextEntry() {
		return !pq.isEmpty();
	}

	private boolean nextEntryHasTerm(String term) {
		return !pq.isEmpty() && pq.peek().x.getTerm() == term;
	}

	private IndexEntry getNextEntryAndAdvance() throws IOException {
		Pair<IndexEntry, Integer> nextEntryPair = pq.remove();
		prepareNext(nextEntryPair.y);
		return nextEntryPair.x;
	}

	private void prepareNext(int bufferIndex) throws IOException {
		if (indexReaders[bufferIndex].hasNextEntry()) {
			pq.add(new Pair<IndexEntry, Integer>(indexReaders[bufferIndex].readNextEntry(), bufferIndex));
		}
	}
}