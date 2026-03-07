package app.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import app.model.WorkoutLog;

public class WorkoutLogDao {
    public WorkoutLog upsertLog(long sessionId, long sessionExerciseId, int setNo, int reps, BigDecimal weight, String note)
            throws SQLException {
        String sql = """
                INSERT INTO workout_logs (
                    session_id,
                    session_exercise_id,
                    set_no,
                    reps,
                    weight,
                    note,
                    logged_at
                )
                SELECT se.session_id,
                       se.id,
                       ?,
                       ?,
                       ?,
                       ?,
                       CURRENT_TIMESTAMP
                FROM session_exercises se
                JOIN workout_sessions ws ON ws.id = se.session_id
                WHERE se.id = ?
                  AND se.session_id = ?
                  AND ws.status = 'ACTIVE'
                ON CONFLICT (session_id, session_exercise_id, set_no)
                DO UPDATE SET
                    reps = EXCLUDED.reps,
                    weight = EXCLUDED.weight,
                    note = EXCLUDED.note,
                    logged_at = CURRENT_TIMESTAMP
                RETURNING id, session_id, session_exercise_id, set_no, reps, weight, note, logged_at
                """;

        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, setNo);
            statement.setInt(2, reps);
            statement.setBigDecimal(3, weight);
            statement.setString(4, note);
            statement.setLong(5, sessionExerciseId);
            statement.setLong(6, sessionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRow(resultSet);
            }
        }
    }

    public WorkoutLog insertLog(Connection connection, long sessionId, long sessionExerciseId, int setNo, int reps, BigDecimal weight,
            String note) throws SQLException {
        String sql = """
                INSERT INTO workout_logs (
                    session_id,
                    session_exercise_id,
                    set_no,
                    reps,
                    weight,
                    note,
                    logged_at
                )
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                RETURNING id, session_id, session_exercise_id, set_no, reps, weight, note, logged_at
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sessionId);
            statement.setLong(2, sessionExerciseId);
            statement.setInt(3, setNo);
            statement.setInt(4, reps);
            statement.setBigDecimal(5, weight);
            statement.setString(6, note);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRow(resultSet);
            }
        }
    }

    public int deleteBySessionId(Connection connection, long sessionId) throws SQLException {
        String sql = "DELETE FROM workout_logs WHERE session_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sessionId);
            return statement.executeUpdate();
        }
    }

    public int deleteByUserId(Connection connection, long userId) throws SQLException {
        String sql = """
                DELETE FROM workout_logs wl
                USING workout_sessions ws
                WHERE wl.session_id = ws.id
                  AND ws.user_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            return statement.executeUpdate();
        }
    }

    public List<WorkoutLog> findBySessionId(long sessionId) throws SQLException {
        String sql = """
                SELECT id,
                       session_id,
                       session_exercise_id,
                       set_no,
                       reps,
                       weight,
                       note,
                       logged_at
                FROM workout_logs
                WHERE session_id = ?
                ORDER BY session_exercise_id ASC, set_no ASC
                """;

        List<WorkoutLog> logs = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sessionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(mapRow(resultSet));
                }
            }
        }
        return logs;
    }

    public boolean existsForSessionExercise(long sessionId, long sessionExerciseId) throws SQLException {
        String sql = """
                SELECT 1
                FROM workout_logs
                WHERE session_id = ?
                  AND session_exercise_id = ?
                LIMIT 1
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sessionId);
            statement.setLong(2, sessionExerciseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public Map<Long, SessionLogSummary> findSessionSummariesBySessionIds(List<Long> sessionIds) throws SQLException {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Map.of();
        }

        StringBuilder sql = new StringBuilder("""
                SELECT session_id,
                       COUNT(*) AS logged_sets,
                       COALESCE(SUM(reps), 0) AS total_reps,
                       COALESCE(SUM(reps * weight), 0) AS total_volume
                FROM workout_logs
                WHERE session_id IN (
                """);
        for (int i = 0; i < sessionIds.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(") GROUP BY session_id");

        Map<Long, SessionLogSummary> summaries = new LinkedHashMap<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (Long sessionId : sessionIds) {
                statement.setLong(paramIndex++, sessionId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    long sessionId = resultSet.getLong("session_id");
                    int loggedSets = resultSet.getInt("logged_sets");
                    int totalReps = resultSet.getInt("total_reps");
                    BigDecimal totalVolume = resultSet.getBigDecimal("total_volume");
                    summaries.put(sessionId, new SessionLogSummary(loggedSets, totalReps, totalVolume));
                }
            }
        }
        return summaries;
    }

    public BigDecimal sumFinishedVolumeByUserId(long userId) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(wl.reps * wl.weight), 0) AS total_volume
                FROM workout_logs wl
                JOIN workout_sessions ws ON ws.id = wl.session_id
                WHERE ws.user_id = ?
                  AND ws.status = 'FINISHED'
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return BigDecimal.ZERO;
                }
                BigDecimal volume = resultSet.getBigDecimal("total_volume");
                return volume == null ? BigDecimal.ZERO : volume;
            }
        }
    }

    public Map<Long, BigDecimal> findLatestFinishedExerciseScoresByUserAndTraining(
            long userId,
            long trainingId,
            long excludedSessionId) throws SQLException {
        String sql = """
                SELECT DISTINCT ON (exercise_scores.exercise_id)
                       exercise_scores.exercise_id,
                       exercise_scores.score
                FROM (
                    SELECT se.exercise_id,
                           ws.id AS session_id,
                           ws.ended_at,
                           COALESCE(SUM(wl.reps * wl.weight), 0) AS score
                    FROM workout_sessions ws
                    JOIN session_exercises se ON se.session_id = ws.id
                    LEFT JOIN workout_logs wl
                           ON wl.session_id = ws.id
                          AND wl.session_exercise_id = se.id
                    WHERE ws.user_id = ?
                      AND ws.training_id = ?
                      AND ws.status = 'FINISHED'
                      AND ws.id <> ?
                    GROUP BY se.exercise_id, ws.id, ws.ended_at
                ) AS exercise_scores
                ORDER BY exercise_scores.exercise_id,
                         exercise_scores.ended_at DESC NULLS LAST,
                         exercise_scores.session_id DESC
                """;

        Map<Long, BigDecimal> latestScores = new LinkedHashMap<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, trainingId);
            statement.setLong(3, excludedSessionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    long exerciseId = resultSet.getLong("exercise_id");
                    BigDecimal score = resultSet.getBigDecimal("score");
                    latestScores.put(exerciseId, score == null ? BigDecimal.ZERO : score);
                }
            }
        }
        return latestScores;
    }

    private WorkoutLog mapRow(ResultSet resultSet) throws SQLException {
        WorkoutLog log = new WorkoutLog();
        log.setId(resultSet.getLong("id"));
        log.setSessionId(resultSet.getLong("session_id"));
        log.setSessionExerciseId(resultSet.getLong("session_exercise_id"));
        log.setSetNo(resultSet.getInt("set_no"));
        log.setReps(resultSet.getInt("reps"));
        log.setWeight(resultSet.getBigDecimal("weight"));
        log.setNote(resultSet.getString("note"));

        Timestamp loggedAt = resultSet.getTimestamp("logged_at");
        if (loggedAt != null) {
            log.setLoggedAt(loggedAt.toLocalDateTime());
        }
        return log;
    }

    public static final class SessionLogSummary {
        private final int loggedSets;
        private final int totalReps;
        private final BigDecimal totalVolume;

        public SessionLogSummary(int loggedSets, int totalReps, BigDecimal totalVolume) {
            this.loggedSets = loggedSets;
            this.totalReps = totalReps;
            this.totalVolume = totalVolume;
        }

        public int getLoggedSets() {
            return loggedSets;
        }

        public int getTotalReps() {
            return totalReps;
        }

        public BigDecimal getTotalVolume() {
            return totalVolume;
        }
    }
}
