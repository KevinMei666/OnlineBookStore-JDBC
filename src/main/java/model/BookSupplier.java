package model;

import java.math.BigDecimal;

public class BookSupplier {

    private Integer bookId;
    private Integer supplierId;
    private BigDecimal supplyPrice;

    public BookSupplier() {
    }

    public BookSupplier(Integer bookId, Integer supplierId, BigDecimal supplyPrice) {
        this.bookId = bookId;
        this.supplierId = supplierId;
        this.supplyPrice = supplyPrice;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public BigDecimal getSupplyPrice() {
        return supplyPrice;
    }

    public void setSupplyPrice(BigDecimal supplyPrice) {
        this.supplyPrice = supplyPrice;
    }
}


