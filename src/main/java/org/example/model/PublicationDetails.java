package org.example.model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Type-specific fields for Journal, Conference, Book, or Book Chapter. */
public class PublicationDetails {
    private String volume;
    private String issue;
    private String pages;
    private String conferenceDate;
    private String isbn;
    private String edition;
    private String bookTitle;
    private Integer chapterNumber;

    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }
    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }
    public String getPages() { return pages; }
    public void setPages(String pages) { this.pages = pages; }
    public String getConferenceDate() { return conferenceDate; }
    public void setConferenceDate(String conferenceDate) { this.conferenceDate = conferenceDate; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public Integer getChapterNumber() { return chapterNumber; }
    public void setChapterNumber(Integer chapterNumber) { this.chapterNumber = chapterNumber; }

    public void validateFor(String type) {
        if (type == null) throw new IllegalArgumentException("Publication type is required.");
        switch (type) {
            case "Journal" -> {
                required(volume, "Volume"); required(issue, "Issue"); required(pages, "Pages");
            }
            case "Conference" -> {
                required(conferenceDate, "Conference date (YYYY-MM-DD)");
                try { LocalDate.parse(conferenceDate.trim()); }
                catch (DateTimeParseException ex) {
                    throw new IllegalArgumentException("Conference date must use YYYY-MM-DD, for example 2026-10-13.");
                }
            }
            case "Book" -> { required(isbn, "ISBN"); required(edition, "Edition"); }
            case "Book Chapter" -> {
                required(bookTitle, "Book title"); required(pages, "Pages");
                if (chapterNumber == null || chapterNumber < 1) {
                    throw new IllegalArgumentException("Chapter number must be a positive integer.");
                }
            }
            default -> throw new IllegalArgumentException("Select Journal, Conference, Book, or Book Chapter.");
        }
    }

    private static void required(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " cannot be empty.");
    }
}
