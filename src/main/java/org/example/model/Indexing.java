package org.example.model;

public class Indexing {
    private String publicationId;
    private String indexingName;

    public Indexing() { }
    public Indexing(String publicationId, String indexingName) {
        this.publicationId = publicationId;
        this.indexingName = indexingName;
    }
    public String getPublicationId() { return publicationId; }
    public void setPublicationId(String publicationId) { this.publicationId = publicationId; }
    public String getIndexingName() { return indexingName; }
    public void setIndexingName(String indexingName) { this.indexingName = indexingName; }
}
