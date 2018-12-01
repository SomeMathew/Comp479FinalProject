package edu.comp479.ranking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

    public Map<Integer, HashMap<Integer, Double>> rankDocuments(List<String> queryList, Map<String, Integer> sentiment) throws IOException {

        HashMap<String, List<Integer>> dictionaryMap = createDictionary(queryList);
        CosineScore cs = new CosineScore();
        Map<Integer, Double> scoresMap = cs.calculateCosineScore(dictionaryMap, sentiment);

        double totalSentiment = cs.getAverageSentimentValue(queryList, sentiment);
        System.out.println("Total Sentiment Value: " + totalSentiment);

        SortScores sco = new SortScores();
        Map<Integer, HashMap<Integer, Double>> sortedMap = sco.printMap(scoresMap, totalSentiment);

        return sortedMap;
    }

    private HashMap<String, List<Integer>> createDictionary(List<String> queryList) {
        HashMap<String, List<Integer>> dictionary = new HashMap();

        for (String term : queryList) {
            List<Integer> postingsList = search(term);
            dictionary.put(term, postingsList);
        }

        return dictionary;
    }

    public List<Integer> search(String query) {
        SearchEngine sen = new SearchEngine();
        List<Integer> result = sen.getPostings(query);

        return result;
    }

}
