package service;

import dao.BookDao;
import dao.BookSupplierDao;
import dao.PurchaseItemDao;
import dao.PurchaseOrderDao;
import dao.SupplierDao;
import model.Book;
import model.BookSupplier;
import model.PurchaseItem;
import model.PurchaseOrder;
import model.Supplier;
import util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 供应商维度综合查询服务
 * 提供供应商供货一览、采购单历史等综合查询功能
 */
public class SupplierQueryService {

    /**
     * 供应商供货一览（包含图书信息、供货价、采购历史）
     */
    public static class SupplierSupplyOverview {
        private Supplier supplier;
        private List<BookSupplyInfo> bookSupplies;
        private BigDecimal totalPurchaseAmount;
        private int totalPurchaseOrders;

        public SupplierSupplyOverview() {
            this.bookSupplies = new ArrayList<>();
            this.totalPurchaseAmount = BigDecimal.ZERO;
        }

        public Supplier getSupplier() {
            return supplier;
        }

        public void setSupplier(Supplier supplier) {
            this.supplier = supplier;
        }

        public List<BookSupplyInfo> getBookSupplies() {
            return bookSupplies;
        }

        public void setBookSupplies(List<BookSupplyInfo> bookSupplies) {
            this.bookSupplies = bookSupplies;
        }

        public BigDecimal getTotalPurchaseAmount() {
            return totalPurchaseAmount;
        }

        public void setTotalPurchaseAmount(BigDecimal totalPurchaseAmount) {
            this.totalPurchaseAmount = totalPurchaseAmount;
        }

        public int getTotalPurchaseOrders() {
            return totalPurchaseOrders;
        }

        public void setTotalPurchaseOrders(int totalPurchaseOrders) {
            this.totalPurchaseOrders = totalPurchaseOrders;
        }
    }

    /**
     * 图书供货信息
     */
    public static class BookSupplyInfo {
        private Book book;
        private BigDecimal supplyPrice;
        private List<PurchaseOrderSummary> purchaseHistory;
        private int totalPurchasedQuantity;

        public BookSupplyInfo() {
            this.purchaseHistory = new ArrayList<>();
        }

        public Book getBook() {
            return book;
        }

        public void setBook(Book book) {
            this.book = book;
        }

        public BigDecimal getSupplyPrice() {
            return supplyPrice;
        }

        public void setSupplyPrice(BigDecimal supplyPrice) {
            this.supplyPrice = supplyPrice;
        }

        public List<PurchaseOrderSummary> getPurchaseHistory() {
            return purchaseHistory;
        }

        public void setPurchaseHistory(List<PurchaseOrderSummary> purchaseHistory) {
            this.purchaseHistory = purchaseHistory;
            // 计算总采购量
            this.totalPurchasedQuantity = purchaseHistory.stream()
                    .mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0)
                    .sum();
        }

