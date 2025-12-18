<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>供应商管理 - 后台管理</title>
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
                    <h2 class="h5 mb-1">供应商管理</h2>
                    <p class="text-muted small mb-0">
                        管理供应商基础信息（名称、联系方式、地址），为采购与供货关系维护提供支持。
                    </p>
                </div>
                <a href="${pageContext.request.contextPath}/admin/supplier/add" class="btn btn-success">
                    <i class="bi bi-plus-lg"></i> 新增供应商
                </a>
            </div>

            <form class="row g-3 mb-3" method="get"
                  action="${pageContext.request.contextPath}/admin/supplier/list">
                <div class="col-md-3">
                    <label for="supplierId" class="form-label">供应商ID</label>
                    <input type="text" class="form-control" id="supplierId" name="supplierId"
                           value="${searchSupplierId}" placeholder="精确匹配供应商ID">
                </div>
                <div class="col-md-4">
                    <label for="name" class="form-label">供应商名称</label>
                    <input type="text" class="form-control" id="name" name="name"
                           value="${searchName}" placeholder="支持模糊查询">
                </div>
                <div class="col-md-3 align-self-end">
                    <button type="submit" class="btn btn-primary me-2">查询</button>
                    <a href="${pageContext.request.contextPath}/admin/supplier/list"
                       class="btn btn-outline-secondary">重置</a>
                </div>
            </form>

            <div class="table-responsive">
                <table class="table table-striped table-hover table-bordered align-middle">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">供应商ID</th>
                        <th scope="col">名称</th>
                        <th scope="col">联系电话</th>
                        <th scope="col">联系邮箱</th>
                        <th scope="col">地址</th>
                        <th scope="col" class="text-center">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="s" items="${suppliers}">
                        <tr>
                            <td>${s.supplierId}</td>
                            <td>${s.name}</td>
                            <td>${s.phone}</td>
                            <td>${s.contactEmail}</td>
                            <td>${s.address}</td>
                            <td class="text-center">
                                <a href="${pageContext.request.contextPath}/admin/supplier/supply?id=${s.supplierId}"
                                   class="btn btn-sm btn-outline-secondary me-1">
                                    供货书目
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/supplier/edit?id=${s.supplierId}"
                                   class="btn btn-sm btn-outline-primary me-1">
                                    编辑
                                </a>
                                <form action="${pageContext.request.contextPath}/admin/supplier/delete" method="post"
                                      style="display:inline;" onsubmit="return confirm('确定删除该供应商吗？删除后无法恢复！');">
                                    <input type="hidden" name="id" value="${s.supplierId}">
                                    <button type="submit" class="btn btn-sm btn-outline-danger">删除</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty suppliers}">
                        <tr>
                            <td colspan="6" class="text-center text-muted py-4">
                                暂无供应商数据
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


