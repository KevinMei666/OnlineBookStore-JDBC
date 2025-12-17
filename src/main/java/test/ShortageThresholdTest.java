package test;

import dao.BookDao;
import dao.ShortageRecordDao;
import model.Book;
import model.ShortageRecord;
import service.ShortageService;

import java.util.List;

/**
 * 测试库存阈值触发的自动缺书记录功能
 */
public class ShortageThresholdTest {

    public static void main(String[] args) {
        System.out.println("=== 库存阈值触发的自动缺书记录测试 ===\n");

        BookDao bookDao = new BookDao();
        ShortageRecordDao shortageRecordDao = new ShortageRecordDao();
        ShortageService shortageService = new ShortageService();

        // 1. 准备测试数据：设置几本书的库存低于阈值
        System.out.println("1. 准备测试数据：设置图书库存...");
        Book book1 = bookDao.findById(1);
        Book book2 = bookDao.findById(2);
        
        if (book1 == null || book2 == null) {
            System.out.println("警告：需要先插入 BookID=1 和 BookID=2 的图书数据");
            return;
        }

        // 设置库存：book1=5（低于阈值10），book2=3（低于阈值10）
        book1.setStockQuantity(5);
        book2.setStockQuantity(3);
        bookDao.update(book1);
        bookDao.update(book2);
        System.out.println("   BookID=1 库存设置为: " + book1.getStockQuantity());
        System.out.println("   BookID=2 库存设置为: " + book2.getStockQuantity());

        // 2. 查看执行前的未处理缺书记录
        System.out.println("\n2. 执行前的未处理缺书记录：");
        List<ShortageRecord> beforeRecords = shortageRecordDao.findUnprocessed();
        System.out.println("   未处理缺书记录数: " + beforeRecords.size());
        for (ShortageRecord r : beforeRecords) {
            System.out.println("   - ShortageID=" + r.getShortageId() + 
                    ", BookID=" + r.getBookId() + 
                    ", Quantity=" + r.getQuantity() + 
                    ", SourceType=" + r.getSourceType());
        }

        // 3. 调用存储过程方式（推荐）
        System.out.println("\n3. 调用存储过程 AutoCreateShortageByThreshold(threshold=10)...");
        int result1 = shortageService.autoCreateShortageByThreshold(10);
        if (result1 == 0) {
            System.out.println("   存储过程调用成功");
        } else {
            System.out.println("   存储过程调用失败，返回码: " + result1);
        }

        // 4. 查看执行后的未处理缺书记录
        System.out.println("\n4. 执行后的未处理缺书记录：");
        List<ShortageRecord> afterRecords = shortageRecordDao.findUnprocessed();
        System.out.println("   未处理缺书记录数: " + afterRecords.size());
        for (ShortageRecord r : afterRecords) {
            if ("THRESHOLD".equals(r.getSourceType())) {
                System.out.println("   - ShortageID=" + r.getShortageId() + 
                        ", BookID=" + r.getBookId() + 
                        ", Quantity=" + r.getQuantity() + 
                        ", SourceType=" + r.getSourceType() + 
                        " (新生成)");
            }
        }

        // 5. 测试Java批处理方式（备选）
        System.out.println("\n5. 测试Java批处理方式 batchCreateShortageByThreshold(threshold=10)...");
        // 先清理刚才生成的THRESHOLD类型记录（仅用于演示）
        // 实际场景中不需要清理
        int createdCount = shortageService.batchCreateShortageByThreshold(10);
        System.out.println("   Java批处理生成的缺书记录数: " + createdCount);

        System.out.println("\n=== 测试完成 ===");
    }
}

