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

public class Product {
    private int id;
    private String name;
    private String unit;
    private BigDecimal pricePerUnit;
    private BigDecimal stockQuantity;
    private String intendedFor;
    private String category;
    private int sales;
    private String status;

    public Product() {}

    public Product(int id, String name, String unit, BigDecimal pricePerUnit,
                   BigDecimal stockQuantity, String intendedFor, String category,
                   int sales, String status) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.pricePerUnit = pricePerUnit;
        this.stockQuantity = stockQuantity;
        this.intendedFor = intendedFor;
        this.category = category;
        this.sales = sales;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public BigDecimal getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(BigDecimal stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getIntendedFor() { return intendedFor; }
    public void setIntendedFor(String intendedFor) { this.intendedFor = intendedFor; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getSales() { return sales; }
    public void setSales(int sales) { this.sales = sales; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isOutOfStock() {
        return stockQuantity.compareTo(BigDecimal.ZERO) <= 0;
    }

    public boolean isPieceUnit() {
        return "piece".equalsIgnoreCase(unit);
    }
}