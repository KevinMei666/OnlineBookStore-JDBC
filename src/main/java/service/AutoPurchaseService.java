package service;

import util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class AutoPurchaseService {

    /**
     * 处理缺书并自动生成采购单（由调用方管理事务和连接）。
     * 1. 锁定 Book 行，检查当前库存
     * 2. 若库存不足：
     *    - 写入 ShortageRecord
     *    - 自动选择一个 Supplier
     *    - 创建 PurchaseOrder（状态：CREATED）
     *    - 创建 PurchaseItem（数量 = shortageQuantity）
     *
     * @param conn             已开启事务的连接
     * @param bookId           缺书的图书ID
     * @param shortageQuantity 需要采购的数量
     */
    public void handleShortageAndAutoPurchase(Connection conn, int bookId, int shortageQuantity) throws SQLException {
        PreparedStatement selectBookPs = null;
        PreparedStatement insertShortagePs = null;
        PreparedStatement selectBookSupplierPs = null;
        PreparedStatement selectAnySupplierPs = null;
        PreparedStatement insertPoPs = null;
        PreparedStatement insertItemPs = null;
        ResultSet rs = null;

        try {

            // 1. 锁定 Book 行并获取当前库存
            String selectBookSql = "SELECT StockQuantity FROM Book WHERE BookID = ? FOR UPDATE";
            selectBookPs = conn.prepareStatement(selectBookSql);
            selectBookPs.setInt(1, bookId);
            rs = selectBookPs.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Book not found, BookID=" + bookId);
            }
            int currentStock = rs.getInt("StockQuantity");
            rs.close();
            rs = null;

            // 若库存已足够，无需生成缺书和采购单
            if (currentStock >= shortageQuantity) {
                return;
            }

            // 2. 向 ShortageRecord 表插入一条记录（来源标记 AUTO）
            String insertShortageSql = "INSERT INTO ShortageRecord " +
                    "(BookID, SupplierID, Quantity, Date, SourceType, Processed) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            insertShortagePs = conn.prepareStatement(insertShortageSql, Statement.RETURN_GENERATED_KEYS);

            // 3. 自动选择一个 Supplier
            Integer supplierId = null;
            BigDecimal supplyPrice = BigDecimal.ZERO;

            // 优先从 BookSupplier 中选第一个供应商及供货价
            String selectBookSupplierSql = "SELECT SupplierID, SupplyPrice " +
                    "FROM BookSupplier WHERE BookID = ? ORDER BY SupplierID LIMIT 1";
            selectBookSupplierPs = conn.prepareStatement(selectBookSupplierSql);
            selectBookSupplierPs.setInt(1, bookId);
            rs = selectBookSupplierPs.executeQuery();
            if (rs.next()) {
                supplierId = (Integer) rs.getObject("SupplierID");
                supplyPrice = rs.getBigDecimal("SupplyPrice");
                if (supplyPrice == null) {
                    supplyPrice = BigDecimal.ZERO;
                }
            }
            rs.close();
            rs = null;

            // 若 BookSupplier 中没有记录，则从 Supplier 表中选任意一个
            if (supplierId == null) {
                String selectAnySupplierSql = "SELECT SupplierID FROM Supplier ORDER BY SupplierID LIMIT 1";
                selectAnySupplierPs = conn.prepareStatement(selectAnySupplierSql);
                rs = selectAnySupplierPs.executeQuery();
                if (rs.next()) {
                    supplierId = (Integer) rs.getObject("SupplierID");
                } else {
                    throw new SQLException("No supplier available to create purchase order");
                }
                rs.close();
                rs = null;
            }

            // 插入 ShortageRecord
            insertShortagePs.setInt(1, bookId);
            insertShortagePs.setInt(2, supplierId);
            insertShortagePs.setInt(3, shortageQuantity);
            insertShortagePs.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            insertShortagePs.setString(5, "AUTO");
            insertShortagePs.setBoolean(6, false);
            insertShortagePs.executeUpdate();

            rs = insertShortagePs.getGeneratedKeys();
            Integer shortageId = null;
            if (rs.next()) {
                shortageId = rs.getInt(1);
            }
            rs.close();
            rs = null;

            // 4. 创建 PurchaseOrder（状态：CREATED）
            String insertPoSql = "INSERT INTO PurchaseOrder " +
                    "(SupplierID, ShortageID, CreateDate, Status, TotalAmount) " +
                    "VALUES (?, ?, ?, ?, ?)";
            insertPoPs = conn.prepareStatement(insertPoSql, Statement.RETURN_GENERATED_KEYS);
            insertPoPs.setInt(1, supplierId);
            if (shortageId != null) {
                insertPoPs.setInt(2, shortageId);
            } else {
                insertPoPs.setNull(2, java.sql.Types.INTEGER);
            }
            LocalDateTime now = LocalDateTime.now();
            insertPoPs.setTimestamp(3, Timestamp.valueOf(now));

            String status = "CREATED";
            insertPoPs.setString(4, status);

            // 采购单总金额 = 单价 * 数量（若没有供货价，则为 0）
            BigDecimal totalAmount = supplyPrice.multiply(BigDecimal.valueOf(shortageQuantity));
            insertPoPs.setBigDecimal(5, totalAmount);
            insertPoPs.executeUpdate();

            rs = insertPoPs.getGeneratedKeys();
            int poId;
            if (rs.next()) {
                poId = rs.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve generated PurchaseOrderID");
            }
            rs.close();
            rs = null;

            // 5. 创建 PurchaseItem（数量 = shortageQuantity）
            String insertItemSql = "INSERT INTO PurchaseItem (POID, BookID, Quantity, UnitPrice) " +
                    "VALUES (?, ?, ?, ?)";
            insertItemPs = conn.prepareStatement(insertItemSql);
            insertItemPs.setInt(1, poId);
            insertItemPs.setInt(2, bookId);
            insertItemPs.setInt(3, shortageQuantity);
            insertItemPs.setBigDecimal(4, supplyPrice);
            insertItemPs.executeUpdate();

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
            if (selectBookPs != null) {
                try {
                    selectBookPs.close();
                } catch (SQLException ignored) {
                }
            }
            if (insertShortagePs != null) {
                try {
                    insertShortagePs.close();
                } catch (SQLException ignored) {
                }
            }
            if (selectBookSupplierPs != null) {
                try {
                    selectBookSupplierPs.close();
                } catch (SQLException ignored) {
                }
            }
            if (selectAnySupplierPs != null) {
                try {
                    selectAnySupplierPs.close();
                } catch (SQLException ignored) {
                }
            }
            if (insertPoPs != null) {
                try {
                    insertPoPs.close();
                } catch (SQLException ignored) {
                }
            }
            if (insertItemPs != null) {
                try {
                    insertItemPs.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * 处理缺书并自动生成采购单（内部自行管理事务和连接）。
     * 供独立调用使用；与上面重载方法共享核心逻辑。
     */
    public void handleShortageAndAutoPurchase(int bookId, int shortageQuantity) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            handleShortageAndAutoPurchase(conn, bookId, shortageQuantity);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
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
            DBUtil.closeQuietly(conn);
        }
    }
}


