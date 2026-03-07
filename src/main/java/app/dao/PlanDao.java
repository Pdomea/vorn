package app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import app.model.Plan;

public class PlanDao {
    public Plan insertPlan(Connection connection, String name, String description, String heroImagePath) throws SQLException {
        String sql = """
                INSERT INTO plans (name, description, hero_image_path, status, created_at, updated_at)
                VALUES (?, ?, ?, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                RETURNING id, name, description, hero_image_path, status
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setString(3, heroImagePath);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRow(resultSet);
            }
        }
    }

    public Plan findById(long planId) throws SQLException {
        String sql = """
                SELECT id, name, description, hero_image_path, status
                FROM plans
                WHERE id = ?
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, planId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRow(resultSet);
            }
        }
    }

    public List<Plan> findAllPlans() throws SQLException {
        return findAllPlans(null);
    }

    public List<Plan> findAllPlans(String statusFilter) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT id, name, description, hero_image_path, status
                FROM plans
                """);

        List<String> params = new ArrayList<>();
        if (statusFilter != null && !statusFilter.isBlank()) {
            sql.append(" WHERE status = ? ");
            params.add(statusFilter);
        }
        sql.append(" ORDER BY id ASC ");

        List<Plan> plans = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            for (String param : params) {
                statement.setString(parameterIndex++, param);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    plans.add(mapRow(resultSet));
                }
            }
        }
        return plans;
    }

    public boolean updateStatus(long planId, String status) throws SQLException {
        String sql = """
                UPDATE plans
                SET status = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setLong(2, planId);
            return statement.executeUpdate() > 0;
        }
    }

    public List<Plan> findActivePlans() throws SQLException {
        String sql = """
                SELECT id, name, description, hero_image_path, status
                FROM plans
                WHERE status = 'ACTIVE'
                ORDER BY id ASC
                """;
        List<Plan> plans = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                plans.add(mapRow(resultSet));
            }
        }
        return plans;
    }

    public boolean updateHeroImagePath(long planId, String heroImagePath) throws SQLException {
        String sql = """
                UPDATE plans
                SET hero_image_path = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, heroImagePath);
            statement.setLong(2, planId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updatePlanMeta(long planId, String name, String description) throws SQLException {
        String sql = """
                UPDATE plans
                SET name = ?, description = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setLong(3, planId);
            return statement.executeUpdate() > 0;
        }
    }

    public int countWorkoutSessionReferences(long planId) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS total
                FROM workout_sessions ws
                LEFT JOIN plan_weeks pw ON pw.id = ws.plan_week_id
                WHERE ws.plan_id = ?
                   OR pw.plan_id = ?
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, planId);
            statement.setLong(2, planId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0;
                }
                return resultSet.getInt("total");
            }
        }
    }

    public int clearActivePlanForUsers(Connection connection, long planId) throws SQLException {
        String sql = """
                UPDATE users
                SET active_plan_id = NULL
                WHERE active_plan_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, planId);
            return statement.executeUpdate();
        }
    }

    public boolean deletePlanById(Connection connection, long planId) throws SQLException {
        String sql = "DELETE FROM plans WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, planId);
            return statement.executeUpdate() > 0;
        }
    }

    private Plan mapRow(ResultSet resultSet) throws SQLException {
        Plan plan = new Plan();
        plan.setId(resultSet.getLong("id"));
        plan.setName(resultSet.getString("name"));
        plan.setDescription(resultSet.getString("description"));
        plan.setHeroImagePath(resultSet.getString("hero_image_path"));
        plan.setStatus(resultSet.getString("status"));
        return plan;
    }
}
