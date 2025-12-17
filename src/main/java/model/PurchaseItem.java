package model;

import java.math.BigDecimal;

public class PurchaseItem {

    private Integer purchaseItemId;
    private Integer purchaseOrderId;
    private Integer bookId;
    private Integer quantity;
    private BigDecimal unitPrice;

    public PurchaseItem() {
    }

    public PurchaseItem(Integer purchaseItemId, Integer purchaseOrderId, Integer bookId,
                        Integer quantity, BigDecimal unitPrice) {
        this.purchaseItemId = purchaseItemId;
        this.purchaseOrderId = purchaseOrderId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Integer getPurchaseItemId() {
        return purchaseItemId;
    }

    public void setPurchaseItemId(Integer purchaseItemId) {
        this.purchaseItemId = purchaseItemId;
    }

    public Integer getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Integer purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}


