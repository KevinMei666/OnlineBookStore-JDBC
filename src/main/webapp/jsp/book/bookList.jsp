<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Book" %>
<%@ page import="model.Author" %>
<%@ page import="model.Keyword" %>
<%@ page import="dao.BookDao" %>
<%@ page import="dao.SeriesDao" %>
<%@ page import="model.Series" %>
<%
    List<Book> books = (List<Book>) request.getAttribute("books");
    String searchType = (String) request.getAttribute("searchType");
    String searchKeyword = (String) request.getAttribute("searchKeyword");
    String searchTitle = (String) request.getAttribute("searchTitle");
    String searchKeywordParam = (String) request.getAttribute("searchKeywordParam");
    String searchAuthor = (String) request.getAttribute("searchAuthor");
    String searchPublisher = (String) request.getAttribute("searchPublisher");
    String searchSeriesId = (String) request.getAttribute("searchSeriesId");
    List<Series> allSeries = (List<Series>) request.getAttribute("allSeries");
    String currentRole = (String) session.getAttribute("currentRole");
    boolean isAdmin = "ADMIN".equals(currentRole);
    
    if (books == null) {
        books = new java.util.ArrayList<>();
    }
    if (searchType == null) {
        searchType = "";
    }
    if (searchKeyword == null) {
        searchKeyword = "";
    }
    if (allSeries == null) {
        allSeries = new java.util.ArrayList<>();
    }
    
    BookDao bookDao = new BookDao();
    SeriesDao seriesDao = new SeriesDao();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>书目查询 - 网上书店管理系统</title>
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
    
    <div class="container mt-4">
        <div class="row">
            <div class="col-12">
                <!-- 搜索区域 -->
                <div class="card mb-4 search-card border-0">
                    <div class="card-header">
                        <i class="bi bi-search"></i> 搜索条件
                    </div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/book" method="GET">
                            <div class="row g-3">
                                <div class="col-md-3">
                                    <label for="isbn" class="form-label">书号(ISBN)</label>
                                    <input type="text" class="form-control" id="isbn" name="isbn" 
                                           value="<%= request.getAttribute("searchIsbn") != null ? request.getAttribute("searchIsbn") : "" %>" 
                                           placeholder="输入书号(ISBN)">
                                </div>
                                <div class="col-md-3">
                                    <label for="title" class="form-label">书名</label>
                                    <input type="text" class="form-control" id="title" name="title" 
                                           value="<%= searchTitle != null ? searchTitle : "" %>" 
                                           placeholder="输入书名">
                                </div>
                                <div class="col-md-3">
                                    <label for="keyword" class="form-label">关键字</label>
                                    <input type="text" class="form-control" id="keyword" name="keyword" 
                                           value="<%= searchKeywordParam != null ? searchKeywordParam : "" %>" 
                                           placeholder="输入关键字">
                                </div>
                                <div class="col-md-3">
                                    <label for="author" class="form-label">作者</label>
                                    <input type="text" class="form-control" id="author" name="author" 
                                           value="<%= searchAuthor != null ? searchAuthor : "" %>" 
                                           placeholder="输入作者名">
                                </div>
                            </div>
                            <div class="row g-3 mt-1">
                                <div class="col-md-3">
                                    <label for="publisher" class="form-label">出版社</label>
                                    <input type="text" class="form-control" id="publisher" name="publisher" 
                                           value="<%= searchPublisher != null ? searchPublisher : "" %>" 
                                           placeholder="输入出版社">
                                </div>
                                <div class="col-md-3">
                                    <label for="seriesId" class="form-label">丛书</label>
                                    <select class="form-select" id="seriesId" name="seriesId">
                                        <option value="">-- 选择丛书 --</option>
                                        <% for (Series series : allSeries) { %>
                                            <option value="<%= series.getSeriesId() %>" 
                                                    <%= (searchSeriesId != null && searchSeriesId.equals(String.valueOf(series.getSeriesId()))) ? "selected" : "" %>>
                                                <%= series.getSeriesName() %>
                                            </option>
                                        <% } %>
                                    </select>
                                </div>
                            </div>
                            <div class="row mt-3">
                                <div class="col-12">
                                    <button type="submit" class="btn btn-primary">
                                        <i class="bi bi-search"></i> 搜索
                                    </button>
                                    <a href="${pageContext.request.contextPath}/book" class="btn btn-secondary">
                                        <i class="bi bi-arrow-clockwise"></i> 重置
                                    </a>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
                
                <!-- 书籍列表区域 -->
                <div class="card result-card border-0">
                    <div class="card-header">
                        <i class="bi bi-list-ul"></i> 查询结果 
                        <span class="badge bg-primary"><%= books.size() %> 本</span>
                    </div>
                    <div class="card-body">
                        <% if (books.isEmpty()) { %>
                            <div class="alert alert-info text-center">
                                <i class="bi bi-info-circle"></i> 暂无书籍数据，请尝试其他搜索条件
                                <hr>
                                <p class="mb-3">未找到您要的书籍？</p>
                                <p class="text-muted mb-3">我们可以为您询价并报价，请点击下方按钮提交询价请求</p>
                                <a href="${pageContext.request.contextPath}/inquiry/create?title=<%= searchTitle != null ? java.net.URLEncoder.encode(searchTitle, "UTF-8") : "" %>&author=<%= searchAuthor != null ? java.net.URLEncoder.encode(searchAuthor, "UTF-8") : "" %>&publisher=<%= searchPublisher != null ? java.net.URLEncoder.encode(searchPublisher, "UTF-8") : "" %>" 
                                   class="btn btn-primary">
                                    <i class="bi bi-question-circle"></i> 提交询价请求
                                </a>
                            </div>
                        <% } else { %>
                            <div class="row g-4">
                                <% for (Book book : books) { 
                                    List<Author> authors = bookDao.findAuthorsByBookId(book.getBookId());
                                    List<Keyword> keywords = bookDao.findKeywordsByBookId(book.getBookId());
                                    // 获取丛书信息
                                    Series series = null;
                                    if (book.getSeriesId() != null) {
                                        series = seriesDao.findById(book.getSeriesId());
                                    }
                                %>
                                    <div class="col-md-6 col-lg-4">
                                        <div class="card h-100 shadow-sm border-0 book-card">
                                            <!-- 封面图片：改为完整显示，保持比例 -->
                                            <% if (book.getCoverImage() != null && book.getCoverImage().length > 0) { %>
                                                <div class="card-img-top bg-white d-flex align-items-center justify-content-center"
                                                     style="height: 260px; overflow: hidden;">
                                                    <img src="data:image/jpeg;base64,<%= java.util.Base64.getEncoder().encodeToString(book.getCoverImage()) %>"
                                                         alt="<%= book.getTitle() %>"
                                                         style="max-height: 100%; max-width: 100%; object-fit: contain;">
                                                </div>
                                            <% } else { %>
                                                <div class="card-img-top bg-light d-flex align-items-center justify-content-center" 
                                                     style="height: 260px;">
                                                    <i class="bi bi-image" style="font-size: 48px; color: #ccc;"></i>
                                                </div>
                                            <% } %>
                                            
                                            <div class="card-body d-flex flex-column">
                                                <!-- 书名 -->
                                                <h5 class="card-title">
                                                    <a href="${pageContext.request.contextPath}/book/detail?bookId=<%= book.getBookId() %>" 
                                                       class="text-decoration-none text-primary">
                                                        <%= book.getTitle() != null ? book.getTitle() : "未知书名" %>
                                                    </a>
                                                </h5>
                                                
                                                <!-- 书号(ISBN) -->
                                                <% if (book.getIsbn() != null && !book.getIsbn().isEmpty()) { %>
                                                    <p class="card-text text-muted small mb-2">
                                                        <i class="bi bi-upc-scan"></i> 书号(ISBN)：<%= book.getIsbn() %>
                                                    </p>
                                                <% } %>
                                                
                                                <!-- 作者 -->
                                                <% if (authors != null && !authors.isEmpty()) { %>
                                                    <p class="card-text text-muted small mb-2">
                                                        <i class="bi bi-person"></i> 作者：
                                                        <% for (int i = 0; i < authors.size(); i++) { %>
                                                            <%= authors.get(i).getName() %><%= i < authors.size() - 1 ? "、" : "" %>
                                                        <% } %>
                                                    </p>
                                                <% } %>
                                                
                                                <!-- 出版社 -->
                                                <% if (book.getPublisher() != null && !book.getPublisher().isEmpty()) { %>
                                                    <p class="card-text text-muted small mb-2">
                                                        <i class="bi bi-building"></i> 出版社：<%= book.getPublisher() %>
                                                    </p>
                                                <% } %>
                                                
                                                <!-- 丛书 -->
                                                <% if (series != null) { %>
                                                    <p class="card-text text-muted small mb-2">
                                                        <i class="bi bi-collection"></i> 丛书：
                                                        <a href="${pageContext.request.contextPath}/book?seriesId=<%= series.getSeriesId() %>" 
                                                           class="text-decoration-none text-primary">
                                                            <%= series.getSeriesName() %>
                                                        </a>
                                                    </p>
                                                <% } %>
                                                
                                                <!-- 价格和库存 -->
                                                <div class="mb-2">
                                                    <span class="badge bg-danger fs-6">
                                                        ¥<%= book.getPrice() != null ? book.getPrice() : "0.00" %>
                                                    </span>
                                                    <span class="badge bg-info ms-2">
                                                        库存：<%= book.getStockQuantity() != null ? book.getStockQuantity() : 0 %>
                                                    </span>
                                                </div>
                                                
                                                <!-- 关键词 -->
                                                <% if (keywords != null && !keywords.isEmpty()) { %>
                                                    <div class="mb-2">
                                                        <% for (Keyword keyword : keywords) { %>
                                                            <span class="badge bg-secondary me-1">
                                                                <%= keyword.getWord() %>
                                                            </span>
                                                        <% } %>
                                                    </div>
                                                <% } %>
                                                
                                                <!-- 按钮组 -->
                                                <div class="mt-auto">
                                                    <a href="${pageContext.request.contextPath}/book/detail?bookId=<%= book.getBookId() %>" 
                                                       class="btn btn-sm btn-outline-primary w-100 mb-2">
                                                        <i class="bi bi-eye"></i> 查看详情
                                                    </a>
                                                    <% if (isAdmin) { %>
                                                        <a href="${pageContext.request.contextPath}/admin/book/edit?bookId=<%= book.getBookId() %>"
                                                           class="btn btn-sm btn-warning w-100">
                                                            <i class="bi bi-pencil-square"></i> 管理
                                                        </a>
                                                    <% } else { %>
                                                    <form action="${pageContext.request.contextPath}/book" method="POST" class="d-inline w-100">
                                                        <input type="hidden" name="action" value="addToCart">
                                                        <input type="hidden" name="bookId" value="<%= book.getBookId() %>">
                                                        <button type="submit" class="btn btn-sm btn-primary w-100">
                                                            <i class="bi bi-cart-plus"></i> 加入购物车
                                                        </button>
                                                    </form>
                                                    <% } %>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                <% } %>
                            </div>
                            
                            <!-- 分页组件 -->
                            <% if (books.size() > 0) { %>
                                <nav aria-label="书籍列表分页" class="mt-4">
                                    <ul class="pagination justify-content-center">
                                        <li class="page-item disabled">
                                            <a class="page-link" href="#" tabindex="-1" aria-disabled="true">上一页</a>
                                        </li>
                                        <li class="page-item active" aria-current="page">
                                            <a class="page-link" href="#">1</a>
                                        </li>
                                        <li class="page-item disabled">
                                            <a class="page-link" href="#">下一页</a>
                                        </li>
                                    </ul>
                                </nav>
                            <% } %>
                        <% } %>
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

