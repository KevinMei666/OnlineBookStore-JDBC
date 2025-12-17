package test;

import dao.CustomerDao;
import service.CustomerQueryService;

/**
 * 测试客户维度综合查询功能
 */
public class CustomerQueryTest {

    public static void main(String[] args) {
        System.out.println("=== 客户维度综合查询测试 ===\n");

        CustomerDao customerDao = new CustomerDao();
        CustomerQueryService queryService = new CustomerQueryService();

        // 查找测试客户
        var customer = customerDao.findByEmail("test_user@example.com");
        if (customer == null) {
            System.out.println("警告：需要先创建测试客户 test_user@example.com");
            return;
        }

        System.out.println("查询客户ID: " + customer.getCustomerId() + " (" + customer.getName() + ")");

        // 查询客户订单历史（优化版）
        System.out.println("\n查询客户订单历史（优化版SQL JOIN）...");
        var orderHistory = queryService.getCustomerOrderHistoryOptimized(customer.getCustomerId());

        System.out.println("订单数量: " + orderHistory.size());
        for (var detail : orderHistory) {
            var order = detail.getOrder();
            System.out.println("\n订单ID: " + order.getOrderId() + 
                    ", 状态: " + order.getStatus() + 
                    ", 总金额: " + order.getTotalAmount() + 
                    ", 日期: " + order.getOrderDate());
            
            System.out.println("  订单明细:");
            for (var item : detail.getItems()) {
                var orderItem = item.getOrderItem();
                var book = item.getBook();
                System.out.println("    - BookID=" + orderItem.getBookId() + 
                        " (" + (book != null ? book.getTitle() : "未知") + ")" +
                        ", 数量=" + orderItem.getQuantity() + 
                        ", 单价=" + orderItem.getUnitPrice());
                
                System.out.println("      发货记录数: " + item.getShipments().size());
                for (var shipment : item.getShipments()) {
                    System.out.println("        * 发货量=" + shipment.getQuantity() + 
                            ", 日期=" + shipment.getShipDate() + 
                            ", 物流=" + shipment.getCarrier() + 
                            ", 单号=" + shipment.getTrackingNo());
                }
                System.out.println("      总发货量: " + item.getTotalShippedQuantity() + 
                        ", 剩余: " + item.getRemainingQuantity());
            }
        }

        System.out.println("\n=== 测试完成 ===");
    }
}

