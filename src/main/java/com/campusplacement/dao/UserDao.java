package com.campusplacement.dao;

import com.campusplacement.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserDao {
    /** User row including the password hash, used only for authentication. */
    public record Credentials(User user, String passwordHash, boolean active) { }

    public Optional<Credentials> findByUsername(Connection c, String username) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.password_hash, u.role, u.display_name, u.is_active, s.student_id "
                + "FROM users u LEFT JOIN students s ON s.user_id = u.user_id WHERE u.username = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                User u = new User(rs.getInt("user_id"), rs.getString("username"),
                        User.Role.valueOf(rs.getString("role")), rs.getString("display_name"), rs.getString("student_id"));
                return Optional.of(new Credentials(u, rs.getString("password_hash"), rs.getBoolean("is_active")));
            }
        }
    }

    public String passwordHash(Connection c, int userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT password_hash FROM users WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public void touchLogin(Connection c, int userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public void updatePassword(Connection c, int userId, String hash) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE users SET password_hash = ? WHERE user_id = ?")) {
            ps.setString(1, hash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public int insert(Connection c, String username, String hash, User.Role role, String displayName) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, display_name) VALUES (?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Jdbc.bind(ps, username, hash, role.name(), displayName);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to retrieve generated user ID for " + username);
                }
                return keys.getInt(1);
            }
        }
    }

    public void updateIdentity(Connection c, int userId, String username, String displayName) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE users SET username = ?, display_name = ? WHERE user_id = ?")) {
            Jdbc.bind(ps, username, displayName, userId);
            ps.executeUpdate();
        }
    }

    public void delete(Connection c, int userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}
