package edu.comp479.search.indexer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;

import static java.util.Map.Entry.comparingByKey;

import edu.comp479.search.index.structure.IndexEntry;
import edu.comp479.search.index.structure.Posting;
import edu.comp479.search.indexer.file.IndexDataMapperFactory;
import edu.comp479.search.indexer.file.IndexReaderStreamed;
import edu.comp479.search.indexer.file.IndexWriter;
import edu.comp479.search.indexer.file.NormFileEntry;
import edu.comp479.search.tokenizer.ITokenStream;

import static edu.comp479.search.util.Weights.tfIdf;

import static com.google.common.base.Preconditions.*;

public class Indexer implements IIndexer {
    private final String indexName;
    private final ITokenStream tokenStream;
    private final Path constructionDir;
    private final Path outputDir;
    private final long maxMemoryUsageMb;
    private final int inputBufferCount;
    private final int inputBufferSize;
    private final int outputBufferSize;

    public Indexer(String indexName, ITokenStream tokenStream, Path constructionDir, Path outputDir,
            long maxMemoryUsageMb, int inputBufferCount, int inputBufferSize, int outputBufferSize) throws IOException {
        this.indexName = checkNotNull(indexName);
        this.tokenStream = checkNotNull(tokenStream);
        this.constructionDir = checkNotNull(constructionDir);
        this.outputDir = checkNotNull(outputDir);
        this.maxMemoryUsageMb = maxMemoryUsageMb;
        this.inputBufferCount = inputBufferCount;
        this.inputBufferSize = inputBufferSize;
        this.outputBufferSize = outputBufferSize;
    }

    @Override
    public void execute() throws IOException {
        SPIMIInverter spimi = new SPIMIInverter(indexName, tokenStream, constructionDir, maxMemoryUsageMb);

        List<String> blocksNames = buildBlocks(spimi);

        IndexBlockMerger merger = new IndexBlockMerger(indexName, blocksNames, constructionDir, constructionDir,
                new IndexDataMapperFactory(), inputBufferCount, inputBufferSize, outputBufferSize);

        String finalBlockName = merger.externalMultiwayMerge();

        computeWeightsAndCreateFinalIndex(finalBlockName, outputDir);
    }

    /**
     * Builds all possible blocks from the SPIMI indexer.
     * 
     * @return List of index block name.
     */
    private List<String> buildBlocks(SPIMIInverter spimiIndexer) {
        ArrayList<String> blockNames = new ArrayList<>();
        String lastBlockFileName = null;
        while (true) {
            lastBlockFileName = spimiIndexer.invert();

            if (lastBlockFileName != null) {
                blockNames.add(lastBlockFileName);
            } else {
                break;
            }
        }
        return blockNames;
    }

    /**
     * Compute the weights (tf-idf) for the postings and the length norm for each
     * document and create the final index.
     * 
     * @param indexName Name of the index to write
     * @param dir       Directory where to write the index
     * @throws IOException
     */
    private void computeWeightsAndCreateFinalIndex(String indexName, Path dir) throws IOException {
        IndexReaderStreamed indexReader = new IndexReaderStreamed(indexName, dir);
        IndexWriter indexWriter = new IndexWriter(indexName, dir);

        Map<Long, NormFileEntry> normAccumulator = new HashMap<>();
        long docCountN = indexReader.getDocCount();

        while (indexReader.hasNextEntry()) {
            IndexEntry nextIndexEntry = indexReader.readNextEntry();
            List<Posting> postingsListWithWeight = computePostingWeight(docCountN, nextIndexEntry);

            accumulateWeightsPerDocId(normAccumulator, postingsListWithWeight);

            indexWriter.write(nextIndexEntry.getDictionaryEntry(), postingsListWithWeight);
        }

        indexReader.close();

        List<NormFileEntry> norms = normAccumulator.entrySet().stream().sorted(comparingByKey())
                .map((entry) -> entry.getValue()).peek(this::finalizeNormEntryLength).collect(toImmutableList());

        indexWriter.writeFinalizeIndexWithNorm(docCountN, norms);
        indexWriter.close();
    }

    /**
     * Accumulate the weights of each posting in the list for each docId to compute
     * the length norm of each document.
     * 
     * This function accumulates the square tf-idf of each term/docid.
     * 
     * <p>
     * <b>Note: </b>The square root of each accumulator must be taken to complete
     * the length calculation.
     * 
     * @see Indexer#finalizeNormEntryLength(NormFileEntry)
     * 
     * @param normAccumulators
     * @param postingsListWithWeight
     */
    private void accumulateWeightsPerDocId(Map<Long, NormFileEntry> normAccumulators,
            List<Posting> postingsListWithWeight) {
        for (Posting posting : postingsListWithWeight) {
            NormFileEntry normEntry = normAccumulators.get(posting.getDocId());
            if (normEntry == null) {
                normEntry = new NormFileEntry(posting.getDocId(), 0, 0);
                normAccumulators.put(posting.getDocId(), normEntry);
            }

            float norm = normEntry.getNorm();
            norm += Math.pow((double) posting.getTfIdf(), 2);
            normEntry.setNorm(norm);
        }
    }

    /**
     * Finalize each length-norm for the entries by taking the square root.
     * 
     * @param entry
     */
    private void finalizeNormEntryLength(NormFileEntry entry) {
        float norm = entry.getNorm();
        norm = (float) Math.sqrt(norm);
        entry.setNorm(norm);
    }

    /**
     * Computes the tf-idf weight of each posting in the index entry.
     * 
     * @param docCountN
     * @param nextIndexEntry
     * @return The new list of Posting with weight
     */
    private List<Posting> computePostingWeight(long docCountN, IndexEntry nextIndexEntry) {
        long termDocFreq = nextIndexEntry.getDocumentFrequency();
        float termIdf = (float) Math.log10((float) docCountN / (float) termDocFreq);

        List<Posting> postingsListWithWeight = nextIndexEntry.getPostingsList().stream()
                .map((posting) -> posting.withWeight(tfIdf(termIdf))).collect(toImmutableList());
        return postingsListWithWeight;
    }
}
