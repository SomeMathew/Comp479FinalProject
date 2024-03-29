package edu.comp479.search.indexer.file;

import java.io.IOException;
import java.nio.file.Path;

public class IndexDataMapperFactory {

	public IndexDataMapperFactory() {
	}

	public IndexWriter createIndexWriter(String indexName, Path dir, int bufferSize) throws IOException {
		return new IndexWriter(indexName, dir, bufferSize);
	}

	public IndexWriter createIndexWriter(String indexName, Path dir) throws IOException {
		return new IndexWriter(indexName, dir);
	}

	public IndexReaderMemoryMapped createIndexReaderMemoryMapped(String indexName, Path dir)
			throws IOException {
		return new IndexReaderMemoryMapped(indexName, dir);
	}

	public IndexReaderStreamed createIndexReaderStreamed(String indexName, Path dir) throws IOException {
		return new IndexReaderStreamed(indexName, dir);
	}
}
