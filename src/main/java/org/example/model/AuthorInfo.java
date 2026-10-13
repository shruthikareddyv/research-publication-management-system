package org.example.model;

public class AuthorInfo {
    private final int authorOrder;
    private final int facultyId;
    private final String facultyName;
    private final String department;

    public AuthorInfo(int authorOrder, int facultyId, String facultyName, String department) {
        this.authorOrder = authorOrder;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.department = department;
    }
    public int getAuthorOrder() { return authorOrder; }
    public int getFacultyId() { return facultyId; }
    public String getFacultyName() { return facultyName; }
    public String getDepartment() { return department; }
}
