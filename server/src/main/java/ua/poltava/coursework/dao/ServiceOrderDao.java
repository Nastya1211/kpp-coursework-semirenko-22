package ua.poltava.coursework.dao;

import ua.poltava.coursework.db.Db;
import ua.poltava.coursework.model.OrderStatus;
import ua.poltava.coursework.model.ServiceOrder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceOrderDao {
    private static final String COLUMNS =
            "id, car_id, description, status, safety_level, mobility_level, created_at";

    public List<ServiceOrder> findAll() throws SQLException {
        List<ServiceOrder> result = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM service_order ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(map(rs));
        }
        return result;
    }

    public Optional<ServiceOrder> findById(int id) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM service_order WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public ServiceOrder create(ServiceOrder o) throws SQLException {
        String sql = "INSERT INTO service_order (car_id, description, status, safety_level, mobility_level, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, o.carId());
            ps.setString(2, o.description());
            ps.setString(3, o.status().name());
            ps.setInt(4, o.safetyLevel());
            ps.setInt(5, o.mobilityLevel());
            ps.setDate(6, Date.valueOf(o.createdAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new ServiceOrder(keys.getInt(1), o.carId(), o.description(), o.status(),
                        o.safetyLevel(), o.mobilityLevel(), o.createdAt());
            }
        }
    }

    public boolean update(ServiceOrder o) throws SQLException {
        String sql = "UPDATE service_order SET car_id = ?, description = ?, status = ?, "
                + "safety_level = ?, mobility_level = ?, created_at = ? WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, o.carId());
            ps.setString(2, o.description());
            ps.setString(3, o.status().name());
            ps.setInt(4, o.safetyLevel());
            ps.setInt(5, o.mobilityLevel());
            ps.setDate(6, Date.valueOf(o.createdAt()));
            ps.setInt(7, o.id());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, OrderStatus status) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE service_order SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM service_order WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private ServiceOrder map(ResultSet rs) throws SQLException {
        return new ServiceOrder(rs.getInt("id"), rs.getInt("car_id"), rs.getString("description"),
                OrderStatus.valueOf(rs.getString("status")), rs.getInt("safety_level"),
                rs.getInt("mobility_level"), rs.getDate("created_at").toLocalDate());
    }
}
