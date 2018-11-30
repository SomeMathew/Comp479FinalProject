package example;

import java.io.IOException;
import java.nio.file.Paths;

import edu.comp479.search.index.IInvertedIndex;
import edu.comp479.search.index.IndexFactory;
import edu.comp479.search.index.structure.IIndexEntry;
import edu.comp479.search.indexer.file.IndexReaderMemoryMapped;

public class InvertedIndexExample {

    public InvertedIndexExample() {
        // TODO Auto-generated constructor stub
    }

    public static void main(String args[]) throws IOException {
        IndexReaderMemoryMapped indexReader = new IndexReaderMemoryMapped("testLargeIndex", Paths.get("./testIndex/index/"));
        indexReader.open();
        IndexFactory indexFactory = new IndexFactory();
        IInvertedIndex index = indexFactory.getIndex(indexReader);

        IIndexEntry indexEntry = index.getPostings("concordia");

        System.out.println(indexEntry);

        indexReader.close();
    }

}
