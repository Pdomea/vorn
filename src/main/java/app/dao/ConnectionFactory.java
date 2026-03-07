package app.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {
    private static volatile String url;
    private static volatile String user;
    private static volatile String password;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("PostgreSQL JDBC driver not found.", ex);
        }
    }

    private ConnectionFactory() {
    }

    public static void configure(String dbUrl, String dbUser, String dbPassword) {
        url = dbUrl;
        user = dbUser;
        password = dbPassword;
    }

    public static Connection getConnection() throws SQLException {
        String resolvedUrl = firstNonBlank(url, System.getenv("BWI520_DB_URL"), "jdbc:postgresql://localhost:5432/bwi520");
        String resolvedUser = firstNonBlank(user, System.getenv("BWI520_DB_USER"), "postgres");
        String resolvedPassword = firstNonBlank(password, System.getenv("BWI520_DB_PASSWORD"), "postgres");
        return DriverManager.getConnection(resolvedUrl, resolvedUser, resolvedPassword);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
