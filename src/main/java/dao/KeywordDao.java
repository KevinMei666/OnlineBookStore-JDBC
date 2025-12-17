package dao;

import model.Keyword;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KeywordDao {

    public Keyword findById(int keywordId) {
        String sql = "SELECT KeywordID, Word FROM Keyword WHERE KeywordID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, keywordId);
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

    public Keyword findByWord(String word) {
        String sql = "SELECT KeywordID, Word FROM Keyword WHERE Word = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, word);
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

    public int insertIfNotExists(String word) {
        Keyword existing = findByWord(word);
        if (existing != null) {
            return existing.getKeywordId();
        }

        String sql = "INSERT INTO Keyword (Word) VALUES (?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, word);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Failed to retrieve generated KeywordID.");
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
    }

    private Keyword mapRow(ResultSet rs) throws SQLException {
        Keyword keyword = new Keyword();
        keyword.setKeywordId((Integer) rs.getObject("KeywordID"));
        keyword.setWord(rs.getString("Word"));
        return keyword;
    }
}


