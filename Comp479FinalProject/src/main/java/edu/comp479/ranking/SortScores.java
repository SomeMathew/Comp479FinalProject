package edu.comp479.ranking;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * This class sorts the ranked documents based on the partial order mentioned in
 * the assignment.
 *
 * @author Mohsen Parisay <mohsenparisay@gmail.com>
 * @version <1.0> - <26.nov.2018>
 */
public class SortScores {

    public void printMap(Map<Integer, Double> input, double order) {
        Map<Integer, Double> baseMap = MapUtil.sortByValue(input);

        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);

        if (order > 0) {
            ArrayList<Integer> keyList = new ArrayList(baseMap.keySet());

            int index = 1;
            for (int i = keyList.size() - 1; i >= 0; i--) {
                Integer key = keyList.get(i);
                double value = baseMap.get(key);

                System.out.println("Rank (" + index + ") docID: [" + key + "] score: <" + df.format(value) + ">");
                index++;
            }

        } else {
            int index = 1;
            for (Map.Entry<Integer, Double> entry : baseMap.entrySet()) {
                Integer key = entry.getKey();
                Double value = entry.getValue();

                System.out.println("Rank (" + index + ") docID: [" + key + "] score: <" + df.format(value) + ">");
                index++;
            }
        }

    }
}
