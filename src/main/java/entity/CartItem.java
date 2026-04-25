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

public class CartItem {
    private Product product;
    private BigDecimal quantity;

    public CartItem(Product product, BigDecimal quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getSubTotal() {
        return product.getPricePerUnit().multiply(quantity);
    }
}
