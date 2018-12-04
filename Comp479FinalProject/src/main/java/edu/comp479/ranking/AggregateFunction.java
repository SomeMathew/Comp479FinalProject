package edu.comp479.ranking;

import edu.comp479.search.index.IInvertedIndex;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Mohsen Parisay <mohsenparisay@gmail.com>
 * @version <1.0> - <04.dec.2018>
 */
public class AggregateFunction {

    Frequency fr = null;

    public AggregateFunction(IInvertedIndex index) throws IOException {
        fr = new Frequency(index);
    }

    public Map<Integer, Double> calculateFinalScore(HashMap<String, List<Integer>> dictionary, Map<String, Integer> sentimentMap) throws IOException {

        Map<Integer, Double> scores = new TreeMap();
        Map<Integer, Float> length = new TreeMap();
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

                double value = aggregateWeightSentimentValue(weight, sentimentValue);
                scores.put(docId, value);

                float docLen = getDocumentLength(docId);
                length.put(docId, docLen);
            }

        }// end of for each query term                                     

        for (Map.Entry<Integer, Float> entry : length.entrySet()) {
            Integer docId = entry.getKey();
            Float len = entry.getValue();

            double finalScore = (double) scores.get(docId) / len;
            scores.put(docId, finalScore);
        }

        return scores;
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

    /**
     * calculate the final score based on the calculated weight score and the
     * sentiment value associated with a term
     *
     * @param weight the value calculated by cosine similarity function
     * @param sentimentValue the value associated with each token
     * @return the final score value
     */
    private double aggregateWeightSentimentValue(double weight, int sentimentValue) {
        double result = (double) (Math.cos(Math.toRadians(weight)) * Math.cos(Math.toRadians(sentimentValue)));
        return result;
    }

    private float getDocumentLength(int docId) {
        return fr.getDocumentLength(docId);
    }
}
