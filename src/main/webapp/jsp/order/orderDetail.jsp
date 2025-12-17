<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="model.Orders" %>
<%@ page import="model.OrderItem" %>
<%@ page import="model.Shipment" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="dao.BookDao" %>
<%
    Orders order = (Orders) request.getAttribute("order");
    List<OrderItem> orderItems = (List<OrderItem>) request.getAttribute("orderItems");
    List<Shipment> shipments = (List<Shipment>) request.getAttribute("shipments");
    
    if (order == null) {
        response.sendRedirect(request.getContextPath() + "/order/list");
        return;
    }
    
    if (orderItems == null) {
        orderItems = new java.util.ArrayList<>();
    }
    if (shipments == null) {
        shipments = new java.util.ArrayList<>();
    }
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    BookDao bookDao = new BookDao();
    
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
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>订单详情 #<%= order.getOrderId() %> - 网上书店管理系统</title>
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
                <!-- 返回按钮 -->
                <a href="${pageContext.request.contextPath}/order/list" class="btn btn-outline-secondary mb-3">
                    <i class="bi bi-arrow-left"></i> 返回订单列表
                </a>
                
                <!-- 订单基本信息 -->
                <div class="card mb-4">
                    <div class="card-header">
                        <i class="bi bi-info-circle"></i> 订单基本信息
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <p class="mb-2">
                                    <strong>订单ID：</strong> #<%= order.getOrderId() %>
                                </p>
                                <p class="mb-2">
                                    <strong>下单时间：</strong> 
                                    <%= order.getOrderDate() != null ? 
                                        order.getOrderDate().format(formatter) : "未知" %>
                                </p>
                                <p class="mb-2">
                                    <strong>订单状态：</strong> 
                                    <span class="badge <%= statusBadgeClass %>">
                                        <%= statusText %>
                                    </span>
                                </p>
                            </div>
                            <div class="col-md-6">
                                <p class="mb-2">
                                    <strong>客户ID：</strong> <%= order.getCustomerId() %>
                                </p>
                                <p class="mb-2">
                                    <strong>收货地址：</strong> 
                                    <%= order.getShippingAddress() != null ? 
                                        order.getShippingAddress() : "未设置" %>
                                </p>
                                <p class="mb-2">
                                    <strong>订单总金额：</strong> 
                                    <span class="fs-5 text-danger fw-bold">
                                        ¥<%= order.getTotalAmount() != null ? order.getTotalAmount() : "0.00" %>
                                    </span>
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- 订单明细 -->
                <div class="card mb-4">
                    <div class="card-header">
                        <i class="bi bi-list-ul"></i> 订单明细
                    </div>
                    <div class="card-body">
                        <% if (orderItems.isEmpty()) { %>
                            <div class="alert alert-warning">
                                <i class="bi bi-exclamation-triangle"></i> 暂无订单明细
                            </div>
                        <% } else { %>
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th width="5%">#</th>
                                            <th width="40%">书名</th>
                                            <th width="15%">单价</th>
                                            <th width="15%">数量</th>
                                            <th width="25%">金额</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% 
                                            int index = 1;
                                            for (OrderItem item : orderItems) { 
                                                model.Book book = bookDao.findById(item.getBookId());
                                                String bookTitle = book != null && book.getTitle() != null ? 
                                                    book.getTitle() : "未知书名";
                                        %>
                                            <tr>
                                                <td><%= index++ %></td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/book/detail?bookId=<%= item.getBookId() %>" 
                                                       class="text-decoration-none">
                                                        <%= bookTitle %>
                                                    </a>
                                                </td>
                                                <td>¥<%= item.getUnitPrice() != null ? item.getUnitPrice() : "0.00" %></td>
                                                <td><%= item.getQuantity() != null ? item.getQuantity() : 0 %></td>
                                                <td class="text-danger fw-bold">
                                                    ¥<%= item.getAmount() != null ? item.getAmount() : "0.00" %>
                                                </td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        <% } %>
                    </div>
                </div>
                
                <!-- 发货记录 -->
                <div class="card mb-4">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <span><i class="bi bi-truck"></i> 发货记录</span>
                        <% if (!"SHIPPED".equals(status)) { %>
                            <a href="${pageContext.request.contextPath}/shipment/list?orderId=<%= order.getOrderId() %>" 
                               class="btn btn-sm btn-success">
                                <i class="bi bi-plus-circle"></i> 执行发货
                            </a>
                        <% } %>
                    </div>
                    <div class="card-body">
                        <% if (shipments.isEmpty()) { %>
                            <div class="alert alert-info">
                                <i class="bi bi-info-circle"></i> 暂无发货记录
                            </div>
                        <% } else { %>
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th width="10%">发货ID</th>
                                            <th width="30%">书名</th>
                                            <th width="15%">发货数量</th>
                                            <th width="20%">发货时间</th>
                                            <th width="15%">承运商</th>
                                            <th width="10%">运单号</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (Shipment shipment : shipments) { 
                                            model.Book book = bookDao.findById(shipment.getBookId());
                                            String bookTitle = book != null && book.getTitle() != null ? 
                                                book.getTitle() : "未知书名";
                                        %>
                                            <tr>
                                                <td>#<%= shipment.getShipmentId() %></td>
                                                <td><%= bookTitle %></td>
                                                <td><%= shipment.getQuantity() != null ? shipment.getQuantity() : 0 %></td>
                                                <td>
                                                    <%= shipment.getShipDate() != null ? 
                                                        shipment.getShipDate().format(formatter) : "未知" %>
                                                </td>
                                                <td>
                                                    <%= shipment.getCarrier() != null ? shipment.getCarrier() : "-" %>
                                                </td>
                                                <td>
                                                    <%= shipment.getTrackingNo() != null ? shipment.getTrackingNo() : "-" %>
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

