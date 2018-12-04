package edu.comp479.ranking;

import edu.comp479.search.index.IInvertedIndex;
import edu.comp479.search.index.IndexFactory;
import edu.comp479.search.index.structure.IIndexEntry;
import edu.comp479.search.index.structure.Posting;
import edu.comp479.search.indexer.file.IndexReaderMemoryMapped;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mohsen Parisay <mohsenparisay@gmail.com>
 * @version <1.0> - <04.dec.2018>
 */
public class TestAggregateFunction {

    private static IInvertedIndex index;

    private void readIndex(String name, String path) throws IOException {
        String directoryPath = path;

        IndexReaderMemoryMapped indexReader = new IndexReaderMemoryMapped(name, Paths.get(directoryPath));
        indexReader.open();
        IndexFactory indexFactory = new IndexFactory();
        TestAggregateFunction.index = indexFactory.getIndex(indexReader);
    }

    private String extractToken(String line) {
        String[] temp = line.split("\t");
        return temp[0].toLowerCase().trim();
    }

    private int extractSentiment(String line) {
        String[] temp = line.split("\t");
        int sentimentValue = Integer.valueOf(temp[1].toLowerCase().trim());
        return sentimentValue;
    }

    private HashMap<String, Integer> readSentimentFile(String filePath) throws FileNotFoundException, IOException {
        List<String> contents = readFile(filePath);
        HashMap<String, Integer> tokenSentimentMap = new HashMap<>();
        
        for (String item : contents) {
            String token = extractToken(item);
            int sentiment = extractSentiment(item);

            tokenSentimentMap.put(token, sentiment);
        }
        return tokenSentimentMap;
    }

    private HashMap<String, List<Integer>> createDictionary(List<String> queryList) {
        HashMap<String, List<Integer>> dictionary = new HashMap();

        for (String term : queryList) {
            List<Integer> postingsList = search(term);
            dictionary.put(term, postingsList);
        }

        return dictionary;
    }
    
    private List<String> readTokensFile(String filePath) throws IOException{
        return readFile(filePath);
    }
    
    private List<String> readFile(String filePath) throws FileNotFoundException, IOException {
        List<String> contents = new ArrayList<>();

        FileReader fr = new FileReader(filePath);
        BufferedReader br = new BufferedReader(fr);
        String line;

        try {
            while ((line = br.readLine()) != null) {
                contents.add(line.trim());
            }
        } finally {
            br.close();
        }
        return contents;
    }

    public List<Integer> search(String query) {
        List<Integer> docIdList = new ArrayList();
        IIndexEntry pos = index.getPostings(query);

        List<Posting> lp = pos.getPostingsList();
        for (Posting posting : lp) {
            int id = (int) posting.getDocId();
            docIdList.add(id);
        }

        return docIdList;
    }

    public static void main(String[] args) throws IOException {
        TestAggregateFunction tgf = new TestAggregateFunction();

        if (args[1] == null) {
            System.err.println("No input file to read");
            System.exit(1);
        } else {

            String sentimentFilePath = args[1];
            String tokensFilePath = args[2];
            String indexPath = args[3];
            String indexName = args[4];
            
            System.out.println("sentiment file: " + sentimentFilePath);
            System.out.println("tokens file: " + tokensFilePath);
            System.out.println("index path: " + indexPath);
            System.out.println("index name: " + indexName);
            
            tgf.readIndex(indexName, indexPath);

            HashMap<String, Integer> tokenSentimentMap = tgf.readSentimentFile(sentimentFilePath);
            List<String> tokens = tgf.readTokensFile(tokensFilePath);
            
            HashMap<String, List<Integer>> dictionaryMap = tgf.createDictionary(tokens);

            AggregateFunction af = new AggregateFunction(index);
            Map<Integer, Double> result = af.calculateFinalScore(dictionaryMap, tokenSentimentMap);

            System.out.println("\nScore results using aggregate function:\n");
            
            for (Map.Entry<Integer, Double> entry : result.entrySet()) {
                Integer docId = entry.getKey();
                Double score = entry.getValue();

                System.out.println("docId: [" + docId + "] score: [" + score + "]");
            }
            
        }// end of else

    }// end of main
}
