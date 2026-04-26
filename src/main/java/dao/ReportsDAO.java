/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author k2
 */
import entity.SalesSummaryData;
import entity.SegmentData;
import entity.TopProductData;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import utility.DBConnection;

public class ReportsDAO {
    private static ReportsDAO instance;

    private ReportsDAO() {}

    public static ReportsDAO getInstance() {
        if (instance == null) instance = new ReportsDAO();
        return instance;
    }

    public SalesSummaryData getSalesSummary(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
            SELECT
                COUNT(DISTINCT t.id) AS total_transactions,
                COALESCE(SUM(i.subtotal), 0) AS total_revenue
            FROM transactions t
            LEFT JOIN items_sold i ON i.transaction_id = t.id
            WHERE t.created_at BETWEEN ? AND ?
            AND t.status = 'active'
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int totalTransactions = rs.getInt("total_transactions");
                    BigDecimal totalRevenue = rs.getBigDecimal("total_revenue");
                    BigDecimal average = totalTransactions > 0
                        ? totalRevenue.divide(BigDecimal.valueOf(totalTransactions), 2, java.math.RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                    return new SalesSummaryData(totalTransactions, totalRevenue, average);
                }
            }
        }
        return new SalesSummaryData(0, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public List<TopProductData> getTopProducts(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
            SELECT
                p.name AS product_name,
                p.category,
                SUM(i.quantity) AS total_quantity,
                SUM(i.subtotal) AS total_revenue
            FROM items_sold i
            JOIN products p ON p.id = i.product_id
            JOIN transactions t ON t.id = i.transaction_id
            WHERE t.created_at BETWEEN ? AND ?
            AND t.status = 'active'
            GROUP BY p.id, p.name, p.category
            ORDER BY total_quantity DESC
        """;
        List<TopProductData> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new TopProductData(
                        rs.getString("product_name"),
                        rs.getString("category"),
                        rs.getBigDecimal("total_quantity"),
                        rs.getBigDecimal("total_revenue")
                    ));
                }
            }
        }
        return results;
    }

    public List<SegmentData> getSegmentPerformance(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
            SELECT
                p.category,
                p.intended_for,
                SUM(i.quantity) AS total_quantity,
                SUM(i.subtotal) AS total_revenue
            FROM items_sold i
            JOIN products p ON p.id = i.product_id
            JOIN transactions t ON t.id = i.transaction_id
            WHERE t.created_at BETWEEN ? AND ?
            AND t.status = 'active'
            GROUP BY p.category, p.intended_for
            ORDER BY p.category, total_revenue DESC
        """;
        List<SegmentData> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new SegmentData(
                        rs.getString("category"),
                        rs.getString("intended_for"),
                        rs.getBigDecimal("total_quantity"),
                        rs.getBigDecimal("total_revenue")
                    ));
                }
            }
        }
        return results;
    }
    
    public BigDecimal getGrossIncome(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(i.subtotal), 0) AS total
            FROM items_sold i
            JOIN transactions t ON t.id = i.transaction_id
            WHERE t.created_at BETWEEN ? AND ?
            AND t.status = 'active'
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("total");
            }
        }
        return BigDecimal.ZERO;
    }
}