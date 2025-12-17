<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>500 - 服务器错误</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/jsp/common/header.jsp"/>

<div class="container mt-4 mb-5">
    <div class="card shadow-sm border-0">
        <div class="card-body py-4 px-4">
            <h2 class="h5 mb-2">服务器错误（500）</h2>
            <p class="text-muted mb-4">服务器处理请求时发生异常。通常是后端代码报错、数据库连接异常，或部署的构件不是最新。</p>
            <div class="d-flex gap-2">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/index.jsp">返回首页</a>
                <a class="btn btn-outline-secondary" href="javascript:history.back()">返回上一页</a>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/jsp/common/footer.jsp"/>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>


