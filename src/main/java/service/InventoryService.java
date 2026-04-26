/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

/**
 *
 * @author k2
 */
import dao.ProductDAO;
import entity.Product;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryService {
    private static InventoryService instance;
    private final ProductDAO productDAO = ProductDAO.getInstance();

    private InventoryService() {}

    public static InventoryService getInstance() {
        if (instance == null) instance = new InventoryService();
        return instance;
    }

    public List<Product> searchProducts(String query) throws SQLException {
        String lower = query.toLowerCase().trim();
        return productDAO.getAll().stream()
            .filter(p -> !p.getStatus().equalsIgnoreCase("unavailable"))
            .filter(p -> p.getName().toLowerCase().startsWith(lower))
            .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
            .collect(Collectors.toList());
    }

    public List<Product> getAllAvailable() throws SQLException {
        return productDAO.getAll().stream()
            .filter(p -> !p.getStatus().equalsIgnoreCase("unavailable"))
            .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
            .collect(Collectors.toList());
    }

    public List<Product> getAllProducts(String query) throws SQLException {
        String lower = query.toLowerCase().trim();
        return productDAO.getAll().stream()
            .filter(p -> p.getName().toLowerCase().startsWith(lower))
            .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
            .collect(Collectors.toList());
    }

    public void adjustStock(Product product, BigDecimal amount, String operation) throws SQLException, IllegalArgumentException {
        BigDecimal current = product.getStockQuantity();
        BigDecimal newStock;

        if (operation.equals("add")) {
            newStock = current.add(amount);
        } else {
            newStock = current.subtract(amount);
            if (newStock.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                    "Cannot subtract " + amount + " from current stock of " + current + ". Stock cannot go negative."
                );
            }
        }

        productDAO.updateStock(product.getId(), newStock);
    }

    public void toggleAvailability(Product product) throws SQLException {
        String newStatus = product.getStatus().equalsIgnoreCase("available") ? "unavailable" : "available";
        productDAO.updateStatus(product.getId(), newStatus);
    }

    public void addProduct(Product product) throws SQLException {
        productDAO.add(product);
    }
}