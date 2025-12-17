package servlet;

import dao.BookSupplierDao;
import dao.SupplierDao;
import dao.BookDao;
import dao.PurchaseItemDao;
import dao.PurchaseOrderDao;
import dao.ShortageRecordDao;
import model.PurchaseItem;
import model.PurchaseOrder;
import model.ShortageRecord;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class PurchaseServlet extends HttpServlet {
    
    private PurchaseOrderDao purchaseOrderDao;
    private PurchaseItemDao purchaseItemDao;
    private ShortageRecordDao shortageRecordDao;
    private BookSupplierDao bookSupplierDao;
    private SupplierDao supplierDao;
    private BookDao bookDao;
    
    @Override
    public void init() throws ServletException {
        super.init();
        purchaseOrderDao = new PurchaseOrderDao();
        purchaseItemDao = new PurchaseItemDao();
        shortageRecordDao = new ShortageRecordDao();
        bookSupplierDao = new BookSupplierDao();
        supplierDao = new SupplierDao();
        bookDao = new BookDao();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
            // 显示采购单列表
            handlePurchaseList(request, response);
        } else if (pathInfo.equals("/detail")) {
            // 显示采购单详情
            handlePurchaseDetail(request, response);
        } else if (pathInfo.equals("/create")) {
            // 显示创建采购单页面（将在后续实现）
            handleCreatePurchase(request, response);
        } else if (pathInfo.startsWith("/shortage")) {
            // 处理缺书记录相关请求
            handleShortageRequest(request, response, pathInfo);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo.equals("/receive")) {
            // 执行到货操作
            handleReceivePurchase(request, response);
        } else if (pathInfo.equals("/shortage/createPo")) {
            // 从缺书记录生成采购单
            handleCreatePoFromShortage(request, response);
        } else if (pathInfo.equals("/shortage/create")) {
            // 手动创建缺书记录
            handleCreateShortagePost(request, response);
        } else if (pathInfo.equals("/create")) {
            // 手动创建采购单
            handleCreatePurchasePost(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * 显示采购单列表
     */
    private void handlePurchaseList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 查询所有采购单
        List<PurchaseOrder> purchaseOrders = purchaseOrderDao.findAll();
        
        request.setAttribute("purchaseOrders", purchaseOrders);
        request.getRequestDispatcher("/jsp/purchase/purchaseList.jsp").forward(request, response);
    }
    
    /**
     * 显示采购单详情
     */
    private void handlePurchaseDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String poIdStr = request.getParameter("poId");
        HttpSession session = request.getSession();
        
        if (poIdStr == null || poIdStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "采购单ID不能为空");
            response.sendRedirect(request.getContextPath() + "/purchase/list");
            return;
        }
        
        try {
            int poId = Integer.parseInt(poIdStr);
            
            // 查询采购单信息
            PurchaseOrder purchaseOrder = purchaseOrderDao.findById(poId);
            if (purchaseOrder == null) {
                session.setAttribute("errorMessage", "未找到指定的采购单");
                response.sendRedirect(request.getContextPath() + "/purchase/list");
                return;
            }
            
            // 查询采购明细
            List<PurchaseItem> purchaseItems = purchaseItemDao.findByPurchaseOrderId(poId);
            
            // 查询关联的缺书记录
            ShortageRecord shortageRecord = null;
            if (purchaseOrder.getShortageId() != null) {
                shortageRecord = shortageRecordDao.findById(purchaseOrder.getShortageId());
            }
            
            request.setAttribute("purchaseOrder", purchaseOrder);
            request.setAttribute("purchaseItems", purchaseItems);
            request.setAttribute("shortageRecord", shortageRecord);
            
            request.getRequestDispatcher("/jsp/purchase/purchaseDetail.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的采购单ID");
            response.sendRedirect(request.getContextPath() + "/purchase/list");
        }
    }
    
    /**
     * 执行到货操作
     */
    private void handleReceivePurchase(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String poIdStr = request.getParameter("poId");
        HttpSession session = request.getSession();
        
        if (poIdStr == null || poIdStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "采购单ID不能为空");
            response.sendRedirect(request.getContextPath() + "/purchase/list");
            return;
        }
        
        try {
            int poId = Integer.parseInt(poIdStr);
            
            // 检查采购单是否存在
            PurchaseOrder purchaseOrder = purchaseOrderDao.findById(poId);
            if (purchaseOrder == null) {
                session.setAttribute("errorMessage", "未找到指定的采购单");
                response.sendRedirect(request.getContextPath() + "/purchase/list");
                return;
            }
            
            // 检查采购单状态
            if ("COMPLETED".equals(purchaseOrder.getStatus())) {
                session.setAttribute("warningMessage", "该采购单已完成，无需重复操作");
                response.sendRedirect(request.getContextPath() + "/purchase/detail?poId=" + poId);
                return;
            }
            
            // 调用DAO执行到货操作
            int result = purchaseOrderDao.receivePurchaseOrder(poId);
            
            if (result > 0) {
                session.setAttribute("successMessage", "采购到货操作成功！库存已增加，采购单状态已更新");
                response.sendRedirect(request.getContextPath() + "/purchase/detail?poId=" + poId);
            } else {
                session.setAttribute("errorMessage", "采购到货操作失败，请重试");
                response.sendRedirect(request.getContextPath() + "/purchase/detail?poId=" + poId);
            }
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的采购单ID");
            response.sendRedirect(request.getContextPath() + "/purchase/list");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "采购到货操作失败：" + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/purchase/detail?poId=" + poIdStr);
        }
    }
    
    /**
     * 显示创建采购单页面（将在后续实现）
     */
    private void handleCreatePurchase(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 加载供应商、书目列表供选择
        List<model.Supplier> suppliers = supplierDao.findAll();
        List<model.Book> books = bookDao.findAll();
        request.setAttribute("suppliers", suppliers);
        request.setAttribute("books", books);
        request.getRequestDispatcher("/jsp/purchase/purchaseCreate.jsp").forward(request, response);
    }

    /**
     * 手动创建采购单（单行明细）
     */
    private void handleCreatePurchasePost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();

        String supplierIdStr = request.getParameter("supplierId");
        String bookIdStr = request.getParameter("bookId");
        String quantityStr = request.getParameter("quantity");
        String unitPriceStr = request.getParameter("unitPrice");

        Integer supplierId = parseIntOrNull(supplierIdStr);
        Integer bookId = parseIntOrNull(bookIdStr);
        Integer quantity = parseIntOrNull(quantityStr);
        java.math.BigDecimal unitPrice = parseBigDecimalOrZero(unitPriceStr);

        if (supplierId == null || supplierId <= 0) {
            session.setAttribute("errorMessage", "请选择供应商");
            response.sendRedirect(request.getContextPath() + "/purchase/create");
            return;
        }
        if (bookId == null || bookId <= 0) {
            session.setAttribute("errorMessage", "请选择书目");
            response.sendRedirect(request.getContextPath() + "/purchase/create");
            return;
        }
        if (quantity == null || quantity <= 0) {
            session.setAttribute("errorMessage", "数量必须大于0");
            response.sendRedirect(request.getContextPath() + "/purchase/create");
            return;
        }

        java.math.BigDecimal total = unitPrice.multiply(new java.math.BigDecimal(quantity));

        PurchaseOrder order = new PurchaseOrder();
        order.setSupplierId(supplierId);
        order.setShortageId(null);
        order.setCreateDate(java.time.LocalDateTime.now());
        order.setStatus("CREATED");
        order.setTotalAmount(total);

        int poId = purchaseOrderDao.insert(order);
        if (poId <= 0) {
            session.setAttribute("errorMessage", "创建采购单失败");
            response.sendRedirect(request.getContextPath() + "/purchase/create");
            return;
        }

        PurchaseItem item = new PurchaseItem();
        item.setPurchaseOrderId(poId);
        item.setBookId(bookId);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        purchaseItemDao.insert(item);

        session.setAttribute("successMessage", "采购单创建成功，ID=" + poId);
        response.sendRedirect(request.getContextPath() + "/purchase/detail?poId=" + poId);
    }
    
    /**
     * 处理缺书记录相关请求
     */
    private void handleShortageRequest(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {
        
        if (pathInfo.equals("/shortage/list")) {
            // 显示缺书记录列表
            handleShortageList(request, response);
        } else if (pathInfo.equals("/shortage/detail")) {
            // 显示缺书记录详情（将在后续实现）
            handleShortageDetail(request, response);
        } else if (pathInfo.equals("/shortage/create")) {
            // 显示创建缺书记录页面
            loadCreateShortagePage(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * 显示缺书记录列表
     */
    private void handleShortageList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 查询所有缺书记录（JSP页面会进行筛选）
        List<ShortageRecord> shortageRecords = shortageRecordDao.findAll();
        
        request.setAttribute("shortageRecords", shortageRecords);
        request.getRequestDispatcher("/jsp/purchase/shortageList.jsp").forward(request, response);
    }
    
    /**
     * 显示创建缺书记录页面
     */
    private void loadCreateShortagePage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<model.Book> books = bookDao.findAll();
        List<model.Supplier> suppliers = supplierDao.findAll();
        request.setAttribute("books", books);
        request.setAttribute("suppliers", suppliers);
        request.getRequestDispatcher("/jsp/purchase/shortageCreate.jsp").forward(request, response);
    }

    /**
     * 手动创建缺书记录
     */
    private void handleCreateShortagePost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();

        Integer bookId = parseIntOrNull(request.getParameter("bookId"));
        Integer supplierId = parseIntOrNull(request.getParameter("supplierId"));
        Integer quantity = parseIntOrNull(request.getParameter("quantity"));
        String sourceType = request.getParameter("sourceType");

        if (bookId == null || bookId <= 0) {
            session.setAttribute("errorMessage", "请选择书目");
            response.sendRedirect(request.getContextPath() + "/purchase/shortage/create");
            return;
        }
        if (quantity == null || quantity <= 0) {
            session.setAttribute("errorMessage", "缺货数量必须大于0");
            response.sendRedirect(request.getContextPath() + "/purchase/shortage/create");
            return;
        }
        if (sourceType == null || sourceType.trim().isEmpty()) {
            sourceType = "MANUAL";
        }

        ShortageRecord record = new ShortageRecord();
        record.setBookId(bookId);
        record.setSupplierId(supplierId);
        record.setCustomerId(null);
        record.setQuantity(quantity);
        record.setDate(java.time.LocalDateTime.now());
        record.setSourceType(sourceType);
        record.setProcessed(false);

        int id = shortageRecordDao.insert(record);
        if (id > 0) {
            session.setAttribute("successMessage", "缺书记录已创建，ID=" + id);
            response.sendRedirect(request.getContextPath() + "/purchase/shortage/list");
        } else {
            session.setAttribute("errorMessage", "创建缺书记录失败");
            response.sendRedirect(request.getContextPath() + "/purchase/shortage/create");
        }
    }

    /**
     * 从缺书记录生成采购单：
     * 1. 校验缺书记录是否存在、是否未处理
     * 2. 选择供应商（优先缺书记录中的 SupplierID，否则 BookSupplier 表的首个，否则报错）
     * 3. 创建采购单（状态 CREATED，关联 ShortageID）
     * 4. 创建采购明细（BookID，Quantity=缺书数量，单价=0 默认；可后续扩展供货价）
     * 5. 标记缺书记录为已处理
     */
    private void handleCreatePoFromShortage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        String shortageIdStr = request.getParameter("shortageId");

        if (shortageIdStr == null || shortageIdStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "缺书记录ID不能为空");
            response.sendRedirect(request.getContextPath() + "/purchase/shortage/list");
            return;
        }

        int shortageId;
        try {
            shortageId = Integer.parseInt(shortageIdStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的缺书记录ID");
            response.sendRedirect(request.getContextPath() + "/purchase/shortage/list");
            return;
        }

        ShortageRecord shortage = shortageRecordDao.findById(shortageId);
        if (shortage == null) {
            session.setAttribute("errorMessage", "缺书记录不存在");
            response.sendRedirect(request.getContextPath() + "/purchase/shortage/list");
            return;
        }
        if (shortage.getProcessed() != null && shortage.getProcessed()) {
            session.setAttribute("warningMessage", "该缺书记录已处理，无需重复生成采购单");
            response.sendRedirect(request.getContextPath() + "/purchase/shortage/list");
            return;
        }
        if (shortage.getBookId() == null || shortage.getQuantity() == null) {
            session.setAttribute("errorMessage", "缺书记录缺少书籍或数量信息，无法生成采购单");
            response.sendRedirect(request.getContextPath() + "/purchase/shortage/list");
            return;
        }

        // 选择供应商：优先用缺书记录里的 SupplierID，否则从 BookSupplier 取第一个，否则报错
        Integer supplierId = shortage.getSupplierId();
        if (supplierId == null) {
            java.util.List<model.BookSupplier> suppliers = bookSupplierDao.findByBookId(shortage.getBookId());
            if (suppliers != null && !suppliers.isEmpty()) {
                supplierId = suppliers.get(0).getSupplierId();
            }
        }
        if (supplierId == null) {
            session.setAttribute("errorMessage", "没有可用的供应商，无法生成采购单");
            response.sendRedirect(request.getContextPath() + "/purchase/shortage/list");
            return;
        }

        // 构造采购单
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplierId(supplierId);
        order.setShortageId(shortageId);
        order.setCreateDate(java.time.LocalDateTime.now());
        order.setStatus("CREATED");
        order.setTotalAmount(java.math.BigDecimal.ZERO); // 简化：后续可接入供货价

        // 插入采购单（包含标记缺书为已处理的事务）
        int poId = purchaseOrderDao.insertWithShortageUpdate(order);
        if (poId <= 0) {
            session.setAttribute("errorMessage", "生成采购单失败");
            response.sendRedirect(request.getContextPath() + "/purchase/shortage/list");
            return;
        }

        // 插入采购明细：单价暂定 0（可后续接入供货价），数量=缺书数量
        PurchaseItem item = new PurchaseItem();
        item.setPurchaseOrderId(poId);
        item.setBookId(shortage.getBookId());
        item.setQuantity(shortage.getQuantity());
        item.setUnitPrice(java.math.BigDecimal.ZERO);
        purchaseItemDao.insert(item);

        session.setAttribute("successMessage", "已根据缺书记录生成采购单，ID=" + poId);
        response.sendRedirect(request.getContextPath() + "/purchase/detail?poId=" + poId);
    }

    private Integer parseIntOrNull(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private java.math.BigDecimal parseBigDecimalOrZero(String val) {
        if (val == null || val.trim().isEmpty()) return java.math.BigDecimal.ZERO;
        try {
            return new java.math.BigDecimal(val.trim());
        } catch (NumberFormatException e) {
            return java.math.BigDecimal.ZERO;
        }
    }

    /**
     * 显示缺书记录详情（将在后续实现）
     */
    private void handleShortageDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.setAttribute("infoMessage", "缺书记录详情功能开发中");
        response.sendRedirect(request.getContextPath() + "/purchase/shortage/list");
    }
}

