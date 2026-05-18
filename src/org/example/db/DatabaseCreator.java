package org.example.db;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Пытается создать БД приложения, подключившись к {@code postgres}.
 */
public final class DatabaseCreator {

    private DatabaseCreator() {}

    public static void tryCreateApplicationDatabase(Dotenv dotenv) {
        if (!isAutoCreateEnabled(dotenv)) {
            return;
        }

        String host = AppConfig.get(dotenv, "PG_HOST", "pg");
        String port = AppConfig.get(dotenv, "PG_PORT", "5432");
        String database = AppConfig.get(dotenv, "PG_DATABASE", "studs");
        if (!isSafeIdent(database)) {
            System.err.println("AUTO_CREATE_DATABASE: unsafe database name, skip: " + database);
            return;
        }

        String user = AppConfig.firstNonBlank(System.getenv("PG_USER"), System.getenv("USER"),
                AppConfig.get(dotenv, "PG_USER", ""));

        if (user == null || user.isBlank()) {
            System.err.println("AUTO_CREATE_DATABASE: PG_USER / USER not set, skip");
            return;
        }
        String rawPassword = AppConfig.firstNonBlank(System.getenv("PG_PASSWORD"),
                System.getenv("PGPASSWORD"),
                AppConfig.get(dotenv, "PG_PASSWORD", ""));

        String password = (rawPassword == null || rawPassword.isBlank()) ? null : rawPassword;

        String adminUrl = "jdbc:postgresql://" + host + ":" + port + "/postgres";

        try (Connection c = DriverManager.getConnection(adminUrl, user, password); Statement st = c.createStatement()) {
                st.executeUpdate("CREATE DATABASE " + quoteIdent(database));
                System.out.println("Created database: " + database);
        } catch (Exception e) {
            System.err.println("AUTO_CREATE_DATABASE: " + e.getMessage());
            System.err.println("Create DB manually on helios: createdb -h " + host + " " + database);
        }
    }

    private static boolean isSafeIdent(String name) {
        return name != null;
    }

    private static boolean isAutoCreateEnabled(Dotenv dotenv) {
        return Boolean.parseBoolean(AppConfig.get(dotenv, "AUTO_CREATE_DATABASE", "false"));
    }

    private static String quoteIdent(String ident) {
        return "\"" + ident.replace("\"", "\"\"") + "\"";
    }
}
