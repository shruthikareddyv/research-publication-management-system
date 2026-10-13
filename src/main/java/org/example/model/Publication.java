package org.example.model;

public class Publication {
    private String publicationId;
    private String title;
    private int year;
    private String publisher;
    private String doi;
    private String url;
    private String publicationType;

    public Publication() { }

    public Publication(String publicationId, String title, int year, String publisher,
                       String doi, String url, String publicationType) {
        this.publicationId = publicationId;
        this.title = title;
        this.year = year;
        this.publisher = publisher;
        this.doi = doi;
        this.url = url;
        this.publicationType = publicationType;
    }

    public String getPublicationId() { return publicationId; }
    public void setPublicationId(String publicationId) { this.publicationId = publicationId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public String getDoi() { return doi; }
    public void setDoi(String doi) { this.doi = doi; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getPublicationType() { return publicationType; }
    public void setPublicationType(String publicationType) { this.publicationType = publicationType; }
}
