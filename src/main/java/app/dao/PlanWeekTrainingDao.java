package app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import app.model.PlanWeekTraining;

public class PlanWeekTrainingDao {
    public PlanWeekTraining insertMapping(long planWeekId, long trainingId, int sortOrder) throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection()) {
            return insertMapping(connection, planWeekId, trainingId, sortOrder);
        }
    }

    public PlanWeekTraining insertMapping(Connection connection, long planWeekId, long trainingId, int sortOrder) throws SQLException {
        String sql = """
                INSERT INTO plan_week_trainings (plan_week_id, training_id, sort_order, created_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                RETURNING id, plan_week_id, training_id, sort_order
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, planWeekId);
            statement.setLong(2, trainingId);
            statement.setInt(3, sortOrder);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                PlanWeekTraining mapping = new PlanWeekTraining();
                mapping.setId(resultSet.getLong("id"));
                mapping.setPlanWeekId(resultSet.getLong("plan_week_id"));
                mapping.setTrainingId(resultSet.getLong("training_id"));
                mapping.setSortOrder(resultSet.getInt("sort_order"));
                return mapping;
            }
        }
    }

    public List<PlanWeekTraining> findByWeekId(long planWeekId) throws SQLException {
        return findByWeekId(planWeekId, null);
    }

    public List<PlanWeekTraining> findByWeekIdAndTrainingStatus(long planWeekId, String trainingStatus) throws SQLException {
        return findByWeekId(planWeekId, trainingStatus);
    }

    private List<PlanWeekTraining> findByWeekId(long planWeekId, String trainingStatus) throws SQLException {
        String sql = """
                SELECT pwt.id,
                       pwt.plan_week_id,
                       pwt.training_id,
                       pwt.sort_order,
                       t.title AS training_title,
                       t.status AS training_status
                FROM plan_week_trainings pwt
                JOIN trainings t ON t.id = pwt.training_id
                WHERE pwt.plan_week_id = ?
                  AND (? IS NULL OR t.status = ?)
                ORDER BY pwt.sort_order ASC
                """;
        List<PlanWeekTraining> mappings = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, planWeekId);
            statement.setString(2, trainingStatus);
            statement.setString(3, trainingStatus);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    mappings.add(mapRow(resultSet));
                }
            }
        }
        return mappings;
    }

    public PlanWeekTraining findById(long mappingId) throws SQLException {
        String sql = """
                SELECT id,
                       plan_week_id,
                       training_id,
                       sort_order
                FROM plan_week_trainings
                WHERE id = ?
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, mappingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                PlanWeekTraining mapping = new PlanWeekTraining();
                mapping.setId(resultSet.getLong("id"));
                mapping.setPlanWeekId(resultSet.getLong("plan_week_id"));
                mapping.setTrainingId(resultSet.getLong("training_id"));
                mapping.setSortOrder(resultSet.getInt("sort_order"));
                return mapping;
            }
        }
    }

    public boolean deleteMapping(long mappingId) throws SQLException {
        String sql = """
                DELETE FROM plan_week_trainings
                WHERE id = ?
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, mappingId);
            return statement.executeUpdate() > 0;
        }
    }

    public int countPublishedSlotsByPlanId(long planId) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS total
                FROM plan_week_trainings pwt
                JOIN plan_weeks pw ON pw.id = pwt.plan_week_id
                JOIN trainings t ON t.id = pwt.training_id
                WHERE pw.plan_id = ?
                  AND t.status = 'PUBLISHED'
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, planId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0;
                }
                return resultSet.getInt("total");
            }
        }
    }

    private PlanWeekTraining mapRow(ResultSet resultSet) throws SQLException {
        PlanWeekTraining mapping = new PlanWeekTraining();
        mapping.setId(resultSet.getLong("id"));
        mapping.setPlanWeekId(resultSet.getLong("plan_week_id"));
        mapping.setTrainingId(resultSet.getLong("training_id"));
        mapping.setSortOrder(resultSet.getInt("sort_order"));
        mapping.setTrainingTitle(resultSet.getString("training_title"));
        mapping.setTrainingStatus(resultSet.getString("training_status"));
        return mapping;
    }
}
