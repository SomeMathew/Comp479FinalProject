package edu.comp479.search.indexer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.base.Preconditions.*;

import edu.comp479.search.index.structure.DictionaryEntry;
import edu.comp479.search.index.structure.Posting;
import edu.comp479.search.indexer.file.IndexWriter;

public class IndexBlockBuilder {
    private final Map<String, Integer> sentimentDictionary;
    private final Map<String, HashMultiset<Long>> block;

    public IndexBlockBuilder(Map<String, Integer> sentimentDictionary) {
        this.sentimentDictionary = checkNotNull(sentimentDictionary);
        this.block = new HashMap<>();
    }

    /**
     * Adds a posting to the underlying block representation.
     * 
     * This function will accumulate the count of term for a specific docId.
     * 
     * @param term  Term to add to the Index Block.
     * @param docId Document Id associated for which the term appears in.
     * 
     * @return True
     */
    public boolean addPosting(String term, long docId) {
        if (term == null || term.isEmpty() || docId < 0) {
            throw new IllegalArgumentException(String.format("Invalid posting: (%s,%d)", term, docId));
        }
        HashMultiset<Long> postings = block.get(term);
        if (postings == null) {
            postings = HashMultiset.create();
            block.put(term, postings);
        }
        return postings.add(docId);
    }

    public Map<String, HashMultiset<Long>> getBlock() {
        return block;
    }

    /**
     * Write the block to disk to {@code directory}.
     * 
     * @param directory Directory to write the block to.
     * @return The index Name.
     * @throws IOException
     */
    public void writeToDisk(IndexWriter indexWriter) throws IOException {
        ImmutableList<Map.Entry<String, HashMultiset<Long>>> sortedEntries = ImmutableList
                .sortedCopyOf(Map.Entry.comparingByKey(), block.entrySet());

        for (Map.Entry<String, HashMultiset<Long>> blockEntry : sortedEntries) {
            List<Posting> postingsList = blockEntry.getValue().entrySet().stream()
                    .map((entry) -> new Posting(entry.getElement(), entry.getCount(), 0))
                    .sorted(comparing(Posting::getDocId)).collect(toImmutableList());

            String term = blockEntry.getKey();
            indexWriter.write(new DictionaryEntry(term, postingsList.size(), getSentimentValue(term)), postingsList);
        }
    }

    /**
     * Returns the number of (term, postingsList) tuples in this block.
     * 
     * @return count of (Term, PostingsList) tuples.
     */
    public int getSize() {
        return block.size();
    }

    private int getSentimentValue(String term) {
        return sentimentDictionary.getOrDefault(term, 0);
    }
}
