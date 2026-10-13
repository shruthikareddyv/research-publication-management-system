package org.example.dao;

import org.example.database.DBConnection;
import org.example.model.Indexing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Member 4: CRUD and lookup operations for indexing agencies. */
public class IndexingDAO {
    public void addIndexing(String publicationId, String agency) throws SQLException {
        validate(publicationId, agency);
        String sql = "INSERT INTO Indexing (Publication_ID, Indexing_Name) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, publicationId.trim());
            ps.setString(2, agency.trim());
            ps.executeUpdate();
        }
    }

    public boolean removeIndexing(String publicationId, String agency) throws SQLException {
        String sql = "DELETE FROM Indexing WHERE Publication_ID=? AND Indexing_Name=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, publicationId.trim());
            ps.setString(2, agency.trim());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Indexing> getIndexingForPublication(String publicationId) throws SQLException {
        List<Indexing> result = new ArrayList<>();
        String sql = "SELECT Publication_ID, Indexing_Name FROM Indexing WHERE Publication_ID=? ORDER BY Indexing_Name";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, publicationId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(new Indexing(rs.getString("Publication_ID"), rs.getString("Indexing_Name")));
            }
        }
        return result;
    }

    public List<String> getAllAgencies() throws SQLException {
        List<String> result = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT DISTINCT Indexing_Name FROM Indexing ORDER BY Indexing_Name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(rs.getString(1));
        }
        return result;
    }

    private void validate(String publicationId, String agency) {
        if (publicationId == null || publicationId.isBlank()) throw new IllegalArgumentException("Publication ID is required.");
        if (publicationId.trim().length() > 10) throw new IllegalArgumentException("Publication ID must be at most 10 characters.");
        if (agency == null || agency.isBlank()) throw new IllegalArgumentException("Indexing agency cannot be empty.");
        if (agency.trim().length() > 50) throw new IllegalArgumentException("Indexing agency must be 50 characters or fewer.");
    }
}
