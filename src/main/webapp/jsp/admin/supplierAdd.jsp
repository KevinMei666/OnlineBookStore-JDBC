<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>新增供应商 - 后台管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/jsp/common/header.jsp"/>
<jsp:include page="/jsp/common/message.jsp"/>

<div class="container mt-4 mb-5">
    <div class="card shadow-sm border-0">
        <div class="card-body">
            <h2 class="h5 mb-4">新增供应商</h2>

            <form method="post" action="${pageContext.request.contextPath}/admin/supplier/save">
                <div class="row mb-3">
                    <label for="name" class="col-sm-2 col-form-label">供应商名称 <span class="text-danger">*</span></label>
                    <div class="col-sm-6">
                        <input type="text" class="form-control" id="name" name="name" required>
                    </div>
                </div>
                <div class="row mb-3">
                    <label for="phone" class="col-sm-2 col-form-label">联系电话</label>
                    <div class="col-sm-6">
                        <input type="text" class="form-control" id="phone" name="phone">
                    </div>
                </div>
                <div class="row mb-3">
                    <label for="contactEmail" class="col-sm-2 col-form-label">联系邮箱</label>
                    <div class="col-sm-6">
                        <input type="email" class="form-control" id="contactEmail" name="contactEmail">
                    </div>
                </div>
                <div class="row mb-3">
                    <label for="address" class="col-sm-2 col-form-label">地址</label>
                    <div class="col-sm-6">
                        <textarea class="form-control" id="address" name="address" rows="2"></textarea>
                    </div>
                </div>

                <hr class="my-4">
                <h5 class="mb-3">供货书目</h5>
                <p class="text-muted small">从现有图书中选择并填写供货价，可多选。</p>
                <div class="table-responsive">
                    <table class="table table-sm table-bordered align-middle">
                        <thead class="table-light">
                        <tr>
                            <th>选择</th>
                            <th>序号</th>
                            <th>书名</th>
                            <th>出版社</th>
                            <th>零售价</th>
                            <th style="width: 160px;">供货价</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="b" items="${books}">
                            <tr>
                                <td class="text-center">
                                    <input type="checkbox" class="form-check-input" name="bookId" value="${b.bookId}">
                                </td>
                                <td>${b.bookId}</td>
                                <td>${b.title}</td>
                                <td>${b.publisher}</td>
                                <td>${b.price}</td>
                                <td>
                                    <input type="number" step="0.01" class="form-control form-control-sm"
                                           name="supplyPrice_${b.bookId}" placeholder="供货价"
                                           value="${supplyPriceMap[b.bookId]}">
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>

                <div class="row mt-4">
                    <div class="col-sm-6 offset-sm-2">
                        <button type="submit" class="btn btn-primary me-2">保存</button>
                        <a href="${pageContext.request.contextPath}/admin/supplier/list" class="btn btn-secondary">取消</a>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/jsp/common/footer.jsp"/>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

