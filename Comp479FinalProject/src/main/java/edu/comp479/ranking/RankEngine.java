package edu.comp479.ranking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class calculates the actual ranking of retrieved documents.
 *
 * @author Mohsen Parisay <mohsenparisay@gmail.com>
 * @version <1.0> - <18.nov.2018>
 */
public class RankEngine {

    private double totalSentimentValue;

    public double getTotalSentimentValue() {
        return totalSentimentValue;
    }

    public Map<Integer, DocumentScore> rankDocuments(String rawQuery, Map<String, Integer> sentiment) throws IOException {

        // since we use the "bag of words model" we need to simulate
        // OR functionality in the search engine:
        String newQuery = rawQuery.replaceAll(" ", " OR ");

        List<Integer> docIdList = searchQuery(newQuery);
        List<String> queryList = splitRawQuery(rawQuery);

        Map<Integer, DocumentScore> scores = getDocScores(queryList, docIdList, sentiment);
        return scores;
    }

    private List<Integer> searchQuery(String query) {

        SearchEngine se = new SearchEngine();
        List<List<String>> result = se.search(query, true);

        List<Integer> resultList = new ArrayList();

        for (List<String> lst : result) {
            for (String item : lst) {
                int docId = Integer.valueOf(item);
                resultList.add(docId);
            }
        }

        return resultList;
    }

    private List<String> splitRawQuery(String rawQuery) {
        String[] tmp = rawQuery.split(" ");
        List<String> result = new ArrayList();

        for (String item : tmp) {
            result.add(item.trim());
        }

        return result;
    }

    private Map<Integer, DocumentScore> getDocScores(List<String> queryList, List<Integer> docIdList, Map<String, Integer> sentimentMap) throws IOException {
        String indexPath = "index.txt";
        String datasetPath = "dataset.dat";

        Frequency fr = new Frequency(indexPath, datasetPath);
        Map<Integer, DocumentScore> scores = fr.getScores(queryList, docIdList, sentimentMap);
        this.totalSentimentValue = fr.getTotalSentimentValue();

        return scores;
    }

}
