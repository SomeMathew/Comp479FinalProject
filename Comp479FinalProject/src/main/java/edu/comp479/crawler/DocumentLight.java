package edu.comp479.crawler;

public class DocumentLight {
    private long documentId;
    private String title;
    private String url;

    public DocumentLight(long docId, String title, String url) {
        this.title = title;
        this.url = url;
        this.documentId = docId;
    }
    
    public DocumentLight(Document original) {
        this.title = original.getTitle();
        this.url = original.getUrl();
        this.documentId = original.getDocumentId();
    }

    /**
     * No-arg constructor for serialization to and from disk.
     */
    public DocumentLight() {

    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return this.title;
    }

    public long getDocumentId() {
        return this.documentId;
    }
}
