package service;

import dao.ProductOptionDAO;
import java.sql.SQLException;
import java.util.List;

public class ProductOptionService {

    private static ProductOptionService instance;
    private final ProductOptionDAO dao = ProductOptionDAO.getInstance();

    private ProductOptionService() {}

    public static ProductOptionService getInstance() {
        if (instance == null) instance = new ProductOptionService();
        return instance;
    }

    public List<String> getIntendedForOptions() throws SQLException {
        return dao.getByType("intended_for");
    }

    public List<String> getCategoryOptions() throws SQLException {
        return dao.getByType("category");
    }
    
    // exposes raw type-based lookup for use in dialogs
    public List<String> getByType(String type) throws SQLException {
        return dao.getByType(type);
    }

    public void addOption(String type, String value) throws SQLException {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException("Value cannot be empty.");
        dao.add(type, value.trim().toLowerCase());
    }

    public void deleteOption(String type, String value) throws SQLException {
        dao.delete(type, value);
    }
}