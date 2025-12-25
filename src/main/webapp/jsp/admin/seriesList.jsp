<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Series" %>
<%@ page import="dao.SeriesDao" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    List<Series> seriesList = (List<Series>) request.getAttribute("seriesList");
    if (seriesList == null) {
        seriesList = new java.util.ArrayList<>();
    }
    SeriesDao seriesDao = new SeriesDao();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>丛书管理 - 网上书店管理系统</title>
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
                <!-- 返回按钮 -->
                <div class="mb-3">
                    <a href="${pageContext.request.contextPath}/admin/book/list" class="btn btn-outline-secondary btn-sm">
                        <i class="bi bi-arrow-left"></i> 返回图书管理
                    </a>
                </div>
                
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                        <div>
                            <i class="bi bi-collection"></i> 丛书管理
                        </div>
                        <a href="${pageContext.request.contextPath}/admin/series/create" class="btn btn-light btn-sm">
                            <i class="bi bi-plus-circle"></i> 新增丛书
                        </a>
                    </div>
                    <div class="card-body">
                        <% if (seriesList.isEmpty()) { %>
                            <div class="alert alert-info text-center">
                                <i class="bi bi-info-circle"></i> 暂无丛书记录
                                <hr>
                                <a href="${pageContext.request.contextPath}/admin/series/create" class="btn btn-primary">
                                    <i class="bi bi-plus-circle"></i> 创建第一个丛书
                                </a>
                            </div>
                        <% } else { %>
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th width="8%">丛书ID</th>
                                            <th width="25%">丛书名称</th>
                                            <th width="15%">出版社</th>
                                            <th width="30%">描述</th>
                                            <th width="12%">创建日期</th>
                                            <th width="10%">操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (Series series : seriesList) { 
                                            int bookCount = seriesDao.countBooksBySeriesId(series.getSeriesId());
                                        %>
                                            <tr>
                                                <td><%= series.getSeriesId() %></td>
                                                <td><strong><%= series.getSeriesName() %></strong></td>
                                                <td><%= series.getPublisher() != null ? series.getPublisher() : "-" %></td>
                                                <td>
                                                    <small class="text-muted">
                                                        <%= series.getDescription() != null && series.getDescription().length() > 50 ? 
                                                            series.getDescription().substring(0, 50) + "..." : 
                                                            (series.getDescription() != null ? series.getDescription() : "-") %>
                                                    </small>
                                                </td>
                                                <td>
                                                    <% if (series.getCreateDate() != null) { %>
                                                        <%= series.getCreateDate().format(formatter) %>
                                                    <% } else { %>
                                                        -
                                                    <% } %>
                                                </td>
                                                <td>
                                                    <div class="btn-group btn-group-sm" role="group">
                                                        <a href="${pageContext.request.contextPath}/admin/series/books?seriesId=<%= series.getSeriesId() %>" 
                                                           class="btn btn-outline-info">
                                                            <i class="bi bi-book"></i> 管理书籍 (<%= bookCount %>)
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/admin/series/edit?seriesId=<%= series.getSeriesId() %>" 
                                                           class="btn btn-outline-primary">
                                                            <i class="bi bi-pencil"></i> 编辑
                                                        </a>
                                                        <button type="button" class="btn btn-outline-danger" 
                                                                data-series-id="<%= series.getSeriesId() %>"
                                                                data-series-name="<%= series.getSeriesName() %>"
                                                                data-book-count="<%= bookCount %>"
                                                                onclick="deleteSeries(this)">
                                                            <i class="bi bi-trash"></i> 删除
                                                        </button>
                                                    </div>
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
    
    <form id="deleteForm" method="POST" action="${pageContext.request.contextPath}/admin/series/delete" style="display: none;">
        <input type="hidden" name="seriesId" id="deleteSeriesId">
    </form>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function deleteSeries(button) {
            var seriesId = button.getAttribute('data-series-id');
            var seriesName = button.getAttribute('data-series-name');
            var bookCount = parseInt(button.getAttribute('data-book-count'));
            
            if (bookCount > 0) {
                alert('该丛书下还有 ' + bookCount + ' 本书籍，无法删除。请先移除书籍的丛书关联。');
                return;
            }
            if (confirm('确定要删除丛书"' + seriesName + '"吗？')) {
                document.getElementById('deleteSeriesId').value = seriesId;
                document.getElementById('deleteForm').submit();
            }
        }
    </script>
</body>
</html>

