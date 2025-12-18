<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>客户注册 - 网上书店管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">
            <div class="card shadow-sm">
                <div class="card-header text-center">
                    <h4 class="mb-0">
                        客户注册
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

                    <form action="${pageContext.request.contextPath}/auth/register" method="post">
                        <div class="mb-3">
                            <label for="email" class="form-label">邮箱<span class="text-danger">*</span></label>
                            <input type="email" class="form-control" id="email" name="email"
                                   placeholder="请输入常用邮箱" required>
                        </div>

                        <div class="mb-3">
                            <label for="name" class="form-label">姓名<span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="name" name="name"
                                   placeholder="请输入您的姓名" required>
                        </div>

                        <div class="mb-3">
                            <label for="address" class="form-label">收货地址</label>
                            <textarea class="form-control" id="address" name="address" rows="2"
                                      placeholder="用于收货的详细地址"></textarea>
                        </div>

                        <div class="mb-3">
                            <label for="password" class="form-label">密码<span class="text-danger">*</span></label>
                            <input type="password" class="form-control" id="password" name="password"
                                   placeholder="请输入登录密码" minlength="6" required>
                        </div>

                        <div class="mb-3">
                            <label for="confirmPassword" class="form-label">确认密码<span class="text-danger">*</span></label>
                            <input type="password" class="form-control" id="confirmPassword" name="confirmPassword"
                                   placeholder="请再次输入密码" minlength="6" required>
                        </div>

                        <div class="mb-3 form-text">
                            初始信用等级默认为 1 级，余额为 0，后续可由管理员根据消费情况调整信用等级和额度。
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary">
                                立即注册
                            </button>
                            <a href="${pageContext.request.contextPath}/auth/login" class="btn btn-outline-secondary">
                                返回登录
                            </a>
                        </div>
                    </form>
                </div>
                <div class="card-footer text-center text-muted small">
                    注册即表示您同意本系统的使用规范。
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>


