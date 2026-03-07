package app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import app.model.SessionExercise;

public class SessionExerciseDao {
    public int insertSnapshotFromTraining(Connection connection, long sessionId, long trainingId) throws SQLException {
        String sql = """
                INSERT INTO session_exercises (
                    session_id,
                    exercise_id,
                    exercise_name_snapshot,
                    planned_sets_snapshot,
                    planned_reps_snapshot,
                    sort_order
                )
                SELECT ?,
                       te.exercise_id,
                       e.name,
                       te.planned_sets,
                       te.planned_reps,
                       te.sort_order
                FROM training_exercises te
                JOIN exercises e ON e.id = te.exercise_id
                WHERE te.training_id = ?
                ORDER BY te.sort_order ASC
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sessionId);
            statement.setLong(2, trainingId);
            return statement.executeUpdate();
        }
    }

    public List<SessionExercise> findBySessionId(long sessionId) throws SQLException {
        String sql = """
                SELECT id,
                       session_id,
                       exercise_id,
                       exercise_name_snapshot,
                       planned_sets_snapshot,
                       planned_reps_snapshot,
                       sort_order
                FROM session_exercises
                WHERE session_id = ?
                ORDER BY sort_order ASC
                """;

        List<SessionExercise> items = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sessionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapRow(resultSet));
                }
            }
        }
        return items;
    }

    public boolean existsInSession(long sessionExerciseId, long sessionId) throws SQLException {
        String sql = """
                SELECT 1
                FROM session_exercises
                WHERE id = ? AND session_id = ?
                LIMIT 1
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sessionExerciseId);
            statement.setLong(2, sessionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public SessionExercise findByIdInSession(long sessionExerciseId, long sessionId) throws SQLException {
        String sql = """
                SELECT id,
                       session_id,
                       exercise_id,
                       exercise_name_snapshot,
                       planned_sets_snapshot,
                       planned_reps_snapshot,
                       sort_order
                FROM session_exercises
                WHERE id = ?
                  AND session_id = ?
                LIMIT 1
                """;

        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sessionExerciseId);
            statement.setLong(2, sessionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                return null;
            }
        }
    }

    public boolean swapExerciseSnapshot(long sessionId, long sessionExerciseId, long replacementExerciseId) throws SQLException {
        String sql = """
                UPDATE session_exercises se
                SET exercise_id = e.id,
                    exercise_name_snapshot = e.name
                FROM exercises e, workout_sessions ws
                WHERE se.id = ?
                  AND se.session_id = ?
                  AND ws.id = se.session_id
                  AND e.id = ?
                  AND e.status = 'ACTIVE'
                  AND ws.status = 'ACTIVE'
                """;

        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sessionExerciseId);
            statement.setLong(2, sessionId);
            statement.setLong(3, replacementExerciseId);
            return statement.executeUpdate() > 0;
        }
    }

    public int deleteByUserId(Connection connection, long userId) throws SQLException {
        String sql = """
                DELETE FROM session_exercises se
                USING workout_sessions ws
                WHERE se.session_id = ws.id
                  AND ws.user_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            return statement.executeUpdate();
        }
    }

    private SessionExercise mapRow(ResultSet resultSet) throws SQLException {
        SessionExercise item = new SessionExercise();
        item.setId(resultSet.getLong("id"));
        item.setSessionId(resultSet.getLong("session_id"));
        item.setExerciseId(resultSet.getLong("exercise_id"));
        item.setExerciseNameSnapshot(resultSet.getString("exercise_name_snapshot"));
        item.setPlannedSetsSnapshot(resultSet.getInt("planned_sets_snapshot"));
        item.setPlannedRepsSnapshot(resultSet.getInt("planned_reps_snapshot"));
        item.setSortOrder(resultSet.getInt("sort_order"));
        return item;
    }
}
