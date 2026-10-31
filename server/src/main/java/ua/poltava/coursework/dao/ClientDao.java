package ua.poltava.coursework.dao;

import ua.poltava.coursework.db.Db;
import ua.poltava.coursework.model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientDao {

    public List<Client> findAll() throws SQLException {
        String sql = "SELECT id, full_name, phone FROM client ORDER BY id";
        List<Client> result = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        }
        return result;
    }

    public Optional<Client> findById(int id) throws SQLException {
        String sql = "SELECT id, full_name, phone FROM client WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Client create(Client client) throws SQLException {
        String sql = "INSERT INTO client (full_name, phone) VALUES (?, ?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, client.fullName());
            ps.setString(2, client.phone());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Client(keys.getInt(1), client.fullName(), client.phone());
            }
        }
    }

    public boolean update(Client client) throws SQLException {
        String sql = "UPDATE client SET full_name = ?, phone = ? WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, client.fullName());
            ps.setString(2, client.phone());
            ps.setInt(3, client.id());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM client WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Client map(ResultSet rs) throws SQLException {
        return new Client(rs.getInt("id"), rs.getString("full_name"), rs.getString("phone"));
    }
}
