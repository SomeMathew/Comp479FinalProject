package edu.comp479.ranking;

import java.io.IOException;
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

        RankEngine ren = new RankEngine();

        FileOperations fo = new FileOperations();
        String afinnPath = "AFINN/AFINN-111.txt";
        Map<String, Integer> sentiment = fo.readAfinnFile(afinnPath);

        List<String> queryList = new ArrayList();
        queryList.add("superb");
        queryList.add("Yoshiro");

        Map<Integer, HashMap<Integer, Double>> sortedMap = ren.rankDocuments(queryList, sentiment);
        for (Map.Entry<Integer, HashMap<Integer, Double>> entry : sortedMap.entrySet()) {
            Integer index = entry.getKey();
            HashMap<Integer, Double> value = entry.getValue();
            for (Map.Entry<Integer, Double> entry1 : value.entrySet()) {
                Integer docId = entry1.getKey();
                Double score = entry1.getValue();

                System.out.println(index + ": " + docId + ": [" + score + "]");
            }

        }
    }

}
