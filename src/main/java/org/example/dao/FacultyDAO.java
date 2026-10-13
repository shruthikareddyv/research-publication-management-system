package org.example.dao;

import org.example.database.DBConnection;
import org.example.model.Faculty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FacultyDAO {
    public void addFaculty(Faculty faculty) throws SQLException {
        validate(faculty);
        String sql = "INSERT INTO Faculty (Faculty_ID, Faculty_Name, Department, Designation) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, faculty.getFacultyId());
            ps.setString(2, faculty.getFacultyName().trim());
            ps.setString(3, faculty.getDepartment().trim());
            ps.setString(4, faculty.getDesignation().trim());
            ps.executeUpdate();
        }
    }

    public boolean updateFaculty(Faculty faculty) throws SQLException {
        validate(faculty);
        String sql = "UPDATE Faculty SET Faculty_Name=?, Department=?, Designation=? WHERE Faculty_ID=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, faculty.getFacultyName().trim());
            ps.setString(2, faculty.getDepartment().trim());
            ps.setString(3, faculty.getDesignation().trim());
            ps.setInt(4, faculty.getFacultyId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteFaculty(int id) throws SQLException {
        // Delete mapping rows first so this also works with the original schema,
        // whose foreign keys may not have ON DELETE CASCADE.
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement mappings = con.prepareStatement(
                        "DELETE FROM Faculty_Publication WHERE Faculty_ID=?")) {
                    mappings.setInt(1, id);
                    mappings.executeUpdate();
                }
                int changed;
                try (PreparedStatement faculty = con.prepareStatement("DELETE FROM Faculty WHERE Faculty_ID=?")) {
                    faculty.setInt(1, id);
                    changed = faculty.executeUpdate();
                }
                con.commit();
                return changed > 0;
            } catch (SQLException | RuntimeException ex) {
                try { con.rollback(); } catch (SQLException rollbackError) { ex.addSuppressed(rollbackError); }
                throw ex;
            }
        }
    }

    public Faculty findById(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT Faculty_ID, Faculty_Name, Department, Designation FROM Faculty WHERE Faculty_ID=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Faculty> findByName(String name) throws SQLException {
        return findMany("SELECT Faculty_ID, Faculty_Name, Department, Designation FROM Faculty WHERE Faculty_Name LIKE ? ORDER BY Faculty_Name",
                "%" + name.trim() + "%");
    }

    public List<Faculty> findByDepartment(String department) throws SQLException {
        return findMany("SELECT Faculty_ID, Faculty_Name, Department, Designation FROM Faculty WHERE Department LIKE ? ORDER BY Faculty_Name",
                "%" + department.trim() + "%");
    }

    public List<Faculty> getAllFaculty() throws SQLException {
        List<Faculty> result = new ArrayList<>();
        String sql = "SELECT Faculty_ID, Faculty_Name, Department, Designation FROM Faculty ORDER BY Faculty_ID";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(map(rs));
        }
        return result;
    }

    private List<Faculty> findMany(String sql, String value) throws SQLException {
        List<Faculty> result = new ArrayList<>();
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    private Faculty map(ResultSet rs) throws SQLException {
        return new Faculty(rs.getInt("Faculty_ID"), rs.getString("Faculty_Name"),
                rs.getString("Department"), rs.getString("Designation"));
    }

    private void validate(Faculty f) {
        if (f == null) throw new IllegalArgumentException("Faculty details are required.");
        if (f.getFacultyId() <= 0) throw new IllegalArgumentException("Faculty ID must be a positive integer.");
        required(f.getFacultyName(), "Faculty name");
        required(f.getDepartment(), "Department");
        required(f.getDesignation(), "Designation");
    }

    private void required(String s, String label) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(label + " cannot be empty.");
    }
}
