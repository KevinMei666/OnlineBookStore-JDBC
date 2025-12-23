<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="model.CartItem" %>
<%@ page import="model.Customer" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="dao.CustomerDao" %>
<%
    HttpSession sessionObj = request.getSession();
    @SuppressWarnings("unchecked")
    List<CartItem> cart = (List<CartItem>) sessionObj.getAttribute("cart");
    
    if (cart == null || cart.isEmpty()) {
        sessionObj.setAttribute("errorMessage", "购物车为空，无法结算");
        response.sendRedirect(request.getContextPath() + "/order/cart");
        return;
    }
    
    // 获取当前登录客户ID（必须先登录）
    Integer customerId = (Integer) sessionObj.getAttribute("currentCustomerId");
    if (customerId == null) {
        sessionObj.setAttribute("warningMessage", "请先登录后再结算订单");
        response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
        return;
    }
    
    CustomerDao customerDao = new CustomerDao();
    Customer customer = customerDao.findById(customerId);
    if (customer == null) {
        sessionObj.setAttribute("errorMessage", "客户信息不存在");
        response.sendRedirect(request.getContextPath() + "/order/cart");
        return;
    }
    
    // 计算总金额
    BigDecimal totalAmount = BigDecimal.ZERO;
    int totalQuantity = 0;
    for (CartItem item : cart) {
        if (item.getPrice() != null) {
            totalAmount = totalAmount.add(item.getSubtotal());
        }
        totalQuantity += item.getQuantity();
    }
    
    // 计算折扣（根据信用等级）
    int creditLevel = customer.getCreditLevel() != null ? customer.getCreditLevel() : 1;
    BigDecimal discountRate = BigDecimal.ZERO;
    String discountDesc = "";
    switch (creditLevel) {
        case 1:
            discountRate = new BigDecimal("0.10");
            discountDesc = "10%折扣";
            break;
        case 2:
            discountRate = new BigDecimal("0.15");
            discountDesc = "15%折扣";
            break;
        case 3:
            discountRate = new BigDecimal("0.15");
            discountDesc = "15%折扣";
            break;
        case 4:
            discountRate = new BigDecimal("0.20");
            discountDesc = "20%折扣";
            break;
        case 5:
            discountRate = new BigDecimal("0.25");
            discountDesc = "25%折扣";
            break;
        default:
            discountRate = new BigDecimal("0.10");
            discountDesc = "10%折扣";
    }
    
    BigDecimal discountAmount = totalAmount.multiply(discountRate);
    BigDecimal finalAmount = totalAmount.subtract(discountAmount);
    
    // 默认收货地址
    String defaultAddress = customer.getAddress() != null ? customer.getAddress() : "";
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>订单确认 - 网上书店管理系统</title>
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
                <h2 class="mb-4">
                    <i class="bi bi-bag-check"></i> 订单确认
                </h2>
                
                <form action="${pageContext.request.contextPath}/order/create" method="POST">
                    <div class="row">
                        <!-- 左侧：订单信息 -->
                        <div class="col-lg-8">
                            <!-- 收货地址 -->
                            <div class="card mb-4">
                                <div class="card-header">
                                    <i class="bi bi-geo-alt"></i> 收货地址
                                </div>
                                <div class="card-body">
                                    <div class="mb-3">
                                        <label for="shippingAddress" class="form-label">收货地址 <span class="text-danger">*</span></label>
                                        <textarea class="form-control" 
                                                  id="shippingAddress" 
                                                  name="shippingAddress" 
                                                  rows="3" 
                                                  required><%= defaultAddress %></textarea>
                                        <small class="form-text text-muted">请输入详细的收货地址</small>
                                    </div>
                                </div>
                            </div>
                            
                            <!-- 商品清单 -->
                            <div class="card mb-4">
                                <div class="card-header">
                                    <i class="bi bi-list-ul"></i> 商品清单
                                </div>
                                <div class="card-body">
                                    <div class="table-responsive">
                                        <table class="table table-hover">
                                            <thead>
                                                <tr>
                                                    <th width="5%">#</th>
                                                    <th width="40%">书名</th>
                                                    <th width="15%">单价</th>
                                                    <th width="15%">数量</th>
                                                    <th width="25%">小计</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <% for (int i = 0; i < cart.size(); i++) { 
                                                    CartItem item = cart.get(i);
                                                %>
                                                    <tr>
                                                        <td><%= i + 1 %></td>
                                                        <td><%= item.getTitle() != null ? item.getTitle() : "未知书名" %></td>
                                                        <td class="text-danger">¥<%= item.getPrice() != null ? item.getPrice() : "0.00" %></td>
                                                        <td><%= item.getQuantity() %></td>
                                                        <td class="text-danger fw-bold">¥<%= item.getSubtotal() != null ? item.getSubtotal() : "0.00" %></td>
                                                    </tr>
                                                <% } %>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <!-- 右侧：订单汇总 -->
                        <div class="col-lg-4">
                            <div class="card sticky-top" style="top: 20px;">
                                <div class="card-header">
                                    <i class="bi bi-calculator"></i> 订单汇总
                                </div>
                                <div class="card-body">
                                    <!-- 客户信息 -->
                                    <div class="mb-3">
                                        <h6>客户信息</h6>
                                        <p class="mb-1">
                                            <strong>姓名：</strong> <%= customer.getName() != null ? customer.getName() : "未知" %>
                                        </p>
                                        <p class="mb-1">
                                            <strong>邮箱：</strong> <%= customer.getEmail() != null ? customer.getEmail() : "未知" %>
                                        </p>
                                    </div>
                                    
                                    <hr>
                                    
                                    <!-- 信用等级和折扣 -->
                                    <div class="mb-3">
                                        <h6>信用等级</h6>
                                        <p class="mb-1">
                                            <span class="badge bg-primary">等级 <%= creditLevel %></span>
                                            <span class="badge bg-success ms-2"><%= discountDesc %></span>
                                        </p>
                                    </div>
                                    
                                    <hr>
                                    
                                    <!-- 金额明细 -->
                                    <div class="mb-3">
                                        <div class="d-flex justify-content-between mb-2">
                                            <span>商品总数：</span>
                                            <strong><%= totalQuantity %> 本</strong>
                                        </div>
                                        <div class="d-flex justify-content-between mb-2">
                                            <span>商品总价：</span>
                                            <strong>¥<%= totalAmount %></strong>
                                        </div>
                                        <div class="d-flex justify-content-between mb-2 text-success">
                                            <span>折扣优惠：</span>
                                            <strong>-¥<%= discountAmount %></strong>
                                        </div>
                                        <hr>
                                        <div class="d-flex justify-content-between">
                                            <span class="fs-5 fw-bold">订单总金额：</span>
                                            <span class="fs-4 text-danger fw-bold">¥<%= finalAmount %></span>
                                        </div>
                                    </div>
                                    
                                    <hr>
                                    
                                    <!-- 提交按钮 -->
                                    <button type="submit" class="btn btn-primary btn-lg w-100">
                                        <i class="bi bi-check-circle"></i> 确认下单
                                    </button>
                                    <a href="${pageContext.request.contextPath}/order/cart" 
                                       class="btn btn-outline-secondary w-100 mt-2">
                                        <i class="bi bi-arrow-left"></i> 返回购物车
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </form>
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

