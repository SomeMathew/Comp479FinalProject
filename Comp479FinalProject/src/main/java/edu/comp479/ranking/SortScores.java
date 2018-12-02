package edu.comp479.ranking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class sorts the ranked documents based on the partial order mentioned in
 * the assignment.
 *
 * @author Mohsen Parisay <mohsenparisay@gmail.com>
 * @version <1.0> - <26.nov.2018>
 */
public class SortScores {

    final int ROUND_PLACES = 4;
    public Map<Integer, HashMap<Integer, Double>> printMap(Map<Integer, Double> input, double order) {
        Map<Integer, Double> baseMap = MapUtil.sortByValue(input);
        if (order >= 0) {
            return sortPositive(baseMap);
        }
        return sortNegative(baseMap);
    }
    
    private Map<Integer, HashMap<Integer, Double>> sortPositive(Map<Integer, Double> baseMap) {
        ArrayList<Integer> keyList = new ArrayList(baseMap.keySet());
        Map<Integer, HashMap<Integer, Double>> rankedResult = new TreeMap();
        int index = 0;

        for (int i = keyList.size() - 1; i >= 0; i--) {
            Integer docId = keyList.get(i);
            double score = baseMap.get(docId);
            score = round(score, ROUND_PLACES);
            
            HashMap<Integer, Double> tmp = new HashMap<>();
            tmp.put(docId, score);
            rankedResult.put(index, tmp);
            index++;

        }
        return rankedResult;
    }
    
    private Map<Integer, HashMap<Integer, Double>> sortNegative(Map<Integer, Double> baseMap) {
        Map<Integer, HashMap<Integer, Double>> rankedResult = new TreeMap();
        int index = 0;
        for (Map.Entry<Integer, Double> entry : baseMap.entrySet()) {
            Integer docId = entry.getKey();
            double score = entry.getValue();
            score = round(score, ROUND_PLACES);
            
            HashMap<Integer, Double> tmp = new HashMap<>();
            tmp.put(docId, score);
            rankedResult.put(index, tmp);
            index++;
                        
        }
        return rankedResult;
    }
    
    public static double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

}
