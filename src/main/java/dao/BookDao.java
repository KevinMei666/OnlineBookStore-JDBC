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

    private static final String BASE_COLUMNS = "BookID, ISBN, Title, Publisher, Price, Catalog, CoverImage, " +
            "StockQuantity, SeriesID, Location, IsActive";
    private static final String BASE_COLUMNS_WITH_ALIAS = "b.BookID, b.ISBN, b.Title, b.Publisher, b.Price, b.Catalog, b.CoverImage, " +
            "b.StockQuantity, b.SeriesID, b.Location, b.IsActive";
    private static final String ACTIVE_CONDITION = "COALESCE(IsActive, 1) = 1";

    public int insert(Book book) {
        String sql = "INSERT INTO Book (BookID, Title, Publisher, Price, Catalog, CoverImage, " +
                "StockQuantity, SeriesID, Location, IsActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            ps.setBoolean(10, book.getActive() == null ? true : book.getActive());
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
        String sql = "UPDATE Book SET ISBN = ?, Title = ?, Publisher = ?, Price = ?, Catalog = ?, " +
                "CoverImage = ?, StockQuantity = ?, SeriesID = ?, Location = ?, IsActive = ? WHERE BookID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getPublisher());
            ps.setBigDecimal(4, book.getPrice());
            ps.setString(5, book.getCatalog());
            ps.setBytes(6, book.getCoverImage());
            ps.setObject(7, book.getStockQuantity());
            ps.setObject(8, book.getSeriesId());
            ps.setString(9, book.getLocation());
            ps.setBoolean(10, book.getActive() == null ? true : book.getActive());
            ps.setObject(11, book.getBookId());
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
        String sql = "SELECT " + BASE_COLUMNS + " FROM Book WHERE BookID = ?";
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

    /**
     * 前台使用：只查已上架的书
     */
    public List<Book> findAll() {
        String sql = "SELECT " + BASE_COLUMNS + " FROM Book WHERE " + ACTIVE_CONDITION + " ORDER BY BookID";
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

    /**
     * 后台列表：包含未上架
     */
    public List<Book> findAllIncludingInactive() {
        String sql = "SELECT " + BASE_COLUMNS + " FROM Book ORDER BY BookID";
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

    /**
     * 低库存（预警）书目数量：StockQuantity < threshold（NULL 按 0 处理）
     */
    public int countLowStock(int threshold) {
        String sql = "SELECT COUNT(*) FROM Book WHERE COALESCE(StockQuantity, 0) < ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, threshold);
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
        String sql = "SELECT " + BASE_COLUMNS + " FROM Book WHERE Title LIKE ? AND " + ACTIVE_CONDITION + " ORDER BY BookID";
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
        String sql = "SELECT " + BASE_COLUMNS + " " +
                "FROM Book " +
                "WHERE " + ACTIVE_CONDITION + " AND Title LIKE CONCAT('%', ?, '%') " +
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
        String sql = "SELECT DISTINCT " + BASE_COLUMNS_WITH_ALIAS +
                " FROM Book b " +
                "JOIN BookKeyword bk ON b.BookID = bk.BookID " +
                "JOIN Keyword k ON bk.KeywordID = k.KeywordID " +
                "WHERE " + activeCondition("b") + " AND k.Word LIKE ? " +
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
        String sql = "SELECT DISTINCT " + BASE_COLUMNS_WITH_ALIAS +
                " FROM Book b " +
                "JOIN BookAuthor ba ON b.BookID = ba.BookID " +
                "JOIN Author a ON ba.AuthorID = a.AuthorID " +
                "WHERE " + activeCondition("b") + " AND a.Name LIKE ? " +
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
        String sql = "SELECT " + BASE_COLUMNS + " FROM Book WHERE " + ACTIVE_CONDITION + " AND Publisher LIKE ? ORDER BY BookID";
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

    /**
     * 多条件组合搜索（支持书名、关键字、作者、出版社、丛书的组合，使用AND逻辑）
     * @param title 书名（可为空）
     * @param keyword 关键字（可为空）
     * @param author 作者（可为空）
     * @param publisher 出版社（可为空）
     * @param seriesId 丛书ID（可为空）
     * @return 符合条件的书籍列表
     */
    public List<Book> searchByMultipleConditions(String title, String keyword, String author, String publisher, Integer seriesId, String isbn) {
        List<String> conditions = new ArrayList<>();
        List<String> joins = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        
        // 基础条件：只查询活跃的书籍
        String baseCondition = activeCondition("b");
        
        // ISBN条件
        if (isbn != null && !isbn.trim().isEmpty()) {
            conditions.add("b.ISBN LIKE ?");
            params.add("%" + isbn.trim() + "%");
        }
        
        // 书名条件
        if (title != null && !title.trim().isEmpty()) {
            conditions.add("b.Title LIKE ?");
            params.add("%" + title.trim() + "%");
        }
        
        // 关键字条件（需要JOIN）
        boolean needKeywordJoin = false;
        if (keyword != null && !keyword.trim().isEmpty()) {
            needKeywordJoin = true;
            conditions.add("k.Word LIKE ?");
            params.add("%" + keyword.trim() + "%");
        }
        
        // 作者条件（需要JOIN）
        boolean needAuthorJoin = false;
        if (author != null && !author.trim().isEmpty()) {
            needAuthorJoin = true;
            conditions.add("a.Name LIKE ?");
            params.add("%" + author.trim() + "%");
        }
        
        // 出版社条件
        if (publisher != null && !publisher.trim().isEmpty()) {
            conditions.add("b.Publisher LIKE ?");
            params.add("%" + publisher.trim() + "%");
        }
        
        // 丛书条件
        if (seriesId != null) {
            conditions.add("b.SeriesID = ?");
            params.add(seriesId);
        }
        
        // 如果没有搜索条件，返回所有书籍
        if (conditions.isEmpty()) {
            return findAll();
        }
        
        // 构建JOIN子句
        if (needKeywordJoin) {
            joins.add("JOIN BookKeyword bk ON b.BookID = bk.BookID");
            joins.add("JOIN Keyword k ON bk.KeywordID = k.KeywordID");
        }
        if (needAuthorJoin) {
            joins.add("JOIN BookAuthor ba ON b.BookID = ba.BookID");
            joins.add("JOIN Author a ON ba.AuthorID = a.AuthorID");
        }
        
        // 构建SQL
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT DISTINCT ").append(BASE_COLUMNS_WITH_ALIAS);
        sqlBuilder.append(" FROM Book b");
        if (!joins.isEmpty()) {
            sqlBuilder.append(" ").append(String.join(" ", joins));
        }
        sqlBuilder.append(" WHERE ").append(baseCondition);
        for (String condition : conditions) {
            sqlBuilder.append(" AND ").append(condition);
        }
        sqlBuilder.append(" ORDER BY b.BookID");
        
        String sql = sqlBuilder.toString();
        List<Book> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
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

    /**
     * 根据丛书ID查询书籍
     */
    public List<Book> findBySeriesId(int seriesId) {
        String sql = "SELECT " + BASE_COLUMNS + " FROM Book WHERE " + ACTIVE_CONDITION + 
                     " AND SeriesID = ? ORDER BY BookID";
        List<Book> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, seriesId);
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
     * 上下架状态切换
     */
    public int updateActiveStatus(int bookId, boolean active) {
        String sql = "UPDATE Book SET IsActive = ? WHERE BookID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setBoolean(1, active);
            ps.setInt(2, bookId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookId((Integer) rs.getObject("BookID"));
        book.setIsbn(rs.getString("ISBN"));
        book.setTitle(rs.getString("Title"));
        book.setPublisher(rs.getString("Publisher"));
        book.setPrice(rs.getBigDecimal("Price"));
        book.setCatalog(rs.getString("Catalog"));
        book.setCoverImage(rs.getBytes("CoverImage"));
        book.setStockQuantity((Integer) rs.getObject("StockQuantity"));
        book.setSeriesId((Integer) rs.getObject("SeriesID"));
        book.setLocation(rs.getString("Location"));
        book.setActive((Boolean) rs.getObject("IsActive"));
        return book;
    }

    private String activeCondition(String alias) {
        if (alias == null || alias.isEmpty()) {
            return ACTIVE_CONDITION;
        }
        return "COALESCE(" + alias + ".IsActive, 1) = 1";
    }
}


