package test;

import dao.BookDao;
import model.Book;
import util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PurchaseTransactionTest {

    public static void main(String[] args) throws Exception {
        // 假设数据库中已存在如下记录：
        // SupplierID = 1
        // BookID = 1 和 BookID = 2
        int supplierId = 1;
        int bookId1 = 1;
        int bookId2 = 2;

        int quantity1 = 3;
        int quantity2 = 5;
        BigDecimal unitPrice1 = new BigDecimal("30.00");
        BigDecimal unitPrice2 = new BigDecimal("45.00");

        // 使用 BookDao 在事务外先查询一次库存快照
        BookDao bookDao = new BookDao();
        Book book1Before = bookDao.findById(bookId1);
        Book book2Before = bookDao.findById(bookId2);
        int beforeTxStock1 = (book1Before != null && book1Before.getStockQuantity() != null)
                ? book1Before.getStockQuantity()
                : 0;
        int beforeTxStock2 = (book2Before != null && book2Before.getStockQuantity() != null)
                ? book2Before.getStockQuantity()
                : 0;

        System.out.println("=== 事务前库存快照（通过 BookDao） ===");
        System.out.println("BookID=" + bookId1 + "，Stock=" + beforeTxStock1);
        System.out.println("BookID=" + bookId2 + "，Stock=" + beforeTxStock2);
        System.out.println();

        Connection conn = null;
        PreparedStatement insertPoPs = null;
        PreparedStatement insertItemPs = null;
        PreparedStatement selectStockPs = null;
        PreparedStatement updateStockPs = null;
        PreparedStatement updatePoStatusPs = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            System.out.println("=== 采购到货入库事务测试开始 ===");

            // 1. 创建 PurchaseOrder（状态：CREATED）
            String insertPoSql = "INSERT INTO PurchaseOrder (SupplierID, CreateDate, Status, TotalAmount) " +
                    "VALUES (?, ?, ?, ?)";
            insertPoPs = conn.prepareStatement(insertPoSql, Statement.RETURN_GENERATED_KEYS);
            insertPoPs.setInt(1, supplierId);
            insertPoPs.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            insertPoPs.setString(3, "CREATED");
            BigDecimal totalAmount = unitPrice1.multiply(BigDecimal.valueOf(quantity1))
                    .add(unitPrice2.multiply(BigDecimal.valueOf(quantity2)));
            insertPoPs.setBigDecimal(4, totalAmount);
            insertPoPs.executeUpdate();

            rs = insertPoPs.getGeneratedKeys();
            int poId;
            if (rs.next()) {
                poId = rs.getInt(1);
            } else {
                throw new SQLException("无法获取生成的 PurchaseOrderID");
            }
            System.out.println("创建采购单成功，POID = " + poId + "，总金额 = " + totalAmount);
            DBUtil.closeQuietly(rs);
            rs = null;

            // 2. 添加 2 条 PurchaseItem（不同 BookID）
            String insertItemSql = "INSERT INTO PurchaseItem (POID, BookID, Quantity, UnitPrice) " +
                    "VALUES (?, ?, ?, ?)";
            insertItemPs = conn.prepareStatement(insertItemSql);

            // 第一条
            insertItemPs.setInt(1, poId);
            insertItemPs.setInt(2, bookId1);
            insertItemPs.setInt(3, quantity1);
            insertItemPs.setBigDecimal(4, unitPrice1);
            insertItemPs.executeUpdate();

            // 第二条
            insertItemPs.setInt(1, poId);
            insertItemPs.setInt(2, bookId2);
            insertItemPs.setInt(3, quantity2);
            insertItemPs.setBigDecimal(4, unitPrice2);
            insertItemPs.executeUpdate();

            System.out.println("已插入 2 条采购明细到 PurchaseItem。");

            // 查询到货前库存
            String selectStockSql = "SELECT StockQuantity FROM Book WHERE BookID = ?";
            selectStockPs = conn.prepareStatement(selectStockSql);

            selectStockPs.setInt(1, bookId1);
            rs = selectStockPs.executeQuery();
            int beforeStock1 = rs.next() ? rs.getInt(1) : 0;
            DBUtil.closeQuietly(rs);
            rs = null;

            selectStockPs.setInt(1, bookId2);
            rs = selectStockPs.executeQuery();
            int beforeStock2 = rs.next() ? rs.getInt(1) : 0;
            DBUtil.closeQuietly(rs);
            rs = null;

            System.out.println("到货前库存：BookID=" + bookId1 + " -> " + beforeStock1
                    + "，BookID=" + bookId2 + " -> " + beforeStock2);

            // 3. 模拟到货：更新库存并将采购单状态改为 COMPLETED
            String updateStockSql = "UPDATE Book SET StockQuantity = StockQuantity + ? WHERE BookID = ?";
            updateStockPs = conn.prepareStatement(updateStockSql);

            // 更新 BookID1 库存
            updateStockPs.setInt(1, quantity1);
            updateStockPs.setInt(2, bookId1);
            if (updateStockPs.executeUpdate() != 1) {
                throw new SQLException("更新 BookID=" + bookId1 + " 库存失败");
            }

            // 更新 BookID2 库存
            updateStockPs.setInt(1, quantity2);
            updateStockPs.setInt(2, bookId2);
            if (updateStockPs.executeUpdate() != 1) {
                throw new SQLException("更新 BookID=" + bookId2 + " 库存失败");
            }

            // 更新采购单状态为 COMPLETED
            String updatePoStatusSql = "UPDATE PurchaseOrder SET Status = 'COMPLETED' WHERE POID = ?";
            updatePoStatusPs = conn.prepareStatement(updatePoStatusSql);
            updatePoStatusPs.setInt(1, poId);
            if (updatePoStatusPs.executeUpdate() != 1) {
                throw new SQLException("更新采购单状态失败，POID=" + poId);
            }

            // 再次查询库存，验证是否增加
            selectStockPs.setInt(1, bookId1);
            rs = selectStockPs.executeQuery();
            int afterStock1 = rs.next() ? rs.getInt(1) : 0;
            DBUtil.closeQuietly(rs);
            rs = null;

            selectStockPs.setInt(1, bookId2);
            rs = selectStockPs.executeQuery();
            int afterStock2 = rs.next() ? rs.getInt(1) : 0;
            DBUtil.closeQuietly(rs);
            rs = null;

            System.out.println("到货后库存：BookID=" + bookId1 + " -> " + afterStock1
                    + "，BookID=" + bookId2 + " -> " + afterStock2);

            conn.commit();
            System.out.println("=== 采购到货入库事务提交成功 ===");

            // 使用 BookDao 再次查询库存，做对比输出
            Book book1After = bookDao.findById(bookId1);
            Book book2After = bookDao.findById(bookId2);
            int afterTxStock1 = (book1After != null && book1After.getStockQuantity() != null)
                    ? book1After.getStockQuantity()
                    : 0;
            int afterTxStock2 = (book2After != null && book2After.getStockQuantity() != null)
                    ? book2After.getStockQuantity()
                    : 0;

            System.out.println();
            System.out.println("=== 采购入库前后库存对比（通过 BookDao） ===");
            System.out.println("BookID=" + bookId1
                    + " | 采购前库存=" + beforeTxStock1
                    + " | 采购数量=" + quantity1
                    + " | 采购后库存=" + afterTxStock1);
            System.out.println("BookID=" + bookId2
                    + " | 采购前库存=" + beforeTxStock2
                    + " | 采购数量=" + quantity2
                    + " | 采购后库存=" + afterTxStock2);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("发生异常，事务已回滚。");
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
            DBUtil.closeQuietly(rs);
            DBUtil.closeQuietly(insertPoPs);
            DBUtil.closeQuietly(insertItemPs);
            DBUtil.closeQuietly(selectStockPs);
            DBUtil.closeQuietly(updateStockPs);
            DBUtil.closeQuietly(updatePoStatusPs);
            DBUtil.closeQuietly(conn);
        }
    }
}


