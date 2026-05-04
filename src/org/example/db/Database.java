package org.example.db;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Database {

    private final String jdbcUrl;
    private final String user;

    public Database(Dotenv dotenv) {
        String fullUrl = AppConfig.get(dotenv, "PG_JDBC_URL", null);
        if (fullUrl != null && !fullUrl.isBlank()) {
            this.jdbcUrl = fullUrl;
        } else {
            String host = AppConfig.get(dotenv, "PG_HOST", "pg");
            String port = AppConfig.get(dotenv, "PG_PORT", "5432");
            String database = AppConfig.get(dotenv, "PG_DATABASE", "studs");
            this.jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        }

        this.user = AppConfig.firstNonBlank(
                System.getenv("PG_USER"),
                System.getenv("USER"),
                AppConfig.get(dotenv, "PG_USER", "")
        );
        if (this.user == null || this.user.isBlank()) {
            throw new IllegalStateException(
                    "Задайте PG_USER или экспортируйте USER.");
        }
    }

    public Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", user);
        return DriverManager.getConnection(jdbcUrl, props);
    }

    public String getJdbcUrlForLogging() {
        return jdbcUrl;
    }

    public String getUser() {
        return user;
    }
}
