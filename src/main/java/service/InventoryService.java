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
            .filter(p -> p.getName().toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }
}