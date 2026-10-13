package org.example.dao;

import org.example.database.DBConnection;
import org.example.model.Metric;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Member 6: aggregate counts and summary statistics. */
public class AnalyticsDAO {
    public List<Metric> departmentWisePublicationCounts() throws SQLException {
        String sql = "SELECT f.Department AS label, COUNT(DISTINCT fp.Publication_ID) AS total " +
                "FROM Faculty f LEFT JOIN Faculty_Publication fp ON fp.Faculty_ID=f.Faculty_ID " +
                "GROUP BY f.Department ORDER BY total DESC, label";
        return queryMetrics(sql);
    }

    public List<Metric> annualPublicationTrends() throws SQLException {
        String sql = "SELECT CAST(`Year` AS CHAR) AS label, COUNT(*) AS total FROM Publication " +
                "GROUP BY `Year` ORDER BY `Year`";
        return queryMetrics(sql);
    }

    public List<Metric> publicationCountsByType() throws SQLException {
        String sql = "SELECT Publication_Type AS label, COUNT(*) AS total FROM Publication " +
                "GROUP BY Publication_Type ORDER BY total DESC, label";
        return queryMetrics(sql);
    }

    public List<Metric> publicationCountsByIndexing() throws SQLException {
        String sql = "SELECT Indexing_Name AS label, COUNT(DISTINCT Publication_ID) AS total FROM Indexing " +
                "GROUP BY Indexing_Name ORDER BY total DESC, label";
        return queryMetrics(sql);
    }

    private List<Metric> queryMetrics(String sql) throws SQLException {
        List<Metric> result = new ArrayList<>();
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new Metric(rs.getString("label"), rs.getLong("total")));
        }
        return result;
    }
}
