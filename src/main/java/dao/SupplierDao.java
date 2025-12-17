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


