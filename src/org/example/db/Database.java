package org.example.db;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import org.example.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class Database {

    private final String connectUrl;
    private final String jdbcUrlForLog;
    @Getter
    private final String user;
    @Getter
    private final String schema;
    private final String password;

    public Database(Dotenv dotenv) {
        this.user = AppConfig.firstNonBlank(
                System.getenv("PG_USER"),
                System.getenv("USER"),
                AppConfig.get(dotenv, "PG_USER", "")
        );
        if (this.user == null || this.user.isBlank()) {
            throw new IllegalStateException("Задайте PG_USER или экспортируйте USER.");
        }

        String schemaFromUrl = null;
        String fullUrl = AppConfig.get(dotenv, "PG_JDBC_URL", null);
        if (fullUrl != null && !fullUrl.isBlank()) {
            UrlParts parts = stripCurrentSchemaFromUrl(fullUrl);
            this.connectUrl = parts.urlWithoutCurrentSchema();
            schemaFromUrl = parts.currentSchema();
            this.jdbcUrlForLog = fullUrl;
        } else {
            String host = AppConfig.get(dotenv, "PG_HOST", "pg");
            String port = AppConfig.get(dotenv, "PG_PORT", "5432");
            String database = AppConfig.get(dotenv, "PG_DATABASE", "studs");
            this.connectUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
            this.jdbcUrlForLog = connectUrl + " (schema=" + AppConfig.get(dotenv, "PG_SCHEMA", user) + ")";
        }

        this.schema = AppConfig.firstNonBlank(
                System.getenv("PG_SCHEMA"),
                AppConfig.get(dotenv, "PG_SCHEMA", ""),
                schemaFromUrl,
                this.user
        );

        String rawPassword = AppConfig.firstNonBlank(
                System.getenv("PG_PASSWORD"),
                System.getenv("PGPASSWORD"),
                AppConfig.get(dotenv, "PG_PASSWORD", "")
        );
        this.password = (rawPassword == null || rawPassword.isBlank()) ? null : rawPassword;
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(connectUrl, user, password);
        applySchema(connection);
        return connection;
    }

    private void applySchema(Connection connection) throws SQLException {
        if (schema == null || schema.isBlank()) {
            return;
        }
        try (Statement st = connection.createStatement()) {
            st.execute("SET search_path TO " + quoteIdent(schema));
        }
    }

    public String getJdbcUrlForLogging() {
        return jdbcUrlForLog + " , schema=" + schema;
    }

    private static String quoteIdent(String ident) {
        return "\"" + ident.replace("\"", "\"\"") + "\"";
    }

    /**
     * PostgreSQL PRO / PgBouncer часто отклоняют startup-параметр {@code search_path}
     * из {@code currentSchema=} в URL. Убираем его из URL и задаём схему через {@code SET}.
     */
    static UrlParts stripCurrentSchemaFromUrl(String jdbcUrl) {
        int queryStart = jdbcUrl.indexOf('?');
        if (queryStart < 0) {
            return new UrlParts(jdbcUrl, null);
        }
        String base = jdbcUrl.substring(0, queryStart);
        String query = jdbcUrl.substring(queryStart + 1);
        String extractedSchema = null;
        List<String> kept = new ArrayList<>();
        for (String part : query.split("&")) {
            if (part.isEmpty()) {
                continue;
            }
            if (part.startsWith("currentSchema=")) {
                extractedSchema = part.substring("currentSchema=".length());
            } else {
                kept.add(part);
            }
        }
        String newUrl = kept.isEmpty() ? base : base + "?" + String.join("&", kept);
        return new UrlParts(newUrl, extractedSchema);
    }

    record UrlParts(String urlWithoutCurrentSchema, String currentSchema) {}
}
