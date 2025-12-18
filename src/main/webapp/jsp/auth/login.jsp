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
<body class="bg-light">
<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-6 col-lg-4">
            <div class="card shadow-sm">
                <div class="card-header text-center">
                    <h4 class="mb-0">
                        <i class="bi bi-box-arrow-in-right"></i>
                        网上书店登录
                    </h4>
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
                            <div class="d-flex gap-3">
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="role" id="roleCustomer"
                                           value="CUSTOMER" <%= "CUSTOMER".equals(role) ? "checked" : "" %>>
                                    <label class="form-check-label" for="roleCustomer">
                                        普通客户
                                    </label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="role" id="roleAdmin"
                                           value="ADMIN" <%= "ADMIN".equals(role) ? "checked" : "" %>>
                                    <label class="form-check-label" for="roleAdmin">
                                        管理员
                                    </label>
                                </div>
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
                            <button type="submit" class="btn btn-primary">
                                登录
                            </button>
                            <a href="${pageContext.request.contextPath}/auth/register" class="btn btn-outline-secondary">
                                注册新客户
                            </a>
                        </div>
                    </form>
                </div>
                <div class="card-footer text-center text-muted small">
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

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>


