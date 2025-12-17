<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="model.PurchaseOrder" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="dao.SupplierDao" %>
<%
    List<PurchaseOrder> purchaseOrders = (List<PurchaseOrder>) request.getAttribute("purchaseOrders");
    
    if (purchaseOrders == null) {
        purchaseOrders = new java.util.ArrayList<>();
    }
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    SupplierDao supplierDao = new SupplierDao();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>采购管理 - 网上书店管理系统</title>
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
    
    <div class="container-fluid mt-4">
        <div class="row">
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2 class="mb-0">
                        <i class="bi bi-box-seam"></i> 采购管理
                    </h2>
                    <a href="${pageContext.request.contextPath}/purchase/create" class="btn btn-primary">
                        <i class="bi bi-plus-circle"></i> 创建采购单
                    </a>
                </div>
                
                <!-- 采购单列表 -->
                <div class="card">
                    <div class="card-header">
                        <i class="bi bi-table"></i> 采购单列表
                        <span class="badge bg-primary ms-2"><%= purchaseOrders.size() %> 条</span>
                    </div>
                    <div class="card-body">
                        <% if (purchaseOrders.isEmpty()) { %>
                            <div class="alert alert-info text-center">
                                <i class="bi bi-info-circle"></i> 暂无采购单数据
                            </div>
                        <% } else { %>
                            <div class="table-responsive">
                                <table class="table table-hover table-striped">
                                    <thead>
                                        <tr>
                                            <th width="10%">采购单ID</th>
                                            <th width="20%">供应商</th>
                                            <th width="20%">创建时间</th>
                                            <th width="15%">状态</th>
                                            <th width="15%">总金额</th>
                                            <th width="20%">操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (PurchaseOrder po : purchaseOrders) { 
                                            String status = po.getStatus() != null ? po.getStatus() : "CREATED";
                                            String statusBadgeClass = "";
                                            String statusText = "";
                                            
                                            if ("CREATED".equals(status)) {
                                                statusBadgeClass = "bg-warning";
                                                statusText = "已创建";
                                            } else if ("COMPLETED".equals(status)) {
                                                statusBadgeClass = "bg-success";
                                                statusText = "已完成";
                                            } else {
                                                statusBadgeClass = "bg-secondary";
                                                statusText = status;
                                            }
                                            
                                            model.Supplier supplier = supplierDao.findById(po.getSupplierId() != null ? po.getSupplierId() : 0);
                                            String supplierName = supplier != null && supplier.getName() != null ? supplier.getName() : "未知供应商";
                                        %>
                                            <tr>
                                                <td><strong>#<%= po.getPoId() %></strong></td>
                                                <td><%= supplierName %></td>
                                                <td>
                                                    <%= po.getCreateDate() != null ? 
                                                        po.getCreateDate().format(formatter) : "未知" %>
                                                </td>
                                                <td>
                                                    <span class="badge <%= statusBadgeClass %>">
                                                        <%= statusText %>
                                                    </span>
                                                </td>
                                                <td class="text-danger fw-bold">
                                                    ¥<%= po.getTotalAmount() != null ? po.getTotalAmount() : "0.00" %>
                                                </td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/purchase/detail?poId=<%= po.getPoId() %>" 
                                                       class="btn btn-sm btn-outline-primary">
                                                        <i class="bi bi-eye"></i> 查看明细
                                                    </a>
                                                    <% if ("CREATED".equals(status)) { %>
                                                        <a href="${pageContext.request.contextPath}/purchase/detail?poId=<%= po.getPoId() %>#receive" 
                                                           class="btn btn-sm btn-success">
                                                            <i class="bi bi-check-circle"></i> 到货
                                                        </a>
                                                    <% } %>
                                                </td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        <% } %>
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
</body>
</html>

