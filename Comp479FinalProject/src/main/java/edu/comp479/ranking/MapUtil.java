package edu.comp479.ranking;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an auxiliary class for hash map. It contains a sort function to
 * receive a hash map and sort it by value.
 *
 *
 * @author Mohsen Parisay <mohsenparisay@gmail.com>
 * @version <1.0> - <09.Nov.2018>
 */
public class MapUtil {

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
