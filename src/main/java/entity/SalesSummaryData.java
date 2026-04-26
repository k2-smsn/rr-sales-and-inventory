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

public class SalesSummaryData {
    private final int totalTransactions;
    private final BigDecimal totalRevenue;
    private final BigDecimal averageTransactionValue;

    public SalesSummaryData(int totalTransactions, BigDecimal totalRevenue, BigDecimal averageTransactionValue) {
        this.totalTransactions = totalTransactions;
        this.totalRevenue = totalRevenue;
        this.averageTransactionValue = averageTransactionValue;
    }

    public int getTotalTransactions() { return totalTransactions; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public BigDecimal getAverageTransactionValue() { return averageTransactionValue; }
}
