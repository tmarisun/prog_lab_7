package org.example.db;

import org.example.auth.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserRepository {

    private final Database database;

    public UserRepository(Database database) {
        this.database = database;
    }

    public Optional<Long> authenticate(String login, String password) throws Exception {
        if (login == null || login.isBlank() || password == null) {
            return Optional.empty();
        }

        String hash = PasswordHasher.sha384Hex(password);
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id FROM users WHERE login = ? AND password_hash = ?")) {
            ps.setString(1, login.trim());
            ps.setString(2, hash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getLong(1));
                }
            }
        }
        return Optional.empty();
    }

    public boolean register(String login, String password) throws Exception {
        if (login == null || login.isBlank() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Login and password are required");
        }
        String hash = PasswordHasher.sha384Hex(password);
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users (login, password_hash) VALUES (?, ?)")) {
            ps.setString(1, login.trim());
            ps.setString(2, hash);
            try {
                return ps.executeUpdate() == 1;
            } catch (SQLException e) {
                if ("23505".equals(e.getSQLState())) {
                    return false;
                }
                throw e;
            }
        }
    }
}
