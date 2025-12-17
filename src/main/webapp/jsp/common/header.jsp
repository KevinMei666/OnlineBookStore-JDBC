<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    // 获取当前请求路径，用于高亮当前页面
    String currentPath = request.getRequestURI();
    String contextPath = request.getContextPath();
    if (contextPath != null && !contextPath.isEmpty()) {
        currentPath = currentPath.substring(contextPath.length());
    }
    
    // 获取当前登录用户（暂时写死，后续可从session获取）
    String currentUser = (String) session.getAttribute("currentUser");
    if (currentUser == null || currentUser.isEmpty()) {
        currentUser = "测试用户";
    }
%>
<!-- 图标字体（Bootstrap Icons） -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">

<!-- 导航栏 -->
    <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
        <div class="container-fluid">
            <!-- 品牌Logo -->
            <a class="navbar-brand" href="${pageContext.request.contextPath}/index.jsp">
                <i class="bi bi-book"></i> 网上书店管理系统
            </a>
            
            <!-- 移动端折叠按钮 -->
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" 
                    aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            
            <!-- 导航菜单 -->
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <a class="nav-link <%= currentPath.equals("/index.jsp") || currentPath.equals("/") ? "active" : "" %>" 
                           href="${pageContext.request.contextPath}/index.jsp">
                            <i class="bi bi-house-door"></i> 首页
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link <%= currentPath.contains("/book") ? "active" : "" %>" 
                           href="${pageContext.request.contextPath}/book">
                            <i class="bi bi-search"></i> 书目查询
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link <%= currentPath.contains("/order/cart") ? "active" : "" %>" 
                           href="${pageContext.request.contextPath}/order/cart">
                            <i class="bi bi-cart-plus"></i> 下单
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link <%= (currentPath.contains("/order") && !currentPath.contains("/order/cart")) ? "active" : "" %>" 
                           href="${pageContext.request.contextPath}/order/list">
                            <i class="bi bi-list-ul"></i> 订单管理
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link <%= (currentPath.contains("/purchase") && !currentPath.contains("/purchase/shortage")) ? "active" : "" %>" 
                           href="${pageContext.request.contextPath}/purchase/list">
                            <i class="bi bi-box-seam"></i> 采购管理
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link <%= currentPath.contains("/admin/customer") ? "active" : "" %>" 
                           href="${pageContext.request.contextPath}/admin/customer/list">
                            <i class="bi bi-people"></i> 客户管理
                        </a>
                    </li>
            <li class="nav-item">
                <a class="nav-link <%= currentPath.contains("/admin/inventory") ? "active" : "" %>" 
                   href="${pageContext.request.contextPath}/admin/inventory/list">
                    <i class="bi bi-box-seam"></i> 库存管理
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link <%= currentPath.contains("/purchase/shortage") ? "active" : "" %>" 
                   href="${pageContext.request.contextPath}/purchase/shortage/list">
                    <i class="bi bi-exclamation-triangle"></i> 缺书记录
                </a>
            </li>
                    <li class="nav-item">
                        <a class="nav-link <%= currentPath.contains("/report") ? "active" : "" %>" 
                           href="${pageContext.request.contextPath}/report/views">
                            <i class="bi bi-bar-chart"></i> 统计报表
                        </a>
                    </li>
                </ul>
                
                <!-- 右侧用户信息 -->
                <ul class="navbar-nav">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="userDropdown" role="button" 
                           data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="bi bi-person-circle"></i> <%= currentUser %>
                        </a>
                        <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="userDropdown">
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/customer/info">
                                <i class="bi bi-person"></i> 个人信息</a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/customer/orders">
                                <i class="bi bi-clock-history"></i> 订单历史</a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item" href="#">
                                <i class="bi bi-box-arrow-right"></i> 退出登录</a></li>
                        </ul>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

