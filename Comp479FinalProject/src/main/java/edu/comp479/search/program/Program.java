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
        buildParser.addArgument("--dest-dir", "-d")
                .help("Selects the destination directory for the inverted index")
                .action(Arguments.store())
                .metavar("DIR")
                .dest("directory")
                .setDefault(".");
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
