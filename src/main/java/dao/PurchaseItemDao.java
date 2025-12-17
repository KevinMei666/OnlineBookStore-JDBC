package dao;

import model.PurchaseItem;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PurchaseItemDao {

    public int insert(PurchaseItem item) {
        String sql = "INSERT INTO PurchaseItem (POID, BookID, Quantity, UnitPrice) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setObject(1, item.getPurchaseOrderId());
            ps.setObject(2, item.getBookId());
            ps.setObject(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitPrice());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    public List<PurchaseItem> findByPurchaseOrderId(int purchaseOrderId) {
        String sql = "SELECT POID, BookID, Quantity, UnitPrice FROM PurchaseItem WHERE POID = ?";
        List<PurchaseItem> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, purchaseOrderId);
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

    private PurchaseItem mapRow(ResultSet rs) throws SQLException {
        PurchaseItem item = new PurchaseItem();
        item.setPurchaseOrderId((Integer) rs.getObject("POID"));
        item.setBookId((Integer) rs.getObject("BookID"));
        item.setQuantity((Integer) rs.getObject("Quantity"));
        item.setUnitPrice(rs.getBigDecimal("UnitPrice"));
        return item;
    }
}


