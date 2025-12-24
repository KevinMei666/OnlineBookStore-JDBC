<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>缺书登记 - 网上书店管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp"/>
    <jsp:include page="/jsp/common/message.jsp"/>
    
    <div class="container mt-3 mb-5">
        <div class="card shadow-sm border-0">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <div>
                        <h2 class="h5 mb-1">缺书登记</h2>
                        <p class="text-muted small mb-0">如果您需要的图书暂时缺货，可以在此登记，我们会尽快安排采购。</p>
                    </div>
                </div>

                <form class="row g-3" method="post" action="${pageContext.request.contextPath}/customer/shortage/register">
                    <div class="col-md-8">
                        <label class="form-label">选择图书 <span class="text-danger">*</span></label>
                        <select class="form-select" name="bookId" required>
                            <option value="">请选择需要登记的图书</option>
                            <c:forEach var="b" items="${books}">
                                <option value="${b.bookId}">${b.bookId} - ${b.title} 
                                    <c:if test="${b.stockQuantity != null && b.stockQuantity > 0}">
                                        (库存: ${b.stockQuantity}本)
                                    </c:if>
                                    <c:if test="${b.stockQuantity == null || b.stockQuantity == 0}">
                                        (缺货)
                                    </c:if>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">需要数量 <span class="text-danger">*</span></label>
                        <input type="number" min="1" class="form-control" name="quantity" value="1" required>
                    </div>
                    <div class="col-12">
                        <div class="alert alert-info">
                            <i class="bi bi-info-circle"></i> 提交后，系统会自动生成缺书记录，管理员会尽快安排采购。
                        </div>
                    </div>
                    <div class="col-12">
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-check-circle"></i> 提交登记
                        </button>
                        <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-outline-secondary ms-2">
                            返回首页
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>
    
    <jsp:include page="/jsp/common/footer.jsp"/>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

