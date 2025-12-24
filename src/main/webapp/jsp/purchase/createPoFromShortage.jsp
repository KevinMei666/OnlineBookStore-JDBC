<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="model.ShortageRecord" %>
<%@ page import="model.Book" %>
<%@ page import="model.BookSupplier" %>
<%@ page import="java.util.List" %>
<%@ page import="dao.SupplierDao" %>
<%
    ShortageRecord shortage = (ShortageRecord) request.getAttribute("shortage");
    Book book = (Book) request.getAttribute("book");
    List<BookSupplier> bookSuppliers = (List<BookSupplier>) request.getAttribute("bookSuppliers");
    
    if (shortage == null || book == null) {
        response.sendRedirect(request.getContextPath() + "/purchase/shortage/list");
        return;
    }
    
    SupplierDao supplierDao = new SupplierDao();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>生成采购单 - 网上书店管理系统</title>
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
    
    <div class="container mt-3 mb-5">
        <div class="row">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">
                            <i class="bi bi-box-seam"></i> 从缺书记录生成采购单
                        </h5>
                    </div>
                    <div class="card-body">
                        <!-- 缺书记录信息 -->
                        <div class="mb-4">
                            <h6 class="text-muted mb-3">缺书记录信息</h6>
                            <div class="row">
                                <div class="col-md-6">
                                    <p><strong>缺书ID：</strong>#<%= shortage.getShortageId() %></p>
                                    <p><strong>书名：</strong><%= book.getTitle() != null ? book.getTitle() : "未知" %></p>
                                    <p><strong>出版社：</strong><%= book.getPublisher() != null ? book.getPublisher() : "未知" %></p>
                                </div>
                                <div class="col-md-6">
                                    <p><strong>缺货数量：</strong><span class="badge bg-danger"><%= shortage.getQuantity() != null ? shortage.getQuantity() : 0 %> 本</span></p>
                                    <p><strong>记录时间：</strong><%= shortage.getDate() != null ? shortage.getDate().toString() : "未知" %></p>
                                    <p><strong>来源类型：</strong><%= shortage.getSourceType() != null ? shortage.getSourceType() : "未知" %></p>
                                </div>
                            </div>
                        </div>
                        
                        <hr>
                        
                        <!-- 采购单编辑表单 -->
                        <form method="post" action="${pageContext.request.contextPath}/purchase/shortage/createPo">
                            <input type="hidden" name="shortageId" value="<%= shortage.getShortageId() %>">
                            
                            <div class="row">
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label for="quantity" class="form-label">
                                            <strong>采购数量 <span class="text-danger">*</span></strong>
                                        </label>
                                        <input type="number" 
                                               class="form-control" 
                                               id="quantity" 
                                               name="quantity" 
                                               value="<%= shortage.getQuantity() != null ? shortage.getQuantity() : 1 %>"
                                               min="1" 
                                               required>
                                        <small class="form-text text-muted">缺书记录数量：<%= shortage.getQuantity() != null ? shortage.getQuantity() : 0 %> 本</small>
                                    </div>
                                </div>
                                
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label for="supplierId" class="form-label">
                                            <strong>选择供应商 <span class="text-danger">*</span></strong>
                                        </label>
                                        <% if (bookSuppliers == null || bookSuppliers.isEmpty()) { %>
                                            <div class="alert alert-warning">
                                                <i class="bi bi-exclamation-triangle"></i> 该图书暂无可用供应商，请先在供应商管理中添加。
                                            </div>
                                        <% } else { %>
                                            <select class="form-select" id="supplierId" name="supplierId" required>
                                                <option value="">请选择供应商</option>
                                                <% for (BookSupplier bs : bookSuppliers) {
                                                    model.Supplier supplier = supplierDao.findById(bs.getSupplierId());
                                                    String supplierName = supplier != null && supplier.getName() != null ? supplier.getName() : "未知供应商";
                                                    String selected = (shortage.getSupplierId() != null && shortage.getSupplierId().equals(bs.getSupplierId())) ? "selected" : "";
                                                %>
                                                    <option value="<%= bs.getSupplierId() %>" <%= selected %>>
                                                        <%= supplierName %> 
                                                        (供货价: ¥<%= bs.getSupplyPrice() != null ? bs.getSupplyPrice() : "0.00" %>)
                                                    </option>
                                                <% } %>
                                            </select>
                                            <small class="form-text text-muted">共 <%= bookSuppliers.size() %> 个可用供应商</small>
                                        <% } %>
                                    </div>
                                </div>
                            </div>
                            
                            <!-- 预计总金额（动态计算） -->
                            <div class="mb-3">
                                <div class="alert alert-info">
                                    <i class="bi bi-info-circle"></i> 
                                    <strong>预计采购总金额：</strong>
                                    <span id="totalAmount" class="fw-bold text-primary">¥0.00</span>
                                </div>
                            </div>
                            
                            <div class="d-flex justify-content-between">
                                <a href="${pageContext.request.contextPath}/purchase/shortage/list" class="btn btn-secondary">
                                    <i class="bi bi-arrow-left"></i> 返回
                                </a>
                                <button type="submit" 
                                        class="btn btn-primary"
                                        <%= (bookSuppliers == null || bookSuppliers.isEmpty()) ? "disabled" : "" %>>
                                    <i class="bi bi-check-circle"></i> 确认生成采购单
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="/jsp/common/footer.jsp"/>
    
    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <!-- 自定义JS -->
    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    
    <script>
        // 动态计算采购总金额
        function calculateTotal() {
            const quantity = parseInt(document.getElementById('quantity').value) || 0;
            const supplierSelect = document.getElementById('supplierId');
            const selectedOption = supplierSelect.options[supplierSelect.selectedIndex];
            
            if (selectedOption && selectedOption.value) {
                // 从选项文本中提取价格（格式：供应商名 (供货价: ¥XX.XX)）
                const text = selectedOption.text;
                const priceMatch = text.match(/¥([\d.]+)/);
                if (priceMatch) {
                    const price = parseFloat(priceMatch[1]);
                    const total = quantity * price;
                    document.getElementById('totalAmount').textContent = '¥' + total.toFixed(2);
                } else {
                    document.getElementById('totalAmount').textContent = '¥0.00';
                }
            } else {
                document.getElementById('totalAmount').textContent = '¥0.00';
            }
        }
        
        // 监听数量和供应商变化
        document.getElementById('quantity').addEventListener('input', calculateTotal);
        document.getElementById('supplierId').addEventListener('change', calculateTotal);
        
        // 页面加载时计算一次
        calculateTotal();
    </script>
</body>
</html>

