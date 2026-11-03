package ua.poltava.coursework.dao;

import ua.poltava.coursework.db.Db;
import ua.poltava.coursework.model.Car;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarDao {
    private static final String COLUMNS = "id, vin, brand, model, year, client_id";

    public List<Car> findAll() throws SQLException {
        List<Car> result = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM car ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(map(rs));
        }
        return result;
    }

    public Optional<Car> findById(int id) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM car WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Car create(Car car) throws SQLException {
        String sql = "INSERT INTO car (vin, brand, model, year, client_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, car.vin());
            ps.setString(2, car.brand());
            ps.setString(3, car.model());
            ps.setInt(4, car.year());
            ps.setInt(5, car.clientId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Car(keys.getInt(1), car.vin(), car.brand(), car.model(), car.year(), car.clientId());
            }
        }
    }

    public boolean update(Car car) throws SQLException {
        String sql = "UPDATE car SET vin = ?, brand = ?, model = ?, year = ?, client_id = ? WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, car.vin());
            ps.setString(2, car.brand());
            ps.setString(3, car.model());
            ps.setInt(4, car.year());
            ps.setInt(5, car.clientId());
            ps.setInt(6, car.id());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM car WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Car map(ResultSet rs) throws SQLException {
        return new Car(rs.getInt("id"), rs.getString("vin"), rs.getString("brand"),
                rs.getString("model"), rs.getInt("year"), rs.getInt("client_id"));
    }
}
