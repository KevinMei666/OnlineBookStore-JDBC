package test;

import dao.BookDao;
import dao.CustomerDao;
import dao.OrdersDao;
import dao.ShipmentDao;
import model.Book;
import model.Customer;
import model.OrderItem;
import model.Orders;
import service.ShipmentService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试批量发货功能
 */
public class BatchShipmentTest {

    public static void main(String[] args) {
        System.out.println("=== 批量发货功能测试 ===\n");

        BookDao bookDao = new BookDao();
        CustomerDao customerDao = new CustomerDao();
        OrdersDao ordersDao = new OrdersDao();
        ShipmentDao shipmentDao = new ShipmentDao();
        ShipmentService shipmentService = new ShipmentService();

        // 1. 准备测试数据
        System.out.println("1. 准备测试数据...");

        // 确保有足够的图书库存
        Book book1 = bookDao.findById(1);
        Book book2 = bookDao.findById(2);
        if (book1 == null || book2 == null) {
            System.out.println("警告：需要先插入 BookID=1 和 BookID=2 的图书数据");
            return;
        }
        book1.setStockQuantity(100);
        book2.setStockQuantity(100);
        bookDao.update(book1);
        bookDao.update(book2);
        System.out.println("   图书库存已设置");

        // 查找或创建测试客户
        Customer customer = customerDao.findByEmail("test_user@example.com");
        if (customer == null) {
            customer = new Customer();
            customer.setEmail("test_user@example.com");
            customer.setPasswordHash("test_hash");
            customer.setName("测试用户");
            customer.setAddress("测试地址");
            customer.setBalance(new BigDecimal("1000.00"));
            customer.setCreditLevel(3);
            customer.setMonthlyLimit(new BigDecimal("2000.00"));
            customerDao.insert(customer);
            customer = customerDao.findByEmail("test_user@example.com");
        }
        System.out.println("   客户ID: " + customer.getCustomerId());

        // 创建测试订单
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(null, 1, 5, book1.getPrice(), book1.getPrice().multiply(BigDecimal.valueOf(5))));
        items.add(new OrderItem(null, 2, 3, book2.getPrice(), book2.getPrice().multiply(BigDecimal.valueOf(3))));

        int orderId = ordersDao.createOrderWithItems(customer.getCustomerId(), items);
        if (orderId <= 0) {
            System.out.println("   创建订单失败");
            return;
        }
        System.out.println("   订单ID: " + orderId);

        // 2. 测试批量发货（部分明细）
        System.out.println("\n2. 测试批量发货（部分明细）...");
        List<ShipmentService.ShipmentItem> shipmentItems = new ArrayList<>();
        shipmentItems.add(new ShipmentService.ShipmentItem(1, 3)); // BookID=1 发货3本
        shipmentItems.add(new ShipmentService.ShipmentItem(2, 2)); // BookID=2 发货2本

        int successCount = shipmentService.batchShipOrderItems(orderId, shipmentItems);
        System.out.println("   成功发货项数: " + successCount);

        // 查看发货记录
        var shipments = shipmentDao.findByOrderId(orderId);
        System.out.println("   发货记录数: " + shipments.size());
        for (var s : shipments) {
            System.out.println("   - BookID=" + s.getBookId() + 
                    ", Quantity=" + s.getQuantity() + 
                    ", TrackingNo=" + s.getTrackingNo());
        }

        // 3. 测试一次性发货所有剩余明细
        System.out.println("\n3. 测试一次性发货所有剩余明细...");
        int allShippedCount = shipmentService.shipAllOrderItems(orderId);
        System.out.println("   成功发货项数: " + allShippedCount);

        // 查看最终发货记录
        shipments = shipmentDao.findByOrderId(orderId);
        System.out.println("   总发货记录数: " + shipments.size());
        int totalShipped1 = 0, totalShipped2 = 0;
        for (var s : shipments) {
            if (s.getBookId() == 1) {
                totalShipped1 += s.getQuantity();
            } else if (s.getBookId() == 2) {
                totalShipped2 += s.getQuantity();
            }
        }
        System.out.println("   BookID=1 总发货量: " + totalShipped1 + " (订单要求: 5)");
        System.out.println("   BookID=2 总发货量: " + totalShipped2 + " (订单要求: 3)");

        // 查看订单状态
        Orders order = ordersDao.findById(orderId);
        System.out.println("   订单状态: " + order.getStatus());

        System.out.println("\n=== 测试完成 ===");
    }
}

