package ru.itmo.anya.mark.storage;

import ru.itmo.anya.mark.model.DilutionSeries;
import ru.itmo.anya.mark.model.DilutionSourceType;
import java.sql.*;
import java.util.*;

public class SeriesRepository {

    // Сохраняет серию. Если ID=0 (новая), вставляет и возвращает новый ID.
    // Если ID>0, обновляет существующую.
    public long save(DilutionSeries series) {
        if (getById(series.getId()) != null) {
            return update(series);
        } else {
            return insert(series);
        }
    }

    private long insert(DilutionSeries series) {
        // INSERT с возвратом нового ID. owner_id берется подзапросом по логину.
        String sql = "INSERT INTO dilution_series (name, source_type, source_id, owner_id, created_at, updated_at) " +
                "VALUES (?, ?, ?, (SELECT id FROM users WHERE login = ?), ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, series.getName());
            stmt.setString(2, series.getSourceType().name());
            stmt.setLong(3, series.getSourceId());
            stmt.setString(4, series.getOwnerUsername()); // Подставляем логин
            stmt.setTimestamp(5, Timestamp.from(series.getCreatedAt()));
            stmt.setTimestamp(6, Timestamp.from(series.getUpdatedAt()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка создания серии: " + e.getMessage());
        }
        return -1;
    }

    private long update(DilutionSeries series) {
        String sql = "UPDATE dilution_series SET name = ?, source_type = ?, source_id = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, series.getName());
            stmt.setString(2, series.getSourceType().name());
            stmt.setLong(3, series.getSourceId());
            stmt.setTimestamp(4, Timestamp.from(series.getUpdatedAt()));
            stmt.setLong(5, series.getId());

            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка обновления серии: " + e.getMessage());
            return -1;
        }
    }

    public DilutionSeries getById(long id) {
        // JOIN с users, чтобы получить owner_username (login)
        String sql = "SELECT s.*, u.login as owner_username FROM dilution_series s " +
                "LEFT JOIN users u ON s.owner_id = u.id WHERE s.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRowToSeries(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<DilutionSeries> findAll() {
        List<DilutionSeries> list = new ArrayList<>();
        String sql = "SELECT s.*, u.login as owner_username FROM dilution_series s " +
                "LEFT JOIN users u ON s.owner_id = u.id ORDER BY s.id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) list.add(mapRowToSeries(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void delete(long id) {
        String sql = "DELETE FROM dilution_series WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private DilutionSeries mapRowToSeries(ResultSet rs) throws SQLException {
        return new DilutionSeries(
                rs.getLong("id"),
                rs.getString("name"),
                DilutionSourceType.valueOf(rs.getString("source_type")),
                rs.getLong("source_id"),
                rs.getString("owner_username"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : rs.getTimestamp("created_at").toInstant()
        );
    }
}