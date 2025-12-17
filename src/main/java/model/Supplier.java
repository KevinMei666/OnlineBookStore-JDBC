package model;

import java.time.LocalDateTime;

public class Supplier {

    private Integer supplierId;
    private String name;
    private String address;
    private String phone;
    private String contactEmail;
    private LocalDateTime createdAt;

    public Supplier() {
    }

    public Supplier(Integer supplierId, String name, String address, String phone,
                    String contactEmail, LocalDateTime createdAt) {
        this.supplierId = supplierId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.contactEmail = contactEmail;
        this.createdAt = createdAt;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


