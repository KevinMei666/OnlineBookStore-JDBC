package dao;

import model.Author;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookAuthorDao {

    public List<Author> findAuthorsByBookId(int bookId) {
        String sql = "SELECT a.AuthorID, a.Name " +
                "FROM BookAuthor ba JOIN Author a ON ba.AuthorID = a.AuthorID " +
                "WHERE ba.BookID = ? ORDER BY ba.AuthorOrder ASC";
        List<Author> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Author author = new Author();
                author.setAuthorId((Integer) rs.getObject("AuthorID"));
                author.setName(rs.getString("Name"));
                list.add(author);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return list;
    }

    public int bindAuthorsToBook(int bookId, List<Integer> authorIds) {
        if (authorIds == null) {
            throw new IllegalArgumentException("authorIds cannot be null");
        }
        if (authorIds.size() > 4) {
            throw new IllegalArgumentException("作者数量不可超过 4");
        }

        String deleteSql = "DELETE FROM BookAuthor WHERE BookID = ?";
        String insertSql = "INSERT INTO BookAuthor (BookID, AuthorID, AuthorOrder) VALUES (?, ?, ?)";

        Connection conn = null;
        PreparedStatement deletePs = null;
        PreparedStatement insertPs = null;
        int inserted = 0;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 清除原有绑定
            deletePs = conn.prepareStatement(deleteSql);
            deletePs.setInt(1, bookId);
            deletePs.executeUpdate();

            // 重新绑定（按列表顺序写入 AuthorOrder = 1..n）
            insertPs = conn.prepareStatement(insertSql);
            int order = 1;
            for (Integer authorId : authorIds) {
                insertPs.setInt(1, bookId);
                insertPs.setInt(2, authorId);
                insertPs.setInt(3, order++);
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


