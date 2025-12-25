<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="model.Book" %>
<%@ page import="model.Author" %>
<%@ page import="model.Keyword" %>
<%@ page import="model.BookSupplier" %>
<%@ page import="model.Supplier" %>
<%@ page import="model.Series" %>
<%@ page import="java.util.List" %>
<%@ page import="dao.SupplierDao" %>
<%
    Book book = (Book) request.getAttribute("book");
    List<Author> authors = (List<Author>) request.getAttribute("authors");
    List<Keyword> keywords = (List<Keyword>) request.getAttribute("keywords");
    List<BookSupplier> bookSuppliers = (List<BookSupplier>) request.getAttribute("bookSuppliers");
    Series series = (Series) request.getAttribute("series");
    List<Book> sameSeriesBooks = (List<Book>) request.getAttribute("sameSeriesBooks");
    String currentRole = (String) session.getAttribute("currentRole");
    boolean isAdmin = "ADMIN".equals(currentRole);
    
    if (book == null) {
        response.sendRedirect(request.getContextPath() + "/book");
        return;
    }
    
    if (authors == null) {
        authors = new java.util.ArrayList<>();
    }
    if (keywords == null) {
        keywords = new java.util.ArrayList<>();
    }
    if (bookSuppliers == null) {
        bookSuppliers = new java.util.ArrayList<>();
    }
    if (sameSeriesBooks == null) {
        sameSeriesBooks = new java.util.ArrayList<>();
    }
    
    SupplierDao supplierDao = new SupplierDao();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= book.getTitle() != null ? book.getTitle() : "书籍详情" %> - 网上书店管理系统</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <!-- 自定义样式 -->
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp"/>
    <jsp:include page="/jsp/common/message.jsp"/>
    
    <div class="container-fluid mt-4">
        <div class="row">
            <div class="col-12">
                <!-- 返回按钮 -->
                <a href="${pageContext.request.contextPath}/book" class="btn btn-outline-secondary mb-3">
                    <i class="bi bi-arrow-left"></i> 返回书目列表
                </a>
                
                <div class="card">
                    <div class="card-body">
                        <div class="row">
                            <!-- 左侧：封面图片 -->
                            <div class="col-md-4 col-lg-3 text-center mb-4 mb-md-0">
                                <% if (book.getCoverImage() != null && book.getCoverImage().length > 0) { %>
                                    <img src="data:image/jpeg;base64,<%= java.util.Base64.getEncoder().encodeToString(book.getCoverImage()) %>" 
                                         class="img-fluid rounded shadow" 
                                         alt="<%= book.getTitle() != null ? book.getTitle() : "书籍封面" %>"
                                         style="max-height: 500px; object-fit: contain;">
                                <% } else { %>
                                    <div class="bg-light d-flex align-items-center justify-content-center rounded shadow" 
                                         style="height: 400px;">
                                        <i class="bi bi-image" style="font-size: 80px; color: #ccc;"></i>
                                    </div>
                                <% } %>
                            </div>
                            
                            <!-- 右侧：书籍信息 -->
                            <div class="col-md-8 col-lg-9">
                                <!-- 书名 -->
                                <h1 class="mb-3">
                                    <%= book.getTitle() != null ? book.getTitle() : "未知书名" %>
                                </h1>
                                
                                <!-- 作者列表 -->
                                <% if (authors != null && !authors.isEmpty()) { %>
                                    <div class="mb-3">
                                        <h5 class="d-inline">作者：</h5>
                                        <% for (int i = 0; i < authors.size(); i++) { %>
                                            <span class="badge bg-primary me-2">
                                                <i class="bi bi-person"></i> <%= authors.get(i).getName() %>
                                            </span>
                                        <% } %>
                                    </div>
                                <% } %>
                                
                                <!-- 书号(ISBN) -->
                                <% if (book.getIsbn() != null && !book.getIsbn().isEmpty()) { %>
                                    <div class="mb-3">
                                        <h5 class="d-inline">书号(ISBN)：</h5>
                                        <span class="text-muted">
                                            <i class="bi bi-upc-scan"></i> <%= book.getIsbn() %>
                                        </span>
                                    </div>
                                <% } %>
                                
                                <!-- 出版社 -->
                                <% if (book.getPublisher() != null && !book.getPublisher().isEmpty()) { %>
                                    <div class="mb-3">
                                        <h5 class="d-inline">出版社：</h5>
                                        <span class="text-muted">
                                            <i class="bi bi-building"></i> <%= book.getPublisher() %>
                                        </span>
                                    </div>
                                <% } %>
                                
                                <!-- 价格和库存 -->
                                <div class="mb-4">
                                    <div class="d-flex align-items-center gap-3">
                                        <div>
                                            <span class="text-muted">价格：</span>
                                            <span class="fs-3 fw-bold text-danger">
                                                ¥<%= book.getPrice() != null ? book.getPrice() : "0.00" %>
                                            </span>
                                        </div>
                                        <div>
                                            <span class="text-muted">库存：</span>
                                            <span class="fs-5 fw-bold <%= (book.getStockQuantity() != null && book.getStockQuantity() > 0) ? "text-success" : "text-danger" %>">
                                                <%= book.getStockQuantity() != null ? book.getStockQuantity() : 0 %> 本
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                
                                <!-- 关键词列表 -->
                                <% if (keywords != null && !keywords.isEmpty()) { %>
                                    <div class="mb-4">
                                        <h5 class="mb-2">关键词：</h5>
                                        <% for (Keyword keyword : keywords) { %>
                                            <span class="badge bg-secondary me-2 mb-2">
                                                <%= keyword.getWord() %>
                                            </span>
                                        <% } %>
                                    </div>
                                <% } %>
                                
                                <!-- 操作按钮 -->
                                <div class="mb-4">
                                    <% if (isAdmin) { %>
                                        <a href="${pageContext.request.contextPath}/admin/book/edit?bookId=<%= book.getBookId() %>"
                                           class="btn btn-warning btn-lg">
                                            <i class="bi bi-pencil-square"></i> 修改信息
                                        </a>
                                    <% } else { %>
                                    <form action="${pageContext.request.contextPath}/book" method="POST" class="d-inline me-2">
                                        <input type="hidden" name="action" value="addToCart">
                                        <input type="hidden" name="bookId" value="<%= book.getBookId() %>">
                                        <button type="submit" class="btn btn-primary btn-lg">
                                            <i class="bi bi-cart-plus"></i> 加入购物车
                                        </button>
                                    </form>
                                    <a href="${pageContext.request.contextPath}/order/checkout?bookId=<%= book.getBookId() %>" 
                                       class="btn btn-success btn-lg">
                                        <i class="bi bi-bag-check"></i> 立即下单
                                    </a>
                                    <% } %>
                                </div>
                            </div>
                        </div>
                        
                        <hr class="my-4">
                        
                        <!-- 详细信息区域 -->
                        <div class="row">
                            <div class="col-12">
                                <!-- 目录 -->
                                <% if (book.getCatalog() != null && !book.getCatalog().trim().isEmpty()) { %>
                                    <div class="mb-4">
                                        <h4 class="mb-3">
                                            <i class="bi bi-list-ul"></i> 目录
                                        </h4>
                                        <div class="card">
                                            <div class="card-body">
                                                <pre class="mb-0" style="white-space: pre-wrap; font-family: inherit;"><%= book.getCatalog() %></pre>
                                            </div>
                                        </div>
                                    </div>
                                <% } %>
                                
                                <!-- 供应商信息 -->
                                <% if (bookSuppliers != null && !bookSuppliers.isEmpty()) { %>
                                    <div class="mb-4">
                                        <h4 class="mb-3">
                                            <i class="bi bi-truck"></i> 供应商信息
                                        </h4>
                                        <div class="table-responsive">
                                            <table class="table table-striped table-hover">
                                                <thead>
                                                    <tr>
                                                        <th>供应商名称</th>
                                                        <th>供应商ID</th>
                                                        <th>供货价格</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <% for (BookSupplier bs : bookSuppliers) { 
                                                        Supplier supplier = supplierDao.findById(bs.getSupplierId());
                                                    %>
                                                        <tr>
                                                            <td>
                                                                <%= supplier != null && supplier.getName() != null ? supplier.getName() : "未知供应商" %>
                                                            </td>
                                                            <td><%= bs.getSupplierId() %></td>
                                                            <td class="text-danger fw-bold">
                                                                ¥<%= bs.getSupplyPrice() != null ? bs.getSupplyPrice() : "0.00" %>
                                                            </td>
                                                        </tr>
                                                    <% } %>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                <% } %>
                                
                                <!-- 其他信息 -->
                                <div class="row">
                                    <div class="col-md-6">
                                        <div class="card">
                                            <div class="card-header">
                                                <i class="bi bi-info-circle"></i> 其他信息
                                            </div>
                                            <div class="card-body">
                                                <p class="mb-2">
                                                    <strong>序号：</strong> <%= book.getBookId() %>
                                                </p>
                                                <% if (book.getSeriesId() != null && series != null) { %>
                                                    <p class="mb-2">
                                                        <strong>丛书：</strong> 
                                                        <a href="${pageContext.request.contextPath}/book?seriesId=<%= series.getSeriesId() %>" 
                                                           class="text-decoration-none">
                                                            <%= series.getSeriesName() %>
                                                        </a>
                                                    </p>
                                                    <% if (series.getDescription() != null && !series.getDescription().trim().isEmpty()) { %>
                                                        <p class="mb-2 text-muted small">
                                                            <%= series.getDescription() %>
                                                        </p>
                                                    <% } %>
                                                <% } else if (book.getSeriesId() != null) { %>
                                                    <p class="mb-2">
                                                        <strong>丛书ID：</strong> <%= book.getSeriesId() %>
                                                    </p>
                                                <% } %>
                                                <% if (book.getLocation() != null && !book.getLocation().isEmpty()) { %>
                                                    <p class="mb-2">
                                                        <strong>存放位置：</strong> <%= book.getLocation() %>
                                                    </p>
                                                <% } %>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="/jsp/common/footer.jsp"/>
    
    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <!-- 自定义JS -->
    <script src="${pageContext.request.contextPath}/js/common.js"></script>
</body>
</html>

