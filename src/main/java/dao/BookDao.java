package dao;

import model.Author;
import model.Book;
import model.Keyword;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDao {

    public int insert(Book book) {
        String sql = "INSERT INTO Book (BookID, Title, Publisher, Price, Catalog, CoverImage, " +
                "StockQuantity, SeriesID, Location) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setObject(1, book.getBookId());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getPublisher());
            ps.setBigDecimal(4, book.getPrice());
            ps.setString(5, book.getCatalog());
            ps.setBytes(6, book.getCoverImage());
            ps.setObject(7, book.getStockQuantity());
            ps.setObject(8, book.getSeriesId());
            ps.setString(9, book.getLocation());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    public int update(Book book) {
        String sql = "UPDATE Book SET Title = ?, Publisher = ?, Price = ?, Catalog = ?, " +
                "CoverImage = ?, StockQuantity = ?, SeriesID = ?, Location = ? WHERE BookID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getPublisher());
            ps.setBigDecimal(3, book.getPrice());
            ps.setString(4, book.getCatalog());
            ps.setBytes(5, book.getCoverImage());
            ps.setObject(6, book.getStockQuantity());
            ps.setObject(7, book.getSeriesId());
            ps.setString(8, book.getLocation());
            ps.setObject(9, book.getBookId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    public int deleteById(int bookId) {
        String sql = "DELETE FROM Book WHERE BookID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    public Book findById(int bookId) {
        String sql = "SELECT BookID, Title, Publisher, Price, Catalog, CoverImage, " +
                "StockQuantity, SeriesID, Location FROM Book WHERE BookID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
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

    public List<Book> findAll() {
        String sql = "SELECT BookID, Title, Publisher, Price, Catalog, CoverImage, " +
                "StockQuantity, SeriesID, Location FROM Book ORDER BY BookID";
        List<Book> list = new ArrayList<>();
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

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM Book";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
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

    public List<Book> findByTitleLike(String keyword) {
        String sql = "SELECT BookID, Title, Publisher, Price, Catalog, CoverImage, " +
                "StockQuantity, SeriesID, Location FROM Book WHERE Title LIKE ? ORDER BY BookID";
        List<Book> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
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

    public List<Book> searchByTitleWithRank(String keyword) {
        String sql = "SELECT BookID, Title, Publisher, Price, Catalog, CoverImage, " +
                "StockQuantity, SeriesID, Location " +
                "FROM Book " +
                "WHERE Title LIKE CONCAT('%', ?, '%') " +
                "ORDER BY CASE " +
                "    WHEN Title = ? THEN 1 " +
                "    WHEN Title LIKE CONCAT(?, '%') THEN 2 " +
                "    WHEN Title LIKE CONCAT('%', ?, '%') THEN 3 " +
                "    ELSE 4 " +
                "END, Title ASC";

        List<Book> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            ps.setString(3, keyword);
            ps.setString(4, keyword);
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
     * 按关键字查询书目（通过 BookKeyword -> Keyword）
     */
    public List<Book> searchByKeyword(String keyword) {
        String sql = "SELECT DISTINCT b.BookID, b.Title, b.Publisher, b.Price, b.Catalog, b.CoverImage, " +
                "b.StockQuantity, b.SeriesID, b.Location " +
                "FROM Book b " +
                "JOIN BookKeyword bk ON b.BookID = bk.BookID " +
                "JOIN Keyword k ON bk.KeywordID = k.KeywordID " +
                "WHERE k.Word LIKE ? " +
                "ORDER BY b.BookID";
        List<Book> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
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
     * 按作者姓名查询书目（通过 BookAuthor -> Author），匹配任意作者（第一/第二作者等）
     */
    public List<Book> searchByAuthor(String authorName) {
        String sql = "SELECT DISTINCT b.BookID, b.Title, b.Publisher, b.Price, b.Catalog, b.CoverImage, " +
                "b.StockQuantity, b.SeriesID, b.Location " +
                "FROM Book b " +
                "JOIN BookAuthor ba ON b.BookID = ba.BookID " +
                "JOIN Author a ON ba.AuthorID = a.AuthorID " +
                "WHERE a.Name LIKE ? " +
                "ORDER BY b.BookID";
        List<Book> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + authorName + "%");
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
     * 按出版社查询书目
     */
    public List<Book> findByPublisher(String publisher) {
        String sql = "SELECT BookID, Title, Publisher, Price, Catalog, CoverImage, " +
                "StockQuantity, SeriesID, Location FROM Book WHERE Publisher LIKE ? ORDER BY BookID";
        List<Book> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + publisher + "%");
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

    public List<Author> findAuthorsByBookId(int bookId) {
        BookAuthorDao bookAuthorDao = new BookAuthorDao();
        return bookAuthorDao.findAuthorsByBookId(bookId);
    }

    public List<Keyword> findKeywordsByBookId(int bookId) {
        BookKeywordDao bookKeywordDao = new BookKeywordDao();
        KeywordDao keywordDao = new KeywordDao();
        List<Integer> keywordIds = bookKeywordDao.findKeywordIdsByBookId(bookId);
        List<Keyword> keywords = new ArrayList<>();
        for (Integer keywordId : keywordIds) {
            Keyword keyword = keywordDao.findById(keywordId);
            if (keyword != null) {
                keywords.add(keyword);
            }
        }
        return keywords;
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookId((Integer) rs.getObject("BookID"));
        book.setTitle(rs.getString("Title"));
        book.setPublisher(rs.getString("Publisher"));
        book.setPrice(rs.getBigDecimal("Price"));
        book.setCatalog(rs.getString("Catalog"));
        book.setCoverImage(rs.getBytes("CoverImage"));
        book.setStockQuantity((Integer) rs.getObject("StockQuantity"));
        book.setSeriesId((Integer) rs.getObject("SeriesID"));
        book.setLocation(rs.getString("Location"));
        return book;
    }
}


