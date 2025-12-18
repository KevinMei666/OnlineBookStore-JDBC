package dao;

import model.OrderItem;
import model.Orders;
import service.AutoPurchaseService;
import util.DBUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrdersDao {

    public int insertOrderWithItems(Orders order, List<OrderItem> items) {
        String insertOrderSql = "INSERT INTO Orders (CustomerID, OrderDate, ShippingAddress, TotalAmount, Status) " +
                "VALUES (?, ?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO OrderItem (OrderID, BookID, Quantity, UnitPrice) " +
                "VALUES (?, ?, ?, ?)";
        String selectStockSql = "SELECT StockQuantity FROM Book WHERE BookID = ? FOR UPDATE";
        String updateStockSql = "UPDATE Book SET StockQuantity = StockQuantity - ? WHERE BookID = ?";

        Connection conn = null;
        PreparedStatement orderPs = null;
        PreparedStatement itemPs = null;
        PreparedStatement selectStockPs = null;
        PreparedStatement updateStockPs = null;
        ResultSet rs = null;
        int generatedOrderId = -1;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            orderPs = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            orderPs.setInt(1, order.getCustomerId());
            LocalDateTime orderDate = order.getOrderDate();
            if (orderDate != null) {
                orderPs.setTimestamp(2, Timestamp.valueOf(orderDate));
            } else {
                orderPs.setTimestamp(2, null);
            }
            orderPs.setString(3, order.getShippingAddress());
            orderPs.setBigDecimal(4, order.getTotalAmount());
            orderPs.setString(5, order.getStatus());
            orderPs.executeUpdate();

            rs = orderPs.getGeneratedKeys();
            if (rs.next()) {
                generatedOrderId = rs.getInt(1);
                order.setOrderId(generatedOrderId);
            } else {
                throw new SQLException("Failed to retrieve generated OrderID.");
            }
            DBUtil.closeQuietly(rs);
            rs = null;

            itemPs = conn.prepareStatement(insertItemSql);
            selectStockPs = conn.prepareStatement(selectStockSql);
            updateStockPs = conn.prepareStatement(updateStockSql);

            for (OrderItem item : items) {
                // 插入订单明细
                itemPs.setInt(1, generatedOrderId);
                itemPs.setInt(2, item.getBookId());
                itemPs.setInt(3, item.getQuantity());
                itemPs.setBigDecimal(4, item.getUnitPrice());
                itemPs.addBatch();

                // 库存校验与扣减
                selectStockPs.setInt(1, item.getBookId());
                rs = selectStockPs.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("Book not found, BookID=" + item.getBookId());
                }
                int currentStock = rs.getInt("StockQuantity");
                DBUtil.closeQuietly(rs);
                rs = null;
                if (currentStock < item.getQuantity()) {
                    throw new SQLException("Insufficient stock for BookID=" + item.getBookId());
                }
                updateStockPs.setInt(1, item.getQuantity());
                updateStockPs.setInt(2, item.getBookId());
                int updated = updateStockPs.executeUpdate();
                if (updated != 1) {
                    throw new SQLException("Failed to update stock for BookID=" + item.getBookId());
                }
            }

            itemPs.executeBatch();

            conn.commit();
            return generatedOrderId;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
            DBUtil.closeQuietly(rs);
            DBUtil.closeQuietly(orderPs);
            DBUtil.closeQuietly(itemPs);
            DBUtil.closeQuietly(selectStockPs);
            DBUtil.closeQuietly(updateStockPs);
            DBUtil.closeQuietly(conn);
        }
    }

    public int createOrderWithItems(int customerId, List<OrderItem> items) {
        String insertOrderSql = "INSERT INTO Orders (CustomerID) VALUES (?)";
        String insertItemSql = "INSERT INTO OrderItem (OrderID, BookID, Quantity, UnitPrice) " +
                "VALUES (?, ?, ?, ?)";
        String selectStockSql = "SELECT StockQuantity FROM Book WHERE BookID = ? FOR UPDATE";
        String updateStockSql = "UPDATE Book SET StockQuantity = StockQuantity - ? WHERE BookID = ?";
        String selectCustomerSql = "SELECT CreditLevel FROM Customer WHERE CustomerID = ?";
        String selectCustomerBalanceSql = "SELECT Balance, CreditLevel, MonthlyLimit FROM Customer WHERE CustomerID = ? FOR UPDATE";
        String updateOrderTotalSql = "UPDATE Orders SET TotalAmount = ? , Status = ? WHERE OrderID = ?";
        String updateCustomerBalanceSql = "UPDATE Customer SET Balance = ? WHERE CustomerID = ?";

        Connection conn = null;
        PreparedStatement orderPs = null;
        PreparedStatement itemPs = null;
        PreparedStatement selectStockPs = null;
        PreparedStatement updateStockPs = null;
        PreparedStatement selectCustomerPs = null;
        PreparedStatement updateOrderPs = null;
        PreparedStatement selectBalancePs = null;
        PreparedStatement updateBalancePs = null;
        ResultSet rs = null;
        int generatedOrderId = -1;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 0. 查询客户信用等级
            selectCustomerPs = conn.prepareStatement(selectCustomerSql);
            selectCustomerPs.setInt(1, customerId);
            rs = selectCustomerPs.executeQuery();
            int creditLevel = 1;
            if (rs.next()) {
                creditLevel = rs.getInt("CreditLevel");
                if (rs.wasNull()) {
                    creditLevel = 1;
                }
            }
            DBUtil.closeQuietly(rs);
            rs = null;
            BigDecimal discountRate = getDiscountRate(creditLevel);

            // 1. 插入 Orders 记录，获取生成的 OrderID
            orderPs = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            orderPs.setInt(1, customerId);
            orderPs.executeUpdate();

            rs = orderPs.getGeneratedKeys();
            if (rs.next()) {
                generatedOrderId = rs.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve generated OrderID.");
            }
            DBUtil.closeQuietly(rs);
            rs = null;

            // 2. 处理每个订单明细：库存校验 + 插入明细 + 扣减库存
            itemPs = conn.prepareStatement(insertItemSql);
            selectStockPs = conn.prepareStatement(selectStockSql);
            updateStockPs = conn.prepareStatement(updateStockSql);

            AutoPurchaseService autoPurchaseService = new AutoPurchaseService();
            BigDecimal discountedTotal = BigDecimal.ZERO;

            for (OrderItem item : items) {
                // 2.1 使用 FOR UPDATE 锁定图书库存行
                selectStockPs.setInt(1, item.getBookId());
                rs = selectStockPs.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("Book not found, BookID=" + item.getBookId());
                }
                int currentStock = rs.getInt("StockQuantity");
                DBUtil.closeQuietly(rs);
                rs = null;

                int requiredQty = item.getQuantity();

                // 2.2 判断库存是否充足
                if (currentStock < requiredQty) {
                    int shortageQty = requiredQty - currentStock;
                    // 库存不足：只为缺少的部分自动生成缺书记录和采购单，但不抛异常，订单继续创建
                    autoPurchaseService.handleShortageAndAutoPurchase(conn, item.getBookId(), shortageQty);
                    // 本次订单中该商品标记为缺货：不插入 OrderItem，也不扣减库存
                    continue;
                }

                // 2.3 插入订单明细
                itemPs.setInt(1, generatedOrderId);
                itemPs.setInt(2, item.getBookId());
                itemPs.setInt(3, item.getQuantity());
                itemPs.setBigDecimal(4, item.getUnitPrice());
                itemPs.executeUpdate();

                // 2.4 扣减库存
                updateStockPs.setInt(1, item.getQuantity());
                updateStockPs.setInt(2, item.getBookId());
                int updated = updateStockPs.executeUpdate();
                if (updated != 1) {
                    throw new SQLException("Failed to update stock for BookID=" + item.getBookId());
                }

                // 2.5 计算折后金额并累计到订单总额（仅对实际成功加入的明细）
                BigDecimal lineAmount = item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal discountedLine = lineAmount
                        .multiply(BigDecimal.ONE.subtract(discountRate));
                discountedTotal = discountedTotal.add(discountedLine);
            }

            // 3. 扣款（下单即支付），按信用规则校验余额/透支
            selectBalancePs = conn.prepareStatement(selectCustomerBalanceSql);
            selectBalancePs.setInt(1, customerId);
            rs = selectBalancePs.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Customer not found, CustomerID=" + customerId);
            }
            BigDecimal balance = rs.getBigDecimal("Balance");
            if (balance == null) {
                balance = BigDecimal.ZERO;
            }
            int creditLevelForPay = rs.getInt("CreditLevel");
            if (rs.wasNull()) {
                creditLevelForPay = 1;
            }
            BigDecimal monthlyLimit = rs.getBigDecimal("MonthlyLimit");
            if (monthlyLimit == null) {
                monthlyLimit = BigDecimal.ZERO;
            }
            DBUtil.closeQuietly(rs);
            rs = null;

            BigDecimal newBalance = balance.subtract(discountedTotal);
            if (creditLevelForPay <= 2) {
                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    throw new SQLException("余额不足，请充值后再下单");
                }
            } else {
                BigDecimal minAllowed = monthlyLimit.negate();
                if (newBalance.compareTo(minAllowed) < 0) {
                    throw new SQLException("超过透支额度，请减少购买或充值");
                }
            }

            updateBalancePs = conn.prepareStatement(updateCustomerBalanceSql);
            updateBalancePs.setBigDecimal(1, newBalance.setScale(2, RoundingMode.HALF_UP));
            updateBalancePs.setInt(2, customerId);
            int balanceUpdated = updateBalancePs.executeUpdate();
            if (balanceUpdated != 1) {
                throw new SQLException("扣款失败，请重试");
            }

            // 4. 更新订单总金额与状态：支付完成标记为 PAID
            updateOrderPs = conn.prepareStatement(updateOrderTotalSql);
            updateOrderPs.setBigDecimal(1, discountedTotal.setScale(2, RoundingMode.HALF_UP));
            updateOrderPs.setString(2, "PAID");
            updateOrderPs.setInt(3, generatedOrderId);
            updateOrderPs.executeUpdate();

            // 5. 所有步骤成功后提交事务
            conn.commit();
            return generatedOrderId;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
            DBUtil.closeQuietly(rs);
            DBUtil.closeQuietly(orderPs);
            DBUtil.closeQuietly(itemPs);
            DBUtil.closeQuietly(selectStockPs);
            DBUtil.closeQuietly(updateStockPs);
            DBUtil.closeQuietly(selectCustomerPs);
            DBUtil.closeQuietly(updateOrderPs);
            DBUtil.closeQuietly(conn);
        }
    }

    public Orders findById(int orderId) {
        String sql = "SELECT OrderID, CustomerID, OrderDate, ShippingAddress, TotalAmount, Status " +
                "FROM Orders WHERE OrderID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
    }

    public List<Orders> findByCustomerId(int customerId) {
        String sql = "SELECT OrderID, CustomerID, OrderDate, ShippingAddress, TotalAmount, Status " +
                "FROM Orders WHERE CustomerID = ? ORDER BY OrderDate DESC";
        List<Orders> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return list;
    }

    /**
     * 管理员查看所有订单
     */
    public List<Orders> findAll() {
        String sql = "SELECT OrderID, CustomerID, OrderDate, ShippingAddress, TotalAmount, Status " +
                "FROM Orders ORDER BY OrderDate DESC";
        List<Orders> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM Orders";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return 0;
    }

    /**
     * 今日新增订单数（按 OrderDate 统计）
     */
    public int countCreatedToday() {
        String sql = "SELECT COUNT(*) FROM Orders " +
                "WHERE OrderDate >= CURDATE() AND OrderDate < DATE_ADD(CURDATE(), INTERVAL 1 DAY)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return 0;
    }

    private Orders mapRow(ResultSet rs) throws SQLException {
        Orders order = new Orders();
        order.setOrderId((Integer) rs.getObject("OrderID"));
        order.setCustomerId((Integer) rs.getObject("CustomerID"));
        Timestamp ts = rs.getTimestamp("OrderDate");
        if (ts != null) {
            order.setOrderDate(ts.toLocalDateTime());
        }
        order.setShippingAddress(rs.getString("ShippingAddress"));
        order.setTotalAmount(rs.getBigDecimal("TotalAmount"));
        order.setStatus(rs.getString("Status"));
        return order;
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
}


