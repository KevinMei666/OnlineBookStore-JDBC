<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String prefillTitle = (String) request.getAttribute("prefillTitle");
    String prefillAuthor = (String) request.getAttribute("prefillAuthor");
    String prefillPublisher = (String) request.getAttribute("prefillPublisher");
    
    if (prefillTitle == null) prefillTitle = "";
    if (prefillAuthor == null) prefillAuthor = "";
    if (prefillPublisher == null) prefillPublisher = "";
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>提交询价请求 - 网上书店管理系统</title>
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
                        <i class="bi bi-question-circle"></i> 提交询价请求
                    </div>
                    <div class="card-body">
                        <p class="text-muted mb-4">
                            <i class="bi bi-info-circle"></i> 
                            如果您在书库中没有找到想要的书籍，可以提交询价请求。我们会尽快为您询价并回复。
                        </p>
                        
                        <form action="${pageContext.request.contextPath}/inquiry" method="POST">
                            <div class="mb-3">
                                <label for="bookTitle" class="form-label">
                                    书名 <span class="text-danger">*</span>
                                </label>
                                <input type="text" class="form-control" id="bookTitle" name="bookTitle" 
                                       value="<%= prefillTitle %>" required>
                            </div>
                            
                            <div class="mb-3">
                                <label for="author" class="form-label">作者</label>
                                <input type="text" class="form-control" id="author" name="author" 
                                       value="<%= prefillAuthor %>" 
                                       placeholder="可选，如有多个作者请用逗号分隔">
                            </div>
                            
                            <div class="mb-3">
                                <label for="publisher" class="form-label">出版社</label>
                                <input type="text" class="form-control" id="publisher" name="publisher" 
                                       value="<%= prefillPublisher %>" 
                                       placeholder="可选">
                            </div>
                            
                            <div class="mb-3">
                                <label for="quantity" class="form-label">数量</label>
                                <input type="number" class="form-control" id="quantity" name="quantity" 
                                       value="1" min="1" required>
                            </div>
                            
                            <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                                <a href="${pageContext.request.contextPath}/book" class="btn btn-secondary">
                                    <i class="bi bi-arrow-left"></i> 返回
                                </a>
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-check-circle"></i> 提交询价请求
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

