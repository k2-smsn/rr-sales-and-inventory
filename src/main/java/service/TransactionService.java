/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

/**
 *
 * @author k2
 */
import dao.ItemSoldDAO;
import dao.ProductDAO;
import dao.TransactionDAO;
import entity.ItemSold;
import entity.Product;
import entity.Transaction;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TransactionService {
    private static TransactionService instance;
    private final TransactionDAO transactionDAO = TransactionDAO.getInstance();
    private final ItemSoldDAO itemSoldDAO = ItemSoldDAO.getInstance();
    private final ProductDAO productDAO = ProductDAO.getInstance();

    private TransactionService() {}

    public static TransactionService getInstance() {
        if (instance == null) instance = new TransactionService();
        return instance;
    }

    public List<Transaction> getTransactionsByDateRange(LocalDate from, LocalDate to) throws SQLException {
        return transactionDAO.getByDateRange(from, to);
    }

    public List<ItemSold> getItemsByTransactionId(int transactionId) throws SQLException {
        return itemSoldDAO.getByTransactionId(transactionId);
    }

    public Product getProductById(int productId) throws SQLException {
        return productDAO.getById(productId);
    }

    public void voidTransaction(int transactionId) throws SQLException {
        transactionDAO.updateStatus(transactionId, "void");
    }
}