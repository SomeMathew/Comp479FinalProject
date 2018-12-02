package edu.comp479.ranking;

import edu.comp479.search.index.IInvertedIndex;
import edu.comp479.search.index.IndexFactory;
import edu.comp479.search.index.structure.IIndexEntry;
import edu.comp479.search.indexer.file.IndexReaderMemoryMapped;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This test class runs the pipeline of searching a query through index and
 * prints the ranked results based on their score and sentiment values.
 *
 * @author Mohsen Parisay <mohsenparisay@gmail.com>
 * @version <1.1> - <1.dec.2018>
 */
public class TestRankEngine {

    public static void main(String[] args) throws IOException {

        IndexReaderMemoryMapped indexReader = new IndexReaderMemoryMapped("testindex",
                Paths.get("C:/Users/mp/Documents/FinalProject/Comp479FinalProject/Comp479FinalProject/testIndex/index/"));
        indexReader.open();
        IndexFactory indexFactory = new IndexFactory();
        IInvertedIndex index = indexFactory.getIndex(indexReader);

        List<String> queryList = new ArrayList();
        queryList.add("concordia");

        System.out.println("Search Query: " + queryList);

        HashMap<String, Integer> sentimentMap = new HashMap();
        for (String term : queryList) {
            IIndexEntry indexEntry = index.getPostings(term);
            int sentimentValue = indexEntry.getSentimentValue();
            sentimentMap.put(term, sentimentValue);
        }

        RankEngine ren = new RankEngine(index);

        Map<Integer, HashMap<Integer, Double>> sortedMap = ren.rankDocuments(queryList, sentimentMap);
        for (Map.Entry<Integer, HashMap<Integer, Double>> entry : sortedMap.entrySet()) {
            Integer rankIndex = entry.getKey();
            HashMap<Integer, Double> value = entry.getValue();
            for (Map.Entry<Integer, Double> entry1 : value.entrySet()) {
                Integer docId = entry1.getKey();
                Double score = entry1.getValue();

                System.out.println("Rank (" + (rankIndex + 1) + ") : docId [" + docId + "]: score [" + score + "]");
            }

        }

        indexReader.close();
    }

}
