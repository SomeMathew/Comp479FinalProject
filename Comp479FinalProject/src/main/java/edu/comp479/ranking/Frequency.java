package edu.comp479.ranking;

import edu.comp479.search.index.IInvertedIndex;
import edu.comp479.search.index.structure.Posting;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains help functions regarding term and document frequency
 * calculations.
 *
 * @author Mohsen Parisay <mohsenparisay@gmail.com>
 * @version <1.1> - <1.dec.2018>
 */
public class Frequency {

    private double totalSentimentValue;
    private IInvertedIndex index;

    public double getTotalSentimentValue() {
        return totalSentimentValue;
    }

    public Frequency(IInvertedIndex index) {
        this.index = index;
    }

    // 'tf-idf' value
    public double getTermFreqInvDocFrequency(String term, int docId) {
        double tf = getTermFrequencyInDocument(term, docId);
        long collectionSize = this.index.getDocumentCount();
        double idf = getInverseDocFrequency(term, collectionSize);
        double tf_idf = tf * idf;

        return tf_idf;
    }

    public long getDocumentFrequency(String term) {
        return this.index.getPostings(term).getDocumentFrequency();
    }

    // 'idf' value
    private double getInverseDocFrequency(String term, long collectionSize) {
        long df = getDocumentFrequency(term);
        double div = 0;
        double idf = 0;

        if (df != 0) {
            div = (double) collectionSize / df;
            idf = Math.log10(div);
        }

        return idf;
    }

    public int getTermFrequencyInDocument(String term, int docId) {
        int tf = 0;
        for (Posting p : index.getPostings(term).getPostingsList()) {
            if (p.getDocId() == docId) {
                tf = p.getTermFreq();
                break;
            }
        }
        return tf;
    }

    public float getDocumentLength(int docId) {
        return this.index.getDocumentLengthNorm(docId);
    }

    public double getCosineNormalization(List<Double> wightsList) {
        double sumSquar = 0.0;
        for (Double weight : wightsList) {
            sumSquar += Math.pow(weight, 2);
        }
        double normalizedValue = (double) 1 / Math.sqrt(sumSquar);
        return normalizedValue;
    }

    public HashMap<Integer, DocumentScore> getScores(List<String> queryList, List<Integer> docIdList, Map<String, Integer> sentiment) {
        HashMap<Integer, DocumentScore> scores = new HashMap();
        double sentimentValue = calculateSentiment(queryList, sentiment);
        this.totalSentimentValue = sentimentValue;

        for (Integer docId : docIdList) {
            double sumScore = 0.0;

            for (String item : queryList) {
                double weight = calculateWeight(item, docId);
                sumScore += weight;
            }// end of for

            DocumentScore ds = new DocumentScore();
            ds.setScore(sumScore, sentimentValue);
            scores.put(docId, ds);

        }// end of for

        return scores;
    }

    private double calculateWeight(String term, int docId) {
        return getInverseDocFrequency(term, docId);
    }

    private double calculateSentiment(List<String> queryList, Map<String, Integer> sentiment) {
        double result = 0.0;

        for (String term : queryList) {
            int val = 0;
            if (sentiment.containsKey(term)) {
                val = sentiment.get(term);
            }
            result += val;
        }

        double avg = (double) result / queryList.size();

        return avg;
    }

}
