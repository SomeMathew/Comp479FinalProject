package edu.comp479.search.tokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import static com.google.common.base.Preconditions.*;

public class TokenizerNormalize {
    private static Logger LOGGER = Logger.getLogger(TokenizerNormalize.class.getName());
    Analyzer analyzer;

    /**
     * Creates a new Tokenizer Normalizer which does a minimal amount of processing.
     * 
     * <p>
     * Filters: <br>
     * <ul>
     * <li>Lower Case Filter
     * <li>StopFilter
     * <li>Lucene Classic Filter
     * </ul>
     * 
     * <p>
     * This Tokenizer uses the implementation of the Classic (Standard) Tokenizer of
     * Apache Lucene.
     * 
     */
    public TokenizerNormalize() {
        analyzer = new ClassicAnalyzer();
    }

    /**
     * Tokenizes and normalizes the given text into a list of String tokens.
     * 
     * @param text Text to normalize and tokenize
     * @return Lists of token for the given text.
     */
    public List<String> analyze(String text) {
        List<String> tokens = new ArrayList<>();
        return analyzeAppendToList(text, tokens);
    }

    /**
     * Tokenizes and normalizes the given text into the given {@code list}.
     * 
     * @param text Text to normalize and tokenize
     * @return The {@code list} given as argument.
     */
    public List<String> analyzeAppendToList(String text, List<String> list) {
        checkNotNull(text);
        checkNotNull(list);

        try (TokenStream ts = analyzer.tokenStream("doc", text)) {
            OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
            ts.reset();
            while (ts.incrementToken()) {
                list.add(offsetAtt.toString());
            }
            ts.end();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Problem when reading the analyzed stream of tokens", e);
        }

        return list;
    }
}
