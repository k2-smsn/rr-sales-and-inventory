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
import entity.CartItem;
import entity.ItemSold;
import entity.Receipt;
import entity.Transaction;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import utility.DBConnection;

public class SalesService {
    private static SalesService instance;
    private final ProductDAO productDAO = ProductDAO.getInstance();
    private final TransactionDAO transactionDAO = TransactionDAO.getInstance();
    private final ItemSoldDAO itemSoldDAO = ItemSoldDAO.getInstance();

    private SalesService() {}

    public static SalesService getInstance() {
        if (instance == null) instance = new SalesService();
        return instance;
    }

    public Receipt processTransaction(List<CartItem> cart) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Transaction transaction = new Transaction(0, LocalDate.now(), "active");
                int transactionId = transactionDAO.create(transaction, conn);
                transaction.setId(transactionId);

                List<ItemSold> itemsSold = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;

                for (CartItem item : cart) {
                    BigDecimal subTotal = item.getSubTotal();
                    total = total.add(subTotal);

                    ItemSold itemSold = new ItemSold(
                        0,
                        transactionId,
                        item.getProduct().getId(),
                        item.getQuantity(),
                        item.getProduct().getPricePerUnit(),
                        subTotal
                    );
                    itemSoldDAO.add(itemSold, conn);
                    productDAO.decrementStock(item.getProduct().getId(), item.getQuantity(), conn);
                    productDAO.incrementSales(item.getProduct().getId(), conn);
                    itemsSold.add(itemSold);
                }

                conn.commit();
                return new Receipt(transaction, itemsSold, total);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}