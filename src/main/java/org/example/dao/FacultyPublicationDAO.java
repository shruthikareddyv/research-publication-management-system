package org.example.dao;

import org.example.database.DBConnection;
import org.example.model.AuthorInfo;
import org.example.model.FacultyPublication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Member 4: manages publication-to-faculty mapping and author sequence. */
public class FacultyPublicationDAO {
    /** Replaces a publication's complete author list; list position defines author order. */
    public void replaceAuthors(String publicationId, List<Integer> facultyIds) throws SQLException {
        if (publicationId == null || publicationId.isBlank()) throw new IllegalArgumentException("Publication ID is required.");
        if (facultyIds == null || facultyIds.isEmpty()) throw new IllegalArgumentException("Add at least one author.");
        if (facultyIds.size() > 5) throw new IllegalArgumentException("A publication can have a maximum of 5 faculty authors.");
        Set<Integer> uniqueIds = new HashSet<>(facultyIds);
        if (uniqueIds.size() != facultyIds.size()) throw new IllegalArgumentException("The same faculty ID cannot be added twice.");
        if (uniqueIds.stream().anyMatch(id -> id == null || id <= 0)) throw new IllegalArgumentException("Faculty IDs must be positive integers.");

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement check = con.prepareStatement("SELECT 1 FROM Publication WHERE Publication_ID=?")) {
                    check.setString(1, publicationId.trim());
                    try (ResultSet rs = check.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Publication ID was not found. Add the publication first.");
                    }
                }
                // Validate each faculty ID before deleting the current author list.
                try (PreparedStatement check = con.prepareStatement("SELECT 1 FROM Faculty WHERE Faculty_ID=?")) {
                    for (int id : facultyIds) {
                        check.setInt(1, id);
                        try (ResultSet rs = check.executeQuery()) {
                            if (!rs.next()) throw new IllegalArgumentException("Faculty ID " + id + " was not found.");
                        }
                    }
                }
                try (PreparedStatement delete = con.prepareStatement("DELETE FROM Faculty_Publication WHERE Publication_ID=?")) {
                    delete.setString(1, publicationId.trim());
                    delete.executeUpdate();
                }
                String insertSql = "INSERT INTO Faculty_Publication (Faculty_ID, Publication_ID, Author_Order) VALUES (?, ?, ?)";
                try (PreparedStatement insert = con.prepareStatement(insertSql)) {
                    for (int i = 0; i < facultyIds.size(); i++) {
                        insert.setInt(1, facultyIds.get(i));
                        insert.setString(2, publicationId.trim());
                        insert.setInt(3, i + 1);
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                try { con.rollback(); } catch (SQLException rollbackError) { ex.addSuppressed(rollbackError); }
                throw ex;
            }
        }
    }

    public List<AuthorInfo> getAuthorsForPublication(String publicationId) throws SQLException {
        List<AuthorInfo> result = new ArrayList<>();
        String sql = "SELECT fp.Author_Order, f.Faculty_ID, f.Faculty_Name, f.Department " +
                "FROM Faculty_Publication fp JOIN Faculty f ON f.Faculty_ID=fp.Faculty_ID " +
                "WHERE fp.Publication_ID=? ORDER BY fp.Author_Order";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, publicationId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(new AuthorInfo(rs.getInt("Author_Order"), rs.getInt("Faculty_ID"),
                        rs.getString("Faculty_Name"), rs.getString("Department")));
            }
        }
        return result;
    }

    public List<FacultyPublication> getMappingsForFaculty(int facultyId) throws SQLException {
        List<FacultyPublication> result = new ArrayList<>();
        String sql = "SELECT Faculty_ID, Publication_ID, Author_Order FROM Faculty_Publication WHERE Faculty_ID=? ORDER BY Publication_ID";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(new FacultyPublication(rs.getInt("Faculty_ID"),
                        rs.getString("Publication_ID"), rs.getInt("Author_Order")));
            }
        }
        return result;
    }
}
