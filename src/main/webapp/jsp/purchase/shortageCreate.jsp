<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>创建缺书记录 - 网上书店管理系统</title>
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
                    <h2 class="h5 mb-1">创建缺书记录</h2>
                    <p class="text-muted small mb-0">填写缺货书目、数量及来源，提交后可在缺书列表中查看并生成采购单。</p>
                </div>
                <a href="${pageContext.request.contextPath}/purchase/shortage/list" class="btn btn-outline-secondary btn-sm">
                    返回缺书列表
                </a>
            </div>

            <form class="row g-3" method="post" action="${pageContext.request.contextPath}/purchase/shortage/create">
                <div class="col-md-6">
                    <label class="form-label">书目</label>
                    <select class="form-select" name="bookId" required>
                        <option value="">请选择书目</option>
                        <c:forEach var="b" items="${books}">
                            <option value="${b.bookId}">${b.bookId} - ${b.title}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">供应商（可选）</label>
                    <select class="form-select" name="supplierId">
                        <option value="">未指定</option>
                        <c:forEach var="s" items="${suppliers}">
                            <option value="${s.supplierId}">${s.name}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-md-4">
                    <label class="form-label">缺货数量</label>
                    <input type="number" min="1" class="form-control" name="quantity" value="1" required>
                </div>
                <div class="col-md-4">
                    <label class="form-label">来源类型</label>
                    <select class="form-select" name="sourceType">
                        <option value="MANUAL">MANUAL（管理员）</option>
                        <option value="CUSTOMER">CUSTOMER（客户反馈）</option>
                        <option value="THRESHOLD">THRESHOLD（阈值触发）</option>
                    </select>
                </div>
                <div class="col-md-4 d-flex align-items-end">
                    <button type="submit" class="btn btn-primary">提交创建</button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/jsp/common/footer.jsp"/>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>


