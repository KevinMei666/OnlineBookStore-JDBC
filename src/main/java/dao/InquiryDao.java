package dao;

import model.Inquiry;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InquiryDao {
    
    /**
     * 创建询价记录
     */
    public int insert(Inquiry inquiry) {
        String sql = "INSERT INTO Inquiry (CustomerID, BookTitle, Author, Publisher, Quantity, InquiryDate, Status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, inquiry.getCustomerId());
            ps.setString(2, inquiry.getBookTitle());
            ps.setString(3, inquiry.getAuthor());
            ps.setString(4, inquiry.getPublisher());
            ps.setInt(5, inquiry.getQuantity());
            ps.setTimestamp(6, inquiry.getInquiryDate() != null ? 
                Timestamp.valueOf(inquiry.getInquiryDate()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(7, inquiry.getStatus() != null ? inquiry.getStatus() : "PENDING");
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
     * 根据ID查找询价记录
     */
    public Inquiry findById(int inquiryId) {
        String sql = "SELECT InquiryID, CustomerID, BookTitle, Author, Publisher, Quantity, " +
                     "InquiryDate, Status, AdminResponse, ResponseDate " +
                     "FROM Inquiry WHERE InquiryID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, inquiryId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return null;
    }
    
    /**
     * 根据客户ID查找所有询价记录
     */
    public List<Inquiry> findByCustomerId(int customerId) {
        String sql = "SELECT InquiryID, CustomerID, BookTitle, Author, Publisher, Quantity, " +
                     "InquiryDate, Status, AdminResponse, ResponseDate " +
                     "FROM Inquiry WHERE CustomerID = ? ORDER BY InquiryDate DESC";
        List<Inquiry> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
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
     * 查找所有询价记录（管理员用）
     */
    public List<Inquiry> findAll() {
        String sql = "SELECT InquiryID, CustomerID, BookTitle, Author, Publisher, Quantity, " +
                     "InquiryDate, Status, AdminResponse, ResponseDate " +
                     "FROM Inquiry ORDER BY InquiryDate DESC";
        List<Inquiry> list = new ArrayList<>();
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
     * 根据状态查找询价记录
     */
    public List<Inquiry> findByStatus(String status) {
        String sql = "SELECT InquiryID, CustomerID, BookTitle, Author, Publisher, Quantity, " +
                     "InquiryDate, Status, AdminResponse, ResponseDate " +
                     "FROM Inquiry WHERE Status = ? ORDER BY InquiryDate DESC";
        List<Inquiry> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
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
     * 更新询价状态和回复
     */
    public int updateResponse(int inquiryId, String status, String adminResponse) {
        String sql = "UPDATE Inquiry SET Status = ?, AdminResponse = ?, ResponseDate = ? WHERE InquiryID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, adminResponse);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(4, inquiryId);
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
     * 删除询价记录
     */
    public int delete(int inquiryId) {
        String sql = "DELETE FROM Inquiry WHERE InquiryID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, inquiryId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }
    
    private Inquiry mapRow(ResultSet rs) throws SQLException {
        Inquiry inquiry = new Inquiry();
        inquiry.setInquiryId(rs.getInt("InquiryID"));
        inquiry.setCustomerId(rs.getInt("CustomerID"));
        inquiry.setBookTitle(rs.getString("BookTitle"));
        inquiry.setAuthor(rs.getString("Author"));
        inquiry.setPublisher(rs.getString("Publisher"));
        inquiry.setQuantity(rs.getInt("Quantity"));
        Timestamp inquiryDate = rs.getTimestamp("InquiryDate");
        if (inquiryDate != null) {
            inquiry.setInquiryDate(inquiryDate.toLocalDateTime());
        }
        inquiry.setStatus(rs.getString("Status"));
        inquiry.setAdminResponse(rs.getString("AdminResponse"));
        Timestamp responseDate = rs.getTimestamp("ResponseDate");
        if (responseDate != null) {
            inquiry.setResponseDate(responseDate.toLocalDateTime());
        }
        return inquiry;
    }
}

