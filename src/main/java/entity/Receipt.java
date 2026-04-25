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
import java.util.List;

public class Receipt {
    private Transaction transaction;
    private List<ItemSold> itemsSold;
    private BigDecimal total;

    public Receipt(Transaction transaction, List<ItemSold> itemsSold, BigDecimal total) {
        this.transaction = transaction;
        this.itemsSold = itemsSold;
        this.total = total;
    }

    public Transaction getTransaction() { return transaction; }
    public List<ItemSold> getItemsSold() { return itemsSold; }
    public BigDecimal getTotal() { return total; }
}