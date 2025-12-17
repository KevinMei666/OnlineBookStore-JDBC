package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PurchaseOrder {

    private Integer poId;
    private Integer supplierId;
    private Integer shortageId;
    private LocalDateTime createDate;
    private String status;
    private BigDecimal totalAmount;

    public PurchaseOrder() {
    }

    public PurchaseOrder(Integer poId, Integer supplierId, Integer shortageId,
                         LocalDateTime createDate, String status, BigDecimal totalAmount) {
        this.poId = poId;
        this.supplierId = supplierId;
        this.shortageId = shortageId;
        this.createDate = createDate;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public Integer getPoId() {
        return poId;
    }

    public void setPoId(Integer poId) {
        this.poId = poId;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public Integer getShortageId() {
        return shortageId;
    }

    public void setShortageId(Integer shortageId) {
        this.shortageId = shortageId;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}


