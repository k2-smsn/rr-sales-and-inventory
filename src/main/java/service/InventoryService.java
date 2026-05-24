package service;

import dao.ProductDAO;
import entity.Product;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
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

    // used by NewTransactionPanel — excludes unavailable products
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

    // used by InventoryPanel — includes all products
    public List<Product> getAllProducts(String query) throws SQLException {
        String lower = query.toLowerCase().trim();
        return productDAO.getAll().stream()
            .filter(p -> p.getName().toLowerCase().startsWith(lower))
            .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
            .collect(Collectors.toList());
    }

    // replaces adjustStock — add only, with optional expiration date update
    public void restockProduct(Product product, BigDecimal amount, LocalDate newExpirationDate) throws SQLException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Restock amount must be greater than zero.");
        if (product.isPieceUnit() && amount.stripTrailingZeros().scale() > 0)
            throw new IllegalArgumentException("Piece unit cannot have decimal amounts.");
        productDAO.restock(product.getId(), amount, newExpirationDate);
    }

    public void toggleAvailability(Product product) throws SQLException {
        String newStatus = product.getStatus().equalsIgnoreCase("available") ? "unavailable" : "available";
        productDAO.updateStatus(product.getId(), newStatus);
    }

    public void addProduct(Product product) throws SQLException {
        productDAO.add(product);
    }

    // returns products with stock at or below 7 (excludes unavailable)
    public List<Product> getStockAlerts() throws SQLException {
        return productDAO.getAll().stream()
            .filter(p -> !p.getStatus().equalsIgnoreCase("unavailable"))
            .filter(p -> p.getStockQuantity().compareTo(BigDecimal.valueOf(7)) <= 0)
            .sorted((a, b) -> a.getStockQuantity().compareTo(b.getStockQuantity()))
            .collect(Collectors.toList());
    }

    // returns products that are expired or expiring within 14 days (excludes unavailable)
    public List<Product> getExpirationAlerts() throws SQLException {
        LocalDate today = LocalDate.now();
        return productDAO.getAll().stream()
            .filter(p -> !p.getStatus().equalsIgnoreCase("unavailable"))
            .filter(p -> p.getExpirationDate() != null)
            .filter(p -> p.isExpired() || p.isExpiringSoon())
            .sorted((a, b) -> a.getExpirationDate().compareTo(b.getExpirationDate()))
            .collect(Collectors.toList());
    }
}