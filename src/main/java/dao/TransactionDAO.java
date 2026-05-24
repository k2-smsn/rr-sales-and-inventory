package dao;

import entity.Transaction;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import utility.DBConnection;

public class TransactionDAO {
    private static TransactionDAO instance;

    private TransactionDAO() {}

    public static TransactionDAO getInstance() {
        if (instance == null) instance = new TransactionDAO();
        return instance;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
            rs.getInt("id"),
            rs.getDate("created_at").toLocalDate(),
            rs.getString("status"),
            rs.getInt("processed_by")
        );
    }

    public List<Transaction> getByDateRange(LocalDate from, LocalDate to) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) transactions.add(mapRow(rs));
            }
        }
        return transactions;
    }

    public int create(Transaction transaction, Connection conn) throws SQLException {
        String sql = "INSERT INTO transactions (created_at, status, processed_by) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(transaction.getCreatedAt()));
            stmt.setString(2, transaction.getStatus());
            stmt.setInt(3, transaction.getProcessedBy());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        throw new SQLException("Failed to create transaction, no ID returned.");
    }

    public void updateStatus(int transactionId, String status) throws SQLException {
        String sql = "UPDATE transactions SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, transactionId);
            stmt.executeUpdate();
        }
    }

    public void updateStatus(int transactionId, Connection conn) throws SQLException {
        String sql = "UPDATE transactions SET status = 'void' WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            stmt.executeUpdate();
        }
    }
}