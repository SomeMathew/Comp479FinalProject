package edu.comp479.ranking;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

/**
 * This class is responsible to retrieve information to match the given search query.
 *
 * @author Mohsen Parisay <mohsenparisay@gmail.com>
 * @version <1.1> - <09.Nov.2018>
 */
public class SearchEngine {

    private final String AND_OPERATOR = "and";
    private final String OR_OPERATOR = "or";
    private final String SPACE = " ";
    private final Queue<String> operatorsQueue = new LinkedList<>();
    private final Queue<List<String>> resultsQueue = new LinkedList<>();
    private final Stack<List<String>> tempResults = new Stack<>();

    /**
     * start the search pipeline.
     *
     * @param searchQuery the given query by user
     * @param ignoreCase if false, the search will be case sensitive
     * @return the list of found items wrapped in a list
     */
    public List<List<String>> search(String searchQuery, boolean ignoreCase) {
        List<String> keywords = new ArrayList();
        if (ignoreCase) {
            searchQuery = searchQuery.toLowerCase();
        }
        List<String> tmpQuery = splitQuery(searchQuery);
        System.out.println("query: " + tmpQuery + "\n");

        for (String word : tmpQuery) {
            word = word.toLowerCase().trim();
            if (word.equals(AND_OPERATOR) || word.equals(OR_OPERATOR)) {
                operatorsQueue.add(word);
            } else {
                keywords.add(word);
            }
        }

        return runSearch(keywords, ignoreCase);
    }

    /**
     * split the search query to extract single tokens
     *
     * @param keyPhrase the given search query
     * @return the extracted single tokens in a list
     */
    public List<String> splitQuery(String keyPhrase) {
        String[] tmp = keyPhrase.split(SPACE);
        List<String> result = new ArrayList<>();
        for (String word : tmp) {
            result.add(word);
        }

        return result;
    }

    /**
     * runs the search operations for the given keywords.
     *
     * @param keywords the given keywords as a list of tokens
     * @param ignoreCase this is a flag to run case sensitive searches
     * @return the extracted document IDs as a list
     */
    private List<List<String>> runSearch(List<String> keywords, boolean ignoreCase) {
        List<List<String>> foundResults = new ArrayList();

        for (String item : keywords) {
            List<String> values = extractKeyword(item, ignoreCase);
            resultsQueue.add(values);
        }

        if (keywords.size() == 1) {
            foundResults.add(resultsQueue.remove());
            return foundResults;
        }

        if (operatorsQueue.isEmpty()) {
            for (int i = 0; i < keywords.size() - 1; i++) {
                operatorsQueue.add(AND_OPERATOR);
            }

        }

        for (Iterator<String> iterator = operatorsQueue.iterator(); iterator.hasNext();) {
            String opr = iterator.next();
            List<String> first;

            if (tempResults.isEmpty()) {
                first = resultsQueue.remove();
            } else {
                first = tempResults.pop();
            }
            List<String> second = resultsQueue.remove();

            List<String> result;
            if (opr.equals(OR_OPERATOR)) {
                result = getUnion(first, second);
            } else {
                result = findIntersection(first, second);
            }

            tempResults.push(result);

        }// end of for

        foundResults.add(tempResults.pop());

        return foundResults;
    }

    /**
     * read the merged blocks from disk
     *
     * @return the merged block as a has map
     */
    private Map<String, List<String>> getDataSet() {
        FileOperations fo = new FileOperations();
        String filePath = fo.getRawIndexPath();
        return fo.readRawIndexFile(filePath);
    }

    /**
     * get all elements of two lists
     *
     * @param first the first list
     * @param second the second list
     * @return the total elements of the both given inputs
     */
    private List<String> getUnion(List<String> first, List<String> second) {
        List<String> results = new ArrayList();

        for (String string : first) {
            results.add(string);
        }

        for (String string : second) {
            if (!results.contains(string)) {
                results.add(string);
            }
        }

        return results;
    }

    public List<Integer> getPostings(String query) {
        query = query.toLowerCase().trim();

        List<Integer> docIdList = new ArrayList();
        List<String> tmpResult = extractKeyword(query, true);
        for (String item : tmpResult) {
            int docId = Integer.valueOf(item);
            docIdList.add(docId);
        }
        return docIdList;
    }    
    
    
    /**
     * find the common elements of two lists
     *
     * @param first the first list
     * @param second the second list
     * @return the common elements which are in the both lists
     */
    private List<String> findIntersection(List<String> first, List<String> second) {
        List<String> result = new ArrayList();

        for (String item : first) {
            if (second.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * extracts the occurrences of the given token in data set
     *
     * @param keyword the given token
     * @param ignoreCase decision flag to check if case sensitive
     * @return the list of found IDs of the given token
     */
    private List<String> extractKeyword(String keyword, boolean ignoreCase) {
        Map<String, List<String>> dataSet = getDataSet();
        List<String> result = new ArrayList();

        for (Map.Entry<String, List<String>> entry : dataSet.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();

            if (ignoreCase) {
                key = key.toLowerCase();
            }

            if (key.equals(keyword)) {
                for (String item : value) {
                    if (!result.contains(item)) {
                        result.add(item);
                    }
                }
            }
        }

        return result;
    }

}
