package dao;

import model.ShortageRecord;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShortageRecordDao {

    public int insert(ShortageRecord record) {
        String sql = "INSERT INTO ShortageRecord " +
                "(BookID, SupplierID, CustomerID, Quantity, Date, SourceType, Processed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, record.getBookId());
            ps.setObject(2, record.getSupplierId());
            ps.setObject(3, record.getCustomerId());
            ps.setObject(4, record.getQuantity());

            LocalDateTime date = record.getDate();
            if (date != null) {
                ps.setTimestamp(5, Timestamp.valueOf(date));
            } else {
                ps.setTimestamp(5, null);
            }

            ps.setString(6, record.getSourceType());

            Boolean processed = record.getProcessed();
            if (processed != null) {
                ps.setBoolean(7, processed);
            } else {
                ps.setNull(7, Types.TINYINT);
            }

            ps.executeUpdate();
            
            // 获取生成的 ShortageID
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                record.setShortageId(generatedId);
                return generatedId;
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(rs);
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    public ShortageRecord findById(int shortageId) {
        String sql = "SELECT ShortageID, BookID, SupplierID, CustomerID, Quantity, Date, SourceType, Processed " +
                "FROM ShortageRecord WHERE ShortageID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, shortageId);
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

    public List<ShortageRecord> findUnprocessed() {
        String sql = "SELECT ShortageID, BookID, SupplierID, CustomerID, Quantity, Date, SourceType, Processed " +
                "FROM ShortageRecord WHERE Processed = 0 ORDER BY Date DESC";
        List<ShortageRecord> list = new ArrayList<>();
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

    public List<ShortageRecord> findAll() {
        String sql = "SELECT ShortageID, BookID, SupplierID, CustomerID, Quantity, Date, SourceType, Processed " +
                "FROM ShortageRecord ORDER BY Date DESC";
        List<ShortageRecord> list = new ArrayList<>();
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

    public int updateStatus(int shortageId, String status) {
        // 将字符串状态映射为布尔处理标记：非空且等于 "processed"（忽略大小写）视为已处理
        boolean processed = status != null && "processed".equalsIgnoreCase(status);
        String sql = "UPDATE ShortageRecord SET Processed = ? WHERE ShortageID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setBoolean(1, processed);
            ps.setInt(2, shortageId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    private ShortageRecord mapRow(ResultSet rs) throws SQLException {
        ShortageRecord record = new ShortageRecord();
        record.setShortageId((Integer) rs.getObject("ShortageID"));
        record.setBookId((Integer) rs.getObject("BookID"));
        record.setSupplierId((Integer) rs.getObject("SupplierID"));
        record.setCustomerId((Integer) rs.getObject("CustomerID"));
        record.setQuantity((Integer) rs.getObject("Quantity"));

        Timestamp ts = rs.getTimestamp("Date");
        if (ts != null) {
            record.setDate(ts.toLocalDateTime());
        }

        record.setSourceType(rs.getString("SourceType"));
        Object processedObj = rs.getObject("Processed");
        if (processedObj == null) {
            record.setProcessed(null);
        } else {
            // TINYINT 在 JDBC 中一般映射为 Integer 或 Byte，这里统一转换为 Boolean
            int processedInt = ((Number) processedObj).intValue();
            record.setProcessed(processedInt != 0);
        }
        return record;
    }
}


