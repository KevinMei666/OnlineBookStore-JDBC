package test;

import dao.SupplierDao;
import service.SupplierQueryService;

/**
 * 测试供应商维度综合查询功能
 */
public class SupplierQueryTest {

    public static void main(String[] args) {
        System.out.println("=== 供应商维度综合查询测试 ===\n");

        SupplierDao supplierDao = new SupplierDao();
        SupplierQueryService queryService = new SupplierQueryService();

        // 查找第一个供应商
        var suppliers = supplierDao.findAll();
        if (suppliers.isEmpty()) {
            System.out.println("警告：需要先插入供应商数据");
            return;
        }

        var supplier = suppliers.get(0);
        System.out.println("查询供应商ID: " + supplier.getSupplierId() + " (" + supplier.getName() + ")");

        // 查询供应商供货一览（优化版）
        System.out.println("\n查询供应商供货一览（优化版SQL JOIN）...");
        var overview = queryService.getSupplierSupplyOverviewOptimized(supplier.getSupplierId());

        System.out.println("供应商信息: " + overview.getSupplier().getName() + 
                " (" + overview.getSupplier().getContactEmail() + ")");
        System.out.println("供货图书数: " + overview.getBookSupplies().size());
        System.out.println("总采购单数: " + overview.getTotalPurchaseOrders());
        System.out.println("总采购金额: " + overview.getTotalPurchaseAmount());

        System.out.println("\n供货图书明细:");
        for (var supplyInfo : overview.getBookSupplies()) {
            var book = supplyInfo.getBook();
            System.out.println("\n  BookID=" + book.getBookId() + 
                    " (" + book.getTitle() + ")" +
                    ", 供货价=" + supplyInfo.getSupplyPrice());
            
            System.out.println("    采购历史记录数: " + supplyInfo.getPurchaseHistory().size());
            for (var purchase : supplyInfo.getPurchaseHistory()) {
                System.out.println("      * POID=" + purchase.getPurchaseOrderId() + 
                        ", 状态=" + purchase.getStatus() + 
                        ", 数量=" + purchase.getQuantity() + 
                        ", 单价=" + purchase.getUnitPrice() + 
                        ", 金额=" + purchase.getAmount() + 
                        ", 日期=" + purchase.getCreateDate());
            }
            System.out.println("    总采购量: " + supplyInfo.getTotalPurchasedQuantity());
        }

        System.out.println("\n=== 测试完成 ===");
    }
}

