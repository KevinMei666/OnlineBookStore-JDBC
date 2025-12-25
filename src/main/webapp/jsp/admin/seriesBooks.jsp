<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Series" %>
<%@ page import="model.Book" %>
<%@ page import="java.util.Base64" %>
<%
    Series series = (Series) request.getAttribute("series");
    List<Book> seriesBooks = (List<Book>) request.getAttribute("seriesBooks");
    List<Book> allBooks = (List<Book>) request.getAttribute("allBooks");
    
    if (series == null) {
        response.sendRedirect(request.getContextPath() + "/admin/series/list");
        return;
    }
    if (seriesBooks == null) {
        seriesBooks = new java.util.ArrayList<>();
    }
    if (allBooks == null) {
        allBooks = new java.util.ArrayList<>();
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理丛书书籍 - <%= series.getSeriesName() %> - 网上书店管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp"/>
    <jsp:include page="/jsp/common/message.jsp"/>
    
    <div class="container mt-4">
        <div class="row">
            <div class="col-12">
                <!-- 返回按钮和丛书信息 -->
                <div class="mb-3">
                    <a href="${pageContext.request.contextPath}/admin/series/list" class="btn btn-outline-secondary btn-sm">
                        <i class="bi bi-arrow-left"></i> 返回丛书列表
                    </a>
                </div>
                
                <!-- 丛书信息卡片 -->
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-primary text-white">
                        <h5 class="mb-0">
                            <i class="bi bi-collection"></i> <%= series.getSeriesName() %> - 书籍管理
                        </h5>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <p><strong>丛书ID:</strong> <%= series.getSeriesId() %></p>
                                <p><strong>出版社:</strong> <%= series.getPublisher() != null ? series.getPublisher() : "-" %></p>
                            </div>
                            <div class="col-md-6">
                                <p><strong>描述:</strong> <%= series.getDescription() != null ? series.getDescription() : "-" %></p>
                                <p><strong>当前书籍数量:</strong> <span class="badge bg-info"><%= seriesBooks.size() %></span></p>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- 添加书籍到丛书 -->
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-success text-white">
                        <h6 class="mb-0">
                            <i class="bi bi-plus-circle"></i> 添加书籍到丛书
                        </h6>
                    </div>
                    <div class="card-body">
                        <form method="POST" action="${pageContext.request.contextPath}/admin/series/books/add">
                            <input type="hidden" name="seriesId" value="<%= series.getSeriesId() %>">
                            <div class="row g-3">
                                <div class="col-md-8">
                                    <label for="bookId" class="form-label">选择书籍</label>
                                    <select class="form-select" id="bookId" name="bookId" required>
                                        <option value="">-- 请选择书籍 --</option>
                                        <% 
                                            for (Book book : allBooks) {
                                                // 排除已经是该丛书的书籍
                                                if (book.getSeriesId() != null && book.getSeriesId().equals(series.getSeriesId())) {
                                                    continue;
                                                }
                                        %>
                                            <option value="<%= book.getBookId() %>">
                                                [<%= book.getBookId() %>] <%= book.getTitle() != null ? book.getTitle() : "未知书名" %>
                                                <% if (book.getPublisher() != null) { %>
                                                    - <%= book.getPublisher() %>
                                                <% } %>
                                                <% if (book.getSeriesId() != null) { %>
                                                    <span class="text-muted">(已属于其他丛书)</span>
                                                <% } %>
                                            </option>
                                        <% } %>
                                    </select>
                                </div>
                                <div class="col-md-4 d-flex align-items-end">
                                    <button type="submit" class="btn btn-success w-100">
                                        <i class="bi bi-plus-circle"></i> 添加到丛书
                                    </button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
                
                <!-- 丛书中的书籍列表 -->
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-info text-white">
                        <h6 class="mb-0">
                            <i class="bi bi-book"></i> 丛书中的书籍 (<%= seriesBooks.size() %>)
                        </h6>
                    </div>
                    <div class="card-body">
                        <% if (seriesBooks.isEmpty()) { %>
                            <div class="alert alert-info text-center">
                                <i class="bi bi-info-circle"></i> 该丛书下暂无书籍
                            </div>
                        <% } else { %>
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th width="8%">序号</th>
                                            <th width="15%">封面</th>
                                            <th width="25%">书名</th>
                                            <th width="15%">出版社</th>
                                            <th width="10%">价格</th>
                                            <th width="10%">库存</th>
                                            <th width="17%">操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (Book book : seriesBooks) { 
                                            String coverBase64 = null;
                                            if (book.getCoverImage() != null && book.getCoverImage().length > 0) {
                                                coverBase64 = Base64.getEncoder().encodeToString(book.getCoverImage());
                                            }
                                        %>
                                            <tr>
                                                <td><%= book.getBookId() %></td>
                                                <td>
                                                    <% if (coverBase64 != null) { %>
                                                        <img src="data:image/jpeg;base64,<%= coverBase64 %>"
                                                             alt="<%= book.getTitle() %>"
                                                             class="img-fluid rounded" 
                                                             style="max-height: 60px; object-fit: cover;">
                                                    <% } else { %>
                                                        <span class="text-muted small">无封面</span>
                                                    <% } %>
                                                </td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/admin/book/edit?bookId=<%= book.getBookId() %>" 
                                                       class="text-decoration-none">
                                                        <%= book.getTitle() != null ? book.getTitle() : "未知书名" %>
                                                    </a>
                                                </td>
                                                <td><%= book.getPublisher() != null ? book.getPublisher() : "-" %></td>
                                                <td>¥<%= book.getPrice() != null ? book.getPrice() : "0.00" %></td>
                                                <td><%= book.getStockQuantity() != null ? book.getStockQuantity() : 0 %></td>
                                                <td>
                                                    <form method="POST" action="${pageContext.request.contextPath}/admin/series/books/remove" 
                                                          style="display: inline;" 
                                                          onsubmit="return confirm('确定要从丛书中移除《<%= book.getTitle() != null ? book.getTitle() : "未知书名" %>》吗？');">
                                                        <input type="hidden" name="seriesId" value="<%= series.getSeriesId() %>">
                                                        <input type="hidden" name="bookId" value="<%= book.getBookId() %>">
                                                        <button type="submit" class="btn btn-outline-danger btn-sm">
                                                            <i class="bi bi-x-circle"></i> 移除
                                                        </button>
                                                    </form>
                                                </td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        <% } %>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

