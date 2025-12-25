<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    model.Series series = (model.Series) request.getAttribute("series");
    Boolean isNew = (Boolean) request.getAttribute("isNew");
    if (series == null) {
        series = new model.Series();
    }
    if (isNew == null) {
        isNew = true;
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= isNew ? "新增丛书" : "编辑丛书" %> - 网上书店管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp"/>
    <jsp:include page="/jsp/common/message.jsp"/>
    
    <div class="container mt-4">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-primary text-white">
                        <i class="bi bi-collection"></i> <%= isNew ? "新增丛书" : "编辑丛书" %>
                    </div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/admin/series/save" method="POST">
                            <% if (!isNew) { %>
                                <input type="hidden" name="seriesId" value="<%= series.getSeriesId() %>">
                            <% } %>
                            
                            <div class="mb-3">
                                <label for="seriesName" class="form-label">
                                    丛书名称 <span class="text-danger">*</span>
                                </label>
                                <input type="text" class="form-control" id="seriesName" name="seriesName" 
                                       value="<%= series.getSeriesName() != null ? series.getSeriesName() : "" %>" 
                                       required>
                            </div>
                            
                            <div class="mb-3">
                                <label for="publisher" class="form-label">出版社</label>
                                <input type="text" class="form-control" id="publisher" name="publisher" 
                                       value="<%= series.getPublisher() != null ? series.getPublisher() : "" %>" 
                                       placeholder="可选">
                            </div>
                            
                            <div class="mb-3">
                                <label for="description" class="form-label">丛书描述</label>
                                <textarea class="form-control" id="description" name="description" rows="4" 
                                          placeholder="可选，描述该丛书的特点、主题等"><%= series.getDescription() != null ? series.getDescription() : "" %></textarea>
                            </div>
                            
                            <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                                <a href="${pageContext.request.contextPath}/admin/series/list" class="btn btn-secondary">
                                    <i class="bi bi-arrow-left"></i> 返回
                                </a>
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-check-circle"></i> 保存
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

