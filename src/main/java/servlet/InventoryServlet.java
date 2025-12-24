package servlet;

import dao.BookDao;
import dao.BookSupplierDao;
import dao.ShortageRecordDao;
import model.Book;
import model.BookSupplier;
import model.ShortageRecord;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员端 - 库存管理 / 缺书登记（按库存阈值）
 */
public class InventoryServlet extends HttpServlet {

    private static final int DEFAULT_THRESHOLD = 10;

    private final BookDao bookDao = new BookDao();
    private final BookSupplierDao bookSupplierDao = new BookSupplierDao();
    private final ShortageRecordDao shortageRecordDao = new ShortageRecordDao();

    public static class InventoryRow {
        private final Integer bookId;
        private final String title;
        private final String publisher;
        private final Integer stockQuantity;
        private final int minStock;
        private final boolean lowStock;
        private final boolean shortageExists;
        private final int shortageQty;

        public InventoryRow(Integer bookId, String title, String publisher, Integer stockQuantity,
                            int minStock, boolean lowStock, boolean shortageExists, int shortageQty) {
            this.bookId = bookId;
            this.title = title;
            this.publisher = publisher;
            this.stockQuantity = stockQuantity;
            this.minStock = minStock;
            this.lowStock = lowStock;
            this.shortageExists = shortageExists;
            this.shortageQty = shortageQty;
        }

        public Integer getBookId() {
            return bookId;
        }

        public String getTitle() {
            return title;
        }

        public String getPublisher() {
            return publisher;
        }

        public Integer getStockQuantity() {
            return stockQuantity;
        }

        public int getMinStock() {
            return minStock;
        }

        public boolean isLowStock() {
            return lowStock;
        }

        public boolean isShortageExists() {
            return shortageExists;
        }

        public int getShortageQty() {
            return shortageQty;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || "/".equals(path) || "/list".equals(path)) {
            handleList(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if ("/generateShortage".equals(path)) {
            handleGenerateShortage(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int threshold = parseIntOrDefault(request.getParameter("threshold"), DEFAULT_THRESHOLD);
        if (threshold < 0) threshold = DEFAULT_THRESHOLD;

        List<Book> books = bookDao.findAll();
        List<InventoryRow> rows = new ArrayList<>();

        for (Book b : books) {
            Integer stock = b.getStockQuantity();
            int stockVal = stock == null ? 0 : stock;
            boolean low = stockVal < threshold;
            int shortageQty = Math.max(0, threshold - stockVal);
            boolean exists = false;
            if (b.getBookId() != null) {
                exists = shortageRecordDao.existsUnprocessedByBookId(b.getBookId());
            }

            // 自动生成缺书记录：低于阈值且不存在未处理记录
            if (low && !exists && b.getBookId() != null) {
                int generatedId = createShortageRecord(b.getBookId(), shortageQty, "THRESHOLD");
                if (generatedId > 0) {
                    exists = true;
                }
            }
            rows.add(new InventoryRow(
                    b.getBookId(),
                    b.getTitle(),
                    b.getPublisher(),
                    b.getStockQuantity(),
                    threshold,
                    low,
                    exists,
                    shortageQty
            ));
        }

        request.setAttribute("threshold", threshold);
        request.setAttribute("inventoryRows", rows);
        request.getRequestDispatcher("/jsp/admin/inventoryList.jsp").forward(request, response);
    }

    private void handleGenerateShortage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int threshold = parseIntOrDefault(request.getParameter("threshold"), DEFAULT_THRESHOLD);
        int bookId = parseIntOrDefault(request.getParameter("bookId"), -1);
        int manualQty = parseIntOrDefault(request.getParameter("shortageQty"), -1);

        if (bookId <= 0) {
            request.getSession().setAttribute("errorMessage", "BookID 无效");
            response.sendRedirect(request.getContextPath() + "/admin/inventory/list?threshold=" + threshold);
            return;
        }

        Book book = bookDao.findById(bookId);
        if (book == null) {
            request.getSession().setAttribute("errorMessage", "图书不存在（BookID=" + bookId + "）");
            response.sendRedirect(request.getContextPath() + "/admin/inventory/list?threshold=" + threshold);
            return;
        }

        int stock = book.getStockQuantity() == null ? 0 : book.getStockQuantity();
        // 避免重复：存在任意未处理缺书记录则不再生成
        if (shortageRecordDao.existsUnprocessedByBookId(bookId)) {
            request.getSession().setAttribute("warningMessage", "该图书已存在未处理的缺书记录");
            response.sendRedirect(request.getContextPath() + "/admin/inventory/list?threshold=" + threshold);
            return;
        }

        int shortageQty;
        if (manualQty > 0) {
            shortageQty = manualQty;
        } else if (stock < threshold) {
            shortageQty = threshold - stock;
        } else {
            shortageQty = 1; // 库存充足时允许手动生成，默认 1 本
        }

        int id = createShortageRecord(bookId, shortageQty, "MANUAL_INV");
        if (id > 0) {
            request.getSession().setAttribute("successMessage", "缺书记录已生成（ShortageID=" + id + "）");
        } else {
            request.getSession().setAttribute("errorMessage", "生成缺书记录失败");
        }

        response.sendRedirect(request.getContextPath() + "/admin/inventory/list?threshold=" + threshold);
    }

    private int parseIntOrDefault(String val, int def) {
        if (val == null) return def;
        String s = val.trim();
        if (s.isEmpty()) return def;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * 公共创建缺书记录方法
     */
    private int createShortageRecord(int bookId, int shortageQty, String sourceType) {
        Integer supplierId = null;
        List<BookSupplier> suppliers = bookSupplierDao.findByBookId(bookId);
        if (!suppliers.isEmpty()) {
            supplierId = suppliers.get(0).getSupplierId();
        }

        ShortageRecord record = new ShortageRecord();
        record.setBookId(bookId);
        record.setSupplierId(supplierId);
        record.setCustomerId(null);
        record.setQuantity(shortageQty);
        record.setDate(LocalDateTime.now());
        record.setSourceType(sourceType);
        record.setProcessed(false);
        return shortageRecordDao.insert(record);
    }
}


