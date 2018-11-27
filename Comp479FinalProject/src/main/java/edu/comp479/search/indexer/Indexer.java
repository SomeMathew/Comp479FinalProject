package edu.comp479.search.indexer;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.*;

public class Indexer implements IIndexer {
    private SPIMIInverter spimiIndexer;

    public Indexer(SPIMIInverter spimiInverter) {
        this.spimiIndexer = checkNotNull(spimiInverter);
    }

    @Override
    public void execute() {
        List<String> blocksNames = buildBlocks();
    }

    /**
     * Builds all possible blocks from the SPIMI indexer.
     * 
     * @return List of index block name.
     */
    private List<String> buildBlocks() {
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

}
