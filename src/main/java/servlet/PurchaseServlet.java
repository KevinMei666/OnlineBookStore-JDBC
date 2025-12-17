package servlet;

import dao.BookDao;
import dao.PurchaseItemDao;
import dao.PurchaseOrderDao;
import dao.ShortageRecordDao;
import dao.SupplierDao;
import model.PurchaseItem;
import model.PurchaseOrder;
import model.ShortageRecord;
import java.util.ArrayList;

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
    private SupplierDao supplierDao;
    private BookDao bookDao;
    
    @Override
    public void init() throws ServletException {
        super.init();
        purchaseOrderDao = new PurchaseOrderDao();
        purchaseItemDao = new PurchaseItemDao();
        shortageRecordDao = new ShortageRecordDao();
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
        HttpSession session = request.getSession();
        session.setAttribute("infoMessage", "创建采购单功能开发中");
        response.sendRedirect(request.getContextPath() + "/purchase/list");
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
            // 显示创建缺书记录页面（将在后续实现）
            HttpSession session = request.getSession();
            session.setAttribute("infoMessage", "手动创建缺书记录功能开发中");
            response.sendRedirect(request.getContextPath() + "/purchase/shortage/list");
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
     * 显示缺书记录详情（将在后续实现）
     */
    private void handleShortageDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.setAttribute("infoMessage", "缺书记录详情功能开发中");
        response.sendRedirect(request.getContextPath() + "/purchase/shortage/list");
    }
}

