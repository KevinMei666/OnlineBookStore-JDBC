package model;

import java.time.LocalDateTime;

public class ShortageRecord {

    private Integer shortageId;
    private Integer bookId;
    private Integer supplierId;
    private Integer customerId;
    private Integer quantity;
    private LocalDateTime date;
    private String sourceType;
    private Boolean processed;

    public ShortageRecord() {
    }

    public ShortageRecord(Integer shortageId, Integer bookId, Integer supplierId,
                          Integer customerId, Integer quantity, LocalDateTime date,
                          String sourceType, Boolean processed) {
        this.shortageId = shortageId;
        this.bookId = bookId;
        this.supplierId = supplierId;
        this.customerId = customerId;
        this.quantity = quantity;
        this.date = date;
        this.sourceType = sourceType;
        this.processed = processed;
    }

    public Integer getShortageId() {
        return shortageId;
    }

    public void setShortageId(Integer shortageId) {
        this.shortageId = shortageId;
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

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }
}


