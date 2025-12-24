package service;

import util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
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

            // 注意：调用此方法时已经确定库存不足，无需再次检查
            // 直接创建缺书记录和采购单

            // 2. 向 ShortageRecord 表插入一条记录（来源标记 AUTO）
            String insertShortageSql = "INSERT INTO ShortageRecord " +
                    "(BookID, SupplierID, CustomerID, Quantity, Date, SourceType, Processed) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
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
                }
                // 如果没有供应商，仍然创建缺书记录，只是supplierId为null
                // 这样管理员可以手动选择供应商
                if (rs != null) {
                    rs.close();
                }
                rs = null;
            }

            // 插入 ShortageRecord（即使没有供应商也创建，supplierId可以为null）
            insertShortagePs.setInt(1, bookId);
            if (supplierId != null) {
                insertShortagePs.setInt(2, supplierId);
            } else {
                insertShortagePs.setNull(2, Types.INTEGER);
            }
            // CustomerID 为 null（订单触发的缺书记录不关联具体客户）
            insertShortagePs.setNull(3, Types.INTEGER);
            insertShortagePs.setInt(4, shortageQuantity);
            insertShortagePs.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            insertShortagePs.setString(6, "AUTO");
            insertShortagePs.setBoolean(7, false);
            insertShortagePs.executeUpdate();

            rs = insertShortagePs.getGeneratedKeys();
            Integer shortageId = null;
            if (rs.next()) {
                shortageId = rs.getInt(1);
            }
            rs.close();
            rs = null;

            // 4. 创建 PurchaseOrder（状态：CREATED）- 只有在有供应商时才创建
            if (supplierId != null) {
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
            }
            // 如果没有供应商，只创建缺书记录，不创建采购单
            // 管理员可以后续手动从缺书记录生成采购单

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


