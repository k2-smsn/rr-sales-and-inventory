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

public class SegmentData {
    private final String category;
    private final String intendedFor;
    private final BigDecimal totalQuantitySold;
    private final BigDecimal totalRevenue;

    public SegmentData(String category, String intendedFor, BigDecimal totalQuantitySold, BigDecimal totalRevenue) {
        this.category = category;
        this.intendedFor = intendedFor;
        this.totalQuantitySold = totalQuantitySold;
        this.totalRevenue = totalRevenue;
    }

    public String getCategory() { return category; }
    public String getIntendedFor() { return intendedFor; }
    public BigDecimal getTotalQuantitySold() { return totalQuantitySold; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
}