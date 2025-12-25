package test;

import dao.OrdersDao;
import dao.ShipmentDao;
import model.Orders;
import model.Shipment;
import service.ShipmentService;
import util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ShipmentTransactionTest {

    public static void main(String[] args) throws Exception {
        int orderId = 14; // 请根据实际数据库中的订单ID修改
        int bookId = 1;   // 对应订单中的某个 BookID
        int shipQuantity = 2;

        ShipmentService shipmentService = new ShipmentService();
        ShipmentDao shipmentDao = new ShipmentDao();
        OrdersDao ordersDao = new OrdersDao();

        // 查询发货前客户余额与订单状态
        System.out.println("=== 发货前状态 ===");
        printOrderAndCustomerState(orderId);
        System.out.println();

        // 执行发货事务
        System.out.println("=== 执行发货事务 ===");
        // 测试时使用默认承运商（传递 null）
        shipmentService.shipOrderItem(orderId, bookId, shipQuantity, null);
        System.out.println("发货事务执行完毕。");
        System.out.println();

        // 查询发货后客户余额与订单状态
        System.out.println("=== 发货后状态 ===");
        printOrderAndCustomerState(orderId);
        System.out.println();

        // 打印 Shipment 记录
        System.out.println("=== 该订单的 Shipment 记录 ===");
        for (Shipment s : shipmentDao.findByOrderId(orderId)) {
            System.out.println("ShipmentID=" + s.getShipmentId()
                    + " | OrderID=" + s.getOrderId()
                    + " | BookID=" + s.getBookId()
                    + " | Quantity=" + s.getQuantity()
                    + " | ShipDate=" + s.getShipDate()
                    + " | TrackingNo=" + s.getTrackingNo());
        }

        // 打印订单整体信息
        System.out.println();
        Orders order = ordersDao.findById(orderId);
        if (order != null) {
            System.out.println("最终订单状态：OrderID=" + order.getOrderId()
                    + " | Status=" + order.getStatus());
        }

        System.out.println();
        System.out.println("=== ShipmentTransactionTest 结束 ===");
    }

    private static void printOrderAndCustomerState(int orderId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT o.OrderID, o.CustomerID, o.Status, c.Balance, c.CreditLevel, c.MonthlyLimit " +
                    "FROM Orders o JOIN Customer c ON o.CustomerID = c.CustomerID WHERE o.OrderID = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            rs = ps.executeQuery();
            if (rs.next()) {
                int customerId = rs.getInt("CustomerID");
                BigDecimal balance = rs.getBigDecimal("Balance");
                int creditLevel = rs.getInt("CreditLevel");
                BigDecimal monthlyLimit = rs.getBigDecimal("MonthlyLimit");
                String status = rs.getString("Status");

                System.out.println("OrderID=" + orderId
                        + " | CustomerID=" + customerId
                        + " | OrderStatus=" + status);
                System.out.println("Customer Balance=" + balance
                        + " | CreditLevel=" + creditLevel
                        + " | MonthlyLimit=" + monthlyLimit);
            } else {
                System.out.println("未找到 OrderID=" + orderId + " 对应的订单/客户信息。");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
    }
}


