package edu.comp479.search.program;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public final class Program {
    public static final String PROGRAM_NAME = "EmoSearch";

    // Don't instantiate
    private Program() {
        throw new UnsupportedOperationException("Static class.");
    }

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor(PROGRAM_NAME).build()
                .description(PROGRAM_NAME
                        + " is a web information retrieval system which uses sentiment values to rank web documents.")
                .epilog("Use \"" + PROGRAM_NAME + " [command] --help\" for more information about a command.");
        Subparsers subparsers = parser.addSubparsers()
                .description("Select the mode of operation of the IR System.")
                .help("build constructs the inverted index.\nsearch executes the search query module.");

        Subparser buildParser = subparsers.addParser("build").description("Builds the inverted index.");
        
        buildParser.addArgument("indexName")
                .help("Name of the index")
                .action(Arguments.store())
                .metavar("INDEX_NAME")
                .dest("indexName");
        
        buildParser.addArgument("--dest-dir", "-d")
                .help("Selects the destination directory for the inverted index.")
                .action(Arguments.store())
                .metavar("DIR_INDEX")
                .dest("indexDir")
                .setDefault("./index/");
        
        buildParser.addArgument("--doc-cache-dir", "-c")
                .help("Selects the destination directory for the document cache.")
                .action(Arguments.store())
                .metavar("DIR_CACHE")
                .dest("cacheDir")
                .setDefault("./cache/");
        
        buildParser.addArgument("--construct-dir")
                .help("Selects the directory for construction of the index.")
                .action(Arguments.store())
                .metavar("DIR_CONSTRUCT")
                .dest("constructDir")
                .setDefault("./construct/");
        
        buildParser.addArgument("--construct-max-mem", "-m")
                .help("Selects the maximum memory use in MB when constructing the inverted index.")
                .action(Arguments.store())
                .metavar("MAX_MEM_USE")
                .type(Integer.class)
                .dest("maxMemUse")
                .setDefault(512);
        
        buildParser.addArgument("--construct-buff-count", "-b")
                .help("Selects the number of input buffer to use when constructing the index (Merge process).")
                .action(Arguments.store())
                .metavar("INPUT_BUFF_COUNT")
                .type(Integer.class)
                .dest("inputBufferCount")
                .setDefault(8);
        
        buildParser.addArgument("--construct-buff-size", "-s")
                .help("Selects the size of the buffers (in KB) to use when constructing the index (Merge process).")
                .action(Arguments.store())
                .metavar("BUFF_SIZE_KB")
                .type(Integer.class)
                .dest("bufferSize")
                .setDefault(4096);
        
        buildParser.setDefault("appObj", new AppIndex());
        
        
        Subparser searchParser = subparsers.addParser("search").description("Search in a previously built index.");
        searchParser.addArgument("--src-dir", "-d")
                .help("Selects the directory of the inverted index files")
                .action(Arguments.store())
                .metavar("DIR")
                .dest("directory")
                .setDefault(".");
       searchParser.setDefault("appObj", new AppSearch()); 
        
       
        try {
            Namespace namespace = parser.parseArgs(args);
            ((IApp) namespace.get("appObj")).execute(namespace);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }

}
