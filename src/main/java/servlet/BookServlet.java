package servlet;

import dao.BookDao;
import dao.SeriesDao;
import model.Book;
import model.Series;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookServlet extends HttpServlet {
    
    private BookDao bookDao;
    
    @Override
    public void init() throws ServletException {
        super.init();
        bookDao = new BookDao();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        // 处理书籍详情页
        if (pathInfo != null && pathInfo.equals("/detail")) {
            handleBookDetail(request, response);
            return;
        }
        
        // 处理搜索请求
        handleSearch(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("addToCart".equals(action)) {
            handleAddToCart(request, response);
        } else {
            // 默认处理搜索
            handleSearch(request, response);
        }
    }
    
    /**
     * 处理搜索请求
     */
    private void handleSearch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<Book> books = new ArrayList<>();
        String searchType = "";
        String searchKeyword = "";
        
        // 获取搜索参数
        String title = request.getParameter("title");
        String keyword = request.getParameter("keyword");
        String author = request.getParameter("author");
        String publisher = request.getParameter("publisher");
        
        // 根据搜索类型调用不同的DAO方法
        if (title != null && !title.trim().isEmpty()) {
            searchType = "title";
            searchKeyword = title.trim();
            books = bookDao.searchByTitleWithRank(searchKeyword);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            searchType = "keyword";
            searchKeyword = keyword.trim();
            books = bookDao.searchByKeyword(searchKeyword);
        } else if (author != null && !author.trim().isEmpty()) {
            searchType = "author";
            searchKeyword = author.trim();
            books = bookDao.searchByAuthor(searchKeyword);
        } else if (publisher != null && !publisher.trim().isEmpty()) {
            searchType = "publisher";
            searchKeyword = publisher.trim();
            books = bookDao.findByPublisher(searchKeyword);
        } else {
            // 如果没有搜索条件，显示所有书籍（或空列表）
            books = bookDao.findAll();
        }
        
        // 获取丛书ID参数
        String seriesIdStr = request.getParameter("seriesId");
        Integer seriesId = null;
        try {
            if (seriesIdStr != null && !seriesIdStr.trim().isEmpty()) {
                seriesId = Integer.parseInt(seriesIdStr.trim());
            }
        } catch (NumberFormatException e) {
            // 忽略无效的seriesId
        }
        
        // 获取ISBN参数
        String isbn = request.getParameter("isbn");
        
        // 始终使用多条件组合搜索（支持同时使用多个条件）
        boolean hasAnyCondition = 
                (title != null && !title.trim().isEmpty() ? 1 : 0) +
                (keyword != null && !keyword.trim().isEmpty() ? 1 : 0) +
                (author != null && !author.trim().isEmpty() ? 1 : 0) +
                (publisher != null && !publisher.trim().isEmpty() ? 1 : 0) +
                (seriesId != null ? 1 : 0) +
                (isbn != null && !isbn.trim().isEmpty() ? 1 : 0) > 0;
        
        if (hasAnyCondition) {
            // 使用多条件组合搜索（所有条件使用AND逻辑）
            books = bookDao.searchByMultipleConditions(
                    title != null ? title.trim() : null,
                    keyword != null ? keyword.trim() : null,
                    author != null ? author.trim() : null,
                    publisher != null ? publisher.trim() : null,
                    seriesId,
                    isbn != null ? isbn.trim() : null
            );
        } else {
            // 如果没有搜索条件，显示所有书籍
            books = bookDao.findAll();
        }
        
        // 构建搜索类型和关键字（用于显示）
        if (title != null && !title.trim().isEmpty()) {
            searchType = "title";
            searchKeyword = title.trim();
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            searchType = "keyword";
            searchKeyword = keyword.trim();
        } else if (author != null && !author.trim().isEmpty()) {
            searchType = "author";
            searchKeyword = author.trim();
        } else if (publisher != null && !publisher.trim().isEmpty()) {
            searchType = "publisher";
            searchKeyword = publisher.trim();
        } else if (seriesId != null) {
            searchType = "series";
            searchKeyword = seriesIdStr;
        }
        
        // 将结果存入request
        request.setAttribute("books", books);
        request.setAttribute("searchType", searchType);
        request.setAttribute("searchKeyword", searchKeyword);
        // 传递所有搜索参数，以便在JSP中回显
        request.setAttribute("searchTitle", title);
        request.setAttribute("searchKeywordParam", keyword);
        request.setAttribute("searchAuthor", author);
        request.setAttribute("searchPublisher", publisher);
        request.setAttribute("searchSeriesId", seriesIdStr);
        String isbnParam = request.getParameter("isbn");
        request.setAttribute("searchIsbn", isbnParam);
        
        // 加载所有丛书列表，用于搜索页面的下拉选择
        SeriesDao seriesDao = new SeriesDao();
        request.setAttribute("allSeries", seriesDao.findAll());
        
        // 转发到bookList.jsp
        request.getRequestDispatcher("/jsp/book/bookList.jsp").forward(request, response);
    }
    
    /**
     * 处理书籍详情页
     */
    private void handleBookDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String bookIdStr = request.getParameter("bookId");
        if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
            request.setAttribute("errorMessage", "书籍ID不能为空");
            request.getRequestDispatcher("/jsp/book/bookList.jsp").forward(request, response);
            return;
        }
        
        try {
            int bookId = Integer.parseInt(bookIdStr);
            Book book = bookDao.findById(bookId);
            
            if (book == null || Boolean.FALSE.equals(book.getActive())) {
                request.setAttribute("errorMessage", "未找到指定的书籍，或该书已下架");
                request.getRequestDispatcher("/jsp/book/bookList.jsp").forward(request, response);
                return;
            }
            
            // 获取作者和关键词
            List<model.Author> authors = bookDao.findAuthorsByBookId(bookId);
            List<model.Keyword> keywords = bookDao.findKeywordsByBookId(bookId);
            
            // 获取供应商信息
            dao.BookSupplierDao bookSupplierDao = new dao.BookSupplierDao();
            List<model.BookSupplier> bookSuppliers = bookSupplierDao.findByBookId(bookId);
            
            // 获取丛书信息
            if (book.getSeriesId() != null) {
                SeriesDao seriesDao = new SeriesDao();
                Series series = seriesDao.findById(book.getSeriesId());
                request.setAttribute("series", series);
                
                // 获取同丛书的其他书籍
                List<Book> sameSeriesBooks = bookDao.findBySeriesId(book.getSeriesId());
                // 排除当前书籍
                sameSeriesBooks.removeIf(b -> b.getBookId().equals(book.getBookId()));
                request.setAttribute("sameSeriesBooks", sameSeriesBooks);
            }
            
            request.setAttribute("book", book);
            request.setAttribute("authors", authors);
            request.setAttribute("keywords", keywords);
            request.setAttribute("bookSuppliers", bookSuppliers);
            
            // 转发到bookDetail.jsp
            request.getRequestDispatcher("/jsp/book/bookDetail.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "无效的书籍ID");
            request.getRequestDispatcher("/jsp/book/bookList.jsp").forward(request, response);
        }
    }
    
    /**
     * 处理加入购物车操作
     */
    private void handleAddToCart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String bookIdStr = request.getParameter("bookId");
        HttpSession session = request.getSession();
        
        if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "书籍ID不能为空");
            response.sendRedirect(request.getContextPath() + "/book");
            return;
        }
        
        try {
            int bookId = Integer.parseInt(bookIdStr);
            Book book = bookDao.findById(bookId);
            
            if (book == null || Boolean.FALSE.equals(book.getActive())) {
                session.setAttribute("errorMessage", "未找到指定的书籍，或该书已下架");
                response.sendRedirect(request.getContextPath() + "/book");
                return;
            }
            
            // 获取购物车（从session）
            @SuppressWarnings("unchecked")
            List<model.CartItem> cart = (List<model.CartItem>) session.getAttribute("cart");
            if (cart == null) {
                cart = new ArrayList<>();
                session.setAttribute("cart", cart);
            }
            
            // 检查购物车中是否已有该书籍
            boolean found = false;
            for (model.CartItem item : cart) {
                if (item.getBookId() == bookId) {
                    item.setQuantity(item.getQuantity() + 1);
                    found = true;
                    break;
                }
            }
            
            // 如果购物车中没有，添加新项
            if (!found) {
                model.CartItem item = new model.CartItem();
                item.setBookId(bookId);
                item.setTitle(book.getTitle());
                item.setPrice(book.getPrice());
                item.setQuantity(1);
                cart.add(item);
            }
            
            session.setAttribute("successMessage", "《" + book.getTitle() + "》已加入购物车");
            response.sendRedirect(request.getContextPath() + "/book");
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的书籍ID");
            response.sendRedirect(request.getContextPath() + "/book");
        }
    }
}

