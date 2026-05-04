package org.example.db;

import java.sql.Connection;
import java.sql.Statement;

public final class SchemaInitializer {

    public void ensureSchema(Connection connection) throws Exception {
        try (Statement st = connection.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id BIGSERIAL PRIMARY KEY,
                        login VARCHAR(128) NOT NULL UNIQUE,
                        password_hash VARCHAR(128) NOT NULL
                    )
                    """);
            st.execute("""
                    CREATE TABLE IF NOT EXISTS cities (
                        id BIGSERIAL PRIMARY KEY,
                        stack_order INTEGER NOT NULL UNIQUE,
                        name TEXT NOT NULL,
                        coordinates_json TEXT NOT NULL,
                        creation_date TIMESTAMPTZ NOT NULL,
                        area DOUBLE PRECISION NOT NULL,
                        population INTEGER NOT NULL,
                        meters_above_sea_level INTEGER NOT NULL,
                        climate TEXT NOT NULL,
                        government TEXT NOT NULL,
                        standard_of_living TEXT,
                        governor_json TEXT,
                        owner_user_id BIGINT NOT NULL REFERENCES users(id)
                    )
                    """);
        }
    }
}
