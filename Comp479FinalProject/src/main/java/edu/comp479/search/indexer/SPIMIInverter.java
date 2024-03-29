package edu.comp479.search.indexer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.comp479.search.indexer.file.IndexWriter;
import edu.comp479.search.indexer.file.IndexDataMapperFactory;
import edu.comp479.search.tokenizer.IToken;
import edu.comp479.search.tokenizer.ITokenStream;
import edu.comp479.search.util.SentimentDictionaryBuilder;

import static com.google.common.base.Preconditions.*;

public class SPIMIInverter {
    private static final Logger LOGGER = Logger.getLogger(SPIMIInverter.class.getName());

    private static final int MAX_MEM_EXCEEDED_COUNT = 3;
    private static final int MAX_BLOCK_SIZE = 1000000;
            
    public static final String DEFAULT_DIRECTORY = ".";
    public static final int MIN_MEMORY_USE = 32;
    public static final int DEFAULT_MAX_MEMORY_USE = 64;

    private final String indexName;
    private final ITokenStream tokenStream;
    private final long maxMemoryUsageByte;
    private final Path directory;

    private final IndexBlockBuilderFactory blockBuilderFactory;
    private final IndexDataMapperFactory indexWriterFactory;
    private final Runtime runtime;

    private int blockCount = 0;
    private int memExceededCount = 0;
    private boolean indexingIsDone = false;
    private boolean useBlockSize = false;

    /**
     * @throws IOException If the sentiment dictionary cannot be loaded.
     * @see SPIMIInverter#SPIMIInverter(String, ITokenStream, Path, long,
     *      IndexBlockBuilderFactory, IndexDataMapperFactory, Runtime)
     */
    public SPIMIInverter(String indexName, ITokenStream tokenStream) throws IOException {
        this(indexName, tokenStream, Paths.get(DEFAULT_DIRECTORY));
    }

    /**
     * @throws IOException If the sentiment dictionary cannot be loaded.
     * @see SPIMIInverter#SPIMIInverter(String, ITokenStream, Path, long,
     *      IndexBlockBuilderFactory, IndexDataMapperFactory, Runtime)
     */
    public SPIMIInverter(String indexName, ITokenStream tokenStream, Path directory) throws IOException {
        this(indexName, tokenStream, directory, DEFAULT_MAX_MEMORY_USE,
                new SentimentDictionaryBuilder().loadSentimentDictionary());
    }

    /**
     * @throws IOException If the sentiment dictionary cannot be loaded.
     * @see SPIMIInverter#SPIMIInverter(String, ITokenStream, Path, long,
     *      IndexBlockBuilderFactory, IndexDataMapperFactory, Runtime)
     */
    public SPIMIInverter(String indexName, ITokenStream tokenStream, Path directory, long maxMemoryUsageMb)
            throws IOException {
        this(indexName, tokenStream, directory, maxMemoryUsageMb,
                new SentimentDictionaryBuilder().loadSentimentDictionary());
    }

    /**
     * @see SPIMIInverter#SPIMIInverter(String, ITokenStream, Path, long,
     *      IndexBlockBuilderFactory, IndexDataMapperFactory, Runtime)
     */
    public SPIMIInverter(String indexName, ITokenStream tokenStream, Path directory, long maxMemoryUsageMb,
            Map<String, Integer> sentimentDictionary) {
        this(indexName, tokenStream, directory, maxMemoryUsageMb, new IndexBlockBuilderFactory(sentimentDictionary),
                new IndexDataMapperFactory(), Runtime.getRuntime());
    }

