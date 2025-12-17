package model;

import java.math.BigDecimal;

public class Customer {

    private Integer customerId;
    private String email;
    private String passwordHash;
    private String name;
    private String address;
    private BigDecimal balance;
    private Integer creditLevel;
    private BigDecimal monthlyLimit;

    public Customer() {
    }

    public Customer(Integer customerId, String email, String passwordHash, String name,
                    String address, BigDecimal balance, Integer creditLevel,
                    BigDecimal monthlyLimit) {
        this.customerId = customerId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.address = address;
        this.balance = balance;
        this.creditLevel = creditLevel;
        this.monthlyLimit = monthlyLimit;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Integer getCreditLevel() {
        return creditLevel;
    }

    public void setCreditLevel(Integer creditLevel) {
        this.creditLevel = creditLevel;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }
}

