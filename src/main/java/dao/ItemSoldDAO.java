/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author k2
 */
import entity.ItemSold;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utility.DBConnection;

public class ItemSoldDAO {
    private static ItemSoldDAO instance;

    private ItemSoldDAO() {}

    public static ItemSoldDAO getInstance() {
        if (instance == null) instance = new ItemSoldDAO();
        return instance;
    }

    private ItemSold mapRow(ResultSet rs) throws SQLException {
        return new ItemSold(
            rs.getInt("id"),
            rs.getInt("transaction_id"),
            rs.getInt("product_id"),
            rs.getBigDecimal("quantity"),
            rs.getBigDecimal("price_per_unit"),
            rs.getBigDecimal("sub_total")
        );
    }

    public void add(ItemSold item, Connection conn) throws SQLException {
        String sql = "INSERT INTO items_sold (transaction_id, product_id, quantity, price_per_unit, sub_total) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item.getTransactionId());
            stmt.setInt(2, item.getProductId());
            stmt.setBigDecimal(3, item.getQuantity());
            stmt.setBigDecimal(4, item.getPricePerUnit());
            stmt.setBigDecimal(5, item.getSubTotal());
            stmt.executeUpdate();
        }
    }

    public List<ItemSold> getByTransactionId(int transactionId) throws SQLException {
        List<ItemSold> items = new ArrayList<>();
        String sql = "SELECT * FROM items_sold WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) items.add(mapRow(rs));
            }
        }
        return items;
    }
}