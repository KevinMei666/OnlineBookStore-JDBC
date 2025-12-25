<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="model.PurchaseOrder" %>
<%@ page import="model.PurchaseItem" %>
<%@ page import="model.ShortageRecord" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="dao.BookDao" %>
<%@ page import="dao.SupplierDao" %>
<%@ page import="dao.ShortageRecordDao" %>
<%
    PurchaseOrder purchaseOrder = (PurchaseOrder) request.getAttribute("purchaseOrder");
    List<PurchaseItem> purchaseItems = (List<PurchaseItem>) request.getAttribute("purchaseItems");
    ShortageRecord shortageRecord = (ShortageRecord) request.getAttribute("shortageRecord");
    
    if (purchaseOrder == null) {
        response.sendRedirect(request.getContextPath() + "/purchase/list");
        return;
    }
    
    if (purchaseItems == null) {
        purchaseItems = new java.util.ArrayList<>();
    }
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    BookDao bookDao = new BookDao();
    SupplierDao supplierDao = new SupplierDao();
    ShortageRecordDao shortageRecordDao = new ShortageRecordDao();
    
    String status = purchaseOrder.getStatus() != null ? purchaseOrder.getStatus() : "CREATED";
    String statusNorm = status != null ? status.trim().toUpperCase() : "CREATED";
    String statusBadgeClass = "";
    String statusText = "";
    
    if ("CREATED".equals(statusNorm)) {
        statusBadgeClass = "bg-warning";
        statusText = "已创建";
    } else if ("COMPLETED".equals(statusNorm)) {
        statusBadgeClass = "bg-success";
        statusText = "已完成";
    } else {
        statusBadgeClass = "bg-secondary";
        statusText = status;
    }
    
    model.Supplier supplier = supplierDao.findById(purchaseOrder.getSupplierId() != null ? purchaseOrder.getSupplierId() : 0);
    String supplierName = supplier != null && supplier.getName() != null ? supplier.getName() : "未知供应商";
    
    // 获取关联的缺书记录
    if (purchaseOrder.getShortageId() != null) {
        shortageRecord = shortageRecordDao.findById(purchaseOrder.getShortageId());
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>采购单详情 #<%= purchaseOrder.getPoId() %> - 网上书店管理系统</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <!-- 自定义样式 -->
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp"/>
    <jsp:include page="/jsp/common/message.jsp"/>
    
    <div class="container mt-4 mb-5">
        <div class="row">
            <div class="col-12">
                <!-- 返回按钮 -->
                <a href="${pageContext.request.contextPath}/purchase/list" class="btn btn-outline-secondary mb-3">
                    <i class="bi bi-arrow-left"></i> 返回采购单列表
                </a>
                
                <!-- 采购单基本信息 -->
                <div class="card mb-4">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <span><i class="bi bi-info-circle"></i> 采购单基本信息</span>
                        <% if ("CREATED".equals(statusNorm)) { %>
                            <form action="${pageContext.request.contextPath}/purchase/receive" method="POST" class="d-inline" 
                                  onsubmit="return confirm('确认执行到货操作吗？这将增加库存并更新采购单状态。')">
                                <input type="hidden" name="poId" value="<%= purchaseOrder.getPoId() %>">
                                <button type="submit" class="btn btn-success" id="receive">
                                    <i class="bi bi-check-circle"></i> 执行到货
                                </button>
                            </form>
                        <% } %>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <p class="mb-2">
                                    <strong>采购单ID：</strong> #<%= purchaseOrder.getPoId() %>
                                </p>
                                <p class="mb-2">
                                    <strong>供应商：</strong> <%= supplierName %>
                                </p>
                                <p class="mb-2">
                                    <strong>创建时间：</strong> 
                                    <%= purchaseOrder.getCreateDate() != null ? 
                                        purchaseOrder.getCreateDate().format(formatter) : "未知" %>
                                </p>
                            </div>
                            <div class="col-md-6">
                                <p class="mb-2">
                                    <strong>采购单状态：</strong> 
                                    <span class="badge <%= statusBadgeClass %>">
                                        <%= statusText %>
                                    </span>
                                </p>
                                <p class="mb-2">
                                    <strong>采购总金额：</strong> 
                                    <span class="fs-5 text-danger fw-bold">
                                        ¥<%= purchaseOrder.getTotalAmount() != null ? purchaseOrder.getTotalAmount() : "0.00" %>
                                    </span>
                                </p>
                                <% if (purchaseOrder.getShortageId() != null) { %>
                                    <p class="mb-2">
                                        <strong>关联缺书记录ID：</strong> #<%= purchaseOrder.getShortageId() %>
                                    </p>
                                <% } %>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- 采购明细 -->
                <div class="card mb-4">
                    <div class="card-header">
                        <i class="bi bi-list-ul"></i> 采购明细
                    </div>
                    <div class="card-body">
                        <% if (purchaseItems.isEmpty()) { %>
                            <div class="alert alert-warning">
                                <i class="bi bi-exclamation-triangle"></i> 暂无采购明细
                            </div>
                        <% } else { %>
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th width="5%">#</th>
                                            <th width="30%">书名</th>
                                            <th width="15%">书号(ISBN)</th>
                                            <th width="15%">单价</th>
                                            <th width="15%">数量</th>
                                            <th width="20%">小计</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% 
                                            int index = 1;
                                            for (PurchaseItem item : purchaseItems) { 
                                                model.Book book = bookDao.findById(item.getBookId());
                                                String bookTitle = book != null && book.getTitle() != null ? 
                                                    book.getTitle() : "未知书名";
                                                String bookIsbn = book != null && book.getIsbn() != null && !book.getIsbn().trim().isEmpty() ? 
                                                    book.getIsbn() : "-";
                                                java.math.BigDecimal subtotal = item.getUnitPrice() != null && item.getQuantity() != null ?
                                                    item.getUnitPrice().multiply(new java.math.BigDecimal(item.getQuantity())) :
                                                    java.math.BigDecimal.ZERO;
                                        %>
                                            <tr>
                                                <td><%= index++ %></td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/book/detail?bookId=<%= item.getBookId() %>" 
                                                       class="text-decoration-none">
                                                        <%= bookTitle %>
                                                    </a>
                                                </td>
                                                <td><%= bookIsbn %></td>
                                                <td>¥<%= item.getUnitPrice() != null ? item.getUnitPrice() : "0.00" %></td>
                                                <td><%= item.getQuantity() != null ? item.getQuantity() : 0 %></td>
                                                <td class="text-danger fw-bold">¥<%= subtotal %></td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        <% } %>
                    </div>
                </div>
                
                <!-- 关联的缺书记录 -->
                <% if (shortageRecord != null) { %>
                    <div class="card mb-4">
                        <div class="card-header">
                            <i class="bi bi-exclamation-triangle"></i> 关联的缺书记录
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <p class="mb-2">
                                        <strong>缺书记录ID：</strong> #<%= shortageRecord.getShortageId() %>
                                    </p>
                                    <p class="mb-2">
                                        <strong>书籍ID：</strong> <%= shortageRecord.getBookId() %>
                                    </p>
                                    <p class="mb-2">
                                        <strong>缺货数量：</strong> 
                                        <span class="badge bg-danger"><%= shortageRecord.getQuantity() != null ? shortageRecord.getQuantity() : 0 %></span>
                                    </p>
                                </div>
                                <div class="col-md-6">
                                    <p class="mb-2">
                                        <strong>记录时间：</strong> 
                                        <%= shortageRecord.getDate() != null ? 
                                            shortageRecord.getDate().format(formatter) : "未知" %>
                                    </p>
                                    <p class="mb-2">
                                        <strong>来源类型：</strong> 
                                        <%= shortageRecord.getSourceType() != null ? shortageRecord.getSourceType() : "-" %>
                                    </p>
                                    <p class="mb-2">
                                        <strong>处理状态：</strong> 
                                        <span class="badge <%= (shortageRecord.getProcessed() != null && shortageRecord.getProcessed()) ? "bg-success" : "bg-warning" %>">
                                            <%= (shortageRecord.getProcessed() != null && shortageRecord.getProcessed()) ? "已处理" : "未处理" %>
                                        </span>
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                <% } %>
            </div>
        </div>
    </div>
    
    <jsp:include page="/jsp/common/footer.jsp"/>
    
    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <!-- 自定义JS -->
    <script src="${pageContext.request.contextPath}/js/common.js"></script>
</body>
</html>

