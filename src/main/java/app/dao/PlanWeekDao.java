package app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import app.model.PlanWeek;

public class PlanWeekDao {
    public PlanWeek insertWeek(Connection connection, long planId, int weekNo) throws SQLException {
        String sql = """
                INSERT INTO plan_weeks (plan_id, week_no, created_at)
                VALUES (?, ?, CURRENT_TIMESTAMP)
                RETURNING id, plan_id, week_no
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, planId);
            statement.setInt(2, weekNo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRow(resultSet);
            }
        }
    }

    public int findMaxWeekNo(long planId) throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection()) {
            return findMaxWeekNo(connection, planId);
        }
    }

    public int findMaxWeekNo(Connection connection, long planId) throws SQLException {
        String sql = """
                SELECT COALESCE(MAX(week_no), 0) AS max_week_no
                FROM plan_weeks
                WHERE plan_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, planId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0;
                }
                return resultSet.getInt("max_week_no");
            }
        }
    }

    public PlanWeek findById(long planWeekId) throws SQLException {
        String sql = """
                SELECT id, plan_id, week_no
                FROM plan_weeks
                WHERE id = ?
                """;
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, planWeekId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRow(resultSet);
            }
        }
    }

    public List<PlanWeek> findByPlanId(long planId) throws SQLException {
        String sql = """
                SELECT id, plan_id, week_no
                FROM plan_weeks
                WHERE plan_id = ?
                ORDER BY week_no ASC
                """;
        List<PlanWeek> weeks = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, planId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    weeks.add(mapRow(resultSet));
                }
            }
        }
        return weeks;
    }

    public boolean deleteWeek(Connection connection, long planWeekId) throws SQLException {
        String sql = """
                DELETE FROM plan_weeks
                WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, planWeekId);
            return statement.executeUpdate() > 0;
        }
    }

    public int shiftWeeksDown(Connection connection, long planId, int deletedWeekNo) throws SQLException {
        String sql = """
                UPDATE plan_weeks
                SET week_no = week_no - 1
                WHERE plan_id = ? AND week_no > ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, planId);
            statement.setInt(2, deletedWeekNo);
            return statement.executeUpdate();
        }
    }

    private PlanWeek mapRow(ResultSet resultSet) throws SQLException {
        PlanWeek week = new PlanWeek();
        week.setId(resultSet.getLong("id"));
        week.setPlanId(resultSet.getLong("plan_id"));
        week.setWeekNo(resultSet.getInt("week_no"));
        return week;
    }
}
