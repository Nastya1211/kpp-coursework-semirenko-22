package ua.poltava.coursework.dao;

import ua.poltava.coursework.db.Db;
import ua.poltava.coursework.model.Work;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkDao {
    private static final String COLUMNS = "id, order_id, name, price, hours";

    public List<Work> findAll() throws SQLException {
        List<Work> result = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM work ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(map(rs));
        }
        return result;
    }

    public List<Work> findByOrderId(int orderId) throws SQLException {
        List<Work> result = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM work WHERE order_id = ? ORDER BY id")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public Optional<Work> findById(int id) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM work WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Work create(Work w) throws SQLException {
        String sql = "INSERT INTO work (order_id, name, price, hours) VALUES (?, ?, ?, ?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, w.orderId());
            ps.setString(2, w.name());
            ps.setBigDecimal(3, w.price());
            ps.setBigDecimal(4, w.hours());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Work(keys.getInt(1), w.orderId(), w.name(), w.price(), w.hours());
            }
        }
    }

    public boolean update(Work w) throws SQLException {
        String sql = "UPDATE work SET order_id = ?, name = ?, price = ?, hours = ? WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, w.orderId());
            ps.setString(2, w.name());
            ps.setBigDecimal(3, w.price());
            ps.setBigDecimal(4, w.hours());
            ps.setInt(5, w.id());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM work WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Work map(ResultSet rs) throws SQLException {
        return new Work(rs.getInt("id"), rs.getInt("order_id"), rs.getString("name"),
                rs.getBigDecimal("price"), rs.getBigDecimal("hours"));
    }
}
