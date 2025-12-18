package servlet;

import dao.BookDao;
import model.Book;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * 管理员端：图书上架/下架及信息维护
 */
@MultipartConfig
public class AdminBookServlet extends HttpServlet {

    private BookDao bookDao;

    @Override
    public void init() throws ServletException {
        super.init();
        bookDao = new BookDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || "/".equals(path) || "/list".equalsIgnoreCase(path)) {
            handleList(request, response);
        } else if ("/edit".equalsIgnoreCase(path)) {
            handleEdit(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getPathInfo();
        if (path != null && "/toggle".equalsIgnoreCase(path)) {
            handleToggle(request, response);
        } else if (path != null && "/save".equalsIgnoreCase(path)) {
            handleSave(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Book> books = bookDao.findAllIncludingInactive();
        request.setAttribute("books", books);
        request.getRequestDispatcher("/jsp/admin/bookList.jsp").forward(request, response);
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String bookIdStr = request.getParameter("bookId");
        if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
            request.getSession().setAttribute("errorMessage", "书号不能为空");
            response.sendRedirect(request.getContextPath() + "/admin/book/list");
            return;
        }
        try {
            int bookId = Integer.parseInt(bookIdStr);
            Book book = bookDao.findById(bookId);
            if (book == null) {
                request.getSession().setAttribute("errorMessage", "未找到该书籍");
                response.sendRedirect(request.getContextPath() + "/admin/book/list");
                return;
            }
            request.setAttribute("book", book);
            request.getRequestDispatcher("/jsp/admin/bookEdit.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "无效的书号");
            response.sendRedirect(request.getContextPath() + "/admin/book/list");
        }
    }

    private void handleSave(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String bookIdStr = request.getParameter("bookId");
        if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
            request.getSession().setAttribute("errorMessage", "书号不能为空");
            response.sendRedirect(request.getContextPath() + "/admin/book/list");
            return;
        }

        try {
            int bookId = Integer.parseInt(bookIdStr);
            Book book = bookDao.findById(bookId);
            if (book == null) {
                request.getSession().setAttribute("errorMessage", "未找到该书籍");
                response.sendRedirect(request.getContextPath() + "/admin/book/list");
                return;
            }

            // 基础字段更新，允许下架状态下修改
            book.setTitle(request.getParameter("title"));
            book.setPublisher(request.getParameter("publisher"));
            book.setCatalog(request.getParameter("catalog"));
            book.setLocation(request.getParameter("location"));

            String priceStr = request.getParameter("price");
            if (priceStr != null && !priceStr.trim().isEmpty()) {
                book.setPrice(new BigDecimal(priceStr.trim()));
            }
            String stockStr = request.getParameter("stockQuantity");
            if (stockStr != null && !stockStr.trim().isEmpty()) {
                book.setStockQuantity(Integer.parseInt(stockStr.trim()));
            }

            // 上下架状态
            boolean active = "on".equalsIgnoreCase(request.getParameter("active")) ||
                    "1".equals(request.getParameter("active"));
            book.setActive(active);

            // 封面上传（可选）
            Part coverPart = request.getPart("coverImage");
            if (coverPart != null && coverPart.getSize() > 0) {
                byte[] data = readAllBytes(coverPart.getInputStream(), (int) coverPart.getSize());
                book.setCoverImage(data);
            }

            int updated = bookDao.update(book);
            if (updated > 0) {
                request.getSession().setAttribute("successMessage", "书籍信息已保存");
            } else {
                request.getSession().setAttribute("errorMessage", "保存失败，请重试");
            }
            response.sendRedirect(request.getContextPath() + "/admin/book/list");
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "数值格式不正确");
            response.sendRedirect(request.getContextPath() + "/admin/book/list");
        }
    }

    private void handleToggle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String bookIdStr = request.getParameter("bookId");
        String activeStr = request.getParameter("active");

        if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
            request.getSession().setAttribute("errorMessage", "书号不能为空");
            response.sendRedirect(request.getContextPath() + "/admin/book/list");
            return;
        }

        try {
            int bookId = Integer.parseInt(bookIdStr);
            boolean active = "1".equals(activeStr) || "true".equalsIgnoreCase(activeStr) || "on".equalsIgnoreCase(activeStr);
            int updated = bookDao.updateActiveStatus(bookId, active);
            if (updated > 0) {
                request.getSession().setAttribute("successMessage", active ? "上架成功" : "下架成功");
            } else {
                request.getSession().setAttribute("errorMessage", "操作失败，可能书籍不存在");
            }
            response.sendRedirect(request.getContextPath() + "/admin/book/list");
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "无效的书号");
            response.sendRedirect(request.getContextPath() + "/admin/book/list");
        }
    }

    private byte[] readAllBytes(InputStream inputStream, int sizeHint) throws IOException {
        byte[] data = new byte[sizeHint];
        int offset = 0;
        int bytesRead;
        while ((bytesRead = inputStream.read(data, offset, data.length - offset)) != -1) {
            offset += bytesRead;
            if (offset == data.length) {
                byte[] newData = new byte[data.length * 2];
                System.arraycopy(data, 0, newData, 0, data.length);
                data = newData;
            }
        }
        byte[] result = new byte[offset];
        System.arraycopy(data, 0, result, 0, offset);
        return result;
    }
}


