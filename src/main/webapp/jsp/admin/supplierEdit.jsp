<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>编辑供应商信息 - 后台管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/jsp/common/header.jsp"/>
<jsp:include page="/jsp/common/message.jsp"/>

<div class="container mt-4 mb-5">
    <div class="card shadow-sm border-0">
        <div class="card-body">
            <h2 class="h5 mb-3">编辑供应商信息</h2>
            <p class="text-muted small mb-4">
                请根据需要调整供应商基础信息（名称、联系方式、地址）。采购模块会引用供应商信息。
            </p>

            <form id="supplierEditForm" method="post"
                  action="${pageContext.request.contextPath}/admin/supplier/update">
                <input type="hidden" name="supplierId" value="${supplier.supplierId}"/>

                <div class="row g-3 mb-3">
                    <div class="col-md-4">
                        <label class="form-label">供应商ID</label>
                        <input type="text" class="form-control" value="${supplier.supplierId}" readonly>
                    </div>
                    <div class="col-md-8">
                        <label for="name" class="form-label">供应商名称 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="name" name="name"
                               data-label="供应商名称"
                               value="${supplier.name}" required maxlength="100">
                    </div>
                </div>

                <div class="row g-3 mb-3">
                    <div class="col-md-6">
                        <label for="phone" class="form-label">联系电话</label>
                        <input type="text" class="form-control" id="phone" name="phone"
                               data-label="联系电话"
                               value="${supplier.phone}" maxlength="50">
                    </div>
                    <div class="col-md-6">
                        <label for="contactEmail" class="form-label">联系邮箱</label>
                        <input type="email" class="form-control" id="contactEmail" name="contactEmail"
                               data-label="联系邮箱"
                               value="${supplier.contactEmail}" maxlength="120">
                    </div>
                </div>

                <div class="row g-3 mb-4">
                    <div class="col-md-12">
                        <label for="address" class="form-label">地址</label>
                        <input type="text" class="form-control" id="address" name="address"
                               data-label="地址"
                               value="${supplier.address}" maxlength="255">
                    </div>
                </div>

                <div class="mt-3">
                    <button type="submit" class="btn btn-primary me-2">保存</button>
                    <a href="${pageContext.request.contextPath}/admin/supplier/list" class="btn btn-outline-secondary">
                        返回列表
                    </a>
                </div>
            </form>

            <hr class="my-4">
            <h5 class="mb-3">供货书目</h5>
            <p class="text-muted small">勾选供货图书并填写供货价，提交后会覆盖原有供货关系。</p>
            <div class="table-responsive">
                <table class="table table-sm table-bordered align-middle">
                    <thead class="table-light">
                    <tr>
                        <th>选择</th>
                        <th>书号</th>
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
                                <input type="checkbox" class="form-check-input" name="bookId" value="${b.bookId}"
                                       <c:if test="${not empty supplyPriceMap[b.bookId]}">checked</c:if>>
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
        </div>
    </div>
</div>

<jsp:include page="/jsp/common/footer.jsp"/>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/common.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        const rules = {
            name: {required: true, message: '供应商名称不能为空'}
        };
        const form = document.getElementById('supplierEditForm');
        form.addEventListener('submit', function (e) {
            if (!validateForm('supplierEditForm', rules)) {
                e.preventDefault();
            }
        });
    });
</script>
</body>
</html>


