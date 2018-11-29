package edu.comp479.search.tokenizer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import edu.comp479.crawler.DocDiskManager;
import edu.comp479.crawler.Document;
import edu.comp479.crawler.Tokenizer;

public class TokenStream implements ITokenStream {
    private DocDiskManager docDiskManager;

    private List<Long> docIds;
    private Iterator<Long> docIdsIter;
    private long currentDocId;

    private List<String> tokens;
    private Iterator<String> tokenIter;

    public TokenStream(DocDiskManager docDiskManager, List<Long> docIdList, Path directory) {
        this.docIds = docIdList;
        this.docIdsIter = docIdList.iterator();
        this.docDiskManager = docDiskManager;
    }

    @Override
    /**
     * Return the next token in this stream.
     * 
     * @return The next token.
     * @throws NoSuchElementException If no tokens are left.
     */
    public IToken next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No Token left");
        }

        return new Token(tokenIter.next(), currentDocId);
    }

    @Override
    /**
     * Returns {@code true} if there is a new token in the stream.
     * 
     * @return {@code true} if a new token is available.
     */
    public boolean hasNext() {
        if (tokens == null || !tokenIter.hasNext()) {
            if (!fetchNewTokens()) {
                return false;
            }
        }

        return tokenIter.hasNext();
    }

    /**
     * Retrieve the next document tokens and prepare them for the stream.
     * 
     * @return {@code true} if no new token are available.
     */
    private boolean fetchNewTokens() {
        if (!docIdsIter.hasNext()) {
            return false;
        }
        this.currentDocId = docIdsIter.next();
        Document nextDoc = docDiskManager.readFromDisk(currentDocId);

        Tokenizer tokenizer = new Tokenizer();
        List<String> tokens = new ArrayList<>();
        tokens.addAll(tokenizer.getTokens(nextDoc.getTitle()));
        tokens.addAll(tokenizer.getTokens(nextDoc.getBody()));

        tokens = tokenizer.removeCap(tokens);
        tokens = tokenizer.removePunctuation(tokens);
        tokens = tokenizer.removeDigits(tokens);

        this.tokens = tokens;
        this.tokenIter = tokens.iterator();
        return true;
    }

}
