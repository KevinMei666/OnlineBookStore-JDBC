<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    // 获取当前请求路径，用于高亮当前页面
    String currentPath = request.getRequestURI();
    String contextPath = request.getContextPath();
    if (contextPath != null && !contextPath.isEmpty()) {
        currentPath = currentPath.substring(contextPath.length());
    }
    
    // 获取当前登录用户（从session获取）
    String currentUser = (String) session.getAttribute("currentUser");
    String currentRole = (String) session.getAttribute("currentRole");
    boolean loggedIn = currentUser != null && !currentUser.isEmpty();
    boolean isAdmin = "ADMIN".equals(currentRole);
    boolean isCustomer = "CUSTOMER".equals(currentRole);
%>
<!-- 图标字体（Bootstrap Icons） -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">

<div class="app-shell">
    <aside class="app-sidebar">
        <div class="brand">
            <i class="bi bi-book"></i>
            <span>网上书店管理系统</span>
        </div>

        <div class="sidebar-section">
            <div class="sidebar-section-title">常用</div>
            <ul class="sidebar-nav">
                <li>
                    <a class="sidebar-link <%= currentPath.equals("/index.jsp") || currentPath.equals("/") ? "active" : "" %>"
                           href="${pageContext.request.contextPath}/index.jsp">
                            <i class="bi bi-house-door"></i> 首页
                        </a>
                    </li>
                <li>
                    <a class="sidebar-link <%= currentPath.contains("/book") ? "active" : "" %>"
                           href="${pageContext.request.contextPath}/book">
                            <i class="bi bi-search"></i> 书目查询
                        </a>
                    </li>
                    <% if (loggedIn) { %>
                <li>
                    <a class="sidebar-link <%= currentPath.contains("/order/cart") ? "active" : "" %>"
                               href="${pageContext.request.contextPath}/order/cart">
                                <i class="bi bi-cart-plus"></i> 下单
                            </a>
                        </li>
                <li>
                    <a class="sidebar-link <%= (currentPath.contains("/order") && !currentPath.contains("/order/cart")) ? "active" : "" %>"
                               href="${pageContext.request.contextPath}/order/list">
                                <i class="bi bi-list-ul"></i> 订单管理
                            </a>
                        </li>
                    <% } %>
            </ul>
        </div>

                    <% if (isAdmin) { %>
        <div class="sidebar-section">
            <div class="sidebar-section-title">后台管理</div>
            <ul class="sidebar-nav">
                <li>
                    <a class="sidebar-link <%= currentPath.contains("/admin/customer") ? "active" : "" %>"
                               href="${pageContext.request.contextPath}/admin/customer/list">
                                <i class="bi bi-people"></i> 客户管理
                            </a>
                        </li>
                <li>
                    <a class="sidebar-link <%= currentPath.contains("/admin/inventory") ? "active" : "" %>"
                               href="${pageContext.request.contextPath}/admin/inventory/list">
                                <i class="bi bi-box-seam"></i> 库存管理
                            </a>
                        </li>
                <li>
                    <a class="sidebar-link <%= currentPath.contains("/admin/book") ? "active" : "" %>"
                               href="${pageContext.request.contextPath}/admin/book/list">
                                <i class="bi bi-journal-bookmark"></i> 图书管理
                            </a>
                        </li>
                <li>
                    <a class="sidebar-link <%= currentPath.contains("/purchase/shortage") ? "active" : "" %>"
                               href="${pageContext.request.contextPath}/purchase/shortage/list">
                                <i class="bi bi-exclamation-triangle"></i> 缺书记录
                            </a>
                        </li>
                <li>
                    <a class="sidebar-link <%= (currentPath.contains("/purchase") && !currentPath.contains("/purchase/shortage")) ? "active" : "" %>"
                               href="${pageContext.request.contextPath}/purchase/list">
                                <i class="bi bi-box-seam"></i> 采购管理
                            </a>
                        </li>
                <li>
                    <a class="sidebar-link <%= currentPath.contains("/admin/supplier") ? "active" : "" %>"
                               href="${pageContext.request.contextPath}/admin/supplier/list">
                                <i class="bi bi-building"></i> 供应商管理
                            </a>
                        </li>
                <li>
                    <a class="sidebar-link <%= currentPath.contains("/shipment") ? "active" : "" %>"
                               href="${pageContext.request.contextPath}/shipment/list">
                                <i class="bi bi-truck"></i> 物流管理
                            </a>
                        </li>
                <li>
                    <a class="sidebar-link <%= currentPath.contains("/report") ? "active" : "" %>"
                               href="${pageContext.request.contextPath}/report/views">
                                <i class="bi bi-bar-chart"></i> 统计报表
                            </a>
                        </li>
                <li>
                    <a class="sidebar-link <%= currentPath.contains("/finance") ? "active" : "" %>"
                               href="${pageContext.request.contextPath}/finance/statistics">
                                <i class="bi bi-cash-stack"></i> 财务统计
                            </a>
                        </li>
            </ul>
        </div>
                    <% } %>

        <div class="sidebar-footer">
            <div class="d-flex align-items-center gap-2">
                <i class="bi bi-database"></i>
                <span>数据库状态：<span class="text-success">已连接</span></span>
            </div>
            <div class="small text-muted mt-1">© 2024 网上书店管理系统</div>
        </div>
    </aside>
                
    <div class="app-main">
        <div class="app-topbar">
                    <% if (loggedIn) { %>
                <div class="user-chip">
                                <i class="bi bi-person-circle"></i>
                    <span><%= currentUser %></span>
                                <% if ("ADMIN".equals(currentRole)) { %>
                        <span class="badge bg-danger">管理员</span>
                                <% } %>
                </div>
                <a class="btn btn-link text-decoration-none ms-3" href="${pageContext.request.contextPath}/auth/logout">
                    <i class="bi bi-box-arrow-right"></i> 退出
                </a>
                                <% } else { %>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/jsp/auth/login.jsp">
                                <i class="bi bi-box-arrow-in-right"></i> 登录
                            </a>
                    <% } %>
            </div>
        <div class="app-content">




