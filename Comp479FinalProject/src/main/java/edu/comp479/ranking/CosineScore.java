package edu.comp479.ranking;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Mohsen Parisay <mohsenparisay@gmail.com>
 * @version <1.0> - <30.nov.2018>
 */
public class CosineScore {

    Frequency fr = null;

    public CosineScore() throws IOException {
        String indexPath = "index.txt";
        String datasetPath = "dataset.dat";

        fr = new Frequency(indexPath, datasetPath);
    }

    public Map<Integer, Double> calculateCosineScore(HashMap<String, List<Integer>> dictionary, Map<String, Integer> sentimentMap) throws IOException {

        Map<Integer, Double> scores = new TreeMap();
        Map<Integer, Integer> length = new TreeMap();
        HashMap<String, Double> queryMap = createQueryMap(dictionary);

        for (Map.Entry<String, Double> entry : queryMap.entrySet()) {
            String term = entry.getKey();
            Double tfidf = entry.getValue();

            List<Integer> postingList = dictionary.get(term);
            for (Integer docId : postingList) {
                double weight = getScore(term, tfidf, postingList);

                int sentimentValue = 0;
                if (sentimentMap.containsKey(term)) {
                    sentimentValue = sentimentMap.get(term);
                }

                double value = calcWeightSentimentValue(weight, sentimentValue);
                scores.put(docId, value);

                int docLen = getDocumentLength(docId);
                length.put(docId, docLen);
            }

        }// end of for each query term                                     

        for (Map.Entry<Integer, Integer> entry : length.entrySet()) {
            Integer docId = entry.getKey();
            Integer len = entry.getValue();

            double finalScore = (double) scores.get(docId) / len;
            scores.put(docId, finalScore);
        }

        return scores;
    }

    public double getAverageSentimentValue(List<String> queryList, Map<String, Integer> sentimentMap) {
        double sum = 0.0;

        for (String term : queryList) {
            int value = 0;
            if (sentimentMap.containsKey(term)) {
                value = sentimentMap.get(term);
            }
            sum += value;
        }

        return (double) sum / queryList.size();
    }

    private double getScore(String term, double wq, List<Integer> postingList) {
        double sum = 0.0;

        for (Integer docId : postingList) {
            double wd = getDocumentWeight(term, docId);
            double weight = wd * wq;
            sum += weight;
        }

        return sum;
    }

    private double getDocumentWeight(String term, int docId) {
        double tf = fr.getTermFreqInvDocFrequency(term, docId);
        return tf;
    }

    private int getDocumentLength(int docId) {
        int len = fr.getDocumentLength(docId);
        return len;
    }

    public double calcWeightSentimentValue(double weight, int sentiment) {
        double finalScore = (double) (Math.cos(Math.toRadians(weight)) * Math.cos(Math.toRadians(sentiment)));
        return finalScore;
    }

    private HashMap<String, Double> createQueryMap(HashMap<String, List<Integer>> dictionary) {
        HashMap<String, Double> queryMap = new HashMap();

        for (Map.Entry<String, List<Integer>> entry : dictionary.entrySet()) {
            String term = entry.getKey();
            List<Integer> postingsList = entry.getValue();

            for (int docId : postingsList) {
                double tfidf = fr.getTermFreqInvDocFrequency(term, docId);
                queryMap.put(term, tfidf);
            }

        }

        return queryMap;
    }

}
