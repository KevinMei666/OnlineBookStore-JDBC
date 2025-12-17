<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="service.CustomerQueryService" %>
<%@ page import="service.CustomerQueryService.CustomerOrderDetail" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    List<CustomerOrderDetail> orderHistory = (List<CustomerOrderDetail>) request.getAttribute("orderHistory");
    
    if (orderHistory == null) {
        orderHistory = new java.util.ArrayList<>();
    }
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>订单历史 - 网上书店管理系统</title>
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
                    <i class="bi bi-clock-history"></i> 订单历史
                </h2>
                
                <!-- 订单列表 -->
                <div class="card">
                    <div class="card-header">
                        <i class="bi bi-table"></i> 订单列表
                        <span class="badge bg-primary ms-2"><%= orderHistory.size() %> 条</span>
                    </div>
                    <div class="card-body">
                        <% if (orderHistory.isEmpty()) { %>
                            <div class="alert alert-info text-center">
                                <i class="bi bi-info-circle"></i> 暂无订单记录
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
                                            <th width="15%">订单明细数量</th>
                                            <th width="25%">操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (CustomerOrderDetail detail : orderHistory) { 
                                            model.Orders order = detail.getOrder();
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
                                            
                                            int itemCount = detail.getItems() != null ? detail.getItems().size() : 0;
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
                                                    <span class="badge bg-info"><%= itemCount %> 项</span>
                                                </td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/order/detail?orderId=<%= order.getOrderId() %>" 
                                                       class="btn btn-sm btn-outline-primary">
                                                        <i class="bi bi-eye"></i> 查看详情
                                                    </a>
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

