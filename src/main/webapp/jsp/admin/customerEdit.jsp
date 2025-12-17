<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>编辑客户账户与信用信息 - 后台管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/jsp/common/header.jsp"/>
<jsp:include page="/jsp/common/message.jsp"/>

<div class="container mt-4 mb-5">
    <div class="card shadow-sm border-0">
        <div class="card-body">
            <h2 class="h5 mb-3">编辑客户账户与信用信息</h2>
            <p class="text-muted small mb-4">
                请根据需要调整客户余额、信用等级和月度透支额度。信用规则参考实验说明的五级信用体系。
            </p>

            <form id="customerEditForm" method="post"
                  action="${pageContext.request.contextPath}/admin/customer/update">
                <input type="hidden" name="customerId" value="${customer.customerId}"/>

                <div class="row g-3 mb-3">
                    <div class="col-md-4">
                        <label class="form-label">客户ID</label>
                        <input type="text" class="form-control" value="${customer.customerId}" readonly>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">客户姓名</label>
                        <input type="text" class="form-control" value="${customer.name}" readonly>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">邮箱</label>
                        <input type="text" class="form-control" value="${customer.email}" readonly>
                    </div>
                </div>

                <div class="row g-3 mb-3">
                    <div class="col-md-4">
                        <label for="balance" class="form-label">当前余额（元）</label>
                        <input type="number" step="0.01" class="form-control" id="balance" name="balance"
                               data-label="当前余额"
                               value="${customer.balance}" data-decimal="2">
                    </div>
                    <div class="col-md-4">
                        <label for="creditLevel" class="form-label">信用等级（1-5级）</label>
                        <select class="form-select" id="creditLevel" name="creditLevel" data-label="信用等级">
                            <option value="">请选择信用等级</option>
                            <c:forEach begin="1" end="5" var="lvl">
                                <option value="${lvl}" <c:if test="${customer.creditLevel == lvl}">selected</c:if>>
                                    第${lvl}级
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label for="monthlyLimit" class="form-label">月度透支额度（元）</label>
                        <input type="number" step="0.01" class="form-control" id="monthlyLimit" name="monthlyLimit"
                               data-label="月度透支额度"
                               value="${customer.monthlyLimit}" data-decimal="2">
                    </div>
                </div>

                <div class="row g-3 mb-4">
                    <div class="col-md-12">
                        <div class="alert alert-info mb-0">
                            <strong>信用等级说明：</strong>
                            <ul class="mb-0 small">
                                <li>1级：10% 折扣，不可透支。</li>
                                <li>2级：15% 折扣，不可透支。</li>
                                <li>3级：15% 折扣，可透支，有额度限制。</li>
                                <li>4级：20% 折扣，可透支，有额度限制。</li>
                                <li>5级：25% 折扣，可透支，无额度限制。</li>
                            </ul>
                        </div>
                    </div>
                </div>

                <div class="mt-3">
                    <button type="submit" class="btn btn-primary me-2">保存</button>
                    <a href="${pageContext.request.contextPath}/admin/customer/list" class="btn btn-outline-secondary">
                        返回列表
                    </a>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/jsp/common/footer.jsp"/>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/common.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        const rules = {
            balance: {required: true, message: '余额不能为空'},
            creditLevel: {required: true, message: '请选择信用等级'}
        };
        const form = document.getElementById('customerEditForm');
        form.addEventListener('submit', function (e) {
            if (!validateForm('customerEditForm', rules)) {
                e.preventDefault();
            }
        });
    });
</script>

</body>
</html>
