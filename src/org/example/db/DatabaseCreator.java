package org.example.db;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

/**
 * Пытается создать БД приложения, подключившись к {@code postgres}.
 */
public final class DatabaseCreator {

    private DatabaseCreator() {
    }

    public static void tryCreateApplicationDatabase(Dotenv dotenv) {
        if (!Boolean.parseBoolean(AppConfig.get(dotenv, "AUTO_CREATE_DATABASE", "false"))) {
            return;
        }
        String host = AppConfig.get(dotenv, "PG_HOST", "pg");
        String port = AppConfig.get(dotenv, "PG_PORT", "5432");
        String database = AppConfig.get(dotenv, "PG_DATABASE", "studs");
        if (!isSafeIdent(database)) {
            System.err.println("AUTO_CREATE_DATABASE: unsafe database name, skip: " + database);
            return;
        }

        String user = AppConfig.firstNonBlank(
                System.getenv("PG_USER"),
                System.getenv("USER"),
                AppConfig.get(dotenv, "PG_USER", "")
        );
        if (user == null || user.isBlank()) {
            System.err.println("AUTO_CREATE_DATABASE: PG_USER / USER not set, skip");
            return;
        }

        String adminUrl = "jdbc:postgresql://" + host + ":" + port + "/postgres";
        Properties props = new Properties();
        props.setProperty("user", user);
        try (Connection c = DriverManager.getConnection(adminUrl, props);
             Statement st = c.createStatement()) {

            st.executeUpdate("CREATE DATABASE " + quoteIdent(database));
            System.out.println("Created database: " + database);
        } catch (Exception e) {
            System.err.println("AUTO_CREATE_DATABASE: " + e.getMessage());
            System.err.println("Create DB manually on helios: createdb -h " + host + " " + database);
        }
    }

    private static boolean isSafeIdent(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return true;
    }

    private static String quoteIdent(String ident) {
        return "\"" + ident.replace("\"", "\"\"") + "\"";
    }
}
