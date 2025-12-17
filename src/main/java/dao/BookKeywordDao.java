package dao;

import model.Keyword;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookKeywordDao {

    public List<Integer> findKeywordIdsByBookId(int bookId) {
        String sql = "SELECT KeywordID FROM BookKeyword WHERE BookID = ?";
        List<Integer> ids = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
            rs = ps.executeQuery();
            while (rs.next()) {
                ids.add((Integer) rs.getObject("KeywordID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return ids;
    }

    public List<Keyword> findKeywordsByBookId(int bookId) {
        String sql = "SELECT k.KeywordID, k.Word " +
                "FROM BookKeyword bk JOIN Keyword k ON bk.KeywordID = k.KeywordID " +
                "WHERE bk.BookID = ?";
        List<Keyword> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Keyword keyword = new Keyword();
                keyword.setKeywordId((Integer) rs.getObject("KeywordID"));
                keyword.setWord(rs.getString("Word"));
                list.add(keyword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return list;
    }

    public int bindKeywordsToBook(int bookId, List<Integer> keywordIds) {
        if (keywordIds == null) {
            throw new IllegalArgumentException("keywordIds cannot be null");
        }
        if (keywordIds.size() > 10) {
            throw new IllegalArgumentException("关键词数量不可超过 10");
        }

        String deleteSql = "DELETE FROM BookKeyword WHERE BookID = ?";
        String insertSql = "INSERT INTO BookKeyword (BookID, KeywordID) VALUES (?, ?)";

        Connection conn = null;
        PreparedStatement deletePs = null;
        PreparedStatement insertPs = null;
        int inserted = 0;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 删除原有关联
            deletePs = conn.prepareStatement(deleteSql);
            deletePs.setInt(1, bookId);
            deletePs.executeUpdate();

            // 重新插入绑定
            insertPs = conn.prepareStatement(insertSql);
            for (Integer keywordId : keywordIds) {
                insertPs.setInt(1, bookId);
                insertPs.setInt(2, keywordId);
                inserted += insertPs.executeUpdate();
            }

            conn.commit();
            return inserted;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return 0;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
            DBUtil.closeQuietly(deletePs);
            DBUtil.closeQuietly(insertPs);
            DBUtil.closeQuietly(conn);
        }
    }
}


