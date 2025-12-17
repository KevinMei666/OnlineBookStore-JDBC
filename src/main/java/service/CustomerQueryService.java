package service;

import dao.BookDao;
import dao.OrderItemDao;
import dao.OrdersDao;
import dao.ShipmentDao;
import model.Book;
import model.OrderItem;
import model.Orders;
import model.Shipment;
import util.DBUtil;

import java.sql.Timestamp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户维度综合查询服务
 * 提供客户订单历史、发货信息等综合查询功能
 */
public class CustomerQueryService {

    /**
     * 客户订单历史详情（包含订单明细和发货信息）
     */
    public static class CustomerOrderDetail {
        private Orders order;
        private List<OrderItemWithShipment> items;

        public CustomerOrderDetail() {
            this.items = new ArrayList<>();
        }

        public Orders getOrder() {
            return order;
        }

        public void setOrder(Orders order) {
            this.order = order;
        }

        public List<OrderItemWithShipment> getItems() {
            return items;
        }

        public void setItems(List<OrderItemWithShipment> items) {
            this.items = items;
        }
    }

    /**
     * 订单明细与发货信息
     */
    public static class OrderItemWithShipment {
        private OrderItem orderItem;
        private Book book;
        private List<Shipment> shipments;
        private int totalShippedQuantity;

        public OrderItemWithShipment() {
            this.shipments = new ArrayList<>();
        }

        public OrderItem getOrderItem() {
            return orderItem;
        }

        public void setOrderItem(OrderItem orderItem) {
            this.orderItem = orderItem;
        }

        public Book getBook() {
            return book;
        }

        public void setBook(Book book) {
            this.book = book;
        }

        public List<Shipment> getShipments() {
            return shipments;
        }

        public void setShipments(List<Shipment> shipments) {
            this.shipments = shipments;
            // 计算总发货量
            this.totalShippedQuantity = shipments.stream()
                    .mapToInt(s -> s.getQuantity() != null ? s.getQuantity() : 0)
                    .sum();
        }

        public int getTotalShippedQuantity() {
            return totalShippedQuantity;
        }

        public int getRemainingQuantity() {
            if (orderItem == null || orderItem.getQuantity() == null) {
                return 0;
            }
            return orderItem.getQuantity() - totalShippedQuantity;
        }
    }

    /**
     * 查询客户的所有订单历史（包含订单明细和发货信息）
     * 
     * @param customerId 客户ID
     * @return 订单详情列表
     */
    public List<CustomerOrderDetail> getCustomerOrderHistory(int customerId) {
        OrdersDao ordersDao = new OrdersDao();
        OrderItemDao orderItemDao = new OrderItemDao();
        ShipmentDao shipmentDao = new ShipmentDao();
        BookDao bookDao = new BookDao();

        // 查询客户的所有订单
        List<Orders> orders = ordersDao.findByCustomerId(customerId);
        List<CustomerOrderDetail> result = new ArrayList<>();

        for (Orders order : orders) {
            CustomerOrderDetail detail = new CustomerOrderDetail();
            detail.setOrder(order);

            // 查询订单的所有明细
            List<OrderItem> orderItems = orderItemDao.findByOrderId(order.getOrderId());
            for (OrderItem item : orderItems) {
                OrderItemWithShipment itemWithShipment = new OrderItemWithShipment();
                itemWithShipment.setOrderItem(item);

                // 查询图书信息
                Book book = bookDao.findById(item.getBookId());
                itemWithShipment.setBook(book);

                // 查询该明细的发货记录
                List<Shipment> shipments = shipmentDao.findByOrderId(order.getOrderId());
                List<Shipment> itemShipments = new ArrayList<>();
                for (Shipment shipment : shipments) {
                    if (shipment.getBookId().equals(item.getBookId())) {
                        itemShipments.add(shipment);
                    }
                }
                itemWithShipment.setShipments(itemShipments);

                detail.getItems().add(itemWithShipment);
            }

            result.add(detail);
        }

        return result;
    }

