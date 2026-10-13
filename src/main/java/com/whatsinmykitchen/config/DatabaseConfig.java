package com.whatsinmykitchen.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public final class DatabaseConfig {
    private DatabaseConfig() {
    }

    public static String getHost() {
        return getenv("DB_HOST", "localhost");
    }

    public static int getPort() {
        return Integer.parseInt(getenv("DB_PORT", "3306"));
    }

    public static String getDatabaseName() {
        return getenv("DB_NAME", "whats_in_my_kitchen");
    }

    public static String getUser() {
        return getenv("DB_USER", "root");
    }

    public static String getPassword() {
        return getenv("DB_PASSWORD", "");
    }

    public static boolean isConfigured() {
        Map<String, String> env = System.getenv();
        return env.containsKey("DB_HOST") || env.containsKey("DB_NAME") || env.containsKey("DB_USER");
    }

    public static String getJdbcUrl() {
        String host = getHost();
        int port = getPort();
        String databaseName = getDatabaseName();
        boolean useSsl = Boolean.parseBoolean(System.getenv().getOrDefault("DB_USE_SSL", "false"));
        StringBuilder url = new StringBuilder();
        url.append("jdbc:mysql://").append(host).append(":").append(port).append("/").append(databaseName);
        url.append("?useSSL=").append(useSsl);
        url.append("&allowPublicKeyRetrieval=true");
        url.append("&serverTimezone=UTC");
        url.append("&useUnicode=true");
        url.append("&characterEncoding=UTF-8");
        return url.toString();
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL Connector/J was not found on the classpath.", ex);
        }
        String url = getJdbcUrl();
        String user = getUser();
        String password = getPassword();
        return DriverManager.getConnection(url, user, password);
    }

    public static boolean isDatabaseReachable() {
        try (Connection connection = getConnection()) {
            return connection.isValid(5);
        } catch (SQLException ex) {
            return false;
        }
    }

    private static String getenv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
