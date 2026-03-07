package app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import app.model.WorkoutSession;

public class WorkoutSessionDao {
    public boolean existsActiveByUserId(long userId) throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection()) {
            return existsActiveByUserId(connection, userId);
        }
    }

    public boolean existsActiveByUserId(Connection connection, long userId) throws SQLException {
        String sql = """
                SELECT 1
                FROM workout_sessions
                WHERE user_id = ? AND status = 'ACTIVE'
                LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public WorkoutSession insertActiveSession(Connection connection, long userId, long trainingId) throws SQLException {
        return insertActiveSession(connection, userId, trainingId, null, null);
    }

    public WorkoutSession insertActiveSession(Connection connection, long userId, long trainingId, Long planId, Long planWeekId)
            throws SQLException {
        String sql = """
                INSERT INTO workout_sessions (user_id, training_id, plan_id, plan_week_id, started_at, status)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, 'ACTIVE')
                RETURNING id, user_id, training_id, plan_id, plan_week_id, started_at, ended_at, status
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, trainingId);
            if (planId == null) {
                statement.setNull(3, Types.BIGINT);
            } else {
                statement.setLong(3, planId);
            }
            if (planWeekId == null) {
                statement.setNull(4, Types.BIGINT);
            } else {
                statement.setLong(4, planWeekId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRow(resultSet);
            }
        }
    }

    public WorkoutSession findByIdForUser(long sessionId, long userId) throws SQLException {
        String sql = """
                SELECT ws.id,
                       ws.user_id,
                       ws.training_id,
                       ws.plan_id,
                       ws.plan_week_id,
                       ws.started_at,
                       ws.ended_at,
                       ws.status,
                       t.title AS training_title
                FROM workout_sessions ws
                JOIN trainings t ON t.id = ws.training_id
                WHERE ws.id = ? AND ws.user_id = ?
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sessionId);
            statement.setLong(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                WorkoutSession session = mapRow(resultSet);
                session.setTrainingTitle(resultSet.getString("training_title"));
                return session;
            }
        }
    }

    public WorkoutSession findActiveByUserId(long userId) throws SQLException {
        String sql = """
                SELECT ws.id,
                       ws.user_id,
                       ws.training_id,
                       ws.plan_id,
                       ws.plan_week_id,
                       ws.started_at,
                       ws.ended_at,
                       ws.status,
                       t.title AS training_title
                FROM workout_sessions ws
                JOIN trainings t ON t.id = ws.training_id
                WHERE ws.user_id = ?
                  AND ws.status = 'ACTIVE'
                ORDER BY ws.started_at DESC
                LIMIT 1
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                WorkoutSession session = mapRow(resultSet);
                session.setTrainingTitle(resultSet.getString("training_title"));
                return session;
            }
        }
    }

    public boolean finishSessionForUser(long sessionId, long userId) throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection()) {
            return finishSessionForUser(connection, sessionId, userId);
        }
    }

    public boolean finishSessionForUser(Connection connection, long sessionId, long userId) throws SQLException {
        String sql = """
                UPDATE workout_sessions
                SET status = 'FINISHED',
                    ended_at = CURRENT_TIMESTAMP
                WHERE id = ?
                  AND user_id = ?
                  AND status = 'ACTIVE'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sessionId);
            statement.setLong(2, userId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteActiveSessionForUser(long sessionId, long userId) throws SQLException {
        String sql = """
                DELETE FROM workout_sessions
                WHERE id = ?
                  AND user_id = ?
                  AND status = 'ACTIVE'
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sessionId);
            statement.setLong(2, userId);
            return statement.executeUpdate() > 0;
        }
    }

    public int deleteByUserId(Connection connection, long userId) throws SQLException {
        String sql = """
                DELETE FROM workout_sessions
                WHERE user_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            return statement.executeUpdate();
        }
    }

    public List<WorkoutSession> findFinishedByUserId(long userId) throws SQLException {
        String sql = """
                SELECT ws.id,
                       ws.user_id,
                       ws.training_id,
                       ws.plan_id,
                       ws.plan_week_id,
                       ws.started_at,
                       ws.ended_at,
                       ws.status,
                       t.title AS training_title
                FROM workout_sessions ws
                JOIN trainings t ON t.id = ws.training_id
                WHERE ws.user_id = ?
                  AND ws.status = 'FINISHED'
                ORDER BY ws.ended_at DESC, ws.id DESC
                """;

        List<WorkoutSession> sessions = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    WorkoutSession session = mapRow(resultSet);
                    session.setTrainingTitle(resultSet.getString("training_title"));
                    sessions.add(session);
                }
            }
        }
        return sessions;
    }

    public List<WorkoutSession> findLatestFinishedByTrainingForPlan(long userId, long planId) throws SQLException {
        String sql = """
                SELECT DISTINCT ON (ws.plan_week_id, ws.training_id)
                       ws.id,
                       ws.user_id,
                       ws.training_id,
                       ws.plan_id,
                       ws.plan_week_id,
                       ws.started_at,
                       ws.ended_at,
                       ws.status
                FROM workout_sessions ws
                WHERE ws.user_id = ?
                  AND ws.plan_id = ?
                  AND ws.plan_week_id IS NOT NULL
                  AND ws.status = 'FINISHED'
                ORDER BY ws.plan_week_id, ws.training_id, ws.ended_at DESC NULLS LAST, ws.id DESC
                """;
        List<WorkoutSession> sessions = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, planId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    sessions.add(mapRow(resultSet));
                }
            }
        }
        return sessions;
    }

    public int countFinishedByUserId(long userId) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS total
                FROM workout_sessions
                WHERE user_id = ?
                  AND status = 'FINISHED'
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0;
                }
                return resultSet.getInt("total");
            }
        }
    }

    public long sumFinishedDurationSecondsByUserId(long userId) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(EXTRACT(EPOCH FROM (ended_at - started_at))), 0) AS total_seconds
                FROM workout_sessions
                WHERE user_id = ?
                  AND status = 'FINISHED'
                  AND ended_at IS NOT NULL
                  AND started_at IS NOT NULL
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0L;
                }
                return resultSet.getLong("total_seconds");
            }
        }
    }

    public int countDistinctFinishedPlanSlotsByUserAndPlan(long userId, long planId) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS total
                FROM (
                    SELECT DISTINCT ws.plan_week_id, ws.training_id
                    FROM workout_sessions ws
                    WHERE ws.user_id = ?
                      AND ws.plan_id = ?
                      AND ws.status = 'FINISHED'
                      AND ws.plan_week_id IS NOT NULL
                ) AS completed_slots
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, planId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0;
                }
                return resultSet.getInt("total");
            }
        }
    }

    private WorkoutSession mapRow(ResultSet resultSet) throws SQLException {
        WorkoutSession session = new WorkoutSession();
        session.setId(resultSet.getLong("id"));
        session.setUserId(resultSet.getLong("user_id"));
        session.setTrainingId(resultSet.getLong("training_id"));
        long planId = resultSet.getLong("plan_id");
        if (!resultSet.wasNull()) {
            session.setPlanId(planId);
        }
        long planWeekId = resultSet.getLong("plan_week_id");
        if (!resultSet.wasNull()) {
            session.setPlanWeekId(planWeekId);
        }

        Timestamp startedAt = resultSet.getTimestamp("started_at");
        if (startedAt != null) {
            session.setStartedAt(startedAt.toLocalDateTime());
        }

        Timestamp endedAt = resultSet.getTimestamp("ended_at");
        if (endedAt != null) {
            session.setEndedAt(endedAt.toLocalDateTime());
        }

        session.setStatus(resultSet.getString("status"));
        return session;
    }
}
