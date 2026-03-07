package app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import app.model.Training;

public class TrainingDao {
    public List<Training> findPublishedTrainings() throws SQLException {
        String sql = """
                SELECT id, title, description, status
                FROM trainings
                WHERE status = 'PUBLISHED'
                ORDER BY title ASC
                """;

        List<Training> trainings = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                trainings.add(mapRow(resultSet));
            }
        }
        return trainings;
    }

    public Training findPublishedTrainingById(long trainingId) throws SQLException {
        String sql = """
                SELECT id, title, description, status
                FROM trainings
                WHERE id = ? AND status = 'PUBLISHED'
                """;

        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, trainingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                return null;
            }
        }
    }

    public List<Training> findAllTrainings() throws SQLException {
        return findAllTrainings("id", "asc", null);
    }

    public List<Training> findAllTrainings(String statusFilter) throws SQLException {
        return findAllTrainings("id", "asc", statusFilter);
    }

    public List<Training> findAllTrainings(String sortBy, String sortDirection, String statusFilter) throws SQLException {
        String orderDirection = "DESC".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";
        String primarySortColumn = "id";
        if ("title".equalsIgnoreCase(sortBy)) {
            primarySortColumn = "LOWER(title)";
        }

        StringBuilder sql = new StringBuilder("""
                SELECT id, title, description, status
                FROM trainings
                """);

        List<String> params = new ArrayList<>();
        if (statusFilter != null && !statusFilter.isBlank()) {
            sql.append(" WHERE status = ? ");
            params.add(statusFilter);
        }
        sql.append(" ORDER BY ").append(primarySortColumn).append(" ").append(orderDirection);
        if ("LOWER(title)".equals(primarySortColumn)) {
            sql.append(", id ASC");
        }

        List<Training> trainings = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            for (String param : params) {
                statement.setString(parameterIndex++, param);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    trainings.add(mapRow(resultSet));
                }
            }
        }
        return trainings;
    }

    public Training findTrainingById(long trainingId) throws SQLException {
        String sql = """
                SELECT id, title, description, status
                FROM trainings
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, trainingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                return null;
            }
        }
    }

    public Training insertTraining(String title, String description) throws SQLException {
        String sql = """
                INSERT INTO trainings (title, description, status, created_at, updated_at)
                VALUES (?, ?, 'DRAFT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                RETURNING id, title, description, status
                """;

        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setString(2, description);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                return null;
            }
        }
    }

    public Training updateTraining(long trainingId, String title, String description) throws SQLException {
        String sql = """
                UPDATE trainings
                SET title = ?, description = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                RETURNING id, title, description, status
                """;

        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setString(2, description);
            statement.setLong(3, trainingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                return null;
            }
        }
    }

    public boolean updateStatus(long trainingId, String status) throws SQLException {
        String sql = """
                UPDATE trainings
                SET status = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setLong(2, trainingId);
            return statement.executeUpdate() > 0;
        }
    }

    public int countUsageReferences(long trainingId) throws SQLException {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM training_exercises WHERE training_id = ?) +
                    (SELECT COUNT(*) FROM workout_sessions WHERE training_id = ?) +
                    (SELECT COUNT(*) FROM plan_week_trainings WHERE training_id = ?) AS usage_count
                """;

        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, trainingId);
            statement.setLong(2, trainingId);
            statement.setLong(3, trainingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("usage_count");
                }
                return 0;
            }
        }
    }

    public boolean deleteTrainingById(long trainingId) throws SQLException {
        String sql = "DELETE FROM trainings WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, trainingId);
            return statement.executeUpdate() > 0;
        }
    }

    private Training mapRow(ResultSet resultSet) throws SQLException {
        Training training = new Training();
        training.setId(resultSet.getLong("id"));
        training.setTitle(resultSet.getString("title"));
        training.setDescription(resultSet.getString("description"));
        training.setStatus(resultSet.getString("status"));
        return training;
    }
}
