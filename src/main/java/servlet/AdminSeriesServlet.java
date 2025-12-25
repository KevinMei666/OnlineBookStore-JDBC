package servlet;

import dao.BookDao;
import dao.SeriesDao;
import model.Book;
import model.Series;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/admin/series/*")
public class AdminSeriesServlet extends HttpServlet {
    
    private SeriesDao seriesDao = new SeriesDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        HttpSession session = request.getSession();
        String currentRole = (String) session.getAttribute("currentRole");
        
        if (!"ADMIN".equals(currentRole)) {
            session.setAttribute("errorMessage", "权限不足");
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
            handleList(request, response);
        } else if (pathInfo.equals("/create")) {
            handleCreateForm(request, response);
        } else if (pathInfo.equals("/edit")) {
            handleEditForm(request, response);
        } else if (pathInfo.equals("/books")) {
            handleBooks(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        HttpSession session = request.getSession();
        String currentRole = (String) session.getAttribute("currentRole");
        
        if (!"ADMIN".equals(currentRole)) {
            session.setAttribute("errorMessage", "权限不足");
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/save")) {
            handleSave(request, response);
        } else if (pathInfo.equals("/delete")) {
            handleDelete(request, response);
        } else if (pathInfo.equals("/books/add")) {
            handleAddBook(request, response);
        } else if (pathInfo.equals("/books/remove")) {
            handleRemoveBook(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * 显示丛书列表
     */
    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("seriesList", seriesDao.findAll());
        request.getRequestDispatcher("/jsp/admin/seriesList.jsp").forward(request, response);
    }
    
    /**
     * 显示创建表单
     */
    private void handleCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("isNew", true);
        request.getRequestDispatcher("/jsp/admin/seriesEdit.jsp").forward(request, response);
    }
    
    /**
     * 显示编辑表单
     */
    private void handleEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String seriesIdStr = request.getParameter("seriesId");
        if (seriesIdStr == null || seriesIdStr.trim().isEmpty()) {
            request.getSession().setAttribute("errorMessage", "丛书ID不能为空");
            response.sendRedirect(request.getContextPath() + "/admin/series/list");
            return;
        }
        
        try {
            int seriesId = Integer.parseInt(seriesIdStr);
            Series series = seriesDao.findById(seriesId);
            if (series == null) {
                request.getSession().setAttribute("errorMessage", "未找到该丛书");
                response.sendRedirect(request.getContextPath() + "/admin/series/list");
                return;
            }
            request.setAttribute("series", series);
            request.setAttribute("isNew", false);
            request.getRequestDispatcher("/jsp/admin/seriesEdit.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "无效的丛书ID");
            response.sendRedirect(request.getContextPath() + "/admin/series/list");
        }
    }
    
    /**
     * 保存丛书（创建或更新）
     */
    private void handleSave(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        
        String seriesIdStr = request.getParameter("seriesId");
        String seriesName = request.getParameter("seriesName");
        String description = request.getParameter("description");
        String publisher = request.getParameter("publisher");
        
        if (seriesName == null || seriesName.trim().isEmpty()) {
            session.setAttribute("errorMessage", "丛书名称不能为空");
            response.sendRedirect(request.getContextPath() + "/admin/series/list");
            return;
        }
        
        boolean isNew = (seriesIdStr == null || seriesIdStr.trim().isEmpty());
        Series series;
        
        if (isNew) {
            series = new Series();
            series.setCreateDate(LocalDateTime.now());
        } else {
            try {
                int seriesId = Integer.parseInt(seriesIdStr);
                series = seriesDao.findById(seriesId);
                if (series == null) {
                    session.setAttribute("errorMessage", "未找到该丛书");
                    response.sendRedirect(request.getContextPath() + "/admin/series/list");
                    return;
                }
            } catch (NumberFormatException e) {
                session.setAttribute("errorMessage", "无效的丛书ID");
                response.sendRedirect(request.getContextPath() + "/admin/series/list");
                return;
            }
        }
        
        series.setSeriesName(seriesName.trim());
        series.setDescription(description != null && !description.trim().isEmpty() ? description.trim() : null);
        series.setPublisher(publisher != null && !publisher.trim().isEmpty() ? publisher.trim() : null);
        
        int result;
        if (isNew) {
            result = seriesDao.insert(series);
        } else {
            result = seriesDao.update(series);
        }
        
        if (result > 0) {
            session.setAttribute("successMessage", isNew ? "丛书创建成功" : "丛书更新成功");
        } else {
            session.setAttribute("errorMessage", isNew ? "丛书创建失败" : "丛书更新失败");
        }
        
        response.sendRedirect(request.getContextPath() + "/admin/series/list");
    }
    
    /**
     * 删除丛书
     */
    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        
        String seriesIdStr = request.getParameter("seriesId");
        if (seriesIdStr == null || seriesIdStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "丛书ID不能为空");
            response.sendRedirect(request.getContextPath() + "/admin/series/list");
            return;
        }
        
        try {
            int seriesId = Integer.parseInt(seriesIdStr);
            // 检查是否有书籍使用该丛书
            int bookCount = seriesDao.countBooksBySeriesId(seriesId);
            if (bookCount > 0) {
                session.setAttribute("errorMessage", "该丛书下还有 " + bookCount + " 本书籍，无法删除");
                response.sendRedirect(request.getContextPath() + "/admin/series/list");
                return;
            }
            
            int result = seriesDao.delete(seriesId);
            if (result > 0) {
                session.setAttribute("successMessage", "丛书删除成功");
            } else {
                session.setAttribute("errorMessage", "丛书删除失败");
            }
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的丛书ID");
        }
        
        response.sendRedirect(request.getContextPath() + "/admin/series/list");
    }
    
    /**
     * 显示丛书书籍管理页面
     */
    private void handleBooks(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String seriesIdStr = request.getParameter("seriesId");
        if (seriesIdStr == null || seriesIdStr.trim().isEmpty()) {
            request.getSession().setAttribute("errorMessage", "丛书ID不能为空");
            response.sendRedirect(request.getContextPath() + "/admin/series/list");
            return;
        }
        
        try {
            int seriesId = Integer.parseInt(seriesIdStr);
            Series series = seriesDao.findById(seriesId);
            if (series == null) {
                request.getSession().setAttribute("errorMessage", "未找到该丛书");
                response.sendRedirect(request.getContextPath() + "/admin/series/list");
                return;
            }
            
            BookDao bookDao = new BookDao();
            // 获取该丛书下的所有书籍（包括已下架的）
            List<Book> allBooks = bookDao.findAllIncludingInactive();
            List<Book> seriesBooks = new java.util.ArrayList<>();
            for (Book book : allBooks) {
                if (book.getSeriesId() != null && book.getSeriesId().equals(seriesId)) {
                    seriesBooks.add(book);
                }
            }
            
            request.setAttribute("series", series);
            request.setAttribute("seriesBooks", seriesBooks);
            request.setAttribute("allBooks", allBooks);
            request.getRequestDispatcher("/jsp/admin/seriesBooks.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "无效的丛书ID");
            response.sendRedirect(request.getContextPath() + "/admin/series/list");
        }
    }
    
    /**
     * 添加书籍到丛书
     */
    private void handleAddBook(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        
        String seriesIdStr = request.getParameter("seriesId");
        String bookIdStr = request.getParameter("bookId");
        
        if (seriesIdStr == null || seriesIdStr.trim().isEmpty() || 
            bookIdStr == null || bookIdStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "丛书ID和书籍ID不能为空");
            response.sendRedirect(request.getContextPath() + "/admin/series/list");
            return;
        }
        
        try {
            int seriesId = Integer.parseInt(seriesIdStr);
            int bookId = Integer.parseInt(bookIdStr);
            
            Series series = seriesDao.findById(seriesId);
            if (series == null) {
                session.setAttribute("errorMessage", "未找到该丛书");
                response.sendRedirect(request.getContextPath() + "/admin/series/list");
                return;
            }
            
            BookDao bookDao = new BookDao();
            Book book = bookDao.findById(bookId);
            if (book == null) {
                session.setAttribute("errorMessage", "未找到该书籍");
                response.sendRedirect(request.getContextPath() + "/admin/series/books?seriesId=" + seriesId);
                return;
            }
            
            // 更新书籍的SeriesID
            book.setSeriesId(seriesId);
            int result = bookDao.update(book);
            
            if (result > 0) {
                session.setAttribute("successMessage", "已成功将书籍添加到丛书");
            } else {
                session.setAttribute("errorMessage", "添加书籍到丛书失败");
            }
            
            response.sendRedirect(request.getContextPath() + "/admin/series/books?seriesId=" + seriesId);
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的ID");
            response.sendRedirect(request.getContextPath() + "/admin/series/list");
        }
    }
    
    /**
     * 从丛书移除书籍
     */
    private void handleRemoveBook(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        
        String seriesIdStr = request.getParameter("seriesId");
        String bookIdStr = request.getParameter("bookId");
        
        if (seriesIdStr == null || seriesIdStr.trim().isEmpty() || 
            bookIdStr == null || bookIdStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "丛书ID和书籍ID不能为空");
            response.sendRedirect(request.getContextPath() + "/admin/series/list");
            return;
        }
        
        try {
            int seriesId = Integer.parseInt(seriesIdStr);
            int bookId = Integer.parseInt(bookIdStr);
            
            BookDao bookDao = new BookDao();
            Book book = bookDao.findById(bookId);
            if (book == null) {
                session.setAttribute("errorMessage", "未找到该书籍");
                response.sendRedirect(request.getContextPath() + "/admin/series/books?seriesId=" + seriesId);
                return;
            }
            
            // 将书籍的SeriesID设置为null
            book.setSeriesId(null);
            int result = bookDao.update(book);
            
            if (result > 0) {
                session.setAttribute("successMessage", "已成功从丛书移除书籍");
            } else {
                session.setAttribute("errorMessage", "从丛书移除书籍失败");
            }
            
            response.sendRedirect(request.getContextPath() + "/admin/series/books?seriesId=" + seriesId);
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的ID");
            response.sendRedirect(request.getContextPath() + "/admin/series/list");
        }
    }
}

