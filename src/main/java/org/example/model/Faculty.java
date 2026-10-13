package org.example.model;

public class Faculty {
    private int facultyId;
    private String facultyName;
    private String department;
    private String designation;

    public Faculty() { }

    public Faculty(int facultyId, String facultyName, String department, String designation) {
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.department = department;
        this.designation = designation;
    }

    public int getFacultyId() { return facultyId; }
    public void setFacultyId(int facultyId) { this.facultyId = facultyId; }
    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
}
