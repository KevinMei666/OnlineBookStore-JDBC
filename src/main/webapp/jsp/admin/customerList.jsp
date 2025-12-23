<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>客户账户与信用等级管理 - 后台管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/jsp/common/header.jsp"/>
<jsp:include page="/jsp/common/message.jsp"/>

<div class="container mt-4 mb-5">
    <div class="card shadow-sm border-0">
        <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                    <h2 class="h5 mb-1">客户账户与信用等级管理</h2>
                    <p class="text-muted small mb-0">
                        管理客户余额与信用等级，为订单与发货业务提供信用支持。
                    </p>
                </div>
                        <div>
                            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/customer/create">
                                <i class="bi bi-person-plus"></i> 新增客户
                            </a>
                        </div>
            </div>

            <form class="row g-3 mb-3" method="get"
                  action="${pageContext.request.contextPath}/admin/customer/list">
                <div class="col-md-3">
                    <label for="customerId" class="form-label">客户ID</label>
                    <input type="text" class="form-control" id="customerId" name="customerId"
                           value="${searchCustomerId}" placeholder="精确匹配客户ID">
                </div>
                <div class="col-md-4">
                    <label for="name" class="form-label">客户姓名</label>
                    <input type="text" class="form-control" id="name" name="name"
                           value="${searchName}" placeholder="支持模糊查询">
                </div>
                <div class="col-md-3 align-self-end">
                    <button type="submit" class="btn btn-primary me-2">查询</button>
                    <a href="${pageContext.request.contextPath}/admin/customer/list"
                       class="btn btn-outline-secondary">重置</a>
                </div>
            </form>

            <div class="table-responsive">
                <table class="table table-striped table-hover table-bordered align-middle">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">客户ID</th>
                        <th scope="col">姓名</th>
                        <th scope="col">邮箱</th>
                        <th scope="col">地址</th>
                        <th scope="col">余额</th>
                        <th scope="col">信用等级</th>
                        <th scope="col">月度透支额度</th>
                        <th scope="col" class="text-center">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="c" items="${customers}">
                        <tr>
                            <td>${c.customerId}</td>
                            <td>${c.name}</td>
                            <td>${c.email}</td>
                            <td>${c.address}</td>
                            <td>${c.balance}</td>
                            <td>${c.creditLevel}</td>
                            <td>${c.monthlyLimit}</td>
                            <td class="text-center">
                                <a href="${pageContext.request.contextPath}/admin/customer/edit?id=${c.customerId}"
                                   class="btn btn-sm btn-outline-primary">
                                    编辑
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty customers}">
                        <tr>
                            <td colspan="7" class="text-center text-muted py-4">
                                暂无客户数据
                            </td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/jsp/common/footer.jsp"/>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/common.js"></script>
</body>
</html>


