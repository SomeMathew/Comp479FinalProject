package edu.comp479.search.tokenizer;

public interface ITokenStream {
    public IToken next();

    public boolean hasNext();
}
