package entity;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    private LocalDate expirationDate; // null means no expiration

    public Product() {}

    public Product(int id, String name, String unit, BigDecimal pricePerUnit,
                   BigDecimal stockQuantity, String intendedFor, String category,
                   int sales, String status) {
        this(id, name, unit, pricePerUnit, stockQuantity, intendedFor, category, sales, status, null);
    }

    public Product(int id, String name, String unit, BigDecimal pricePerUnit,
                   BigDecimal stockQuantity, String intendedFor, String category,
                   int sales, String status, LocalDate expirationDate) {
        this.id             = id;
        this.name           = name;
        this.unit           = unit;
        this.pricePerUnit   = pricePerUnit;
        this.stockQuantity  = stockQuantity;
        this.intendedFor    = intendedFor;
        this.category       = category;
        this.sales          = sales;
        this.status         = status;
        this.expirationDate = expirationDate;
    }

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }
    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }
    public String getUnit()                     { return unit; }
    public void setUnit(String unit)            { this.unit = unit; }
    public BigDecimal getPricePerUnit()         { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal p)   { this.pricePerUnit = p; }
    public BigDecimal getStockQuantity()        { return stockQuantity; }
    public void setStockQuantity(BigDecimal s)  { this.stockQuantity = s; }
    public String getIntendedFor()              { return intendedFor; }
    public void setIntendedFor(String i)        { this.intendedFor = i; }
    public String getCategory()                 { return category; }
    public void setCategory(String c)           { this.category = c; }
    public int getSales()                       { return sales; }
    public void setSales(int sales)             { this.sales = sales; }
    public String getStatus()                   { return status; }
    public void setStatus(String status)        { this.status = status; }
    public LocalDate getExpirationDate()        { return expirationDate; }
    public void setExpirationDate(LocalDate d)  { this.expirationDate = d; }

    public boolean isOutOfStock() {
        return stockQuantity.compareTo(BigDecimal.ZERO) <= 0;
    }

    public boolean isPieceUnit() {
        return "piece".equalsIgnoreCase(unit);
    }

    // true if expiration date is today or in the past
    public boolean isExpired() {
        return expirationDate != null && !expirationDate.isAfter(LocalDate.now());
    }

    // true if expiration date is within the next 14 days (but not yet expired)
    public boolean isExpiringSoon() {
        if (expirationDate == null) return false;
        LocalDate today = LocalDate.now();
        return expirationDate.isAfter(today) && !expirationDate.isAfter(today.plusDays(14));
    }
}