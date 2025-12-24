package dao;

import model.BookSupplier;
import model.SupplierSupply;
import util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookSupplierDao {

    /**
     * 绑定（或更新）某本书与某个供应商的供货价。
     * 对于已存在的 (BookID, SupplierID) 记录，更新 SupplyPrice。
     */
    public int bindSupplierToBook(int bookId, int supplierId, BigDecimal supplyPrice) {
        String sql = "INSERT INTO BookSupplier (BookID, SupplierID, SupplyPrice) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE SupplyPrice = VALUES(SupplyPrice)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
            ps.setInt(2, supplierId);
            ps.setBigDecimal(3, supplyPrice);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    /**
     * 查询某本书的所有供应商及供货价。
     */
    public List<BookSupplier> findByBookId(int bookId) {
        String sql = "SELECT BookID, SupplierID, SupplyPrice FROM BookSupplier WHERE BookID = ?";
        List<BookSupplier> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
            rs = ps.executeQuery();
            while (rs.next()) {
                BookSupplier bs = new BookSupplier();
                bs.setBookId((Integer) rs.getObject("BookID"));
                bs.setSupplierId((Integer) rs.getObject("SupplierID"));
                bs.setSupplyPrice(rs.getBigDecimal("SupplyPrice"));
                list.add(bs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return list;
    }

    /**
     * 查询某个供应商的所有供货图书及供货价。
     */
    public List<BookSupplier> findBySupplierId(int supplierId) {
        String sql = "SELECT BookID, SupplierID, SupplyPrice FROM BookSupplier WHERE SupplierID = ?";
        List<BookSupplier> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, supplierId);
            rs = ps.executeQuery();
            while (rs.next()) {
                BookSupplier bs = new BookSupplier();
                bs.setBookId((Integer) rs.getObject("BookID"));
                bs.setSupplierId((Integer) rs.getObject("SupplierID"));
                bs.setSupplyPrice(rs.getBigDecimal("SupplyPrice"));
                list.add(bs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return list;
    }

    /**
     * 查询某个供应商供应的书目（带书名/出版社/零售价/库存/供货价）
     */
    public List<SupplierSupply> findSupplyBooksBySupplierId(int supplierId) {
        String sql = "SELECT bs.SupplierID, b.BookID, b.Title, b.Publisher, b.Price AS RetailPrice, " +
                "b.StockQuantity, bs.SupplyPrice " +
                "FROM BookSupplier bs " +
                "JOIN Book b ON bs.BookID = b.BookID " +
                "WHERE bs.SupplierID = ? " +
                "ORDER BY b.BookID";

        List<SupplierSupply> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, supplierId);
            rs = ps.executeQuery();
            while (rs.next()) {
                SupplierSupply s = new SupplierSupply();
                s.setSupplierId((Integer) rs.getObject("SupplierID"));
                s.setBookId((Integer) rs.getObject("BookID"));
                s.setTitle(rs.getString("Title"));
                s.setPublisher(rs.getString("Publisher"));
                s.setRetailPrice(rs.getBigDecimal("RetailPrice"));
                s.setStockQuantity((Integer) rs.getObject("StockQuantity"));
                s.setSupplyPrice(rs.getBigDecimal("SupplyPrice"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return list;
    }

    /**
     * 删除某个供应商的所有供货记录
     */
    public int deleteBySupplierId(int supplierId) {
        String sql = "DELETE FROM BookSupplier WHERE SupplierID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, supplierId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }
}


