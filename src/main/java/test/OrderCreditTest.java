package test;

import dao.BookDao;
import dao.OrdersDao;
import model.Book;
import model.OrderItem;
import model.Orders;
import util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderCreditTest {

    public static void main(String[] args) throws Exception {
        int customerId = 1; // 请根据实际数据调整
        int bookId = 1;     // 请根据实际数据调整

        // 0. 强制设置客户信用等级和余额，方便测试
        setCustomerCredit(customerId, 3, new BigDecimal("500.00"), new BigDecimal("200.00"));

        // 1. 确保该图书库存充足，避免触发自动采购分支
        forceSetBookStock(bookId, 100);
        BookDao bookDao = new BookDao();
        Book book = bookDao.findById(bookId);
        System.out.println("=== 信用折扣测试：前置状态 ===");
        System.out.println("BookID=" + bookId + " 当前库存=" +
                (book != null ? book.getStockQuantity() : null));
        System.out.println();

        BigDecimal unitPrice = new BigDecimal("100.00");
        int quantity = 2;

        OrderItem item = new OrderItem();
        item.setBookId(bookId);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);

        List<OrderItem> items = new ArrayList<>();
        items.add(item);

        OrdersDao ordersDao = new OrdersDao();
        int orderId = ordersDao.createOrderWithItems(customerId, items);

        System.out.println("=== 信用折扣测试：下单结果 ===");
        System.out.println("生成的 OrderID = " + orderId);

        Orders order = ordersDao.findById(orderId);
        if (order != null) {
            BigDecimal rawAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
            BigDecimal expectedDiscountRate = new BigDecimal("0.15"); // CreditLevel=3
            BigDecimal expectedTotal = rawAmount
                    .multiply(BigDecimal.ONE.subtract(expectedDiscountRate))
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            System.out.println("原始金额 rawAmount = " + rawAmount);
            System.out.println("期望折扣率 = " + expectedDiscountRate);
            System.out.println("期望订单总金额 expectedTotal = " + expectedTotal);
            System.out.println("实际订单总金额 order.TotalAmount = " + order.getTotalAmount());
        } else {
            System.out.println("未查询到 OrderID=" + orderId + " 的订单。");
        }

        System.out.println("=== OrderCreditTest 结束 ===");
    }

    private static void setCustomerCredit(int customerId, int creditLevel,
                                          BigDecimal balance, BigDecimal monthlyLimit) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE Customer SET CreditLevel = ?, Balance = ?, MonthlyLimit = ? WHERE CustomerID = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, creditLevel);
            ps.setBigDecimal(2, balance);
            ps.setBigDecimal(3, monthlyLimit);
            ps.setInt(4, customerId);
            int rows = ps.executeUpdate();
            System.out.println("更新 Customer 信用信息影响行数 = " + rows);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    private static void forceSetBookStock(int bookId, int stockQuantity) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE Book SET StockQuantity = ? WHERE BookID = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, stockQuantity);
            ps.setInt(2, bookId);
            int rows = ps.executeUpdate();
            System.out.println("强制更新 Book.StockQuantity 影响行数 = " + rows);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }
}

