package dao;

import model.PurchaseOrder;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDao {

    public int insert(PurchaseOrder order) {
        String insertSql = "INSERT INTO PurchaseOrder (SupplierID, ShortageID, CreateDate, Status, TotalAmount) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement insertPs = null;
        ResultSet rs = null;
        int generatedId = -1;

        try {
            conn = DBUtil.getConnection();
            insertPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertPs.setObject(1, order.getSupplierId());
            insertPs.setObject(2, order.getShortageId());
            LocalDateTime createDate = order.getCreateDate();
            if (createDate != null) {
                insertPs.setTimestamp(3, Timestamp.valueOf(createDate));
            } else {
                insertPs.setTimestamp(3, null);
            }
            insertPs.setString(4, order.getStatus());
            insertPs.setBigDecimal(5, order.getTotalAmount());
            insertPs.executeUpdate();

            rs = insertPs.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1);
                order.setPoId(generatedId);
            } else {
                throw new SQLException("Failed to retrieve generated POID.");
            }
            return generatedId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            DBUtil.closeQuietly(rs);
            DBUtil.closeQuietly(insertPs);
            DBUtil.closeQuietly(conn);
        }
    }

    public int insertWithShortageUpdate(PurchaseOrder order) {
        String insertSql = "INSERT INTO PurchaseOrder (SupplierID, ShortageID, CreateDate, Status, TotalAmount) " +
                "VALUES (?, ?, ?, ?, ?)";
        String updateShortageSql = "UPDATE ShortageRecord SET Processed = 1 WHERE ShortageID = ?";

        Connection conn = null;
        PreparedStatement insertPs = null;
        PreparedStatement updatePs = null;
        ResultSet rs = null;
        int generatedId = -1;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            insertPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertPs.setObject(1, order.getSupplierId());
            insertPs.setObject(2, order.getShortageId());
            LocalDateTime createDate = order.getCreateDate();
            if (createDate != null) {
                insertPs.setTimestamp(3, Timestamp.valueOf(createDate));
            } else {
                insertPs.setTimestamp(3, null);
            }
            insertPs.setString(4, order.getStatus());
            insertPs.setBigDecimal(5, order.getTotalAmount());
            insertPs.executeUpdate();

            rs = insertPs.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1);
                order.setPoId(generatedId);
            } else {
                throw new SQLException("Failed to retrieve generated POID.");
            }
            DBUtil.closeQuietly(rs);
            rs = null;

            updatePs = conn.prepareStatement(updateShortageSql);
            updatePs.setObject(1, order.getShortageId());
            updatePs.executeUpdate();

            conn.commit();
            return generatedId;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
            DBUtil.closeQuietly(rs);
            DBUtil.closeQuietly(insertPs);
            DBUtil.closeQuietly(updatePs);
            DBUtil.closeQuietly(conn);
        }
    }

    /**
     * 采购到货：
     * 1. 根据采购单号查询所有采购明细（PurchaseItem）
     * 2. 遍历每一项，增加对应图书的库存（Book.StockQuantity += Quantity）
     * 3. 更新采购单状态（例如标记为 COMPLETED）
     * 4. 标记相关缺书记录为已处理（根据采购明细中的 BookID 匹配 ShortageRecord）
     * 整个过程使用一个事务，任一步失败则回滚。
     *
     * @param purchaseOrderId 采购单号
     * @return 成功返回 1，失败返回 -1
     */
    public int receivePurchaseOrder(int purchaseOrderId) {
        String selectItemsSql = "SELECT BookID, Quantity FROM PurchaseItem WHERE POID = ?";
        String updateBookSql = "UPDATE Book SET StockQuantity = StockQuantity + ? WHERE BookID = ?";
        String updateOrderSql = "UPDATE PurchaseOrder SET Status = 'COMPLETED' WHERE POID = ?";
        String updateShortageSql = "UPDATE ShortageRecord sr " +
                "INNER JOIN PurchaseItem pi ON sr.BookID = pi.BookID " +
                "SET sr.Processed = 1 " +
                "WHERE pi.POID = ? AND sr.Processed = 0";

        Connection conn = null;
        PreparedStatement selectItemsPs = null;
        PreparedStatement updateBookPs = null;
        PreparedStatement updateOrderPs = null;
        PreparedStatement updateShortagePs = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 查询采购明细
            selectItemsPs = conn.prepareStatement(selectItemsSql);
            selectItemsPs.setInt(1, purchaseOrderId);
            rs = selectItemsPs.executeQuery();

            // 2. 遍历明细，增加库存
            updateBookPs = conn.prepareStatement(updateBookSql);
            while (rs.next()) {
                int bookId = rs.getInt("BookID");
                int quantity = rs.getInt("Quantity");

                updateBookPs.setInt(1, quantity);
                updateBookPs.setInt(2, bookId);
                int updated = updateBookPs.executeUpdate();
                if (updated != 1) {
                    throw new SQLException("Failed to increase stock for BookID=" + bookId);
                }
            }

            DBUtil.closeQuietly(rs);
            rs = null;

            // 3. 更新采购单状态
            updateOrderPs = conn.prepareStatement(updateOrderSql);
            updateOrderPs.setInt(1, purchaseOrderId);
            int orderUpdated = updateOrderPs.executeUpdate();
            if (orderUpdated != 1) {
                throw new SQLException("Failed to update status for PurchaseOrderID=" + purchaseOrderId);
            }

            // 4. 标记相关缺书记录为已处理
            // 根据采购明细中的 BookID，将匹配的未处理缺书记录标记为已处理
            updateShortagePs = conn.prepareStatement(updateShortageSql);
            updateShortagePs.setInt(1, purchaseOrderId);
            updateShortagePs.executeUpdate();
            // 注意：可能没有匹配的缺书记录，这是正常的，不抛异常

            conn.commit();
            return 1;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
            DBUtil.closeQuietly(rs);
            DBUtil.closeQuietly(selectItemsPs);
            DBUtil.closeQuietly(updateBookPs);
            DBUtil.closeQuietly(updateOrderPs);
            DBUtil.closeQuietly(updateShortagePs);
            DBUtil.closeQuietly(conn);
        }
    }

    public PurchaseOrder findById(int purchaseOrderId) {
        String sql = "SELECT POID, SupplierID, ShortageID, CreateDate, Status, TotalAmount " +
                "FROM PurchaseOrder WHERE POID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, purchaseOrderId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
    }

    public List<PurchaseOrder> findAll() {
        String sql = "SELECT POID, SupplierID, ShortageID, CreateDate, Status, TotalAmount " +
                "FROM PurchaseOrder ORDER BY POID DESC";
        List<PurchaseOrder> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return list;
    }

    private PurchaseOrder mapRow(ResultSet rs) throws SQLException {
        PurchaseOrder order = new PurchaseOrder();
        order.setPoId((Integer) rs.getObject("POID"));
        order.setSupplierId((Integer) rs.getObject("SupplierID"));
        order.setShortageId((Integer) rs.getObject("ShortageID"));

        Timestamp ts = rs.getTimestamp("CreateDate");
        if (ts != null) {
            order.setCreateDate(ts.toLocalDateTime());
        }

        order.setStatus(rs.getString("Status"));
        order.setTotalAmount(rs.getBigDecimal("TotalAmount"));
        return order;
    }
}


