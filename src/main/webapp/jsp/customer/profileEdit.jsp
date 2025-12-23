<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="model.Customer" %>
<%
    Customer customer = (Customer) request.getAttribute("customer");
    if (customer == null) {
        response.sendRedirect(request.getContextPath() + "/customer/info");
        return;
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户中心 - 修改个人信息</title>
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
                <div class="card shadow-sm">
                    <div class="card-header d-flex align-items-center gap-2">
                        <i class="bi bi-person-gear"></i>
                        <span>修改个人信息</span>
                    </div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/customer/updateProfile" method="POST" class="row g-3">
                            <div class="col-12">
                                <label class="form-label">邮箱 <span class="text-danger">*</span></label>
                                <input type="email" name="email" class="form-control" required
                                       value="<%= customer.getEmail() != null ? customer.getEmail() : "" %>">
                                <small class="text-muted">用于登录和通知，请确保可用</small>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">姓名</label>
                                <input type="text" name="name" class="form-control"
                                       value="<%= customer.getName() != null ? customer.getName() : "" %>">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">联系方式/地址</label>
                                <input type="text" name="address" class="form-control"
                                       value="<%= customer.getAddress() != null ? customer.getAddress() : "" %>">
                            </div>
                            <div class="col-12 d-flex justify-content-end gap-2">
                                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/customer/info">
                                    <i class="bi bi-arrow-left"></i> 返回
                                </a>
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-save"></i> 保存修改
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="/jsp/common/footer.jsp"/>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/common.js"></script>
</body>
</html>

