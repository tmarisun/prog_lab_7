package org.example.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaInitializer {

    private static final String CREATE_USERS = """
            CREATE TABLE IF NOT EXISTS users (
                id BIGSERIAL PRIMARY KEY,
                login VARCHAR(128) NOT NULL UNIQUE,
                password_hash VARCHAR(128) NOT NULL
            )
            """;

    private static final String CREATE_CITIES = """
            CREATE TABLE IF NOT EXISTS cities (
                id BIGSERIAL PRIMARY KEY,
                stack_order INTEGER NOT NULL UNIQUE,
                name TEXT NOT NULL,
                coordinates_json TEXT NOT NULL,
                creation_date TIMESTAMPTZ NOT NULL,
                area DOUBLE PRECISION NOT NULL,
                population INTEGER NOT NULL,
                meters_above_sea_level INTEGER NOT NULL,
                climate TEXT,
                government TEXT NOT NULL,
                standard_of_living TEXT,
                governor_json TEXT,
                owner_user_id BIGINT NOT NULL REFERENCES users(id)
            )
            """;

    private static final String MIGRATE_CLIMATE_NULLABLE =
            "ALTER TABLE cities ALTER COLUMN climate DROP NOT NULL";

    public void ensureSchema(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute(CREATE_USERS);
            st.execute(CREATE_CITIES);
            st.execute(MIGRATE_CLIMATE_NULLABLE);
        }
    }
}
