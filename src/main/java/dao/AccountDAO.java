package dao;

import entity.Account;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utility.DBConnection;

public class AccountDAO {
    private static AccountDAO instance;

    private AccountDAO() {}

    public static AccountDAO getInstance() {
        if (instance == null) instance = new AccountDAO();
        return instance;
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        return new Account(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("role"),
            rs.getString("security_question"),
            rs.getString("security_answer"),
            rs.getString("status")
        );
    }

    public Account getByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Account> getAll() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) accounts.add(mapRow(rs));
        }
        return accounts;
    }

    public void add(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (username, password_hash, role, security_question, security_answer, status) VALUES (?, ?, ?, ?, ?, 'active')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, account.getUsername());
            stmt.setString(2, account.getPasswordHash());
            stmt.setString(3, account.getRole());
            stmt.setString(4, account.getSecurityQuestion());
            stmt.setString(5, account.getSecurityAnswer());
            stmt.executeUpdate();
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE accounts SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public void updatePassword(int id, String newPasswordHash) throws SQLException {
        String sql = "UPDATE accounts SET password_hash = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }
}