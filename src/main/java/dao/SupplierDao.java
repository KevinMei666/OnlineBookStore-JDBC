package dao;

import model.Supplier;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SupplierDao {

    public Supplier findById(int supplierId) {
        String sql = "SELECT SupplierID, Name, Address, Phone, ContactEmail, CreatedAt " +
                "FROM Supplier WHERE SupplierID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, supplierId);
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

    public List<Supplier> findAll() {
        String sql = "SELECT SupplierID, Name, Address, Phone, ContactEmail, CreatedAt " +
                "FROM Supplier ORDER BY SupplierID";
        List<Supplier> list = new ArrayList<>();
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

    /**
     * 管理员端查询：支持按 SupplierID 精确匹配、按 Name 模糊匹配
     */
    public List<Supplier> queryForAdmin(String supplierId, String name) {
        StringBuilder sql = new StringBuilder(
                "SELECT SupplierID, Name, Address, Phone, ContactEmail, CreatedAt FROM Supplier WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (supplierId != null && !supplierId.trim().isEmpty()) {
            sql.append(" AND SupplierID = ?");
            try {
                params.add(Integer.parseInt(supplierId.trim()));
            } catch (NumberFormatException e) {
                // 如果不是数字则忽略该条件
            }
        }
        if (name != null && !name.trim().isEmpty()) {
            sql.append(" AND Name LIKE ?");
            params.add("%" + name.trim() + "%");
        }
        sql.append(" ORDER BY SupplierID");

        List<Supplier> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return list;
    }

    /**
     * 管理员端更新供应商基础信息
     */
    public int updateBasicInfo(int supplierId, String name, String address, String phone, String contactEmail) {
        String sql = "UPDATE Supplier SET Name = ?, Address = ?, Phone = ?, ContactEmail = ? WHERE SupplierID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, address);
            ps.setString(3, phone);
            ps.setString(4, contactEmail);
            ps.setInt(5, supplierId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setSupplierId((Integer) rs.getObject("SupplierID"));
        supplier.setName(rs.getString("Name"));
        supplier.setAddress(rs.getString("Address"));
        supplier.setPhone(rs.getString("Phone"));
        supplier.setContactEmail(rs.getString("ContactEmail"));
        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) {
            supplier.setCreatedAt(ts.toLocalDateTime());
        }
        return supplier;
    }
}


