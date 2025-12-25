<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>供应商供货书目 - 后台管理</title>
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
                    <h2 class="h5 mb-1">供应商供货书目</h2>
                    <p class="text-muted small mb-0">
                        供应商：<span class="fw-semibold">${supplier.name}</span>
                        （ID=${supplier.supplierId}）
                    </p>
                </div>
                <div>
                    <a href="${pageContext.request.contextPath}/admin/supplier/list" class="btn btn-outline-secondary">
                        返回供应商列表
                    </a>
                </div>
            </div>

            <div class="table-responsive">
                <table class="table table-striped table-hover table-bordered align-middle">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">序号</th>
                        <th scope="col">书名</th>
                        <th scope="col">出版社</th>
                        <th scope="col">零售价</th>
                        <th scope="col">库存</th>
                        <th scope="col">供货价</th>
                        <th scope="col" class="text-center">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="b" items="${supplyBooks}">
                        <tr>
                            <td>${b.bookId}</td>
                            <td>${b.title}</td>
                            <td>${b.publisher}</td>
                            <td>${b.retailPrice}</td>
                            <td>${b.stockQuantity}</td>
                            <td>${b.supplyPrice}</td>
                            <td class="text-center">
                                <div class="d-flex justify-content-center gap-2 flex-wrap">
                                    <a class="btn btn-sm btn-outline-primary"
                                       href="${pageContext.request.contextPath}/book/detail?bookId=${b.bookId}">
                                        查看图书
                                    </a>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty supplyBooks}">
                        <tr>
                            <td colspan="7" class="text-center text-muted py-4">
                                该供应商暂无供货书目（BookSupplier 无记录）
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


