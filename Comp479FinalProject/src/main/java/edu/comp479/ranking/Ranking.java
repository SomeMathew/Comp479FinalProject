package edu.comp479.ranking;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.google.common.base.Preconditions.*;

import edu.comp479.search.index.IInvertedIndex;
import edu.comp479.search.index.IndexFactory;
import edu.comp479.search.index.structure.IIndexEntry;
import edu.comp479.search.index.structure.Posting;
import edu.comp479.search.indexer.file.IndexReaderMemoryMapped;
import edu.comp479.search.tokenizer.TokenizerNormalize;
import edu.comp479.search.util.SentimentDictionaryBuilder;

public class Ranking {
	private IInvertedIndex index;
	private TokenizerNormalize tokenizer;
	private Map<String, Integer> sentimentDictionary;

	public Ranking(IInvertedIndex index, TokenizerNormalize tokenizer, Map<String, Integer> sentimentDictionary) {
		this.index = checkNotNull(index);
		this.tokenizer = checkNotNull(tokenizer);
		this.sentimentDictionary = checkNotNull(sentimentDictionary);
	}

	public TreeMap<Long, Double> search(String query) {
		List<String> queryTokens = tokenizer.analyze(query);

		List<IIndexEntry> indexEntries = new ArrayList<>();

		for (String t : queryTokens) {
			index.getPostings(t);
		}
		
		int sentimentVal = sentimentDictionary.get(queryTokens.get(0));

		index.getDocumentLengthNorm(5);
		// Process postings here
		for (IIndexEntry entry : indexEntries) {
			entry.getDocumentFrequency();
			entry.getSentimentValue();
			entry.getTerm();
			List<Posting> postingsList = entry.getPostingsList();
			Posting posting = postingsList.get(0);
			posting.getDocId();
			posting.getTermFreq();
			posting.getTfIdf();
		}

		return null;
	}

	public static void main(String args[]) throws IOException {
		IndexReaderMemoryMapped indexReader = new IndexReaderMemoryMapped("testIndex", Paths.get("./testIndex/index/"));
		indexReader.open();
		IndexFactory indexFactory = new IndexFactory();
		IInvertedIndex index = indexFactory.getIndex(indexReader);

		Ranking ranking = new Ranking(index, new TokenizerNormalize(),
				new SentimentDictionaryBuilder().loadSentimentDictionary());

		// TODO here add your test
		ranking.search("Some Search Here");

		indexReader.close();
	}

}
