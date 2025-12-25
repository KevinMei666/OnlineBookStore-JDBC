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
        return createOrderWithItems(customerId, items, false);
    }
    
    public int createOrderWithItems(int customerId, List<OrderItem> items, boolean hasShortage) {
        String insertOrderSql = "INSERT INTO Orders (CustomerID) VALUES (?)";
        String insertItemSql = "INSERT INTO OrderItem (OrderID, BookID, Quantity, UnitPrice) " +
                "VALUES (?, ?, ?, ?)";
        String selectStockSql = "SELECT StockQuantity FROM Book WHERE BookID = ? FOR UPDATE";
        // 注意：不再在下单时扣减库存，库存扣减在发货时进行（ShipmentService中处理）
        String selectCustomerSql = "SELECT CreditLevel FROM Customer WHERE CustomerID = ?";
        String selectCustomerBalanceSql = "SELECT Balance, CreditLevel, MonthlyLimit FROM Customer WHERE CustomerID = ? FOR UPDATE";
        String updateOrderTotalSql = "UPDATE Orders SET TotalAmount = ? , Status = ? WHERE OrderID = ?";
        String updateCustomerBalanceSql = "UPDATE Customer SET Balance = ? WHERE CustomerID = ?";

        Connection conn = null;
        PreparedStatement orderPs = null;
        PreparedStatement itemPs = null;
        PreparedStatement selectStockPs = null;
        // 不再需要updateStockPs，库存扣减在发货时进行
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

            // 2. 处理每个订单明细：库存校验 + 插入明细（不扣减库存，库存扣减在发货时进行）
            itemPs = conn.prepareStatement(insertItemSql);
            selectStockPs = conn.prepareStatement(selectStockSql);

            AutoPurchaseService autoPurchaseService = new AutoPurchaseService();
            BigDecimal discountedTotal = BigDecimal.ZERO;
            boolean actualHasShortage = false; // 实际是否有库存不足的商品

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
                boolean itemHasShortage = currentStock < requiredQty;

                // 2.2 判断库存是否充足
                if (itemHasShortage) {
                    actualHasShortage = true;
                    int shortageQty = requiredQty - currentStock;
                    // 库存不足：只为缺少的部分自动生成缺书记录和采购单
                    // 使用 try-catch 确保即使采购单创建失败，缺书记录也能创建
                    boolean shortageRecordCreated = false;
                    try {
                        autoPurchaseService.handleShortageAndAutoPurchase(conn, item.getBookId(), shortageQty);
                        shortageRecordCreated = true;
                    } catch (SQLException e) {
                        // 如果自动创建采购单失败，至少确保缺书记录被创建
                        // 记录错误但不中断订单创建流程
                        System.err.println("自动创建采购单失败，尝试直接创建缺书记录: " + e.getMessage());
                        e.printStackTrace();
                        // 手动创建缺书记录
                        try {
                            createShortageRecordDirectly(conn, item.getBookId(), shortageQty);
                            shortageRecordCreated = true;
                        } catch (Exception e2) {
                            System.err.println("直接创建缺书记录也失败: " + e2.getMessage());
                            e2.printStackTrace();
                        }
                    }
                    // 如果缺书记录创建失败，记录警告但继续订单创建
                    if (!shortageRecordCreated) {
                        System.err.println("警告：缺书记录创建失败，BookID=" + item.getBookId() + ", ShortageQty=" + shortageQty);
                    }
                    // 即使库存不足，也插入OrderItem，但不扣减库存
                    // 这样订单记录完整，但库存不减少，订单状态会设为CREATED
                } else {
                    // 2.3 库存充足时，只检查库存，不扣减库存
                    // 库存扣减应该在发货时进行（ShipmentService中处理）
                    // 这样可以避免下单后未发货就扣减库存的问题
                }

                // 2.4 插入订单明细（无论库存是否充足都插入）
                itemPs.setInt(1, generatedOrderId);
                itemPs.setInt(2, item.getBookId());
                itemPs.setInt(3, item.getQuantity());
                itemPs.setBigDecimal(4, item.getUnitPrice());
                itemPs.executeUpdate();

                // 2.5 计算折后金额并累计到订单总额（包含所有商品，即使库存不足）
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

            // 3.1 根据余额和透支规则判断本次是否能够“立即支付”
            BigDecimal newBalance = balance.subtract(discountedTotal);
            boolean canPayNow = true;
            if (creditLevelForPay <= 2) {
                // 普通客户：不允许透支
                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    canPayNow = false;
                }
            } else {
                // 高信用客户：允许在月度透支额度内
                BigDecimal minAllowed = monthlyLimit.negate();
                if (newBalance.compareTo(minAllowed) < 0) {
                    canPayNow = false;
                }
            }

            // 3.2 如果可以支付且没有库存不足，则立即扣款并将订单标记为 PAID；否则只创建为 CREATED，稍后由用户"继续支付"
            String finalStatus;
            // 如果有库存不足的商品，订单状态必须为CREATED（未付款）
            if (actualHasShortage || hasShortage) {
                finalStatus = "CREATED";
            } else if (discountedTotal.compareTo(BigDecimal.ZERO) > 0 && canPayNow) {
                // 扣款
                updateBalancePs = conn.prepareStatement(updateCustomerBalanceSql);
                updateBalancePs.setBigDecimal(1, newBalance.setScale(2, RoundingMode.HALF_UP));
                updateBalancePs.setInt(2, customerId);
                int balanceUpdated = updateBalancePs.executeUpdate();
                if (balanceUpdated != 1) {
                    throw new SQLException("扣款失败，请重试");
                }
                finalStatus = "PAID";
            } else {
                // 余额或透支额度不足：先生成未支付订单，状态为 CREATED
                finalStatus = "CREATED";
            }

            // 4. 更新订单总金额与状态
            updateOrderPs = conn.prepareStatement(updateOrderTotalSql);
            updateOrderPs.setBigDecimal(1, discountedTotal.setScale(2, RoundingMode.HALF_UP));
            updateOrderPs.setString(2, finalStatus);
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
            // 不再需要关闭updateStockPs
            DBUtil.closeQuietly(selectCustomerPs);
            DBUtil.closeQuietly(updateOrderPs);
            DBUtil.closeQuietly(conn);
        }
    }

    public Orders findById(int orderId) {
        String sql = "SELECT OrderID, CustomerID, OrderDate, ShippingAddress, TotalAmount, Status, " +
                "COALESCE(Confirmed, 0) AS Confirmed " +
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
        String sql = "SELECT OrderID, CustomerID, OrderDate, ShippingAddress, TotalAmount, Status, " +
                "COALESCE(Confirmed, 0) AS Confirmed " +
                "FROM Orders WHERE CustomerID = ? ORDER BY OrderID DESC";
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
        String sql = "SELECT OrderID, CustomerID, OrderDate, ShippingAddress, TotalAmount, Status, " +
                "COALESCE(Confirmed, 0) AS Confirmed " +
                "FROM Orders ORDER BY OrderID DESC";
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
        // 读取 Confirmed 字段，如果不存在或为 null 则默认为 false
        try {
            Object confirmedObj = rs.getObject("Confirmed");
            if (confirmedObj != null) {
                if (confirmedObj instanceof Boolean) {
                    order.setConfirmed((Boolean) confirmedObj);
                } else if (confirmedObj instanceof Number) {
                    order.setConfirmed(((Number) confirmedObj).intValue() != 0);
                } else {
                    order.setConfirmed(false);
                }
            } else {
                order.setConfirmed(false);
            }
        } catch (SQLException e) {
            // 如果字段不存在，默认为 false
            order.setConfirmed(false);
        }
        return order;
    }

    /**
     * 客户确认收货
     * 只允许 SHIPPED 状态的订单确认收货，且不能重复确认
     */
    public int confirmOrder(int orderId, int customerId) {
        // 先检查订单状态和确认状态
        String checkSql = "SELECT Status, COALESCE(Confirmed, 0) AS Confirmed FROM Orders WHERE OrderID = ? AND CustomerID = ?";
        String updateSql = "UPDATE Orders SET Confirmed = 1 WHERE OrderID = ? AND CustomerID = ? " +
                "AND Status = 'SHIPPED' AND (Confirmed IS NULL OR Confirmed = 0)";
        Connection conn = null;
        PreparedStatement checkPs = null;
        PreparedStatement updatePs = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 先检查订单状态
            checkPs = conn.prepareStatement(checkSql);
            checkPs.setInt(1, orderId);
            checkPs.setInt(2, customerId);
            rs = checkPs.executeQuery();
            if (!rs.next()) {
                // 订单不存在或不属于该客户
                return 0;
            }
            String status = rs.getString("Status");
            boolean confirmed = rs.getInt("Confirmed") != 0;
            if (!"SHIPPED".equals(status)) {
                // 订单状态不是 SHIPPED，无法确认收货
                return 0;
            }
            if (confirmed) {
                // 已经确认过了，不能重复确认
                return 0;
            }
            DBUtil.closeQuietly(rs);
            rs = null;
            DBUtil.closeQuietly(checkPs);
            checkPs = null;
            
            // 执行更新
            updatePs = conn.prepareStatement(updateSql);
            updatePs.setInt(1, orderId);
            updatePs.setInt(2, customerId);
            return updatePs.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(rs);
            DBUtil.closeQuietly(checkPs);
            DBUtil.closeQuietly(updatePs);
            DBUtil.closeQuietly(conn);
        }
    }

    /**
     * 继续支付订单（针对CREATED状态的订单）
     * @return null表示成功，否则返回错误信息
     */
    public String payOrder(int orderId, int customerId) {
        String selectOrderSql = "SELECT TotalAmount, Status FROM Orders WHERE OrderID = ? AND CustomerID = ? FOR UPDATE";
        String selectCustomerSql = "SELECT Balance, CreditLevel, MonthlyLimit FROM Customer WHERE CustomerID = ? FOR UPDATE";
        String updateBalanceSql = "UPDATE Customer SET Balance = ? WHERE CustomerID = ?";
        String updateOrderSql = "UPDATE Orders SET Status = 'PAID' WHERE OrderID = ?";

        Connection conn = null;
        PreparedStatement selectOrderPs = null;
        PreparedStatement selectCustomerPs = null;
        PreparedStatement updateBalancePs = null;
        PreparedStatement updateOrderPs = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 查询订单
            selectOrderPs = conn.prepareStatement(selectOrderSql);
            selectOrderPs.setInt(1, orderId);
            selectOrderPs.setInt(2, customerId);
            rs = selectOrderPs.executeQuery();
            if (!rs.next()) {
                conn.rollback();
                return "订单不存在或不属于当前用户";
            }
            BigDecimal totalAmount = rs.getBigDecimal("TotalAmount");
            String status = rs.getString("Status");
            rs.close();

            if (totalAmount == null) {
                totalAmount = BigDecimal.ZERO;
            }
            if (!"CREATED".equalsIgnoreCase(status)) {
                conn.rollback();
                return "该订单状态不是待支付";
            }

            // 2. 查询客户余额和信用
            selectCustomerPs = conn.prepareStatement(selectCustomerSql);
            selectCustomerPs.setInt(1, customerId);
            rs = selectCustomerPs.executeQuery();
            if (!rs.next()) {
                conn.rollback();
                return "客户信息不存在";
            }
            BigDecimal balance = rs.getBigDecimal("Balance");
            int creditLevel = rs.getInt("CreditLevel");
            BigDecimal monthlyLimit = rs.getBigDecimal("MonthlyLimit");
            rs.close();

            if (balance == null) balance = BigDecimal.ZERO;
            if (monthlyLimit == null) monthlyLimit = BigDecimal.ZERO;

            // 3. 校验余额
            BigDecimal newBalance = balance.subtract(totalAmount);
            if (creditLevel <= 2) {
                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    conn.rollback();
                    return "余额不足，请充值后再支付";
                }
            } else {
                BigDecimal minAllowed = monthlyLimit.negate();
                if (newBalance.compareTo(minAllowed) < 0) {
                    conn.rollback();
                    return "超过透支额度，请减少购买或充值";
                }
            }

            // 4. 扣款
            updateBalancePs = conn.prepareStatement(updateBalanceSql);
            updateBalancePs.setBigDecimal(1, newBalance.setScale(2, RoundingMode.HALF_UP));
            updateBalancePs.setInt(2, customerId);
            updateBalancePs.executeUpdate();

            // 5. 更新订单状态为PAID
            updateOrderPs = conn.prepareStatement(updateOrderSql);
            updateOrderPs.setInt(1, orderId);
            updateOrderPs.executeUpdate();

            conn.commit();
            return null; // 成功

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            e.printStackTrace();
            return "支付失败：" + e.getMessage();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            DBUtil.closeQuietly(rs);
            DBUtil.closeQuietly(selectOrderPs);
            DBUtil.closeQuietly(selectCustomerPs);
            DBUtil.closeQuietly(updateBalancePs);
            DBUtil.closeQuietly(updateOrderPs);
            DBUtil.closeQuietly(conn);
        }
    }

    /**
     * 直接创建缺书记录（当自动采购服务失败时的备用方案）
     */
    private void createShortageRecordDirectly(Connection conn, int bookId, int shortageQty) {
        String insertShortageSql = "INSERT INTO ShortageRecord " +
                "(BookID, SupplierID, CustomerID, Quantity, Date, SourceType, Processed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(insertShortageSql);
            ps.setInt(1, bookId);
            // 尝试查找供应商，如果没有则为null
            String selectSupplierSql = "SELECT SupplierID FROM BookSupplier WHERE BookID = ? ORDER BY SupplierID LIMIT 1";
            PreparedStatement selectPs = conn.prepareStatement(selectSupplierSql);
            selectPs.setInt(1, bookId);
            ResultSet rs = selectPs.executeQuery();
            if (rs.next()) {
                ps.setInt(2, rs.getInt("SupplierID"));
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            rs.close();
            selectPs.close();
            
            // CustomerID 为 null（订单触发的缺书记录不关联具体客户）
            ps.setNull(3, java.sql.Types.INTEGER);
            ps.setInt(4, shortageQty);
            ps.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
            ps.setString(6, "ORDER");
            ps.setBoolean(7, false);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("创建缺书记录失败: " + e.getMessage());
            e.printStackTrace();
            // 不抛出异常，避免影响订单创建
        } finally {
            DBUtil.closeQuietly(ps);
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
}


