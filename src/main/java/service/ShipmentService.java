package service;

import util.DBUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class ShipmentService {

    /**
     * 对单个订单明细发货，并按信用规则扣款。
     *
     * @param orderId     订单号
     * @param bookId      发货的图书ID
     * @param shipQuantity 本次发货数量
     * @throws SQLException 如果发货过程中发生数据库错误
     */
    public void shipOrderItem(int orderId, int bookId, int shipQuantity) throws SQLException {
        Connection conn = null;
        PreparedStatement selectOrderPs = null;
        PreparedStatement selectCustomerPs = null;
        PreparedStatement selectOrderItemPs = null;
        PreparedStatement selectShippedPs = null;
        PreparedStatement selectStockPs = null;
        PreparedStatement updateStockPs = null;
        PreparedStatement updateCustomerPs = null;
        PreparedStatement insertShipmentPs = null;
        PreparedStatement updateOrderStatusPs = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 锁定订单，获取 CustomerID
            String selectOrderSql = "SELECT CustomerID, Status FROM Orders WHERE OrderID = ? FOR UPDATE";
            selectOrderPs = conn.prepareStatement(selectOrderSql);
            selectOrderPs.setInt(1, orderId);
            rs = selectOrderPs.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Order not found, OrderID=" + orderId);
            }
            int customerId = rs.getInt("CustomerID");
            String orderStatus = rs.getString("Status");
            rs.close();
            rs = null;

            // 2. 锁定客户，获取余额、信用等级、透支额度（如订单已支付则仅校验存在性）
            String selectCustomerSql = "SELECT Balance, CreditLevel, MonthlyLimit FROM Customer WHERE CustomerID = ? FOR UPDATE";
            selectCustomerPs = conn.prepareStatement(selectCustomerSql);
            selectCustomerPs.setInt(1, customerId);
            rs = selectCustomerPs.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Customer not found, CustomerID=" + customerId);
            }
            BigDecimal balance = rs.getBigDecimal("Balance");
            int creditLevel = rs.getInt("CreditLevel");
            if (rs.wasNull()) {
                creditLevel = 1;
            }
            BigDecimal monthlyLimit = rs.getBigDecimal("MonthlyLimit");
            if (balance == null) {
                balance = BigDecimal.ZERO;
            }
            if (monthlyLimit == null) {
                monthlyLimit = BigDecimal.ZERO;
            }
            rs.close();
            rs = null;

            // 3. 锁定订单明细，获取应发数量和单价
            String selectOrderItemSql = "SELECT Quantity, UnitPrice FROM OrderItem WHERE OrderID = ? AND BookID = ? FOR UPDATE";
            selectOrderItemPs = conn.prepareStatement(selectOrderItemSql);
            selectOrderItemPs.setInt(1, orderId);
            selectOrderItemPs.setInt(2, bookId);
            rs = selectOrderItemPs.executeQuery();
            if (!rs.next()) {
                throw new SQLException("OrderItem not found, OrderID=" + orderId + ", BookID=" + bookId);
            }
            int totalQuantity = rs.getInt("Quantity");
            BigDecimal unitPrice = rs.getBigDecimal("UnitPrice");
            rs.close();
            rs = null;

            // 4. 已发货数量
            String selectShippedSql = "SELECT COALESCE(SUM(Quantity), 0) AS ShippedQty " +
                    "FROM Shipment WHERE OrderID = ? AND BookID = ?";
            selectShippedPs = conn.prepareStatement(selectShippedSql);
            selectShippedPs.setInt(1, orderId);
            selectShippedPs.setInt(2, bookId);
            rs = selectShippedPs.executeQuery();
            int shippedQty = 0;
            if (rs.next()) {
                shippedQty = rs.getInt("ShippedQty");
            }
            rs.close();
            rs = null;

            int remaining = totalQuantity - shippedQty;
            if (shipQuantity <= 0 || shipQuantity > remaining) {
                throw new SQLException("Invalid ship quantity. Remaining=" + remaining + ", shipQuantity=" + shipQuantity);
            }

            // 4.5 检查并扣减库存
            String selectStockSql = "SELECT StockQuantity FROM Book WHERE BookID = ? FOR UPDATE";
            selectStockPs = conn.prepareStatement(selectStockSql);
            selectStockPs.setInt(1, bookId);
            rs = selectStockPs.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Book not found, BookID=" + bookId);
            }
            int currentStock = rs.getInt("StockQuantity");
            rs.close();
            rs = null;
            
            if (currentStock < shipQuantity) {
                throw new SQLException("库存不足，无法发货。当前库存：" + currentStock + "，需要发货：" + shipQuantity);
            }
            
            // 扣减库存
            String updateStockSql = "UPDATE Book SET StockQuantity = StockQuantity - ? WHERE BookID = ?";
            updateStockPs = conn.prepareStatement(updateStockSql);
            updateStockPs.setInt(1, shipQuantity);
            updateStockPs.setInt(2, bookId);
            int stockUpdated = updateStockPs.executeUpdate();
            if (stockUpdated != 1) {
                throw new SQLException("扣减库存失败，BookID=" + bookId);
            }

            boolean alreadyPaid = "PAID".equalsIgnoreCase(orderStatus);

            BigDecimal charge = BigDecimal.ZERO;
            BigDecimal newBalance = balance;
            if (!alreadyPaid) {
                // 5. 计算折扣和扣款金额
                BigDecimal discountRate = getDiscountRate(creditLevel);
                BigDecimal discountedUnit = unitPrice.multiply(BigDecimal.ONE.subtract(discountRate));
                charge = discountedUnit.multiply(BigDecimal.valueOf(shipQuantity)).setScale(2, RoundingMode.HALF_UP);

                // 6. 信用规则：1-2级不可透支；3-5级可透支，额度为 MonthlyLimit
                newBalance = balance.subtract(charge);
                if (creditLevel <= 2) {
                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                        throw new SQLException("Insufficient balance for shipment. Need=" + charge + ", Balance=" + balance);
                    }
                } else {
                    // 允许透支：余额最低可到 -MonthlyLimit
                    BigDecimal minAllowed = monthlyLimit.negate();
                    if (newBalance.compareTo(minAllowed) < 0) {
                        throw new SQLException("Over monthly credit limit. NewBalance=" + newBalance + ", Limit=" + monthlyLimit);
                    }
                }

                // 7. 扣减余额
                String updateCustomerSql = "UPDATE Customer SET Balance = ? WHERE CustomerID = ?";
                updateCustomerPs = conn.prepareStatement(updateCustomerSql);
                updateCustomerPs.setBigDecimal(1, newBalance);
                updateCustomerPs.setInt(2, customerId);
                if (updateCustomerPs.executeUpdate() != 1) {
                    throw new SQLException("Failed to update customer balance, CustomerID=" + customerId);
                }
            }

            // 8. 插入发货记录
            String insertShipmentSql = "INSERT INTO Shipment (OrderID, BookID, Quantity, ShipDate, Carrier, TrackingNo) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            insertShipmentPs = conn.prepareStatement(insertShipmentSql);
            insertShipmentPs.setInt(1, orderId);
            insertShipmentPs.setInt(2, bookId);
            insertShipmentPs.setInt(3, shipQuantity);
            insertShipmentPs.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            insertShipmentPs.setString(5, "DEFAULT_CARRIER");
            insertShipmentPs.setString(6, "TRACK-" + System.currentTimeMillis());
            insertShipmentPs.executeUpdate();

            // 9. 判断订单是否全部发完，更新订单状态
            String checkRemainingSql =
                    "SELECT COUNT(*) AS NotFulfilled " +
                    "FROM OrderItem oi " +
                    "LEFT JOIN ( " +
                    "   SELECT OrderID, BookID, SUM(Quantity) AS ShippedQty " +
                    "   FROM Shipment GROUP BY OrderID, BookID" +
                    ") s ON oi.OrderID = s.OrderID AND oi.BookID = s.BookID " +
                    "WHERE oi.OrderID = ? AND COALESCE(s.ShippedQty, 0) < oi.Quantity";
            PreparedStatement checkRemainingPs = conn.prepareStatement(checkRemainingSql);
            checkRemainingPs.setInt(1, orderId);
            rs = checkRemainingPs.executeQuery();
            int notFulfilled = 0;
            if (rs.next()) {
                notFulfilled = rs.getInt("NotFulfilled");
            }
            rs.close();
            rs = null;
            checkRemainingPs.close();

            String newStatus = (notFulfilled == 0) ? "SHIPPED" : "PARTIAL";
            String updateOrderStatusSql = "UPDATE Orders SET Status = ? WHERE OrderID = ?";
            updateOrderStatusPs = conn.prepareStatement(updateOrderStatusSql);
            updateOrderStatusPs.setString(1, newStatus);
            updateOrderStatusPs.setInt(2, orderId);
            if (updateOrderStatusPs.executeUpdate() != 1) {
                throw new SQLException("Failed to update order status, OrderID=" + orderId);
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            // 重新抛出异常，让调用者处理
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
            closeQuietly(rs);
            closeQuietly(selectOrderPs);
            closeQuietly(selectCustomerPs);
            closeQuietly(selectOrderItemPs);
            closeQuietly(selectShippedPs);
            closeQuietly(selectStockPs);
            closeQuietly(updateStockPs);
            closeQuietly(updateCustomerPs);
            closeQuietly(insertShipmentPs);
            closeQuietly(updateOrderStatusPs);
            DBUtil.closeQuietly(conn);
        }
    }

    private BigDecimal getDiscountRate(int creditLevel) {
        switch (creditLevel) {
            case 1:
                return new BigDecimal("0.10");
            case 2:
                return new BigDecimal("0.15");
            case 3:
                return new BigDecimal("0.15");
            case 4:
                return new BigDecimal("0.20");
            case 5:
                return new BigDecimal("0.25");
            default:
                // 默认按 1 级处理（避免数据库异常值导致显示/计算不一致）
                return new BigDecimal("0.10");
        }
    }

    /**
     * 发货项信息（用于批量发货）
     */
    public static class ShipmentItem {
        private int bookId;
        private int quantity;

        public ShipmentItem(int bookId, int quantity) {
            this.bookId = bookId;
            this.quantity = quantity;
        }

        public int getBookId() {
            return bookId;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    /**
     * 批量发货多个订单明细
     *
     * @param orderId       订单号
     * @param shipmentItems 发货项列表
     * @return 成功发货的项数
     */
    public int batchShipOrderItems(int orderId, List<ShipmentItem> shipmentItems) {
        int successCount = 0;
        for (ShipmentItem item : shipmentItems) {
            try {
                shipOrderItem(orderId, item.getBookId(), item.getQuantity());
                successCount++;
            } catch (Exception e) {
                System.err.println("发货失败: OrderID=" + orderId + ", BookID=" + item.getBookId() + 
                        ", Quantity=" + item.getQuantity() + ", Error=" + e.getMessage());
            }
        }
        return successCount;
    }

    /**
     * 一次性发货订单的所有剩余明细
     *
     * @param orderId 订单号
     * @return 成功发货的项数
     */
    public int shipAllOrderItems(int orderId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int successCount = 0;

        try {
            conn = DBUtil.getConnection();
            // 查询所有未完全发货的订单明细
            String sql = "SELECT oi.BookID, oi.Quantity - COALESCE(SUM(s.Quantity), 0) AS RemainingQty " +
                    "FROM OrderItem oi " +
                    "LEFT JOIN Shipment s ON oi.OrderID = s.OrderID AND oi.BookID = s.BookID " +
                    "WHERE oi.OrderID = ? " +
                    "GROUP BY oi.BookID, oi.Quantity " +
                    "HAVING RemainingQty > 0";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            rs = ps.executeQuery();

            while (rs.next()) {
                int bookId = rs.getInt("BookID");
                int remainingQty = rs.getInt("RemainingQty");
                try {
                    shipOrderItem(orderId, bookId, remainingQty);
                    successCount++;
                } catch (Exception e) {
                    System.err.println("发货失败: OrderID=" + orderId + ", BookID=" + bookId + 
                            ", Quantity=" + remainingQty + ", Error=" + e.getMessage());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return successCount;
    }

    private void closeQuietly(AutoCloseable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception ignored) {
            }
        }
    }
}


