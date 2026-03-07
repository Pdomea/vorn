package app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import app.model.Exercise;
import app.model.MuscleGroup;

public class ExerciseDao {
    public List<Exercise> findAllExercises(String sortBy, String sortDirection) throws SQLException {
        return findAllExercises(sortBy, sortDirection, null);
    }

    public List<Exercise> findAllExercises(String sortBy, String sortDirection, String statusFilter) throws SQLException {
        String orderDirection = "DESC".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";
        String primarySortColumn = "LOWER(e.name)";
        if ("muscle".equalsIgnoreCase(sortBy)) {
            primarySortColumn = "COALESCE(MIN(LOWER(mg.label)), '')";
        }

        StringBuilder sql = new StringBuilder("""
                SELECT e.id,
                       e.name,
                       e.description,
                       e.status,
                       COALESCE(string_agg(DISTINCT mg.label, ', ' ORDER BY mg.label), '') AS muscle_group_labels
                FROM exercises e
                LEFT JOIN exercise_muscle_groups emg ON emg.exercise_id = e.id
                LEFT JOIN muscle_groups mg ON mg.id = emg.muscle_group_id
                WHERE 1 = 1
                """);

        List<String> params = new ArrayList<>();
        if (statusFilter != null && !statusFilter.isBlank()) {
            sql.append(" AND e.status = ? ");
            params.add(statusFilter);
        }

        sql.append(" GROUP BY e.id, e.name, e.description, e.status ");
        sql.append(" ORDER BY ").append(primarySortColumn).append(" ").append(orderDirection).append(", LOWER(e.name) ASC");

        List<Exercise> exercises = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            for (String param : params) {
                statement.setString(parameterIndex++, param);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exercises.add(mapListRow(resultSet));
                }
            }
        }
        return exercises;
    }

    public List<Exercise> findActiveExercises() throws SQLException {
        String sql = """
                SELECT id, name, description, status
                FROM exercises
                WHERE status = 'ACTIVE'
                ORDER BY name ASC
                """;

        List<Exercise> exercises = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                exercises.add(mapBaseRow(resultSet));
            }
        }
        return exercises;
    }

    public List<Exercise> findActiveExercisesForMapping(Long muscleGroupId, String searchTerm, String sortDirection) throws SQLException {
        String orderDirection = "DESC".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";
        String normalizedSearch = searchTerm == null ? "" : searchTerm.trim().toLowerCase(Locale.ROOT);

        StringBuilder sql = new StringBuilder("""
                SELECT e.id,
                       e.name,
                       e.description,
                       e.status,
                       COALESCE(string_agg(DISTINCT mg.label, ', ' ORDER BY mg.label), '') AS muscle_group_labels
                FROM exercises e
                LEFT JOIN exercise_muscle_groups emg ON emg.exercise_id = e.id
                LEFT JOIN muscle_groups mg ON mg.id = emg.muscle_group_id
                WHERE e.status = 'ACTIVE'
                """);

        List<Object> params = new ArrayList<>();
        if (muscleGroupId != null) {
            sql.append("""
                    AND EXISTS (
                        SELECT 1
                        FROM exercise_muscle_groups emg_filter
                        WHERE emg_filter.exercise_id = e.id
                          AND emg_filter.muscle_group_id = ?
                    )
                    """);
            params.add(muscleGroupId);
        }
        if (!normalizedSearch.isBlank()) {
            sql.append(" AND LOWER(e.name) LIKE ? ");
            params.add("%" + normalizedSearch + "%");
        }

        sql.append(" GROUP BY e.id, e.name, e.description, e.status ");
        sql.append(" ORDER BY LOWER(e.name) ").append(orderDirection);

        List<Exercise> exercises = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            for (Object param : params) {
                if (param instanceof Long longValue) {
                    statement.setLong(parameterIndex++, longValue);
                } else {
                    statement.setString(parameterIndex++, String.valueOf(param));
                }
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exercises.add(mapListRow(resultSet));
                }
            }
        }
        return exercises;
    }

    public boolean existsActiveExerciseById(long exerciseId) throws SQLException {
        String sql = """
                SELECT 1
                FROM exercises
                WHERE id = ?
                  AND status = 'ACTIVE'
                LIMIT 1
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, exerciseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean hasSharedMuscleGroup(long sourceExerciseId, long targetExerciseId) throws SQLException {
        String sql = """
                SELECT 1
                FROM exercise_muscle_groups src
                JOIN exercise_muscle_groups tgt
                  ON tgt.muscle_group_id = src.muscle_group_id
                WHERE src.exercise_id = ?
                  AND tgt.exercise_id = ?
                LIMIT 1
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sourceExerciseId);
            statement.setLong(2, targetExerciseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public List<Exercise> findActiveSwapCandidatesByExerciseId(long sourceExerciseId) throws SQLException {
        String sql = """
                SELECT e.id,
                       e.name,
                       e.description,
                       e.status,
                       COALESCE(string_agg(DISTINCT mg2.label, ', ' ORDER BY mg2.label), '') AS muscle_group_labels
                FROM exercises e
                JOIN exercise_muscle_groups emg_tgt ON emg_tgt.exercise_id = e.id
                JOIN exercise_muscle_groups emg_src ON emg_src.muscle_group_id = emg_tgt.muscle_group_id
                LEFT JOIN exercise_muscle_groups emg2 ON emg2.exercise_id = e.id
                LEFT JOIN muscle_groups mg2 ON mg2.id = emg2.muscle_group_id
                WHERE emg_src.exercise_id = ?
                  AND e.status = 'ACTIVE'
                  AND e.id <> ?
                GROUP BY e.id, e.name, e.description, e.status
                ORDER BY LOWER(e.name) ASC
                """;

        List<Exercise> exercises = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sourceExerciseId);
            statement.setLong(2, sourceExerciseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exercises.add(mapListRow(resultSet));
                }
            }
        }
        return exercises;
    }

    public Exercise findExerciseById(long exerciseId) throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection()) {
            return findExerciseById(connection, exerciseId);
        }
    }

    public Exercise insertExercise(String name, String description, List<Long> muscleGroupIds) throws SQLException {
        String sql = """
                INSERT INTO exercises (name, description, status)
                VALUES (?, ?, 'ACTIVE')
                RETURNING id, name, description, status
                """;

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Exercise inserted;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, name);
                    statement.setString(2, description);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new SQLException("Übung konnte nicht angelegt werden.");
                        }
                        inserted = mapBaseRow(resultSet);
                    }
                }

                replaceExerciseMuscleGroups(connection, inserted.getId(), muscleGroupIds);
                Exercise withGroups = findExerciseById(connection, inserted.getId());
                connection.commit();
                return withGroups;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public Exercise updateExercise(long exerciseId, String name, String description, List<Long> muscleGroupIds) throws SQLException {
        String sql = """
                UPDATE exercises
                SET name = ?, description = ?
                WHERE id = ?
                RETURNING id, name, description, status
                """;

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Exercise updated = null;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, name);
                    statement.setString(2, description);
                    statement.setLong(3, exerciseId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            updated = mapBaseRow(resultSet);
                        }
                    }
                }

                if (updated == null) {
                    connection.rollback();
                    return null;
                }

                replaceExerciseMuscleGroups(connection, exerciseId, muscleGroupIds);
                Exercise withGroups = findExerciseById(connection, exerciseId);
                connection.commit();
                return withGroups;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public boolean updateStatus(long exerciseId, String status) throws SQLException {
        String sql = """
                UPDATE exercises
                SET status = ?
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setLong(2, exerciseId);
            return statement.executeUpdate() > 0;
        }
    }

    public int countUsageReferences(long exerciseId) throws SQLException {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM training_exercises WHERE exercise_id = ?) +
                    (SELECT COUNT(*) FROM session_exercises WHERE exercise_id = ?) AS usage_count
                """;

        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, exerciseId);
            statement.setLong(2, exerciseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("usage_count");
                }
                return 0;
            }
        }
    }

    public boolean deleteExerciseById(long exerciseId) throws SQLException {
        String sql = "DELETE FROM exercises WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, exerciseId);
            return statement.executeUpdate() > 0;
        }
    }

    public List<MuscleGroup> findAllMuscleGroups() throws SQLException {
        String sql = """
                SELECT id, code, label, sort_order
                FROM muscle_groups
                ORDER BY sort_order ASC, label ASC
                """;

        List<MuscleGroup> groups = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                MuscleGroup group = new MuscleGroup();
                group.setId(resultSet.getLong("id"));
                group.setCode(resultSet.getString("code"));
                group.setLabel(resultSet.getString("label"));
                group.setSortOrder(resultSet.getInt("sort_order"));
                groups.add(group);
            }
        }
        return groups;
    }

    public int countMuscleGroupsByIds(List<Long> muscleGroupIds) throws SQLException {
        if (muscleGroupIds == null || muscleGroupIds.isEmpty()) {
            return 0;
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(muscleGroupIds.size(), "?"));
        String sql = "SELECT COUNT(*) AS total FROM muscle_groups WHERE id IN (" + placeholders + ")";

        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            for (Long muscleGroupId : muscleGroupIds) {
                statement.setLong(parameterIndex++, muscleGroupId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
                return 0;
            }
        }
    }

    private Exercise findExerciseById(Connection connection, long exerciseId) throws SQLException {
        String sql = """
                SELECT id, name, description, status
                FROM exercises
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, exerciseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Exercise exercise = mapBaseRow(resultSet);
                exercise.setMuscleGroupIds(findMuscleGroupIdsByExerciseId(connection, exerciseId));
                exercise.setMuscleGroupLabels(findMuscleGroupLabelsByExerciseId(connection, exerciseId));
                return exercise;
            }
        }
    }

    private List<Long> findMuscleGroupIdsByExerciseId(Connection connection, long exerciseId) throws SQLException {
        String sql = """
                SELECT muscle_group_id
                FROM exercise_muscle_groups
                WHERE exercise_id = ?
                ORDER BY muscle_group_id ASC
                """;

        List<Long> ids = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, exerciseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ids.add(resultSet.getLong("muscle_group_id"));
                }
            }
        }
        return ids;
    }

    private List<String> findMuscleGroupLabelsByExerciseId(Connection connection, long exerciseId) throws SQLException {
        String sql = """
                SELECT mg.label
                FROM muscle_groups mg
                JOIN exercise_muscle_groups emg ON emg.muscle_group_id = mg.id
                WHERE emg.exercise_id = ?
                ORDER BY mg.sort_order ASC, mg.label ASC
                """;

        List<String> labels = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, exerciseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    labels.add(resultSet.getString("label"));
                }
            }
        }
        return labels;
    }

    private void replaceExerciseMuscleGroups(Connection connection, long exerciseId, List<Long> muscleGroupIds) throws SQLException {
        String deleteSql = "DELETE FROM exercise_muscle_groups WHERE exercise_id = ?";
        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
            deleteStatement.setLong(1, exerciseId);
            deleteStatement.executeUpdate();
        }

        if (muscleGroupIds == null || muscleGroupIds.isEmpty()) {
            return;
        }

        String insertSql = """
                INSERT INTO exercise_muscle_groups (exercise_id, muscle_group_id)
                VALUES (?, ?)
                """;
        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            for (Long muscleGroupId : muscleGroupIds) {
                insertStatement.setLong(1, exerciseId);
                insertStatement.setLong(2, muscleGroupId);
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
        }
    }

    private Exercise mapBaseRow(ResultSet resultSet) throws SQLException {
        Exercise exercise = new Exercise();
        exercise.setId(resultSet.getLong("id"));
        exercise.setName(resultSet.getString("name"));
        exercise.setDescription(resultSet.getString("description"));
        exercise.setStatus(resultSet.getString("status"));
        return exercise;
    }

    private Exercise mapListRow(ResultSet resultSet) throws SQLException {
        Exercise exercise = mapBaseRow(resultSet);
        String labelsValue = resultSet.getString("muscle_group_labels");
        if (labelsValue == null || labelsValue.isBlank()) {
            exercise.setMuscleGroupLabels(new ArrayList<>());
            return exercise;
        }
        exercise.setMuscleGroupLabels(new ArrayList<>(Arrays.asList(labelsValue.split(", "))));
        return exercise;
    }
}
