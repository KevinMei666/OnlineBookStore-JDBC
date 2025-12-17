package test;

import dao.BookDao;
import dao.PurchaseItemDao;
import dao.PurchaseOrderDao;
import dao.ShortageRecordDao;
import model.Book;
import model.PurchaseItem;
import model.PurchaseOrder;
import model.ShortageRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测试采购到货后标记缺书记录为已处理的功能
 */
public class PurchaseReceiveTest {

    public static void main(String[] args) {
        System.out.println("=== 采购到货后标记缺书记录为已处理功能测试 ===\n");

        BookDao bookDao = new BookDao();
        PurchaseOrderDao purchaseOrderDao = new PurchaseOrderDao();
        PurchaseItemDao purchaseItemDao = new PurchaseItemDao();
        ShortageRecordDao shortageRecordDao = new ShortageRecordDao();

        // 1. 准备测试数据
        System.out.println("1. 准备测试数据...");
        
        // 确保有测试图书
        Book book = bookDao.findById(1);
        if (book == null) {
            System.out.println("   警告：需要先插入 BookID=1 的图书数据");
            return;
        }
        System.out.println("   图书ID: " + book.getBookId() + ", 书名: " + book.getTitle());
        
        // 记录初始库存
        int initialStock = book.getStockQuantity() != null ? book.getStockQuantity() : 0;
        System.out.println("   初始库存: " + initialStock);

        // 2. 创建未处理的缺书记录
        System.out.println("\n2. 创建未处理的缺书记录...");
        ShortageRecord shortageRecord = new ShortageRecord();
        shortageRecord.setBookId(1);
        shortageRecord.setSupplierId(1);
        shortageRecord.setQuantity(10);
        shortageRecord.setDate(LocalDateTime.now());
        shortageRecord.setSourceType("TEST");
        shortageRecord.setProcessed(false);
        
        int shortageId = shortageRecordDao.insert(shortageRecord);
        if (shortageId > 0) {
            System.out.println("   ✓ 缺书记录创建成功，ShortageID: " + shortageId);
        } else {
            System.out.println("   ✗ 缺书记录创建失败");
            return;
        }

        // 验证缺书记录状态
        ShortageRecord createdShortage = shortageRecordDao.findById(shortageId);
        if (createdShortage != null) {
            boolean isProcessed = createdShortage.getProcessed() != null && createdShortage.getProcessed();
            System.out.println("   缺书记录状态（Processed）: " + isProcessed);
            if (!isProcessed) {
                System.out.println("   ✓ 缺书记录状态为未处理");
            } else {
                System.out.println("   ✗ 缺书记录状态应为未处理");
            }
        }

        // 3. 创建采购单和采购明细
        System.out.println("\n3. 创建采购单和采购明细...");
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplierId(1);
        purchaseOrder.setCreateDate(LocalDateTime.now());
        purchaseOrder.setStatus("CREATED");
        purchaseOrder.setTotalAmount(new BigDecimal("1000.00"));
        
        int purchaseOrderId = purchaseOrderDao.insert(purchaseOrder);
        if (purchaseOrderId > 0) {
            System.out.println("   ✓ 采购单创建成功，POID: " + purchaseOrderId);
        } else {
            System.out.println("   ✗ 采购单创建失败");
            return;
        }

        // 创建采购明细（采购10本，对应缺书记录）
        PurchaseItem purchaseItem = new PurchaseItem();
        purchaseItem.setPurchaseOrderId(purchaseOrderId);
        purchaseItem.setBookId(1);
        purchaseItem.setQuantity(10);
        purchaseItem.setUnitPrice(new BigDecimal("50.00"));
        
        int itemResult = purchaseItemDao.insert(purchaseItem);
        if (itemResult > 0) {
            System.out.println("   ✓ 采购明细创建成功");
        } else {
            System.out.println("   ✗ 采购明细创建失败");
            return;
        }

        // 4. 执行采购到货操作
        System.out.println("\n4. 执行采购到货操作...");
        int receiveResult = purchaseOrderDao.receivePurchaseOrder(purchaseOrderId);
        if (receiveResult > 0) {
            System.out.println("   ✓ 采购到货操作成功");
        } else {
            System.out.println("   ✗ 采购到货操作失败");
            return;
        }

        // 5. 验证库存增加
        System.out.println("\n5. 验证库存增加...");
        Book updatedBook = bookDao.findById(1);
        if (updatedBook != null) {
            int newStock = updatedBook.getStockQuantity() != null ? updatedBook.getStockQuantity() : 0;
            int expectedStock = initialStock + 10;
            System.out.println("   更新后库存: " + newStock);
            System.out.println("   期望库存: " + expectedStock);
            
            if (newStock == expectedStock) {
                System.out.println("   ✓ 库存增加验证通过");
            } else {
                System.out.println("   ✗ 库存增加验证失败：实际库存与期望不符");
            }
        }

        // 6. 验证采购单状态更新
        System.out.println("\n6. 验证采购单状态更新...");
        PurchaseOrder updatedOrder = purchaseOrderDao.findById(purchaseOrderId);
        if (updatedOrder != null) {
            String status = updatedOrder.getStatus();
            System.out.println("   采购单状态: " + status);
            
            if ("COMPLETED".equals(status)) {
                System.out.println("   ✓ 采购单状态更新验证通过");
            } else {
                System.out.println("   ✗ 采购单状态应为 COMPLETED，实际为: " + status);
            }
        }

        // 7. 验证缺书记录被标记为已处理
        System.out.println("\n7. 验证缺书记录被标记为已处理...");
        ShortageRecord updatedShortage = shortageRecordDao.findById(shortageId);
        if (updatedShortage != null) {
            boolean isProcessed = updatedShortage.getProcessed() != null && updatedShortage.getProcessed();
            System.out.println("   缺书记录状态（Processed）: " + isProcessed);
            
            if (isProcessed) {
                System.out.println("   ✓ 缺书记录已标记为已处理");
            } else {
                System.out.println("   ✗ 缺书记录应被标记为已处理，但状态仍为未处理");
            }
        } else {
            System.out.println("   ✗ 无法查询到缺书记录");
        }

        // 8. 测试多个缺书记录的情况
        System.out.println("\n8. 测试多个缺书记录的情况...");
        
        // 创建第二个缺书记录
        ShortageRecord shortageRecord2 = new ShortageRecord();
        shortageRecord2.setBookId(1);
        shortageRecord2.setSupplierId(1);
        shortageRecord2.setQuantity(5);
        shortageRecord2.setDate(LocalDateTime.now());
        shortageRecord2.setSourceType("TEST2");
        shortageRecord2.setProcessed(false);
        
        int shortageId2 = shortageRecordDao.insert(shortageRecord2);
        if (shortageId2 > 0) {
            System.out.println("   创建第二个缺书记录，ShortageID: " + shortageId2);
        }

        // 创建第二个采购单
        PurchaseOrder purchaseOrder2 = new PurchaseOrder();
        purchaseOrder2.setSupplierId(1);
        purchaseOrder2.setCreateDate(LocalDateTime.now());
        purchaseOrder2.setStatus("CREATED");
        purchaseOrder2.setTotalAmount(new BigDecimal("250.00"));
        
        int purchaseOrderId2 = purchaseOrderDao.insert(purchaseOrder2);
        if (purchaseOrderId2 > 0) {
            System.out.println("   创建第二个采购单，POID: " + purchaseOrderId2);
        }

        // 创建采购明细（采购5本）
        PurchaseItem purchaseItem2 = new PurchaseItem();
        purchaseItem2.setPurchaseOrderId(purchaseOrderId2);
        purchaseItem2.setBookId(1);
        purchaseItem2.setQuantity(5);
        purchaseItem2.setUnitPrice(new BigDecimal("50.00"));
        
        purchaseItemDao.insert(purchaseItem2);
        System.out.println("   创建采购明细（5本）");

        // 执行采购到货
        receiveResult = purchaseOrderDao.receivePurchaseOrder(purchaseOrderId2);
        if (receiveResult > 0) {
            System.out.println("   ✓ 第二个采购单到货操作成功");
        }

        // 验证第二个缺书记录也被标记为已处理
        ShortageRecord updatedShortage2 = shortageRecordDao.findById(shortageId2);
        if (updatedShortage2 != null) {
            boolean isProcessed2 = updatedShortage2.getProcessed() != null && updatedShortage2.getProcessed();
            System.out.println("   第二个缺书记录状态（Processed）: " + isProcessed2);
            
            if (isProcessed2) {
                System.out.println("   ✓ 第二个缺书记录也被标记为已处理");
            } else {
                System.out.println("   ✗ 第二个缺书记录应被标记为已处理");
            }
        }

        System.out.println("\n=== 测试完成 ===");
    }
}

