<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>新增客户 - 后台管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/jsp/common/header.jsp"/>
<jsp:include page="/jsp/common/message.jsp"/>

<div class="container mt-4 mb-5">
    <div class="card shadow-sm border-0">
        <div class="card-body">
            <h2 class="h5 mb-3">新增客户</h2>
            <p class="text-muted small mb-4">
                创建客户账号并设置初始余额、信用等级与透支额度。密码目前为明文存储，如需改为加密请在后端调整。
            </p>

            <form id="customerCreateForm" method="post"
                  action="${pageContext.request.contextPath}/admin/customer/create">
                <div class="row g-3 mb-3">
                    <div class="col-md-4">
                        <label for="email" class="form-label">邮箱 <span class="text-danger">*</span></label>
                        <input type="email" class="form-control" id="email" name="email"
                               placeholder="请输入邮箱" required>
                    </div>
                    <div class="col-md-4">
                        <label for="name" class="form-label">姓名</label>
                        <input type="text" class="form-control" id="name" name="name"
                               placeholder="请输入客户姓名">
                    </div>
                    <div class="col-md-4">
                        <label for="address" class="form-label">地址</label>
                        <input type="text" class="form-control" id="address" name="address"
                               placeholder="收货或联系地址">
                    </div>
                </div>

                <div class="row g-3 mb-3">
                    <div class="col-md-4">
                        <label for="password" class="form-label">密码 <span class="text-danger">*</span></label>
                        <input type="password" class="form-control" id="password" name="password"
                               placeholder="请输入密码" minlength="6" required>
                    </div>
                    <div class="col-md-4">
                        <label for="confirmPassword" class="form-label">确认密码</label>
                        <input type="password" class="form-control" id="confirmPassword" name="confirmPassword"
                               placeholder="再次输入密码" minlength="6">
                    </div>
                    <div class="col-md-4">
                        <label for="balance" class="form-label">初始余额（元）</label>
                        <input type="number" step="0.01" class="form-control" id="balance" name="balance"
                               placeholder="默认 0.00" value="0">
                    </div>
                </div>

                <div class="row g-3 mb-4">
                    <div class="col-md-4">
                        <label for="creditLevel" class="form-label">信用等级（1-5级）</label>
                        <select class="form-select" id="creditLevel" name="creditLevel">
                            <option value="">默认 1 级</option>
                            <c:forEach begin="1" end="5" var="lvl">
                                <option value="${lvl}">第${lvl}级</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label for="monthlyLimit" class="form-label">月度透支额度（元）</label>
                        <input type="number" step="0.01" class="form-control" id="monthlyLimit" name="monthlyLimit"
                               placeholder="默认 0.00" value="0">
                    </div>
                    <div class="col-md-4 d-flex align-items-end">
                        <div class="form-text">
                            信用等级与透支额度用于订单透支与折扣计算，可后续在列表中调整。
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
            email: {required: true, message: '邮箱不能为空'},
            password: {required: true, message: '密码不能为空'}
        };
        const form = document.getElementById('customerCreateForm');
        form.addEventListener('submit', function (e) {
            if (!validateForm('customerCreateForm', rules)) {
                e.preventDefault();
            }
        });
    });
</script>
</body>
</html>

