package edu.comp479.search.program;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.comp479.crawler.CrawlerMain;
import edu.comp479.crawler.DocDiskManager;
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

        index(indexDir, cacheDir, constructDir, indexName, maxMemUseMb, inputBufferCount, bufferSize);
    }

    public void index(String indexDir, String cacheDir, String constructDir, String indexName, int maxMemoryUsageMb,
            int inputBufferCount, int bufferSize) {
        CrawlerMain crawler = new CrawlerMain();

        LOGGER.info("Executing the Crawler sub-module...");
        try {
            crawler.execute();
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
    }

}
