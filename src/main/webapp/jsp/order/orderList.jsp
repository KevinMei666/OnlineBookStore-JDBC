<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.Orders" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    List<Orders> orders = (List<Orders>) request.getAttribute("orders");
    String filterStatus = request.getParameter("status");
    String currentRole = (String) session.getAttribute("currentRole");
    boolean isAdmin = "ADMIN".equals(currentRole);
    Map<Integer, String> customerNameMap = (Map<Integer, String>) request.getAttribute("customerNameMap");
    
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
    
    <div class="container mt-3 mb-5">
        <div class="row">
            <div class="col-12">
                <!-- 状态筛选：仅管理员显示，客户直接查看自己的全部订单 -->
                <% if (isAdmin) { %>
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
                <% } %>
                
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
                                <table class="table table-hover table-striped table-modern">
                                    <thead>
                                        <tr>
                                            <th width="10%">订单ID</th>
                                            <% if (isAdmin) { %>
                                                <th width="15%">下单客户</th>
                                            <% } %>
                                            <th width="20%">下单时间</th>
                                            <th width="12%">订单状态</th>
                                            <th width="10%">收货状态</th>
                                            <th width="13%">总金额</th>
                                            <th width="30%">操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (Orders order : orders) { 
                                            String statusRaw = order.getStatus() != null ? order.getStatus() : "CREATED";
                                            String status = statusRaw != null ? statusRaw.trim() : "CREATED";
                                            String statusUpper = status.toUpperCase();
                                            String statusBadgeClass = "";
                                            String statusText = "";
                                            
                                            switch (statusUpper) {
                                                case "PENDING": // 兼容历史数据：Pending 视为已创建/待处理
                                                case "CREATED":
                                                    statusBadgeClass = "bg-secondary";
                                                    statusText = "已创建";
                                                    break;
                                                case "PAID":
                                                    statusBadgeClass = "bg-info";
                                                    statusText = "已支付";
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
                                                <% if (isAdmin) { %>
                                                    <td>
                                                        <%
                                                            Integer cid = order.getCustomerId();
                                                            String customerLabel = "未知客户";
                                                            if (cid != null && customerNameMap != null) {
                                                                String name = customerNameMap.get(cid);
                                                                if (name != null) {
                                                                    customerLabel = "#" + cid + " - " + name;
                                                                } else {
                                                                    customerLabel = "客户#" + cid;
                                                                }
                                                            }
                                                        %>
                                                        <span class="text-nowrap"><%= customerLabel %></span>
                                                    </td>
                                                <% } %>
                                                <td>
                                                    <%= order.getOrderDate() != null ? 
                                                        order.getOrderDate().format(formatter) : "未知" %>
                                                </td>
                                                <td>
                                                    <span class="badge <%= statusBadgeClass %>">
                                                        <%= statusText %>
                                                    </span>
                                                </td>
                                                <td>
                                                    <%
                                                        Boolean confirmed = order.getConfirmed();
                                                        boolean isConfirmed = confirmed != null && confirmed;
                                                    %>
                                                    <% if (isConfirmed) { %>
                                                        <span class="badge bg-success">已收货</span>
                                                    <% } else { %>
                                                        <span class="badge bg-secondary">未确认</span>
                                                    <% } %>
                                                </td>
                                                <td class="text-danger fw-bold">
                                                    ¥<%= order.getTotalAmount() != null ? order.getTotalAmount() : "0.00" %>
                                                </td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/order/detail?orderId=<%= order.getOrderId() %>" 
                                                       class="btn btn-sm btn-outline-primary">
                                                        <i class="bi bi-eye"></i> 查看详情
                                                    </a>
                                                    <% if (isAdmin && ("PAID".equals(statusUpper) || "PARTIAL".equals(statusUpper))) { %>
                                                        <a href="${pageContext.request.contextPath}/shipment/list?orderId=<%= order.getOrderId() %>" 
                                                           class="btn btn-sm btn-success">
                                                            <i class="bi bi-truck"></i> 发货
                                                        </a>
                                                    <% } %>
                                                    <% if (!isAdmin && "SHIPPED".equals(statusUpper) && !isConfirmed) { %>
                                                        <form action="${pageContext.request.contextPath}/order/confirm" method="post" style="display:inline;">
                                                            <input type="hidden" name="orderId" value="<%= order.getOrderId() %>">
                                                            <button type="submit" class="btn btn-sm btn-warning">
                                                                <i class="bi bi-check-lg"></i> 确认收货
                                                            </button>
                                                        </form>
                                                    <% } %>
                                                    <% if (!isAdmin && "CREATED".equals(statusUpper)) { %>
                                                        <a href="${pageContext.request.contextPath}/order/payPage?orderId=<%= order.getOrderId() %>" 
                                                           class="btn btn-sm btn-success">
                                                            <i class="bi bi-credit-card"></i> 继续支付
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

