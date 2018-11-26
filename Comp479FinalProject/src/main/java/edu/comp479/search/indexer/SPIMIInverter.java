package edu.comp479.search.indexer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.comp479.search.indexer.file.IndexWriter;
import edu.comp479.search.indexer.file.IndexDataMapperFactory;
import edu.comp479.search.tokenizer.IToken;
import edu.comp479.search.tokenizer.ITokenStream;

import static com.google.common.base.Preconditions.*;

public class SPIMIInverter {
    private static final Logger LOGGER = Logger.getLogger(SPIMIInverter.class.getName());

    public static final String DEFAULT_DIRECTORY = ".";
    public static final int DEFAULT_MIN_MEMORY_USE = 32;
    public static final int DEFAULT_MAX_MEMORY_USE = 64;

    private final String indexName;
    private final ITokenStream tokenStream;
    private final long maxMemoryUsageMb;
    private final Path directory;

    private final IndexBlockBuilderFactory blockBuilderFactory;
    private final IndexDataMapperFactory indexWriterFactory;

    private int blockCount = 0;

    /**
     * @see SPIMIInverter#SPIMIInverter(String, ITokenStream, long, Path,
     *      IndexBlockBuilderFactory, IndexDataMapperFactory)
     */
    public SPIMIInverter(String indexName, ITokenStream tokenStream, IndexBlockBuilderFactory blockBuilderFactory,
            IndexDataMapperFactory indexDataMapperFactory) {
        this(indexName, tokenStream, DEFAULT_MAX_MEMORY_USE, Paths.get(DEFAULT_DIRECTORY), blockBuilderFactory,
                indexDataMapperFactory);
    }

    /**
     * Create a new SPIMI Inverter to process a tokenStream.
     * 
     * @param indexName              Prefix name for this index, each block will be
     *                               individually parameterized.
     * @param tokenStream            Stream of {@link ITokenStream} to process.
     * @param maxMemoryUsageMb       Maximum program memory usage before a new block
     *                               is created. Must be at least
     *                               {@value #DEFAULT_MIN_MEMORY_USE}.
     * @param directory              Directory to write the blocks to.
     * @param blockBuilderFactory    Factory of Block Builder.
     * @param indexDataMapperFactory Factory of Datamapper to write the blocks to
     *                               disk.
     */
    public SPIMIInverter(String indexName, ITokenStream tokenStream, long maxMemoryUsageMb, Path directory,
            IndexBlockBuilderFactory blockBuilderFactory, IndexDataMapperFactory indexDataMapperFactory) {
        this.indexName = checkNotNull(indexName);
        checkArgument(!indexName.isEmpty(), "Index Name cannot be empty.");
        this.tokenStream = checkNotNull(tokenStream);
        this.directory = checkNotNull(directory);
        this.blockBuilderFactory = checkNotNull(blockBuilderFactory);
        this.indexWriterFactory = checkNotNull(indexDataMapperFactory);

        if (maxMemoryUsageMb < DEFAULT_MIN_MEMORY_USE) {
            maxMemoryUsageMb = DEFAULT_MIN_MEMORY_USE;
        }
        this.maxMemoryUsageMb = maxMemoryUsageMb;
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

        while (validateMemoryUsage() && tokenStream.hasNext()) {
            IToken nextToken = tokenStream.next();
            builder.addPosting(nextToken.getTerm(), nextToken.getDocId());
        }

        if (builder.getSize() > 0) {
            try (IndexWriter indexWriter = indexWriterFactory.createIndexWriter(indexBlockName, directory)) {
                builder.writeToDisk(indexWriter);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to write the index Block.", e);
                return null;
            }
            LOGGER.log(Level.INFO, "New block written to disk: " + indexBlockName);
        }

        return indexBlockName;
    }

    /**
     * Validates the current memory usage of the program.
     * 
     * @return {@code true} if the limit is hit, {@code false} otherwise
     */
    private boolean validateMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemoryUsage = runtime.totalMemory();
        return totalMemoryUsage < this.maxMemoryUsageMb;
    }
}
