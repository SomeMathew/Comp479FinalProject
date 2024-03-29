package edu.comp479.search.program;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.comp479.crawler.CrawlerMain;
import edu.comp479.crawler.DocDiskManager;
import edu.comp479.crawler.Document;
import edu.comp479.crawler.DocumentLight;
import edu.comp479.search.indexer.Indexer;
import edu.comp479.search.tokenizer.TokenStream;
import net.sourceforge.argparse4j.inf.Namespace;

public class AppIndex implements IApp {
    private static final Logger LOGGER = Logger.getLogger(AppIndex.class.getName());

    public AppIndex() {
    }

    @Override
    public void execute(Namespace args) {
        String indexName = args.getString("indexName");
        String indexDir = args.getString("indexDir");
        String cacheDir = args.getString("cacheDir");
        String constructDir = args.getString("constructDir");
        int maxMemUseMb = args.getInt("maxMemUse");
        int inputBufferCount = args.getInt("inputBufferCount");
        int bufferSize = args.getInt("bufferSize");
        int maxDocCount = args.getInt("docMaxCount");

        index(indexDir, cacheDir, constructDir, indexName, maxMemUseMb, inputBufferCount, bufferSize, maxDocCount);
    }

    public void index(String indexDir, String cacheDir, String constructDir, String indexName, int maxMemoryUsageMb,
            int inputBufferCount, int bufferSize, int maxDocCount) {
        CrawlerMain crawler = new CrawlerMain();

        LOGGER.info("Executing the Crawler sub-module...");
        try {
            crawler.execute(maxDocCount);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to run the crawler to completion, aborting.", e);
            return;
        }

        Path cachePath = Paths.get(cacheDir);
        Path indexPath = Paths.get(indexDir);
        Path constructPath = Paths.get(constructDir);

        LOGGER.info("Dumping to document cache...");
        DocDiskManager docDiskManager = null;
        try {
            docDiskManager = new DocDiskManager(cachePath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to initialize the document disk cache", e);
        }
        List<Long> docIds = crawler.dumpToDisk(docDiskManager);

        // Null the crawler to help GC
        LOGGER.info("Trying to clear memory..");
        crawler.clearDocuments();
        crawler = null;
        System.gc();

        LOGGER.info("Preparing cache for indexing...");
        // For partial restart
//        List<String> files = null;
//        try {
//            files = Files.readAllLines(Paths.get("./indexLarge/docIds.txt"));
//        } catch (IOException e1) {
//            e1.printStackTrace();
//            return;
//        }
//        List<Long> docIds = files.stream().map(Long::parseLong).sorted().collect(Collectors.toList());
        TokenStream tokenStream = new TokenStream(docDiskManager, docIds);

        Indexer indexer = new Indexer(indexName, tokenStream, constructPath, indexPath, maxMemoryUsageMb,
                inputBufferCount, bufferSize, bufferSize);

        LOGGER.info("Indexing...");
        try {
            indexer.execute();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to run the indexer to completion, aborting.", e);
            return;
        }

        LOGGER.info(String.format("Index completed succesfully! IndexName: %s, Index Directory: %s", indexName,
                indexPath.toString()));

        LOGGER.info("Creating light cache...");
        createLightCacheForRetrieval(docDiskManager, docIds);
        LOGGER.info("Light URL cache created.");
    }

    private void createLightCacheForRetrieval(DocDiskManager docDiskManager, List<Long> docIds) {
        for (long docId : docIds) {
            Document docOriginal = docDiskManager.readFromDisk(docId);
            DocumentLight light = new DocumentLight(docOriginal);
            docDiskManager.writeToDisk(light);
        }
    }

}
