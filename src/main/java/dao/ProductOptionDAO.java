package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utility.DBConnection;

public class ProductOptionDAO {

    private static ProductOptionDAO instance;

    private ProductOptionDAO() {}

    public static ProductOptionDAO getInstance() {
        if (instance == null) instance = new ProductOptionDAO();
        return instance;
    }

    public List<String> getByType(String type) throws SQLException {
        List<String> options = new ArrayList<>();
        String sql = "SELECT value FROM product_options WHERE type = ? ORDER BY value ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) options.add(rs.getString("value"));
        }
        return options;
    }

    public void add(String type, String value) throws SQLException {
        String sql = "INSERT INTO product_options (type, value) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type);
            stmt.setString(2, value);
            stmt.executeUpdate();
        }
    }

    public void delete(String type, String value) throws SQLException {
        String sql = "DELETE FROM product_options WHERE type = ? AND value = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type);
            stmt.setString(2, value);
            stmt.executeUpdate();
        }
    }
}