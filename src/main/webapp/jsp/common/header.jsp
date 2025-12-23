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

    // 页面标题信息：优先取页面传入，否则根据路由推断
    String pageTitle = (String) request.getAttribute("pageTitle");
    String pageSubtitle = (String) request.getAttribute("pageSubtitle");
    String pageIcon = (String) request.getAttribute("pageIcon");
    if (pageTitle == null || pageTitle.isEmpty()) {
        if (currentPath.contains("/book")) {
            pageTitle = "书目查询";
            pageIcon = "bi-book";
        } else if (currentPath.contains("/order/cart")) {
            pageTitle = "下单";
            pageIcon = "bi-cart-plus";
        } else if (currentPath.contains("/order")) {
            pageTitle = "订单管理";
            pageIcon = "bi-list-ul";
        } else if (currentPath.contains("/customer")) {
            pageTitle = "我的钱包";
            pageIcon = "bi-wallet2";
        } else if (currentPath.contains("/admin")) {
            pageTitle = "后台管理";
            pageIcon = "bi-speedometer2";
        } else if (currentPath.contains("/purchase")) {
            pageTitle = "采购管理";
            pageIcon = "bi-box-seam";
        } else if (currentPath.contains("/shipment")) {
            pageTitle = "物流管理";
            pageIcon = "bi-truck";
        } else if (currentPath.contains("/report")) {
            pageTitle = "统计报表";
            pageIcon = "bi-bar-chart";
        } else if (currentPath.contains("/finance")) {
            pageTitle = "财务统计";
            pageIcon = "bi-cash-stack";
        } else {
            pageTitle = "首页";
            pageIcon = "bi-house-door";
        }
    }
    if (pageIcon == null || pageIcon.isEmpty()) {
        pageIcon = "bi-dot";
    }
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
                    <% if (!isAdmin) { %>
                <li>
                    <a class="sidebar-link <%= currentPath.contains("/order/cart") ? "active" : "" %>"
                               href="${pageContext.request.contextPath}/order/cart">
                                <i class="bi bi-cart-plus"></i> 下单
                            </a>
                        </li>
                    <% } %>
                <li>
                    <a class="sidebar-link <%= (currentPath.contains("/order") && !currentPath.contains("/order/cart")) ? "active" : "" %>"
                               href="${pageContext.request.contextPath}/order/list">
                                <i class="bi bi-list-ul"></i> 订单管理
                            </a>
                        </li>
                    <% } %>
            </ul>
        </div>

                    <% if (isCustomer) { %>
        <div class="sidebar-section">
            <div class="sidebar-section-title">我的账户</div>
            <ul class="sidebar-nav">
                <li>
                    <a class="sidebar-link <%= currentPath.contains("/customer") ? "active" : "" %>"
                           href="${pageContext.request.contextPath}/customer/info">
                            <i class="bi bi-wallet2"></i> 我的钱包
                        </a>
                    </li>
            </ul>
        </div>
                    <% } %>

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
            <div class="small text-muted mt-1">© 2025 网上书店管理系统</div>
        </div>
    </aside>
                
    <div class="app-main">
        <div class="app-topbar">
            <div class="topbar-title flex-wrap">
                <div class="d-flex align-items-center gap-2">
                    <span class="d-inline-flex align-items-center justify-content-center rounded-circle bg-light" style="width: 40px; height: 40px;">
                        <i class="bi <%= pageIcon %> fs-5 text-primary"></i>
                    </span>
                    <div>
                        <div class="fw-semibold fs-5 mb-0"><%= pageTitle %></div>
                        <% if (pageSubtitle != null && !pageSubtitle.isEmpty()) { %>
                            <div class="text-muted small"><%= pageSubtitle %></div>
                        <% } %>
                    </div>
                </div>
            </div>
            <div class="topbar-actions">
                    <% if (loggedIn) { %>
                <div class="user-chip">
                                <i class="bi bi-person-circle"></i>
                    <span><%= currentUser %></span>
                                <% if ("ADMIN".equals(currentRole)) { %>
                        <span class="badge bg-danger">管理员</span>
                                <% } %>
                </div>
                <a class="btn btn-link text-decoration-none ms-1" href="${pageContext.request.contextPath}/auth/logout">
                    <i class="bi bi-box-arrow-right"></i> 退出
                </a>
                                <% } else { %>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/jsp/auth/login.jsp">
                                <i class="bi bi-box-arrow-in-right"></i> 登录
                            </a>
                    <% } %>
            </div>
            </div>
        <div class="app-content">





