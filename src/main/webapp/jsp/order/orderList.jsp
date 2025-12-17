<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Orders" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    List<Orders> orders = (List<Orders>) request.getAttribute("orders");
    String filterStatus = request.getParameter("status");
    
    if (orders == null) {
        orders = new java.util.ArrayList<>();
    }
    if (filterStatus == null) {
        filterStatus = "";
    }
    
    // 过滤订单（如果指定了状态）
    if (filterStatus != null && !filterStatus.isEmpty()) {
        List<Orders> filteredOrders = new java.util.ArrayList<>();
        for (Orders order : orders) {
            if (filterStatus.equals(order.getStatus())) {
                filteredOrders.add(order);
            }
        }
        orders = filteredOrders;
    }
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>订单管理 - 网上书店管理系统</title>
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
                <h2 class="mb-4">
                    <i class="bi bi-list-ul"></i> 订单管理
                </h2>
                
                <!-- 状态筛选 -->
                <div class="card mb-4">
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/order/list" method="GET" class="d-flex gap-2 align-items-end">
                            <div class="flex-grow-1">
                                <label for="status" class="form-label">按状态筛选</label>
                                <select class="form-select" id="status" name="status" onchange="this.form.submit()">
                                    <option value="">全部订单</option>
                                    <option value="CREATED" <%= "CREATED".equals(filterStatus) ? "selected" : "" %>>已创建</option>
                                    <option value="PARTIAL" <%= "PARTIAL".equals(filterStatus) ? "selected" : "" %>>部分发货</option>
                                    <option value="SHIPPED" <%= "SHIPPED".equals(filterStatus) ? "selected" : "" %>>已发货</option>
                                </select>
                            </div>
                            <div>
                                <a href="${pageContext.request.contextPath}/order/list" class="btn btn-outline-secondary">
                                    <i class="bi bi-arrow-clockwise"></i> 重置
                                </a>
                            </div>
                        </form>
                    </div>
                </div>
                
                <!-- 订单列表 -->
                <div class="card">
                    <div class="card-header">
                        <i class="bi bi-table"></i> 订单列表
                        <span class="badge bg-primary ms-2"><%= orders.size() %> 条</span>
                    </div>
                    <div class="card-body">
                        <% if (orders.isEmpty()) { %>
                            <div class="alert alert-info text-center">
                                <i class="bi bi-info-circle"></i> 暂无订单数据
                            </div>
                        <% } else { %>
                            <div class="table-responsive">
                                <table class="table table-hover table-striped">
                                    <thead>
                                        <tr>
                                            <th width="10%">订单ID</th>
                                            <th width="20%">下单时间</th>
                                            <th width="15%">订单状态</th>
                                            <th width="15%">总金额</th>
                                            <th width="40%">操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (Orders order : orders) { 
                                            String status = order.getStatus() != null ? order.getStatus() : "CREATED";
                                            String statusBadgeClass = "";
                                            String statusText = "";
                                            
                                            switch (status) {
                                                case "CREATED":
                                                    statusBadgeClass = "bg-secondary";
                                                    statusText = "已创建";
                                                    break;
                                                case "PARTIAL":
                                                    statusBadgeClass = "bg-warning";
                                                    statusText = "部分发货";
                                                    break;
                                                case "SHIPPED":
                                                    statusBadgeClass = "bg-success";
                                                    statusText = "已发货";
                                                    break;
                                                default:
                                                    statusBadgeClass = "bg-secondary";
                                                    statusText = status;
                                            }
                                        %>
                                            <tr>
                                                <td><strong>#<%= order.getOrderId() %></strong></td>
                                                <td>
                                                    <%= order.getOrderDate() != null ? 
                                                        order.getOrderDate().format(formatter) : "未知" %>
                                                </td>
                                                <td>
                                                    <span class="badge <%= statusBadgeClass %>">
                                                        <%= statusText %>
                                                    </span>
                                                </td>
                                                <td class="text-danger fw-bold">
                                                    ¥<%= order.getTotalAmount() != null ? order.getTotalAmount() : "0.00" %>
                                                </td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/order/detail?orderId=<%= order.getOrderId() %>" 
                                                       class="btn btn-sm btn-outline-primary">
                                                        <i class="bi bi-eye"></i> 查看详情
                                                    </a>
                                                    <% if (!"SHIPPED".equals(status)) { %>
                                                        <a href="${pageContext.request.contextPath}/shipment/list?orderId=<%= order.getOrderId() %>" 
                                                           class="btn btn-sm btn-success">
                                                            <i class="bi bi-truck"></i> 发货
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

