<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.util.Base64" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>编辑图书 - 后台管理</title>
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
                    <h2 class="h5 mb-1">编辑图书</h2>
                    <p class="text-muted small mb-0">下架状态也可编辑，保存后可选择重新上架。</p>
                </div>
                <a href="${pageContext.request.contextPath}/admin/book/list" class="btn btn-outline-secondary btn-sm">
                    返回列表
                </a>
            </div>

            <c:if test="${empty book}">
                <div class="alert alert-danger">未找到书籍信息。</div>
            </c:if>

            <c:if test="${not empty book}">
                <form class="row g-3" method="post" action="${pageContext.request.contextPath}/admin/book/save"
                      enctype="multipart/form-data">
                    <input type="hidden" name="bookId" value="${book.bookId}">

                    <div class="col-md-6">
                        <label class="form-label">书名</label>
                        <input type="text" class="form-control" name="title" value="${book.title}" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">出版社</label>
                        <input type="text" class="form-control" name="publisher" value="${book.publisher}">
                    </div>

                    <div class="col-md-4">
                        <label class="form-label">价格</label>
                        <input type="number" step="0.01" min="0" class="form-control" name="price"
                               value="${book.price}">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">库存量</label>
                        <input type="number" min="0" class="form-control" name="stockQuantity"
                               value="${book.stockQuantity}">
                    </div>
                    <div class="col-md-4 d-flex align-items-end">
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" name="active" id="active"
                                   <c:if test="${book.active == null || book.active}">checked</c:if>>
                            <label class="form-check-label" for="active">
                                上架（保存后立即对前台可见）
                            </label>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label">存放位置</label>
                        <input type="text" class="form-control" name="location" value="${book.location}">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">目录/简介</label>
                        <textarea class="form-control" name="catalog" rows="3">${book.catalog}</textarea>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label">封面上传（jpg/png，小于5MB）</label>
                        <input type="file" class="form-control" name="coverImage" accept="image/*">
                        <small class="text-muted">若不选择文件则保持原封面。</small>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">当前封面</label>
                        <div class="border rounded p-2 bg-light text-center">
                            <%
                                String coverBase64 = null;
                                if (request.getAttribute("book") instanceof model.Book) {
                                    model.Book bk = (model.Book) request.getAttribute("book");
                                    byte[] coverBytes = bk.getCoverImage();
                                    if (coverBytes != null && coverBytes.length > 0) {
                                        coverBase64 = Base64.getEncoder().encodeToString(coverBytes);
                                    }
                                }
                                if (coverBase64 != null) {
                            %>
                                <img src="data:image/jpeg;base64,<%= coverBase64 %>"
                                     alt="${book.title}" class="img-fluid rounded" style="max-height: 200px; object-fit: contain;">
                            <%
                                } else {
                            %>
                                <span class="text-muted">暂无封面</span>
                            <%
                                }
                            %>
                        </div>
                    </div>

                    <div class="col-12">
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-save"></i> 保存
                        </button>
                        <a href="${pageContext.request.contextPath}/admin/book/list" class="btn btn-outline-secondary ms-2">
                            取消
                        </a>
                    </div>
                </form>
            </c:if>
        </div>
    </div>
</div>

<jsp:include page="/jsp/common/footer.jsp"/>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/common.js"></script>
</body>
</html>

