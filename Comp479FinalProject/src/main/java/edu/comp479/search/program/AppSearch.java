package edu.comp479.search.program;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.comp479.crawler.DocDiskManager;
import edu.comp479.crawler.Document;
import edu.comp479.ranking.RankEngine;
import edu.comp479.search.index.IInvertedIndex;
import edu.comp479.search.index.IndexFactory;
import edu.comp479.search.index.structure.IIndexEntry;
import edu.comp479.search.indexer.file.IndexReaderMemoryMapped;
import edu.comp479.search.tokenizer.TokenizerNormalize;
import net.sourceforge.argparse4j.inf.Namespace;

import static java.lang.System.out;

public class AppSearch implements IApp {
    private static final Logger LOGGER = Logger.getLogger(AppSearch.class.getName());

    private IndexReaderMemoryMapped indexReader;
    private IInvertedIndex index;
    private DocDiskManager cache;
    private TokenizerNormalize tokenizer;

    private Integer resultLimit;
    private boolean limitResult;

    public AppSearch() {
    }

    @Override
    public void execute(Namespace args) {
        String indexName = args.getString("indexName");
        String indexDir = args.getString("indexDir");
        String cacheDir = args.getString("cacheDir");
        Integer resultLimit = args.getInt("resultLimit");

        limitResult = resultLimit != null;
        this.resultLimit = resultLimit;

        init(indexName, indexDir, cacheDir);
        runLoop();
        closeIndex();
    }

    public void init(String indexName, String indexDir, String cacheDir) {
        LOGGER.info("Initializing the index for retrieval.");
        try {
            indexReader = new IndexReaderMemoryMapped(indexName, Paths.get(indexDir));
            indexReader.open();
            IndexFactory indexFactory = new IndexFactory();
            index = indexFactory.getIndex(indexReader);
            cache = new DocDiskManager(Paths.get(cacheDir));
            tokenizer = new TokenizerNormalize();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "IO error when accessing the index", e);
            try {
                indexReader.close();
            } catch (IOException e2) {
                LOGGER.log(Level.WARNING, "Error when closing the index reader.", e2);
            }
        }
    }

    public void closeIndex() {
        try {
            indexReader.close();
            index = null;
            indexReader = null;
            cache = null;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when closing the index memory map", e);
        }
    }

    public void runLoop() {
        try (Scanner scan = new Scanner(System.in)) {
            while (true) {
                out.println("Enter your query. (q to exit)");
                String rawQuery = scan.nextLine();
                if (rawQuery.equalsIgnoreCase("q")) {
                    break;
                }
                try {
                    search(rawQuery);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "IO Error when processing query. Please retry!", e);
                    continue;
                }
                retrieve(scan);
            }

        }

        out.println("Goodbye!");
    }

    /**
     * Document retrieval mode.
     * 
     * @param scan Opened {@link Scanner} to display the retrieval UI.
     */
    public void retrieve(Scanner scan) {
        while (true) {
            out.println("Enter your a document Id to the URL. (q to exit)");
            String userInput = scan.nextLine();
            if (userInput.equals("q")) {
                break;
            }
            long docId;
            try {
                docId = Long.parseLong(userInput);
            } catch (NumberFormatException e) {
                out.println("Invalid doc Id");
                continue;
            }
            Document doc = cache.readFromDisk(docId);
            out.println("URL: " + doc.getUrl());
        }
    }

    /**
     * Executes the search for a given query by the user.
     * 
     * @param rawQuery The user query.
     * @throws IOException
     */
    public void search(String rawQuery) throws IOException {
        List<String> queryTokens = tokenizer.analyze(rawQuery);

        LOGGER.info("Retrieving for tokens: " + queryTokens);
        HashMap<String, Integer> sentimentMap = new HashMap<>();
        for (String term : queryTokens) {
            IIndexEntry indexEntry = index.getPostings(term);
            int sentimentValue = indexEntry.getSentimentValue();
            sentimentMap.put(term, sentimentValue);
        }

        RankEngine ren = new RankEngine(index);

        Map<Integer, HashMap<Integer, Double>> sortedMap = ren.rankDocuments(queryTokens, sentimentMap);

        int resultDisplayed = 0;
        for (Map.Entry<Integer, HashMap<Integer, Double>> entry : sortedMap.entrySet()) {
            Integer rankIndex = entry.getKey();
            HashMap<Integer, Double> value = entry.getValue();

            for (Map.Entry<Integer, Double> entry1 : value.entrySet()) {
                if (limitResult && resultDisplayed >= resultLimit) {
                    break;
                }
                Integer docId = entry1.getKey();
                Double score = entry1.getValue();

                String url = cache.readLightFromDisk(docId).getUrl();
                if (url == null) {
                    LOGGER.info("Unable to read URL from cache.");
                    url = "no-url-err";
                }
                
                out.printf("Rank (%d) : docId [%d] : score [%f] : url [%s])\n", rankIndex + 1, docId, score, url);
                resultDisplayed++;
            }

        }
        out.println(String.format("\nDisplayed %d results out of %d", resultDisplayed, sortedMap.size()));
    }
}
