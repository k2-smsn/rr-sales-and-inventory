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

public class ItemSold {
    private int id;
    private int transactionId;
    private int productId;
    private BigDecimal quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal subTotal;

    public ItemSold() {}

    public ItemSold(int id, int transactionId, int productId, BigDecimal quantity,
                    BigDecimal pricePerUnit, BigDecimal subTotal) {
        this.id = id;
        this.transactionId = transactionId;
        this.productId = productId;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.subTotal = subTotal;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public BigDecimal getSubTotal() { return subTotal; }
    public void setSubTotal(BigDecimal subTotal) { this.subTotal = subTotal; }
}
