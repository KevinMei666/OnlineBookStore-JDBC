<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String role = request.getParameter("role");
    if (role == null || role.trim().isEmpty()) {
        Object roleAttr = request.getAttribute("role");
        if (roleAttr instanceof String) {
            role = (String) roleAttr;
        }
    }
    if (role == null || role.trim().isEmpty()) {
        role = "CUSTOMER";
    }
    role = role.trim().toUpperCase();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户登录 - 网上书店管理系统</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body class="auth-hero">
<div class="container">
    <div class="row justify-content-center">
        <div class="col-lg-10">
            <div class="row g-4 align-items-center">
                <div class="col-lg-6">
                    <div class="landing-hero">
                        <div class="d-inline-flex align-items-center gap-2 mb-3">
                            <span class="badge bg-primary-subtle text-primary">欢迎回来</span>
                            <span class="text-muted small">网上书店管理系统</span>
                        </div>
                        <h1 class="mb-3">一站式书店运营与实验平台</h1>
                        <p class="mb-4">
                            支持书目查询、下单与购物车、库存与采购、物流发货、客户与财务统计，覆盖完整业务流，适合教学与实验演示。
                        </p>
                        <div class="d-flex flex-wrap gap-2">
                            <span class="badge bg-primary text-white">图书/库存</span>
                            <span class="badge bg-success text-white">订单/发货</span>
                            <span class="badge bg-info text-white">采购/缺书</span>
                            <span class="badge bg-warning text-dark">统计/报表</span>
                        </div>
                    </div>
                </div>
                <div class="col-lg-6">
                    <div class="card auth-card">
                        <div class="card-header">
                            <h4 class="mb-1"><i class="bi bi-box-arrow-in-right"></i> 登录</h4>
                            <div class="text-muted small">请选择身份并输入账号密码</div>
                        </div>
                        <div class="card-body">
                            <c:if test="${not empty errorMessage}">
                                <div class="alert alert-danger">${errorMessage}</div>
                            </c:if>
                            <c:if test="${not empty warningMessage}">
                                <div class="alert alert-warning">${warningMessage}</div>
                            </c:if>
                            <c:if test="${not empty successMessage}">
                                <div class="alert alert-success">${successMessage}</div>
                            </c:if>

                            <form action="${pageContext.request.contextPath}/auth/login" method="post">
                                <div class="mb-3">
                                    <label class="form-label">登录身份</label>
                                    <div class="auth-switch">
                                        <label class="form-check">
                                            <input class="form-check-input" type="radio" name="role" id="roleCustomer"
                                                   value="CUSTOMER" <%= "CUSTOMER".equals(role) ? "checked" : "" %>>
                                            <span class="switch-pill">普通客户</span>
                                        </label>
                                        <label class="form-check">
                                            <input class="form-check-input" type="radio" name="role" id="roleAdmin"
                                                   value="ADMIN" <%= "ADMIN".equals(role) ? "checked" : "" %>>
                                            <span class="switch-pill">管理员</span>
                                        </label>
                                    </div>
                                </div>

                                <div class="mb-3">
                                    <label for="username" class="form-label">
                                        <span class="customer-label" style="<%= "CUSTOMER".equals(role) ? "" : "display:none;" %>">邮箱</span>
                                        <span class="admin-label" style="<%= "ADMIN".equals(role) ? "" : "display:none;" %>">管理员账号</span>
                                    </label>
                                    <input type="text" class="form-control" id="username" name="username"
                                           placeholder="请输入邮箱或管理员账号" required>
                                </div>

                                <div class="mb-3">
                                    <label for="password" class="form-label">密码</label>
                                    <input type="password" class="form-control" id="password" name="password"
                                           placeholder="请输入密码" required>
                                </div>

                                <div class="d-grid gap-2">
                                    <button type="submit" class="btn btn-primary btn-lg">
                                        登录
                                    </button>
                                    <a href="${pageContext.request.contextPath}/auth/register" class="btn btn-outline-secondary">
                                        注册新客户
                                    </a>
                                </div>
                            </form>
                        </div>
                        <div class="card-footer text-center text-muted small auth-tip">
                            <span class="customer-label" style="<%= "CUSTOMER".equals(role) ? "" : "display:none;" %>">
                                客户使用注册邮箱登录。
                            </span>
                            <span class="admin-label" style="<%= "ADMIN".equals(role) ? "" : "display:none;" %>">
                                管理员由系统预先创建账号。
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    (function() {
        const customerLabelNodes = document.querySelectorAll('.customer-label');
        const adminLabelNodes = document.querySelectorAll('.admin-label');
        const usernameInput = document.getElementById('username');
        const radios = document.querySelectorAll('input[name="role"]');
        function updateRole() {
            const isAdmin = document.getElementById('roleAdmin').checked;
            customerLabelNodes.forEach(n => n.style.display = isAdmin ? 'none' : '');
            adminLabelNodes.forEach(n => n.style.display = isAdmin ? '' : 'none');
            usernameInput.placeholder = isAdmin ? '请输入管理员账号' : '请输入邮箱';
        }
        radios.forEach(r => r.addEventListener('change', updateRole));
        updateRole();
    })();
</script>
</body>
</html>


