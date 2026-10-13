package org.example.dao;

import org.example.database.DBConnection;
import org.example.model.Publication;
import org.example.model.PublicationDetails;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Writes the common publication row and its type-specific row atomically. */
public class PublicationDAO {
    private static final String INSERT_PUBLICATION =
            "INSERT INTO Publication (Publication_ID, Title, Publication_Type, `Year`, Publisher, DOI, URL) VALUES (?, ?, ?, ?, ?, ?, ?)";

    public void addPublication(Publication publication, PublicationDetails details) throws SQLException {
        validatePublication(publication);
        if (details == null) throw new IllegalArgumentException("Type-specific publication details are required.");
        details.validateFor(publication.getPublicationType());

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(INSERT_PUBLICATION)) {
                    ps.setString(1, publication.getPublicationId().trim());
                    ps.setString(2, publication.getTitle().trim());
                    ps.setString(3, publication.getPublicationType());
                    ps.setInt(4, publication.getYear());
                    ps.setString(5, publication.getPublisher().trim());
                    ps.setString(6, emptyToNull(publication.getDoi()));
                    ps.setString(7, emptyToNull(publication.getUrl()));
                    ps.executeUpdate();
                }
                insertTypeDetails(con, publication.getPublicationId().trim(), publication.getPublicationType(), details);
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                try { con.rollback(); } catch (SQLException rollbackError) { ex.addSuppressed(rollbackError); }
                throw ex;
            }
        }
    }

    public boolean exists(String publicationId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT 1 FROM Publication WHERE Publication_ID=?")) {
            ps.setString(1, publicationId.trim());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public Publication findById(String publicationId) throws SQLException {
        String sql = "SELECT Publication_ID, Title, `Year`, Publisher, DOI, URL, Publication_Type FROM Publication WHERE Publication_ID=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, publicationId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Publication(rs.getString("Publication_ID"), rs.getString("Title"),
                        rs.getInt("Year"), rs.getString("Publisher"), rs.getString("DOI"),
                        rs.getString("URL"), rs.getString("Publication_Type"));
            }
        }
    }

    public List<Publication> getAllPublications() throws SQLException {
        List<Publication> result = new ArrayList<>();
        String sql = "SELECT Publication_ID, Title, `Year`, Publisher, DOI, URL, Publication_Type FROM Publication ORDER BY `Year` DESC, Title";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Publication(rs.getString("Publication_ID"), rs.getString("Title"),
                        rs.getInt("Year"), rs.getString("Publisher"), rs.getString("DOI"),
                        rs.getString("URL"), rs.getString("Publication_Type")));
            }
        }
        return result;
    }

    private void insertTypeDetails(Connection con, String id, String type, PublicationDetails d) throws SQLException {
        String sql;
        switch (type) {
            case "Journal" -> {
                sql = "INSERT INTO Journal (Publication_ID, Volume, Issue, Pages) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, id); ps.setString(2, d.getVolume().trim());
                    ps.setString(3, d.getIssue().trim()); ps.setString(4, d.getPages().trim()); ps.executeUpdate();
                }
            }
            case "Conference" -> {
                sql = "INSERT INTO Conference (Publication_ID, Conference_Date) VALUES (?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, id); ps.setDate(2, Date.valueOf(d.getConferenceDate().trim())); ps.executeUpdate();
                }
            }
            case "Book" -> {
                sql = "INSERT INTO Book (Publication_ID, ISBN, Edition) VALUES (?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, id); ps.setString(2, d.getIsbn().trim());
                    ps.setString(3, d.getEdition().trim()); ps.executeUpdate();
                }
            }
            case "Book Chapter" -> {
                sql = "INSERT INTO Book_Chapter (Publication_ID, Book_Title, Chapter_Number, Pages) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, id); ps.setString(2, d.getBookTitle().trim());
                    ps.setInt(3, d.getChapterNumber()); ps.setString(4, d.getPages().trim()); ps.executeUpdate();
                }
            }
            default -> throw new IllegalArgumentException("Unsupported publication type: " + type);
        }
    }

    private void validatePublication(Publication p) {
        if (p == null) throw new IllegalArgumentException("Publication details are required.");
        if (p.getPublicationId() == null || p.getPublicationId().isBlank() || p.getPublicationId().trim().length() > 10)
            throw new IllegalArgumentException("Publication ID is required and must be at most 10 characters.");
        required(p.getTitle(), "Title");
        required(p.getPublisher(), "Publisher");
        if (p.getYear() < 1900 || p.getYear() > 2200) throw new IllegalArgumentException("Enter a valid publication year.");
        if (p.getPublicationType() == null || !List.of("Journal", "Conference", "Book", "Book Chapter").contains(p.getPublicationType()))
            throw new IllegalArgumentException("Publication type must be Journal, Conference, Book, or Book Chapter.");
    }

    private void required(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " cannot be empty.");
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
