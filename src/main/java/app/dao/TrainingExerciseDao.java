package app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import app.model.TrainingExercise;

public class TrainingExerciseDao {
    public List<TrainingExercise> findByTrainingId(long trainingId) throws SQLException {
        String sql = """
                SELECT te.id,
                       te.training_id,
                       te.exercise_id,
                       te.planned_sets,
                       te.planned_reps,
                       te.sort_order,
                       e.name AS exercise_name,
                       e.description AS exercise_description
                FROM training_exercises te
                JOIN exercises e ON e.id = te.exercise_id
                WHERE te.training_id = ?
                ORDER BY te.sort_order ASC
                """;

        List<TrainingExercise> items = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, trainingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapRow(resultSet));
                }
            }
        }
        return items;
    }

    public TrainingExercise insertMapping(long trainingId, long exerciseId, int plannedSets, int plannedReps, int sortOrder)
            throws SQLException {
        String sql = """
                INSERT INTO training_exercises (training_id, exercise_id, planned_sets, planned_reps, sort_order)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id, training_id, exercise_id, planned_sets, planned_reps, sort_order
                """;

        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, trainingId);
            statement.setLong(2, exerciseId);
            statement.setInt(3, plannedSets);
            statement.setInt(4, plannedReps);
            statement.setInt(5, sortOrder);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    TrainingExercise created = new TrainingExercise();
                    created.setId(resultSet.getLong("id"));
                    created.setTrainingId(resultSet.getLong("training_id"));
                    created.setExerciseId(resultSet.getLong("exercise_id"));
                    created.setPlannedSets(resultSet.getInt("planned_sets"));
                    created.setPlannedReps(resultSet.getInt("planned_reps"));
                    created.setSortOrder(resultSet.getInt("sort_order"));
                    return created;
                }
                return null;
            }
        }
    }

    public boolean updateMapping(long mappingId, long trainingId, int plannedSets, int plannedReps, int sortOrder)
            throws SQLException {
        String sql = """
                UPDATE training_exercises
                SET planned_sets = ?, planned_reps = ?, sort_order = ?
                WHERE id = ? AND training_id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, plannedSets);
            statement.setInt(2, plannedReps);
            statement.setInt(3, sortOrder);
            statement.setLong(4, mappingId);
            statement.setLong(5, trainingId);
            return statement.executeUpdate() > 0;
        }
    }

    private TrainingExercise mapRow(ResultSet resultSet) throws SQLException {
        TrainingExercise item = new TrainingExercise();
        item.setId(resultSet.getLong("id"));
        item.setTrainingId(resultSet.getLong("training_id"));
        item.setExerciseId(resultSet.getLong("exercise_id"));
        item.setPlannedSets(resultSet.getInt("planned_sets"));
        item.setPlannedReps(resultSet.getInt("planned_reps"));
        item.setSortOrder(resultSet.getInt("sort_order"));
        item.setExerciseName(resultSet.getString("exercise_name"));
        item.setExerciseDescription(resultSet.getString("exercise_description"));
        return item;
    }
}
