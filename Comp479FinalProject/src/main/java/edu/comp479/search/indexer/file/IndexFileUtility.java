package edu.comp479.search.indexer.file;

public final class IndexFileUtility {
    public static final String DICTIONARY_EXTENSION = ".dic";
    public static final String POSTINGS_EXTENSION = ".pst";
    public static final String DESCRIPTOR_EXTENSION = ".desc";
    public static final String NORMS_EXTENSION = ".nrm";
    
    public static final int FILE_VERSION = 0xAB01;
    public static final float TFIDF_VAR_FLOAT_PRECISION = 1000.0f;
    
    public static final int NORM_HEADER_SIZE = 8;
    public static final int NORM_ENTRY_SIZE = 16;

    // Static Utility class
    private IndexFileUtility() {
    }

}
