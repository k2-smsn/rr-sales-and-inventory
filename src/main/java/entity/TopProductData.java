/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

/**
 *
 * @author k2
 */
import java.math.BigDecimal;

public class TopProductData {
    private final String productName;
    private final String category;
    private final BigDecimal totalQuantitySold;
    private final BigDecimal totalRevenue;

    public TopProductData(String productName, String category, BigDecimal totalQuantitySold, BigDecimal totalRevenue) {
        this.productName = productName;
        this.category = category;
        this.totalQuantitySold = totalQuantitySold;
        this.totalRevenue = totalRevenue;
    }

    public String getProductName() { return productName; }
    public String getCategory() { return category; }
    public BigDecimal getTotalQuantitySold() { return totalQuantitySold; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
}
