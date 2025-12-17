package model;

import java.time.LocalDateTime;

public class Shipment {

    private Integer shipmentId;
    private Integer orderId;
    private Integer bookId;
    private Integer quantity;
    private LocalDateTime shipDate;
    private String carrier;
    private String trackingNo;

    public Shipment() {
    }

    public Shipment(Integer shipmentId, Integer orderId, Integer bookId, Integer quantity,
                    LocalDateTime shipDate, String carrier, String trackingNo) {
        this.shipmentId = shipmentId;
        this.orderId = orderId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.shipDate = shipDate;
        this.carrier = carrier;
        this.trackingNo = trackingNo;
    }

    public Integer getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Integer shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
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

    public LocalDateTime getShipDate() {
        return shipDate;
    }

    public void setShipDate(LocalDateTime shipDate) {
        this.shipDate = shipDate;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }
}


