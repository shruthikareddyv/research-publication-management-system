package org.example.dao;

import org.example.database.DBConnection;
import org.example.model.PublicationReportRow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Member 5: detailed filtered reports and CSV export. */
public class ReportDAO {
    private static final String SELECT_BASE = "SELECT p.Publication_ID, p.Title, p.Publication_Type, p.`Year`, p.Publisher, p.DOI, p.URL, " +
            "COALESCE((SELECT GROUP_CONCAT(CONCAT(f.Faculty_Name, ' (Order ', fp.Author_Order, ')') " +
            "ORDER BY fp.Author_Order SEPARATOR '; ') FROM Faculty_Publication fp " +
            "JOIN Faculty f ON f.Faculty_ID=fp.Faculty_ID WHERE fp.Publication_ID=p.Publication_ID), '') AS Authors, " +
            "COALESCE((SELECT GROUP_CONCAT(ix.Indexing_Name ORDER BY ix.Indexing_Name SEPARATOR '; ') " +
            "FROM Indexing ix WHERE ix.Publication_ID=p.Publication_ID), '') AS Indexing_Agencies " +
            "FROM Publication p ";

    public List<PublicationReportRow> getAllPublications() throws SQLException {
        return queryRows("ORDER BY p.`Year` DESC, p.Title");
    }

    public List<PublicationReportRow> byFaculty(int facultyId) throws SQLException {
        String condition = "WHERE EXISTS (SELECT 1 FROM Faculty_Publication fp WHERE fp.Publication_ID=p.Publication_ID AND fp.Faculty_ID=?) ORDER BY p.`Year` DESC, p.Title";
        return queryRows(condition, facultyId);
    }

    public List<PublicationReportRow> byYearRange(int startYear, int endYear) throws SQLException {
        if (startYear > endYear) throw new IllegalArgumentException("Start year cannot be greater than end year.");
        return queryRows("WHERE p.`Year` BETWEEN ? AND ? ORDER BY p.`Year`, p.Title", startYear, endYear);
    }

    public List<PublicationReportRow> byType(String publicationType) throws SQLException {
        if (publicationType == null || publicationType.isBlank()) throw new IllegalArgumentException("Publication type is required.");
        return queryRows("WHERE LOWER(p.Publication_Type)=LOWER(?) ORDER BY p.`Year` DESC, p.Title", publicationType.trim());
    }

    public List<PublicationReportRow> byIndexing(String agency) throws SQLException {
        if (agency == null || agency.isBlank()) throw new IllegalArgumentException("Indexing agency is required.");
        String condition = "WHERE EXISTS (SELECT 1 FROM Indexing ix WHERE ix.Publication_ID=p.Publication_ID AND LOWER(ix.Indexing_Name)=LOWER(?)) ORDER BY p.`Year` DESC, p.Title";
        return queryRows(condition, agency.trim());
    }

    private List<PublicationReportRow> queryRows(String suffix, Object... params) throws SQLException {
        List<PublicationReportRow> result = new ArrayList<>();
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(SELECT_BASE + suffix)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new PublicationReportRow(
                            rs.getString("Publication_ID"), rs.getString("Title"),
                            rs.getString("Publication_Type"), rs.getInt("Year"),
                            rs.getString("Publisher"), rs.getString("DOI"), rs.getString("URL"),
                            rs.getString("Authors"), rs.getString("Indexing_Agencies")));
                }
            }
        }
        return result;
    }

    public void exportCsv(List<PublicationReportRow> rows, Path destination) throws IOException {
        Path parent = destination.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
            writer.write("Publication ID,Title,Type,Year,Publisher,DOI,URL,Authors,Indexing Agencies");
            writer.newLine();
            for (PublicationReportRow r : rows) {
                writer.write(csv(r.getPublicationId()) + "," + csv(r.getTitle()) + "," +
                        csv(r.getPublicationType()) + "," + r.getYear() + "," + csv(r.getPublisher()) + "," +
                        csv(r.getDoi()) + "," + csv(r.getUrl()) + "," + csv(r.getAuthors()) + "," +
                        csv(r.getIndexingAgencies()));
                writer.newLine();
            }
        }
    }

    private String csv(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
