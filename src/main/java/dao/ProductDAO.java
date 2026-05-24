package dao;

import entity.Product;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import utility.DBConnection;

public class ProductDAO {
    private static ProductDAO instance;

    private ProductDAO() {}

    public static ProductDAO getInstance() {
        if (instance == null) instance = new ProductDAO();
        return instance;
    }

    // maps a result set row to a Product, including nullable expiration_date
    private Product mapRow(ResultSet rs) throws SQLException {
        Date expDate       = rs.getDate("expiration_date");
        LocalDate expLocal = expDate != null ? expDate.toLocalDate() : null;
        return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("unit"),
            rs.getBigDecimal("price_per_unit"),
            rs.getBigDecimal("stock_quantity"),
            rs.getString("intended_for"),
            rs.getString("category"),
            rs.getInt("sales"),
            rs.getString("status"),
            expLocal
        );
    }

    public List<Product> getAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) products.add(mapRow(rs));
        }
        return products;
    }

    public Product getById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public void add(Product product) throws SQLException {
        String sql = "INSERT INTO products (name, unit, price_per_unit, stock_quantity, intended_for, category, sales, status, expiration_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getUnit());
            stmt.setBigDecimal(3, product.getPricePerUnit());
            stmt.setBigDecimal(4, product.getStockQuantity());
            stmt.setString(5, product.getIntendedFor());
            stmt.setString(6, product.getCategory());
            stmt.setInt(7, 0);
            stmt.setString(8, "available");
            // set null if no expiration date provided
            if (product.getExpirationDate() != null) {
                stmt.setDate(9, Date.valueOf(product.getExpirationDate()));
            } else {
                stmt.setNull(9, Types.DATE);
            }
            stmt.executeUpdate();
        }
    }

    public void update(Product product) throws SQLException {
        String sql = "UPDATE products SET name=?, unit=?, price_per_unit=?, stock_quantity=?, intended_for=?, category=?, status=?, expiration_date=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getUnit());
            stmt.setBigDecimal(3, product.getPricePerUnit());
            stmt.setBigDecimal(4, product.getStockQuantity());
            stmt.setString(5, product.getIntendedFor());
            stmt.setString(6, product.getCategory());
            stmt.setString(7, product.getStatus());
            if (product.getExpirationDate() != null) {
                stmt.setDate(8, Date.valueOf(product.getExpirationDate()));
            } else {
                stmt.setNull(8, Types.DATE);
            }
            stmt.setInt(9, product.getId());
            stmt.executeUpdate();
        }
    }

    public void decrementStock(int productId, BigDecimal quantity, Connection conn) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, quantity);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }

    public void incrementSales(int productId, Connection conn) throws SQLException {
        String sql = "UPDATE products SET sales = sales + 1 WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        }
    }

    public void updateStatus(int productId, String status) throws SQLException {
        String sql = "UPDATE products SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }

    public void updateStock(int productId, BigDecimal newStock) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, newStock);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }

    // updates both stock and optionally the expiration date in one query
    public void restock(int productId, BigDecimal addAmount, LocalDate newExpirationDate) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ?, expiration_date = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, addAmount);
            if (newExpirationDate != null) {
                stmt.setDate(2, Date.valueOf(newExpirationDate));
            } else {
                stmt.setNull(2, Types.DATE);
            }
            stmt.setInt(3, productId);
            stmt.executeUpdate();
        }
    }

    public void incrementStock(int productId, BigDecimal quantity, Connection conn) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, quantity);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }

    public void decrementSales(int productId, Connection conn) throws SQLException {
        String sql = "UPDATE products SET sales = GREATEST(sales - 1, 0) WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        }
    }
}