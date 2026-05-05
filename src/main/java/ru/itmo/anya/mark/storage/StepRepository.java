package ru.itmo.anya.mark.storage;

import ru.itmo.anya.mark.model.DilutionStep;
import ru.itmo.anya.mark.model.FinalQuantityUnit;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class StepRepository {

    public long save(DilutionStep step) {
        if (getById(step.getId()) != null) {
            return update(step);
        } else {
            return insert(step);
        }
    }

    private long insert(DilutionStep step) {
        String sql = "INSERT INTO dilution_steps (series_id, step_number, factor, final_quantity, final_unit, created_at) " +
                "VALUES (?, ?, ?, ?, ?::final_quantity_unit, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, step.getSeriesId());
            stmt.setInt(2, step.getStepNumber());
            stmt.setDouble(3, step.getFactor());
            stmt.setDouble(4, step.getFinalQuantity());
            stmt.setString(5, step.getFinalUnit().name()); // "ML", "L", etc.
            stmt.setTimestamp(6, Timestamp.from(step.getCreatedAt()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка сохранения шага: " + e.getMessage());
        }
        return -1;
    }

    private long update(DilutionStep step) {
        String sql = "UPDATE dilution_steps SET step_number = ?, factor = ?, final_quantity = ?, final_unit = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, step.getStepNumber());
            stmt.setDouble(2, step.getFactor());
            stmt.setDouble(3, step.getFinalQuantity());
            stmt.setString(4, step.getFinalUnit().name());
            stmt.setLong(5, step.getId());

            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return -1; }
    }

    public DilutionStep getById(long id) {
        String sql = "SELECT * FROM dilution_steps WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRowToStep(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<DilutionStep> findBySeriesId(long seriesId) {
        List<DilutionStep> list = new ArrayList<>();
        String sql = "SELECT * FROM dilution_steps WHERE series_id = ? ORDER BY step_number";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, seriesId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRowToStep(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<DilutionStep> findAll() {
        List<DilutionStep> list = new ArrayList<>();
        String sql = "SELECT * FROM dilution_steps ORDER BY id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRowToStep(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void delete(long id) {
        String sql = "DELETE FROM dilution_steps WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private DilutionStep mapRowToStep(ResultSet rs) throws SQLException {
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        Instant createdAt = (createdAtTs != null) ? createdAtTs.toInstant() : Instant.now();

        return new DilutionStep(
                rs.getLong("id"),
                rs.getLong("series_id"),
                rs.getInt("step_number"),
                rs.getDouble("factor"),
                rs.getDouble("final_quantity"),
                FinalQuantityUnit.valueOf(rs.getString("final_unit")),
                createdAt
        );
    }
}