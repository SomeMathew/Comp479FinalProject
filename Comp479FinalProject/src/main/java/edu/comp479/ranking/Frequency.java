package edu.comp479.ranking;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class contains help functions regarding term and document frequency calculations.
 *
 * @author Mohsen Parisay <mohsenparisay@gmail.com>
 * @version <1.0> - <18.nov.2018>
 */
public class Frequency {

    Map<String, List<String>> myIndex = new TreeMap();
    Map<Integer, List<String>> dataset = new TreeMap();

    private double totalSentimentValue;

    public double getTotalSentimentValue() {
        return totalSentimentValue;
    }

    public Frequency(String indexPath, String datasetPath) throws IOException {
        FileOperations fo = new FileOperations();
        myIndex = fo.readRawIndexFile(indexPath);

        dataset = fo.readDataMap(datasetPath);
    }

    public double getTermFreqInvDocFrequency(String term, int docId) {
        double tf = getTermFrequencyInDocument(term, docId);
        int collectionSize = dataset.size();
        double idf = getInverseDocFrequency(term, collectionSize);

        return tf * idf;
    }

    private int getDocumentFrequency(String term) {
        int df = 0;

        if (myIndex.containsKey(term.toLowerCase())) {
            df += myIndex.get(term).size();
        }
        if (myIndex.containsKey(term.toUpperCase())) {
            df += myIndex.get(term.toUpperCase()).size();
        }

        return df;
    }

    private double getInverseDocFrequency(String term, int collectionSize) {
        int df = getDocumentFrequency(term);
        double div = 0;
        double idf = 0;

        if (df != 0) {
            div = (double) collectionSize / df;
            idf = Math.log10(div);
        }

        return idf;
    }

    private int getTermFrequencyInDocument(String term, int docId) {
        List<String> docContents = getRecord(docId);
        int freq = 0;

        for (String item : docContents) {
            if (item.toLowerCase().contains(term.toLowerCase())) {
                freq++;
            }
        }

        return freq;
    }

    private List<String> getRecord(int docId) {
        return dataset.get(docId);
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
