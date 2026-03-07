package app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import app.model.User;
import app.model.UserRole;

public class UserDao {
    public void createTableIfNotExists() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id BIGSERIAL PRIMARY KEY,
                    email VARCHAR(255) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    display_name VARCHAR(120) NOT NULL,
                    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
                    active_plan_id BIGINT,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    public User findByEmail(String email) throws SQLException {
        String sql = """
                SELECT id, email, password_hash, display_name, role, created_at
                     , active_plan_id
                FROM users
                WHERE lower(email) = lower(?)
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRow(resultSet);
            }
        }
    }

    public boolean existsAdmin() throws SQLException {
        String sql = "SELECT 1 FROM users WHERE role = 'ADMIN' LIMIT 1";
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next();
        }
    }

    public User insert(User user) throws SQLException {
        String sql = """
                INSERT INTO users (email, password_hash, display_name, role)
                VALUES (?, ?, ?, ?)
                RETURNING id, created_at
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getDisplayName());
            statement.setString(4, user.getRole().name());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    user.setId(resultSet.getLong("id"));
                    Timestamp createdAt = resultSet.getTimestamp("created_at");
                    if (createdAt != null) {
                        user.setCreatedAt(createdAt.toLocalDateTime());
                    }
                }
            }
        }
        return user;
    }

    public void updateRole(long userId, UserRole role) throws SQLException {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role.name());
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }

    public void updateActivePlanId(long userId, Long activePlanId) throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection()) {
            updateActivePlanId(connection, userId, activePlanId);
        }
    }

    public void updateActivePlanId(Connection connection, long userId, Long activePlanId) throws SQLException {
        String sql = "UPDATE users SET active_plan_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (activePlanId == null) {
                statement.setNull(1, Types.BIGINT);
            } else {
                statement.setLong(1, activePlanId);
            }
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        long activePlanIdRaw = resultSet.getLong("active_plan_id");
        Long activePlanId = resultSet.wasNull() ? null : activePlanIdRaw;
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                resultSet.getString("display_name"),
                UserRole.valueOf(resultSet.getString("role")),
                activePlanId,
                createdAt != null ? createdAt.toLocalDateTime() : null);
    }
}
