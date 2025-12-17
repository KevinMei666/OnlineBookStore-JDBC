package dao;

import model.OrderItem;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDao {

    public List<OrderItem> findByOrderId(int orderId) {
        String sql = "SELECT OrderID, BookID, Quantity, UnitPrice, Amount FROM OrderItem WHERE OrderID = ?";
        List<OrderItem> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
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

    public List<OrderItem> findByBookId(int bookId) {
        String sql = "SELECT OrderID, BookID, Quantity, UnitPrice, Amount FROM OrderItem WHERE BookID = ?";
        List<OrderItem> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
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

    private OrderItem mapRow(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setOrderId((Integer) rs.getObject("OrderID"));
        item.setBookId((Integer) rs.getObject("BookID"));
        item.setQuantity((Integer) rs.getObject("Quantity"));
        item.setUnitPrice(rs.getBigDecimal("UnitPrice"));
        item.setAmount(rs.getBigDecimal("Amount"));
        return item;
    }
}


