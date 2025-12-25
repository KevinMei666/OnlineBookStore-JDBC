<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.Base64" %>
<%@ page import="dao.SeriesDao" %>
<%@ page import="model.Series" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>图书管理 - 后台管理</title>
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
                    <h2 class="h5 mb-1">图书上架/下架</h2>
                    <p class="text-muted small mb-0">仅影响前台可见性，不删除库存与历史记录。</p>
                </div>
                <div class="btn-group" role="group">
                    <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/admin/book/create">
                        <i class="bi bi-plus-circle"></i> 新增图书
                    </a>
                    <a class="btn btn-outline-primary btn-sm" href="${pageContext.request.contextPath}/admin/series/list">
                        <i class="bi bi-collection"></i> 丛书管理
                    </a>
                </div>
            </div>

            <div class="table-responsive">
                <table class="table table-striped table-hover table-bordered align-middle table-modern">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">序号</th>
                        <th scope="col">书号(ISBN)</th>
                        <th scope="col">书名</th>
                        <th scope="col">封面</th>
                        <th scope="col">出版社</th>
                        <th scope="col">丛书</th>
                        <th scope="col">价格</th>
                        <th scope="col">库存量</th>
                        <th scope="col">状态</th>
                        <th scope="col" class="text-center">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="b" items="${books}">
                        <%
                            String coverBase64 = null;
                            Object obj = pageContext.getAttribute("b");
                            if (obj instanceof model.Book) {
                                model.Book bk = (model.Book) obj;
                                byte[] coverBytes = bk.getCoverImage();
                                if (coverBytes != null && coverBytes.length > 0) {
                                    coverBase64 = Base64.getEncoder().encodeToString(coverBytes);
                                }
                            }
                        %>
                        <tr>
                            <td>${b.bookId}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty b.isbn}">
                                        <span class="text-muted small">${b.isbn}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-muted small">-</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>${b.title}</td>
                            <td style="width: 120px;">
                                <%
                                    if (coverBase64 != null) {
                                %>
                                    <img src="data:image/jpeg;base64,<%= coverBase64 %>"
                                         alt="<%= ((model.Book)pageContext.getAttribute("b")).getTitle() %>"
                                         class="img-fluid rounded" style="max-height: 80px; object-fit: cover;">
                                <%
                                    } else {
                                %>
                                    <span class="text-muted small">无</span>
                                <%
                                    }
                                %>
                            </td>
                            <td>${b.publisher}</td>
                            <td>
                                <%
                                    model.Book book = (model.Book) pageContext.getAttribute("b");
                                    if (book.getSeriesId() != null) {
                                        SeriesDao seriesDao = new SeriesDao();
                                        Series series = seriesDao.findById(book.getSeriesId());
                                        if (series != null) {
                                %>
                                    <a href="${pageContext.request.contextPath}/admin/series/edit?seriesId=<%= series.getSeriesId() %>" 
                                       class="text-decoration-none">
                                        <i class="bi bi-collection"></i> <%= series.getSeriesName() %>
                                    </a>
                                <%
                                        } else {
                                %>
                                    <span class="text-muted small">ID: <%= book.getSeriesId() %></span>
                                <%
                                        }
                                    } else {
                                %>
                                    <span class="text-muted small">-</span>
                                <%
                                    }
                                %>
                            </td>
                            <td>¥ ${b.price}</td>
                            <td>${b.stockQuantity}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${b.active == null || b.active}">
                                        <span class="badge bg-success">已上架</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-secondary">已下架</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-center">
                                <c:choose>
                                    <c:when test="${b.active == null || b.active}">
                                        <form method="post" action="${pageContext.request.contextPath}/admin/book/toggle"
                                              class="d-inline" onsubmit="return confirm('确认下架该书？');">
                                            <input type="hidden" name="bookId" value="${b.bookId}">
                                            <input type="hidden" name="active" value="0">
                                            <button type="submit" class="btn btn-sm btn-outline-secondary">
                                                <i class="bi bi-toggle-off"></i> 下架
                                            </button>
                                        </form>
                                        <a class="btn btn-sm btn-outline-primary ms-1"
                                           href="${pageContext.request.contextPath}/admin/book/edit?bookId=${b.bookId}">
                                            <i class="bi bi-pencil-square"></i> 编辑
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <form method="post" action="${pageContext.request.contextPath}/admin/book/toggle"
                                              class="d-inline">
                                            <input type="hidden" name="bookId" value="${b.bookId}">
                                            <input type="hidden" name="active" value="1">
                                            <button type="submit" class="btn btn-sm btn-outline-primary">
                                                <i class="bi bi-toggle-on"></i> 上架
                                            </button>
                                        </form>
                                        <a class="btn btn-sm btn-outline-primary ms-1"
                                           href="${pageContext.request.contextPath}/admin/book/edit?bookId=${b.bookId}">
                                            <i class="bi bi-pencil-square"></i> 编辑
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty books}">
                        <tr>
                            <td colspan="7" class="text-center text-muted py-4">暂无图书数据</td>
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

