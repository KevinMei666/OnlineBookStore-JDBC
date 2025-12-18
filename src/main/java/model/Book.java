package model;

import java.math.BigDecimal;

public class Book {

    private Integer bookId;
    private String title;
    private String publisher;
    private BigDecimal price;
    private Integer stockQuantity;
    private String catalog;
    private byte[] coverImage;
    private Integer seriesId;
    private String location;
    private Boolean active;

    public Book() {
    }

    public Book(Integer bookId, String title, String publisher, BigDecimal price,
                Integer stockQuantity, String catalog, byte[] coverImage,
                Integer seriesId, String location) {
        this.bookId = bookId;
        this.title = title;
        this.publisher = publisher;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.catalog = catalog;
        this.coverImage = coverImage;
        this.seriesId = seriesId;
        this.location = location;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public byte[] getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(byte[] coverImage) {
        this.coverImage = coverImage;
    }

    public Integer getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Integer seriesId) {
        this.seriesId = seriesId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}

