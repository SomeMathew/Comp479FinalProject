package edu.comp479.search.indexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import com.google.common.collect.Lists;

import static java.util.Comparator.comparing;

import edu.comp479.search.index.structure.IndexEntry;
import edu.comp479.search.indexer.file.IndexDataMapperFactory;
import edu.comp479.search.indexer.file.IndexReaderStreamed;
import edu.comp479.search.indexer.file.IndexWriter;

import static com.google.common.base.Preconditions.*;

public class IndexBlockMerger {
	public static final int DEFAULT_INPUT_BUFFER_COUNT = 4;
	public static final int DEFAULT_BUFFER_SIZE = 4096;

	private final Path inputDir;
	private final Path outputDir;

	private final String indexOutputName;

	private final int inputBufferCount;
	private final int inputBufferSize;
	private final int outputBufferSize;
	private final List<String> initialBlockNames;
	private final IndexDataMapperFactory indexDataMapperFactory;

	private int currentIteration;

	/**
	 * Creates a new {@link IndexBlockMerger} to merge multi blocks with an external
	 * k-way merge algorithm.
	 * 
	 * <p>
	 * This merger will merge the index in multiple pass by opening
	 * {@code inputBufferCount} blocks at a time. Each pass will output a single
	 * block for each {@code inputBufferCount} blocks. The algorithm will then merge
	 * the created merged block until a single file remain.
	 * 
	 * <p>
	 * The goal of this multi-pass algorithm to external merging is to reduce the
	 * amount of seek to the hard-drive. If all files are merged at the same time
	 * then a large amount of seeks or memory is required to complete successfully.
	 * This implementation trades Disk Space for less I/O lag induced by the drive
	 * seek.
	 * 
	 * <p>
	 * <b>Precondition:</b> This merging algorithm assumes the well ordering of each
	 * block that it should merge. The dictionarry entries must be alphabetically
	 * ordered and the postings must be orderered by docid.
	 * 
	 * <p>
	 * <b>Note:</b> A single pass algorithm can be effectively done by setting
	 * {@code inputBufferCount} equal to {@code blockNames.size()}.
	 * 
	 * 
	 * @param indexName              The name of the final index to create.
	 * @param blockNames             The list of index blocks name to merge.
	 * @param inputDir               The directory of the blocks to merge.
	 * @param outputDir              The directory into which to write the final
	 *                               merged index.
	 * @param indexDataMapperFactory File Mapper Factory to read/write the index to
	 *                               disk.
	 * @param inputBufferCount       The number of maximum input files to process at
	 *                               a time.
	 * @param inputBufferSize        Size of each input buffer.
	 * @param outputBufferSize       Size of the single outputbuffer.
	 * 
	 * @throws IOException If {@code outputDir} doesn't exist and cannot be created.
	 */
	public IndexBlockMerger(String indexName, List<String> blockNames, Path inputDir, Path outputDir,
			IndexDataMapperFactory indexDataMapperFactory, int inputBufferCount, int inputBufferSize,
			int outputBufferSize) throws IOException {
		this.indexOutputName = checkNotNull(indexName);
		this.initialBlockNames = checkNotNull(blockNames);

		checkArgument(!blockNames.isEmpty(), "The blocks cannot be empty: size: %s", blockNames.size());
		checkArgument(inputBufferCount > 0, "The number of input buffers must be greater than 0. Given: %s",
				inputBufferCount);
		checkArgument(inputBufferSize > 0, "The input buffer size must be greater than 0. Given: %s", inputBufferSize);

		checkArgument(outputBufferSize > 0, "The output buffer size must be greater than 0. Given: %s",
				outputBufferSize);

		this.inputBufferCount = inputBufferCount;
		this.inputBufferSize = inputBufferSize;
		this.outputBufferSize = outputBufferSize;

		this.inputDir = checkNotNull(inputDir);
		this.outputDir = checkNotNull(outputDir);

		this.indexDataMapperFactory = indexDataMapperFactory;

		Files.createDirectories(outputDir);
	}

	/**
	 * Merges the blocks in a multi-pass external merge algorithm.
	 * 
	 * @see #IndexBlockMerger(String, List, Path, Path, IndexDataMapperFactory, int,
	 *      int, int)
	 */
	public String externalMultiwayMerge() {
		currentIteration = 0;
		// TODO method stub
		throw new UnsupportedOperationException("Method stub");
	}

	/**
	 * Executes one pass of the merge algorithm over all currently considered
	 * blocks.
	 * 
	 * @param blockNames Blocks to merge for this iteration.
	 * @return The list of blocks that were created as a result of this pass merges.
	 * @throws IOException 
	 */
	private List<String> mergeIteration(List<String> blockNames) throws IOException {
		List<List<String>> partitionedBlockForEachMerge = Lists.partition(blockNames, inputBufferCount);

		List<String> outputBlocks = new ArrayList<>();

		for (int i = 0; i < partitionedBlockForEachMerge.size(); i++) {
			String nextBlockName = String.format("%s.partial%d_pass%d", indexOutputName, i, currentIteration);

			outputBlocks.add(merge(partitionedBlockForEachMerge.get(i), nextBlockName));
		}

		return outputBlocks;
	}

	private String merge(List<String> inputNames, String outputName) throws IOException {
		IndexWriter outputWriter = indexDataMapperFactory.createIndexWriter(outputName, outputDir, outputBufferSize);
		IndexReaderStreamed[] inputReaders = createInputs(inputNames);

		inputReaders[0].readNextEntry();
		for (int i = 0; i < inputNames.size(); i++) {

		}

		// TODO method stub
		return outputName;
	}

	private IndexReaderStreamed[] createInputs(List<String> inputNames) throws IOException {
		IndexReaderStreamed[] inputReaders = new IndexReaderStreamed[inputNames.size()];
		for (int i = 0; i < inputReaders.length; i++) {
			IndexReaderStreamed reader = indexDataMapperFactory.createIndexReaderStreamed(inputNames.get(i), inputDir);
			reader.open(inputBufferSize);
			inputReaders[i] = reader;
		}
		return inputReaders;
	}

	private boolean closeInputs(IndexReaderStreamed[] inputs) {
		// TODO method stub
		return false;
	}

	private static class MergeHelper {
		private PriorityQueue<IndexEntry> pq;
		private IndexReaderStreamed[] inputReaders;

		public MergeHelper(IndexReaderStreamed[] inputReaders) {
			this.pq = new PriorityQueue<>(comparing(IndexEntry::getTerm));
			this.inputReaders = inputReaders;
		}
	}
}