    /**
     * Create a new SPIMI Inverter to process a tokenStream.
     * 
     * @param indexName              Prefix name for this index, each block will be
     *                               individually parameterized.
     * @param tokenStream            Stream of {@link ITokenStream} to process.
     * @param directory              Directory to write the blocks to.
     * @param maxMemoryUsageMb       Maximum program memory usage before a new block
     *                               is created. Must be at least
     *                               {@value #MIN_MEMORY_USE}.
     * @param blockBuilderFactory    Factory of Block Builder.
     * @param indexDataMapperFactory Factory of Datamapper to write the blocks to
     *                               disk.
     * @param runtime                The java application runtime object.
     */
    public SPIMIInverter(String indexName, ITokenStream tokenStream, Path directory, long maxMemoryUsageMb,
            IndexBlockBuilderFactory blockBuilderFactory, IndexDataMapperFactory indexDataMapperFactory,
            Runtime runtime) {
        this.indexName = checkNotNull(indexName);
        checkArgument(!indexName.isEmpty(), "Index Name cannot be empty.");
        this.tokenStream = checkNotNull(tokenStream);
        this.directory = checkNotNull(directory);
        this.blockBuilderFactory = checkNotNull(blockBuilderFactory);
        this.indexWriterFactory = checkNotNull(indexDataMapperFactory);
        this.runtime = checkNotNull(runtime);

        if (maxMemoryUsageMb < MIN_MEMORY_USE) {
            maxMemoryUsageMb = MIN_MEMORY_USE;
        }
        this.maxMemoryUsageByte = ((long) maxMemoryUsageMb) * 1024 * 1024;
    }

    /**
     * Creates the next block of the inverted index and writes it to disk.
     * 
     * This algorithm will continue until either the stream is empty or the
     * {@code maxMemoryUsageMb} is attained in program use.
     * 
     * <p>
     * <b>Note:</b> If {@code maxMemoryUsageMb} is hit, it will attempt to call the
     * garbage collector of the JVM.
     * 
     * @return The name of the index block created by this pass of the algorithm.
     */
    public String invert() {
        String indexBlockName = String.format("%s_%s.blk", indexName, blockCount);
        IndexBlockBuilder builder = blockBuilderFactory.createIndexBlockBuilder();
        while (true) {
            if (!tokenStream.hasNext()) {
                indexingIsDone = true;
                break;
            }
            if (!validateMemoryUsage(builder.getSize())) {
                System.gc();
                break;
            }
            IToken nextToken = tokenStream.next();
            builder.addPosting(nextToken.getTerm(), nextToken.getDocId());
        }

        if (builder.getSize() > 0) {
            try (IndexWriter indexWriter = indexWriterFactory.createIndexWriter(indexBlockName, directory)) {
                builder.writeToDisk(indexWriter);
                builder = null;
                blockCount++;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to write the index Block.", e);
                return null;
            }
            LOGGER.log(Level.INFO, "New block written to disk: " + indexBlockName);
            return indexBlockName;
        } else {
            return null;
        }
    }

    public boolean indexingIsDone() {
        return indexingIsDone;
    }

    /**
     * Validates the current memory usage of the program.
     * 
     * @return {@code true} if the limit is hit, {@code false} otherwise
     */
    private boolean validateMemoryUsage(int blockSize) {
        if (memExceededCount > MAX_MEM_EXCEEDED_COUNT) {
            LOGGER.log(Level.WARNING, "JVM is being a pain, not clearing the gc, revert to block size.");
            useBlockSize = true;
            memExceededCount = 0;
            return true;
        } else if (useBlockSize == true) {
            // Override the check for mem usage
            boolean validateCheck = blockSize <= MAX_BLOCK_SIZE;
            if (!validateCheck) {
                LOGGER.info("In mem override mode: blockSize hit -> " + blockSize);
            }
            return validateCheck;
        }
        long totalMemoryUsage = runtime.totalMemory();
        boolean usageExceeded = totalMemoryUsage < this.maxMemoryUsageByte;
        if (!usageExceeded) {
            LOGGER.log(Level.INFO, "Memory exceeded, starting next block. Limit count: " + memExceededCount);
            memExceededCount++;
        }
        return totalMemoryUsage < this.maxMemoryUsageByte;
    }
}
