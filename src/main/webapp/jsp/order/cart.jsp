<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="model.CartItem" %>
<%@ page import="java.math.BigDecimal" %>
<%
    HttpSession sessionObj = request.getSession();
    @SuppressWarnings("unchecked")
    List<CartItem> cart = (List<CartItem>) sessionObj.getAttribute("cart");
    
    if (cart == null) {
        cart = new java.util.ArrayList<>();
    }
    
    // 计算总金额和总数量
    BigDecimal totalAmount = BigDecimal.ZERO;
    int totalQuantity = 0;
    for (CartItem item : cart) {
        if (item.getPrice() != null) {
            totalAmount = totalAmount.add(item.getSubtotal());
        }
        totalQuantity += item.getQuantity();
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>购物车 - 网上书店管理系统</title>
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
                    <i class="bi bi-cart"></i> 购物车
                </h2>
                
                <% if (cart.isEmpty()) { %>
                    <div class="card">
                        <div class="card-body text-center py-5">
                            <i class="bi bi-cart-x" style="font-size: 64px; color: #ccc;"></i>
                            <h4 class="mt-3 text-muted">购物车是空的</h4>
                            <p class="text-muted">快去挑选您喜欢的书籍吧！</p>
                            <a href="${pageContext.request.contextPath}/book" class="btn btn-primary">
                                <i class="bi bi-book"></i> 去选购
                            </a>
                        </div>
                    </div>
                <% } else { %>
                    <div class="card">
                        <div class="card-header">
                            <i class="bi bi-list-ul"></i> 购物车商品
                        </div>
                        <div class="card-body">
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th width="5%">#</th>
                                            <th width="30%">书名</th>
                                            <th width="15%">单价</th>
                                            <th width="20%">数量</th>
                                            <th width="15%">小计</th>
                                            <th width="15%">操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (int i = 0; i < cart.size(); i++) { 
                                            CartItem item = cart.get(i);
                                        %>
                                            <tr>
                                                <td><%= i + 1 %></td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/book/detail?bookId=<%= item.getBookId() %>" 
                                                       class="text-decoration-none">
                                                        <%= item.getTitle() != null ? item.getTitle() : "未知书名" %>
                                                    </a>
                                                </td>
                                                <td class="text-danger fw-bold">
                                                    ¥<%= item.getPrice() != null ? item.getPrice() : "0.00" %>
                                                </td>
                                                <td>
                                                    <form action="${pageContext.request.contextPath}/order/updateQuantity" 
                                                          method="POST" class="d-inline">
                                                        <input type="hidden" name="bookId" value="<%= item.getBookId() %>">
                                                        <div class="input-group" style="width: 120px;">
                                                            <input type="number" 
                                                                   name="quantity" 
                                                                   value="<%= item.getQuantity() %>" 
                                                                   min="1" 
                                                                   class="form-control form-control-sm" 
                                                                   onchange="this.form.submit()">
                                                            <button type="submit" class="btn btn-outline-secondary btn-sm">
                                                                <i class="bi bi-check"></i>
                                                            </button>
                                                        </div>
                                                    </form>
                                                </td>
                                                <td class="text-danger fw-bold">
                                                    ¥<%= item.getSubtotal() != null ? item.getSubtotal() : "0.00" %>
                                                </td>
                                                <td>
                                                    <form action="${pageContext.request.contextPath}/order/remove" 
                                                          method="POST" 
                                                          class="d-inline"
                                                          onsubmit="return confirm('确定要删除《<%= item.getTitle() != null ? item.getTitle() : "未知" %>》吗？')">
                                                        <input type="hidden" name="bookId" value="<%= item.getBookId() %>">
                                                        <button type="submit" class="btn btn-sm btn-danger">
                                                            <i class="bi bi-trash"></i> 删除
                                                        </button>
                                                    </form>
                                                </td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                            
                            <!-- 底部汇总 -->
                            <div class="row mt-4">
                                <div class="col-md-6">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">购物车统计</h5>
                                            <p class="mb-1">
                                                <strong>商品总数：</strong> 
                                                <span class="badge bg-primary"><%= totalQuantity %> 本</span>
                                            </p>
                                            <p class="mb-0">
                                                <strong>总金额：</strong> 
                                                <span class="fs-4 text-danger fw-bold">¥<%= totalAmount %></span>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-6 text-end">
                                    <div class="d-flex justify-content-end gap-2">
                                        <form action="${pageContext.request.contextPath}/order/clear" 
                                              method="POST" 
                                              class="d-inline"
                                              onsubmit="return confirm('确定要清空购物车吗？')">
                                            <button type="submit" class="btn btn-outline-danger">
                                                <i class="bi bi-trash"></i> 清空购物车
                                            </button>
                                        </form>
                                        <a href="${pageContext.request.contextPath}/order/checkout" 
                                           class="btn btn-primary btn-lg">
                                            <i class="bi bi-bag-check"></i> 去结算
                                        </a>
                                    </div>
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

