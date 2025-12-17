package service;

import dao.BookDao;
import dao.BookSupplierDao;
import dao.ShortageRecordDao;
import model.Book;
import model.ShortageRecord;
import util.DBUtil;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 缺书记录服务类
 * 提供库存阈值触发的自动缺书记录、人工/客户缺书登记等功能
 */
public class ShortageService {

    /**
     * 调用存储过程：根据库存阈值自动生成缺书记录
     * 
     * @param threshold 库存阈值（低于此值的图书将生成缺书记录）
     * @return 成功处理的记录数（存储过程不返回具体数量，返回0表示调用成功）
     */
    public int autoCreateShortageByThreshold(int threshold) {
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "{CALL AutoCreateShortageByThreshold(?)}";
            cs = conn.prepareCall(sql);
            cs.setInt(1, threshold);
            cs.execute();
            return 0; // 存储过程执行成功
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            DBUtil.closeQuietly(cs);
            DBUtil.closeQuietly(conn);
        }
    }

    /**
     * Java实现：根据库存阈值自动生成缺书记录（批处理）
     * 扫描所有库存低于阈值的图书，为每本图书生成缺书记录
     * 
     * @param threshold 库存阈值
     * @return 生成的缺书记录数量
     */
    public int batchCreateShortageByThreshold(int threshold) {
        BookDao bookDao = new BookDao();
        BookSupplierDao bookSupplierDao = new BookSupplierDao();
        ShortageRecordDao shortageRecordDao = new ShortageRecordDao();
        
        // 查询所有库存低于阈值的图书
        List<Book> allBooks = bookDao.findAll();
        int createdCount = 0;
        
        for (Book book : allBooks) {
            if (book.getStockQuantity() != null && book.getStockQuantity() < threshold) {
                // 检查是否已存在未处理的缺书记录（避免重复）
                List<ShortageRecord> existingRecords = shortageRecordDao.findUnprocessed();
                boolean alreadyExists = false;
                for (ShortageRecord record : existingRecords) {
                    if (record.getBookId().equals(book.getBookId()) 
                            && "THRESHOLD".equals(record.getSourceType())) {
                        alreadyExists = true;
                        break;
                    }
                }
                
                if (!alreadyExists) {
                    // 计算缺货数量（假设补货到阈值）
                    int shortageQty = threshold - book.getStockQuantity();
                    
                    // 查找该图书的供应商
                    Integer supplierId = null;
                    var suppliers = bookSupplierDao.findByBookId(book.getBookId());
                    if (!suppliers.isEmpty()) {
                        supplierId = suppliers.get(0).getSupplierId();
                    }
                    
                    // 创建缺书记录
                    ShortageRecord record = new ShortageRecord();
                    record.setBookId(book.getBookId());
                    record.setSupplierId(supplierId);
                    record.setCustomerId(null);
                    record.setQuantity(shortageQty);
                    record.setDate(LocalDateTime.now());
                    record.setSourceType("THRESHOLD");
                    record.setProcessed(false);
                    
                    int result = shortageRecordDao.insert(record);
                    if (result > 0) {
                        createdCount++;
                    }
                }
            }
        }
        
        return createdCount;
    }

    /**
     * 人工/客户缺书登记
     * 由管理员或客户主动登记缺书需求
     * 
     * @param bookId     图书ID
     * @param quantity   缺货数量
     * @param customerId 客户ID（可为null，表示管理员登记）
     * @param sourceType 来源类型（如 "MANUAL" 管理员登记、"CUSTOMER" 客户登记）
     * @return 生成的缺书记录ID，失败返回-1
     */
    public int registerShortageManually(int bookId, int quantity, Integer customerId, String sourceType) {
        BookDao bookDao = new BookDao();
        BookSupplierDao bookSupplierDao = new BookSupplierDao();
        ShortageRecordDao shortageRecordDao = new ShortageRecordDao();
        
        // 验证图书是否存在
        Book book = bookDao.findById(bookId);
        if (book == null) {
            System.err.println("图书不存在，BookID=" + bookId);
            return -1;
        }
        
        // 查找该图书的供应商
        Integer supplierId = null;
        var suppliers = bookSupplierDao.findByBookId(bookId);
        if (!suppliers.isEmpty()) {
            supplierId = suppliers.get(0).getSupplierId();
        }
        
        // 创建缺书记录
        ShortageRecord record = new ShortageRecord();
        record.setBookId(bookId);
        record.setSupplierId(supplierId);
        record.setCustomerId(customerId);
        record.setQuantity(quantity);
        record.setDate(LocalDateTime.now());
        record.setSourceType(sourceType != null ? sourceType : "MANUAL");
        record.setProcessed(false);
        
        int result = shortageRecordDao.insert(record);
        if (result > 0) {
            // 返回生成的ID（需要查询获取）
            List<ShortageRecord> records = shortageRecordDao.findUnprocessed();
            for (ShortageRecord r : records) {
                if (r.getBookId().equals(bookId) 
                        && r.getQuantity().equals(quantity)
                        && r.getSourceType().equals(record.getSourceType())) {
                    return r.getShortageId();
                }
            }
        }
        
        return -1;
    }

    /**
     * 客户缺书登记（便捷方法）
     * 
     * @param bookId     图书ID
     * @param quantity   缺货数量
     * @param customerId 客户ID
     * @return 生成的缺书记录ID，失败返回-1
     */
    public int registerShortageByCustomer(int bookId, int quantity, int customerId) {
        return registerShortageManually(bookId, quantity, customerId, "CUSTOMER");
    }

    /**
     * 管理员缺书登记（便捷方法）
     * 
     * @param bookId   图书ID
     * @param quantity 缺货数量
     * @return 生成的缺书记录ID，失败返回-1
     */
    public int registerShortageByAdmin(int bookId, int quantity) {
        return registerShortageManually(bookId, quantity, null, "MANUAL");
    }
}

