package model;

import java.time.LocalDateTime;

public class Inquiry {
    
    private Integer inquiryId;
    private Integer customerId;
    private String bookTitle;
    private String author;
    private String publisher;
    private Integer quantity;
    private LocalDateTime inquiryDate;
    private String status; // PENDING, QUOTED, REJECTED
    private String adminResponse;
    private LocalDateTime responseDate;
    
    public Inquiry() {
    }
    
    public Inquiry(Integer inquiryId, Integer customerId, String bookTitle, String author,
                   String publisher, Integer quantity, LocalDateTime inquiryDate,
                   String status, String adminResponse, LocalDateTime responseDate) {
        this.inquiryId = inquiryId;
        this.customerId = customerId;
        this.bookTitle = bookTitle;
        this.author = author;
        this.publisher = publisher;
        this.quantity = quantity;
        this.inquiryDate = inquiryDate;
        this.status = status;
        this.adminResponse = adminResponse;
        this.responseDate = responseDate;
    }
    
    public Integer getInquiryId() {
        return inquiryId;
    }
    
    public void setInquiryId(Integer inquiryId) {
        this.inquiryId = inquiryId;
    }
    
    public Integer getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public LocalDateTime getInquiryDate() {
        return inquiryDate;
    }
    
    public void setInquiryDate(LocalDateTime inquiryDate) {
        this.inquiryDate = inquiryDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getAdminResponse() {
        return adminResponse;
    }
    
    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }
    
    public LocalDateTime getResponseDate() {
        return responseDate;
    }
    
    public void setResponseDate(LocalDateTime responseDate) {
        this.responseDate = responseDate;
    }
    
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case "PENDING":
                return "待处理";
            case "QUOTED":
                return "已报价";
            case "REJECTED":
                return "已拒绝";
            default:
                return status;
        }
    }
}