    /**
     * 查询客户订单历史（简化版，使用SQL JOIN一次性查询）
     * 
     * @param customerId 客户ID
     * @return 订单详情列表
     */
    public List<CustomerOrderDetail> getCustomerOrderHistoryOptimized(int customerId) {
        String sql = "SELECT " +
                "o.OrderID, o.CustomerID, o.OrderDate, o.ShippingAddress, o.Status, o.TotalAmount, " +
                "oi.BookID, oi.Quantity AS OrderQuantity, oi.UnitPrice, oi.Amount, " +
                "b.Title, b.Publisher, b.Price AS BookPrice, " +
                "s.ShipmentID, s.Quantity AS ShipQuantity, s.ShipDate, s.Carrier, s.TrackingNo " +
                "FROM Orders o " +
                "LEFT JOIN OrderItem oi ON o.OrderID = oi.OrderID " +
                "LEFT JOIN Book b ON oi.BookID = b.BookID " +
                "LEFT JOIN Shipment s ON o.OrderID = s.OrderID AND oi.BookID = s.BookID " +
                "WHERE o.CustomerID = ? " +
                "ORDER BY o.OrderDate DESC, o.OrderID, oi.BookID, s.ShipDate";

        List<CustomerOrderDetail> result = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
            rs = ps.executeQuery();

            Integer currentOrderId = null;
            CustomerOrderDetail currentDetail = null;
            Integer currentBookId = null;
            OrderItemWithShipment currentItem = null;

            while (rs.next()) {
                Integer orderId = (Integer) rs.getObject("OrderID");

                // 新订单
                if (currentOrderId == null || !currentOrderId.equals(orderId)) {
                    if (currentDetail != null) {
                        result.add(currentDetail);
                    }
                    currentDetail = new CustomerOrderDetail();
                    Orders order = new Orders();
                    order.setOrderId(orderId);
                    order.setCustomerId((Integer) rs.getObject("CustomerID"));
                    order.setShippingAddress(rs.getString("ShippingAddress"));
                    order.setStatus(rs.getString("Status"));
                    order.setTotalAmount(rs.getBigDecimal("TotalAmount"));
                    java.sql.Timestamp ts = rs.getTimestamp("OrderDate");
                    if (ts != null) {
                        order.setOrderDate(ts.toLocalDateTime());
                    }
                    currentDetail.setOrder(order);
                    currentOrderId = orderId;
                    currentBookId = null;
                    currentItem = null;
                }

                // 订单明细
                Integer bookId = (Integer) rs.getObject("BookID");
                if (bookId != null) {
                    // 新明细项
                    if (currentBookId == null || !currentBookId.equals(bookId)) {
                        currentItem = new OrderItemWithShipment();
                        OrderItem orderItem = new OrderItem();
                        orderItem.setOrderId(orderId);
                        orderItem.setBookId(bookId);
                        orderItem.setQuantity((Integer) rs.getObject("OrderQuantity"));
                        orderItem.setUnitPrice(rs.getBigDecimal("UnitPrice"));
                        orderItem.setAmount(rs.getBigDecimal("Amount"));
                        currentItem.setOrderItem(orderItem);

                        Book book = new Book();
                        book.setBookId(bookId);
                        book.setTitle(rs.getString("Title"));
                        book.setPublisher(rs.getString("Publisher"));
                        book.setPrice(rs.getBigDecimal("BookPrice"));
                        currentItem.setBook(book);

                        currentItem.setShipments(new ArrayList<>());
                        currentDetail.getItems().add(currentItem);
                        currentBookId = bookId;
                    }

                    // 发货记录
                    Integer shipmentId = (Integer) rs.getObject("ShipmentID");
                    if (shipmentId != null && currentItem != null) {
                        Shipment shipment = new Shipment();
                        shipment.setShipmentId(shipmentId);
                        shipment.setOrderId(orderId);
                        shipment.setBookId(bookId);
                        shipment.setQuantity((Integer) rs.getObject("ShipQuantity"));
                        shipment.setCarrier(rs.getString("Carrier"));
                        shipment.setTrackingNo(rs.getString("TrackingNo"));
                    Timestamp shipTs = rs.getTimestamp("ShipDate");
                    if (shipTs != null) {
                        shipment.setShipDate(shipTs.toLocalDateTime());
                    }
                        currentItem.getShipments().add(shipment);
                    }
                }
            }

            if (currentDetail != null) {
                result.add(currentDetail);
            }

            // 重新计算总发货量
            for (CustomerOrderDetail detail : result) {
                for (OrderItemWithShipment item : detail.getItems()) {
                    item.setShipments(item.getShipments());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }

        return result;
    }
}

