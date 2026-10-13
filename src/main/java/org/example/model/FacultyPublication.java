package org.example.model;

/** Link between a faculty member and a publication, including author order. */
public class FacultyPublication {
    private int facultyId;
    private String publicationId;
    private int authorOrder;

    public FacultyPublication() { }
    public FacultyPublication(int facultyId, String publicationId, int authorOrder) {
        this.facultyId = facultyId;
        this.publicationId = publicationId;
        this.authorOrder = authorOrder;
    }
    public int getFacultyId() { return facultyId; }
    public void setFacultyId(int facultyId) { this.facultyId = facultyId; }
    public String getPublicationId() { return publicationId; }
    public void setPublicationId(String publicationId) { this.publicationId = publicationId; }
    public int getAuthorOrder() { return authorOrder; }
    public void setAuthorOrder(int authorOrder) { this.authorOrder = authorOrder; }
}
