<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Orders" %>
<%@ page import="model.OrderItem" %>
<%@ page import="model.Customer" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    Orders order = (Orders) request.getAttribute("order");
    List<OrderItem> orderItems = (List<OrderItem>) request.getAttribute("orderItems");
    Customer customer = (Customer) request.getAttribute("customer");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>确认支付 - 网上书店管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp"/>
    <jsp:include page="/jsp/common/message.jsp"/>
    
    <div class="container mt-4 mb-5">
        <div class="row justify-content-center">
            <div class="col-lg-8">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-primary text-white">
                        <h5 class="mb-0"><i class="bi bi-credit-card"></i> 确认支付</h5>
                    </div>
                    <div class="card-body">
                        <!-- 订单信息 -->
                        <div class="mb-4">
                            <h6 class="text-muted">订单信息</h6>
                            <table class="table table-borderless">
                                <tr>
                                    <td width="30%"><strong>订单编号：</strong></td>
                                    <td>#<%= order.getOrderId() %></td>
                                </tr>
                                <tr>
                                    <td><strong>下单时间：</strong></td>
                                    <td><%= order.getOrderDate() != null ? order.getOrderDate().format(formatter) : "未知" %></td>
                                </tr>
                                <tr>
                                    <td><strong>收货地址：</strong></td>
                                    <td><%= order.getShippingAddress() != null ? order.getShippingAddress() : "未填写" %></td>
                                </tr>
                            </table>
                        </div>
                        
                        <!-- 商品明细 -->
                        <div class="mb-4">
                            <h6 class="text-muted">商品明细</h6>
                            <table class="table table-striped">
                                <thead>
                                    <tr>
                                        <th>书籍ID</th>
                                        <th>单价</th>
                                        <th>数量</th>
                                        <th class="text-end">小计</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% if (orderItems != null) {
                                        for (OrderItem item : orderItems) {
                                            java.math.BigDecimal subtotal = item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity()));
                                    %>
                                    <tr>
                                        <td>#<%= item.getBookId() %></td>
                                        <td>¥<%= item.getUnitPrice() %></td>
                                        <td><%= item.getQuantity() %></td>
                                        <td class="text-end text-danger">¥<%= subtotal %></td>
                                    </tr>
                                    <% }} %>
                                </tbody>
                            </table>
                        </div>
                        
                        <!-- 支付信息 -->
                        <div class="mb-4 p-3 bg-light rounded">
                            <div class="row">
                                <div class="col-6">
                                    <p class="mb-1"><strong>账户余额：</strong></p>
                                    <h4 class="text-primary">¥<%= customer != null && customer.getBalance() != null ? customer.getBalance() : "0.00" %></h4>
                                </div>
                                <div class="col-6 text-end">
                                    <p class="mb-1"><strong>应付金额：</strong></p>
                                    <h4 class="text-danger">¥<%= order.getTotalAmount() != null ? order.getTotalAmount() : "0.00" %></h4>
                                </div>
                            </div>
                            <% if (customer != null && customer.getCreditLevel() != null && customer.getCreditLevel() >= 3) { %>
                            <hr>
                            <p class="mb-0 text-muted small">
                                <i class="bi bi-info-circle"></i> 您的信用等级为 <%= customer.getCreditLevel() %>，可透支额度：¥<%= customer.getMonthlyLimit() != null ? customer.getMonthlyLimit() : "0.00" %>
                            </p>
                            <% } %>
                        </div>
                        
                        <!-- 操作按钮 -->
                        <div class="d-flex justify-content-between">
                            <a href="${pageContext.request.contextPath}/order/list" class="btn btn-secondary">
                                <i class="bi bi-arrow-left"></i> 返回订单列表
                            </a>
                            <form action="${pageContext.request.contextPath}/order/pay" method="post">
                                <input type="hidden" name="orderId" value="<%= order.getOrderId() %>">
                                <button type="submit" class="btn btn-success btn-lg">
                                    <i class="bi bi-check-circle"></i> 确认支付
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="/jsp/common/footer.jsp"/>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

