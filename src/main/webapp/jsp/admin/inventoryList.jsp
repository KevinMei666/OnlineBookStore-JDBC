<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>库存管理 - 后台管理</title>
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
                    <h2 class="h5 mb-1">库存管理</h2>
                    <p class="text-muted small mb-0">低库存自动生成缺书记录，库存充足时也可手动生成。</p>
                </div>
            </div>

            <form class="row g-3 mb-3" method="get"
                  action="${pageContext.request.contextPath}/admin/inventory/list">
                <div class="col-md-3">
                    <label for="threshold" class="form-label">最低库存量（阈值）</label>
                    <input type="number" min="0" class="form-control" id="threshold" name="threshold"
                           value="${threshold}" data-label="最低库存量" required>
                    <small class="text-muted">库存量低于该值的书目将被标记为低库存。</small>
                </div>
                <div class="col-md-3 align-self-end">
                    <button type="submit" class="btn btn-primary me-2">刷新</button>
                    <a href="${pageContext.request.contextPath}/admin/inventory/list" class="btn btn-outline-secondary">重置</a>
                </div>
            </form>

            <div class="table-responsive">
                <table class="table table-striped table-hover table-bordered align-middle">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">序号</th>
                        <th scope="col">书名</th>
                        <th scope="col">出版社</th>
                        <th scope="col">库存量</th>
                        <th scope="col">最低库存量</th>
                        <th scope="col">缺书数量</th>
                        <th scope="col" class="text-center">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="r" items="${inventoryRows}">
                        <tr class="<c:if test='${r.lowStock}'>table-warning</c:if>">
                            <td>${r.bookId}</td>
                            <td>${r.title}</td>
                            <td>${r.publisher}</td>
                            <td>
                                <span class="fw-semibold">${r.stockQuantity}</span>
                                <c:if test="${r.lowStock}">
                                    <span class="badge bg-warning text-dark ms-2">低库存</span>
                                </c:if>
                                <c:if test="${not r.lowStock}">
                                    <span class="badge bg-success ms-2">充足</span>
                                </c:if>
                            </td>
                            <td>${r.minStock}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${r.lowStock}">
                                        <span class="text-danger fw-semibold">${r.shortageQty}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-muted">0</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-center">
                                <c:choose>
                                    <c:when test="${r.shortageExists}">
                                        <button class="btn btn-sm btn-outline-secondary" disabled>已存在缺书记录</button>
                                    </c:when>
                                    <c:otherwise>
                                        <form method="post" action="${pageContext.request.contextPath}/admin/inventory/generateShortage"
                                              class="d-inline-flex align-items-center gap-2">
                                            <input type="hidden" name="bookId" value="${r.bookId}">
                                            <input type="hidden" name="threshold" value="${threshold}">
                                            <input type="number" name="shortageQty" class="form-control form-control-sm"
                                                   style="width: 90px;" min="1"
                                                   placeholder="数量">
                                            <button type="submit" class="btn btn-sm btn-outline-primary">
                                                生成缺书记录
                                            </button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty inventoryRows}">
                        <tr>
                            <td colspan="7" class="text-center text-muted py-4">暂无库存数据</td>
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


