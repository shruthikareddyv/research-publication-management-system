package org.example.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton configuration holder for JDBC connectivity.
 * A new Connection is returned for each DAO operation, avoiding the unsafe
 * practice of sharing one open JDBC Connection across the whole application.
 */
public final class DBConnection {
    private static final DBConnection INSTANCE = new DBConnection();

    private final String url;
    private final String user;
    private final String password;

    private DBConnection() {
        this.url = setting("DB_URL",
                "jdbc:mysql://localhost:3306/ResearchPublicationDB?serverTimezone=UTC");
        this.user = setting("DB_USER", "root");
        this.password = setting("DB_PASSWORD", setting("MYSQL_PASSWORD", ""));
    }

    public static DBConnection getInstance() {
        return INSTANCE;
    }

    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /** Convenience method used by DAO classes. */
    public static Connection getConnection() throws SQLException {
        return INSTANCE.openConnection();
    }

    private static String setting(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