        public int getTotalPurchasedQuantity() {
            return totalPurchasedQuantity;
        }
    }

    /**
     * 采购单摘要
     */
    public static class PurchaseOrderSummary {
        private Integer purchaseOrderId;
        private String status;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private java.time.LocalDateTime createDate;

        public Integer getPurchaseOrderId() {
            return purchaseOrderId;
        }

        public void setPurchaseOrderId(Integer purchaseOrderId) {
            this.purchaseOrderId = purchaseOrderId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public java.time.LocalDateTime getCreateDate() {
            return createDate;
        }

        public void setCreateDate(java.time.LocalDateTime createDate) {
            this.createDate = createDate;
        }
    }

    /**
     * 查询供应商的供货一览（包含所有供货图书及采购历史）
     * 
     * @param supplierId 供应商ID
     * @return 供应商供货一览
     */
    public SupplierSupplyOverview getSupplierSupplyOverview(int supplierId) {
        SupplierDao supplierDao = new SupplierDao();
        BookSupplierDao bookSupplierDao = new BookSupplierDao();
        BookDao bookDao = new BookDao();
        PurchaseOrderDao purchaseOrderDao = new PurchaseOrderDao();
        PurchaseItemDao purchaseItemDao = new PurchaseItemDao();

        SupplierSupplyOverview overview = new SupplierSupplyOverview();

        // 查询供应商信息
        Supplier supplier = supplierDao.findById(supplierId);
        if (supplier == null) {
            return overview;
        }
        overview.setSupplier(supplier);

        // 查询该供应商的所有供货图书
        List<BookSupplier> bookSuppliers = bookSupplierDao.findBySupplierId(supplierId);
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalOrders = 0;

        for (BookSupplier bs : bookSuppliers) {
            BookSupplyInfo supplyInfo = new BookSupplyInfo();
            supplyInfo.setSupplyPrice(bs.getSupplyPrice());

            // 查询图书信息
            Book book = bookDao.findById(bs.getBookId());
            supplyInfo.setBook(book);

            // 查询该供应商对该图书的采购历史
            List<PurchaseOrder> allPos = purchaseOrderDao.findAll();
            List<PurchaseOrderSummary> purchaseHistory = new ArrayList<>();
            for (PurchaseOrder po : allPos) {
                if (po.getSupplierId().equals(supplierId)) {
                    List<PurchaseItem> items = purchaseItemDao.findByPurchaseOrderId(po.getPoId());
                    for (PurchaseItem item : items) {
                        if (item.getBookId().equals(bs.getBookId())) {
                            PurchaseOrderSummary summary = new PurchaseOrderSummary();
                            summary.setPurchaseOrderId(po.getPoId());
                            summary.setStatus(po.getStatus());
                            summary.setQuantity(item.getQuantity());
                            summary.setUnitPrice(item.getUnitPrice());
                            summary.setAmount(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                            summary.setCreateDate(po.getCreateDate());
                            purchaseHistory.add(summary);
                            totalAmount = totalAmount.add(summary.getAmount());
                            totalOrders++;
                        }
                    }
                }
            }
            supplyInfo.setPurchaseHistory(purchaseHistory);
            overview.getBookSupplies().add(supplyInfo);
        }

        overview.setTotalPurchaseAmount(totalAmount);
        overview.setTotalPurchaseOrders(totalOrders);

        return overview;
    }

    /**
     * 查询供应商供货一览（优化版，使用SQL JOIN一次性查询）
     * 
     * @param supplierId 供应商ID
     * @return 供应商供货一览
     */
    public SupplierSupplyOverview getSupplierSupplyOverviewOptimized(int supplierId) {
        String sql = "SELECT " +
                "s.SupplierID, s.Name AS SupplierName, s.Address, s.Phone, s.ContactEmail, " +
                "bs.BookID, bs.SupplyPrice, " +
                "b.Title, b.Publisher, b.Price AS BookPrice, b.StockQuantity, " +
                "po.POID, po.Status AS POStatus, po.CreateDate, po.TotalAmount AS POTotalAmount, " +
                "pi.Quantity AS PIQuantity, pi.UnitPrice AS PIUnitPrice " +
                "FROM Supplier s " +
                "LEFT JOIN BookSupplier bs ON s.SupplierID = bs.SupplierID " +
                "LEFT JOIN Book b ON bs.BookID = b.BookID " +
                "LEFT JOIN PurchaseOrder po ON s.SupplierID = po.SupplierID " +
                "LEFT JOIN PurchaseItem pi ON po.POID = pi.POID AND bs.BookID = pi.BookID " +
                "WHERE s.SupplierID = ? " +
                "ORDER BY bs.BookID, po.CreateDate DESC";

        SupplierSupplyOverview overview = new SupplierSupplyOverview();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, supplierId);
            rs = ps.executeQuery();

            Supplier supplier = new Supplier();
            supplier.setSupplierId(supplierId);
            supplier.setName(rs.getString("SupplierName"));
            supplier.setAddress(rs.getString("Address"));
            supplier.setPhone(rs.getString("Phone"));
            supplier.setContactEmail(rs.getString("ContactEmail"));
            overview.setSupplier(supplier);

            Integer currentBookId = null;
            BookSupplyInfo currentSupplyInfo = null;
            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalOrders = 0;

            while (rs.next()) {
                Integer bookId = (Integer) rs.getObject("BookID");
                if (bookId == null) {
                    continue;
                }

                // 新图书
                if (currentBookId == null || !currentBookId.equals(bookId)) {
                    if (currentSupplyInfo != null) {
                        overview.getBookSupplies().add(currentSupplyInfo);
                    }
                    currentSupplyInfo = new BookSupplyInfo();
                    Book book = new Book();
                    book.setBookId(bookId);
                    book.setTitle(rs.getString("Title"));
                    book.setPublisher(rs.getString("Publisher"));
                    book.setPrice(rs.getBigDecimal("BookPrice"));
                    book.setStockQuantity((Integer) rs.getObject("StockQuantity"));
                    currentSupplyInfo.setBook(book);
                    currentSupplyInfo.setSupplyPrice(rs.getBigDecimal("SupplyPrice"));
                    currentSupplyInfo.setPurchaseHistory(new ArrayList<>());
                    currentBookId = bookId;
                }

                // 采购记录
                Integer poId = (Integer) rs.getObject("POID");
                if (poId != null && currentSupplyInfo != null) {
                    PurchaseOrderSummary summary = new PurchaseOrderSummary();
                    summary.setPurchaseOrderId(poId);
                    summary.setStatus(rs.getString("POStatus"));
                    summary.setQuantity((Integer) rs.getObject("PIQuantity"));
                    summary.setUnitPrice(rs.getBigDecimal("PIUnitPrice"));
                    if (summary.getQuantity() != null && summary.getUnitPrice() != null) {
                        summary.setAmount(summary.getUnitPrice().multiply(BigDecimal.valueOf(summary.getQuantity())));
                        totalAmount = totalAmount.add(summary.getAmount());
                    }
                    java.sql.Timestamp ts = rs.getTimestamp("CreateDate");
                    if (ts != null) {
                        summary.setCreateDate(ts.toLocalDateTime());
                    }
                    currentSupplyInfo.getPurchaseHistory().add(summary);
                    totalOrders++;
                }
            }

            if (currentSupplyInfo != null) {
                overview.getBookSupplies().add(currentSupplyInfo);
            }

            overview.setTotalPurchaseAmount(totalAmount);
            overview.setTotalPurchaseOrders(totalOrders);

            // 重新计算总采购量
            for (BookSupplyInfo supplyInfo : overview.getBookSupplies()) {
                supplyInfo.setPurchaseHistory(supplyInfo.getPurchaseHistory());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }

        return overview;
    }
}

