package org.example.model;

public class PublicationReportRow {
    private final String publicationId;
    private final String title;
    private final String publicationType;
    private final int year;
    private final String publisher;
    private final String doi;
    private final String url;
    private final String authors;
    private final String indexingAgencies;

    public PublicationReportRow(String publicationId, String title, String publicationType,
                                int year, String publisher, String doi, String url,
                                String authors, String indexingAgencies) {
        this.publicationId = publicationId;
        this.title = title;
        this.publicationType = publicationType;
        this.year = year;
        this.publisher = publisher;
        this.doi = doi;
        this.url = url;
        this.authors = authors;
        this.indexingAgencies = indexingAgencies;
    }
    public String getPublicationId() { return publicationId; }
    public String getTitle() { return title; }
    public String getPublicationType() { return publicationType; }
    public int getYear() { return year; }
    public String getPublisher() { return publisher; }
    public String getDoi() { return doi; }
    public String getUrl() { return url; }
    public String getAuthors() { return authors; }
    public String getIndexingAgencies() { return indexingAgencies; }
}
