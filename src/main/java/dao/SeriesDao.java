package dao;

import model.Series;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SeriesDao {
    
    /**
     * 创建丛书
     */
    public int insert(Series series) {
        String sql = "INSERT INTO Series (SeriesName, Description, Publisher, CreateDate) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, series.getSeriesName());
            ps.setString(2, series.getDescription());
            ps.setString(3, series.getPublisher());
            ps.setTimestamp(4, series.getCreateDate() != null ? 
                Timestamp.valueOf(series.getCreateDate()) : Timestamp.valueOf(LocalDateTime.now()));
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
     * 更新丛书信息
     */
    public int update(Series series) {
        String sql = "UPDATE Series SET SeriesName = ?, Description = ?, Publisher = ? WHERE SeriesID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, series.getSeriesName());
            ps.setString(2, series.getDescription());
            ps.setString(3, series.getPublisher());
            ps.setInt(4, series.getSeriesId());
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
     * 根据ID查找丛书
     */
    public Series findById(int seriesId) {
        String sql = "SELECT SeriesID, SeriesName, Description, Publisher, CreateDate FROM Series WHERE SeriesID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, seriesId);
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
     * 查找所有丛书
     */
    public List<Series> findAll() {
        String sql = "SELECT SeriesID, SeriesName, Description, Publisher, CreateDate FROM Series ORDER BY SeriesName";
        List<Series> list = new ArrayList<>();
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
     * 根据名称模糊查询丛书
     */
    public List<Series> findByNameLike(String name) {
        String sql = "SELECT SeriesID, SeriesName, Description, Publisher, CreateDate FROM Series " +
                     "WHERE SeriesName LIKE ? ORDER BY SeriesName";
        List<Series> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + name + "%");
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
     * 删除丛书
     */
    public int delete(int seriesId) {
        String sql = "DELETE FROM Series WHERE SeriesID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, seriesId);
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
     * 统计丛书中的书籍数量
     */
    public int countBooksBySeriesId(int seriesId) {
        String sql = "SELECT COUNT(*) FROM Book WHERE SeriesID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, seriesId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return 0;
    }
    
    private Series mapRow(ResultSet rs) throws SQLException {
        Series series = new Series();
        series.setSeriesId(rs.getInt("SeriesID"));
        series.setSeriesName(rs.getString("SeriesName"));
        series.setDescription(rs.getString("Description"));
        series.setPublisher(rs.getString("Publisher"));
        Timestamp createDate = rs.getTimestamp("CreateDate");
        if (createDate != null) {
            series.setCreateDate(createDate.toLocalDateTime());
        }
        return series;
    }
}

