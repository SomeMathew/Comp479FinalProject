package edu.comp479.crawler;

public class Document {
    private static long count = 0;

    private long documentId = 0;
    private String title;
    private String body;

    public Document(String title, String body) {

        this.title = title;
        this.body = body;
        this.documentId = count;

        count++;

    }

    /**
     * No-arg constructor for serialization to and from disk.
     */
    public Document() {

    }

    public String getTitle() {
        return this.title;
    }

    public String getBody() {
        return this.body;
    }

    public long getDocumentId() {
        return this.documentId;
    }
}
