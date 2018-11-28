package edu.comp479.ranking;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This test class runs the pipeline of searching a query through index and prints
 * the ranked results based on their score and sentiment values.
 * 
 * @author Mohsen Parisay <mohsenparisay@gmail.com>
 * @version <1.0> - <19.nov.2018>
 */
public class TestRankEngine {

    public static void main(String[] args) throws IOException {

        String query = "nervously abductions";

        FileOperations fo = new FileOperations();
        String afinnPath = "AFINN/AFINN-111.txt";
        Map<String, Integer> sentiment = fo.readAfinnFile(afinnPath);

        RankEngine re = new RankEngine();
        Map<Integer, DocumentScore> scores = re.rankDocuments(query, sentiment);

        double totalSentimentValue = re.getTotalSentimentValue();
        System.out.println("Total Sentiment [" + totalSentimentValue + "]\n");

        Map<Integer, Double> scoreMap = new HashMap();

        for (Map.Entry<Integer, DocumentScore> entry : scores.entrySet()) {
            Integer key = entry.getKey();
            DocumentScore value = entry.getValue();
            double totalScore = value.calculateTotalScore(value.getWeight(), value.getSentiment());

            scoreMap.put(key, totalScore);
        }

        SortScores ssc = new SortScores();
        ssc.printMap(scoreMap, totalSentimentValue);
        System.out.println("");
    }

}
