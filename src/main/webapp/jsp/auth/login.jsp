<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String role = request.getParameter("role");
    if (role == null || role.trim().isEmpty()) {
        Object roleAttr = request.getAttribute("role");
        if (roleAttr instanceof String) {
            role = (String) roleAttr;
        }
    }
    if (role == null || role.trim().isEmpty()) {
        role = "CUSTOMER";
    }
    role = role.trim().toUpperCase();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户登录 - 网上书店管理系统</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body class="auth-hero login-hero neutral">
<div class="container login-shell">
    <div class="login-grid balanced">
        <div class="login-info">
            <div class="login-info-header">
                <span class="badge bg-light text-primary border-0">欢迎使用</span>
                <span class="text-muted small">网上书店管理系统</span>
            </div>
            <h1 class="mb-2 fw-bold">一站式书店运营与实验平台</h1>
            <p class="text-muted mb-3 small">
                覆盖查书、下单、库存采购、物流发货与财务统计，既适合教学演示，也适合日常运营。
            </p>
            <div class="login-badges">
                <span class="pill">查书/库存</span>
                <span class="pill">订单/发货</span>
                <span class="pill">采购/缺书</span>
                <span class="pill">统计/报表</span>
            </div>
            <div class="login-metrics">
                <div class="metric-card">
                    <div class="metric-title">馆藏量</div>
                    <div class="metric-value">+3.2k</div>
                    <div class="metric-bar"><div class="fill" style="width: 75%;"></div></div>
                </div>
                <div class="metric-card">
                    <div class="metric-title">日订单</div>
                    <div class="metric-value">+120</div>
                    <div class="metric-bar"><div class="fill" style="width: 55%;"></div></div>
                </div>
                <div class="metric-card">
                    <div class="metric-title">准时率</div>
                    <div class="metric-value">98.6%</div>
                    <div class="metric-bar"><div class="fill" style="width: 90%;"></div></div>
                </div>
            </div>
            <div class="login-points">
                <div class="point">
                    <i class="bi bi-shield-check text-primary"></i>
                    <div>
                        <div class="fw-semibold">安全加密</div>
                        <div class="text-muted small">账号与密码传输全程加密</div>
                    </div>
                </div>
                <div class="point">
                    <i class="bi bi-truck text-primary"></i>
                    <div>
                        <div class="fw-semibold">物流跟踪</div>
                        <div class="text-muted small">下单后全程可视化进度</div>
                    </div>
                </div>
                <div class="point">
                    <i class="bi bi-people text-primary"></i>
                    <div>
                        <div class="fw-semibold">多角色协同</div>
                        <div class="text-muted small">管理员与客户分角色入口</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="login-form-wrap">
            <div class="card auth-card shadow-sm border-0">
                <div class="card-header border-0 text-center pb-0">
                    <h4 class="mb-1"><i class="bi bi-box-arrow-in-right"></i> 登录</h4>
                    <div class="text-muted small">请选择身份并输入账号密码</div>
                </div>
                <div class="card-body">
                            <c:if test="${not empty errorMessage}">
                                <div class="alert alert-danger">${errorMessage}</div>
                            </c:if>
                            <c:if test="${not empty warningMessage}">
                                <div class="alert alert-warning">${warningMessage}</div>
                            </c:if>
                            <c:if test="${not empty successMessage}">
                                <div class="alert alert-success">${successMessage}</div>
                            </c:if>

                            <form action="${pageContext.request.contextPath}/auth/login" method="post" class="login-form">
                                <div class="mb-3">
                                    <label class="form-label">登录身份</label>
                                    <div class="auth-switch">
                                        <label class="form-check">
                                            <input class="form-check-input" type="radio" name="role" id="roleCustomer"
                                                   value="CUSTOMER" <%= "CUSTOMER".equals(role) ? "checked" : "" %>>
                                            <span class="switch-pill">普通客户</span>
                                        </label>
                                        <label class="form-check">
                                            <input class="form-check-input" type="radio" name="role" id="roleAdmin"
                                                   value="ADMIN" <%= "ADMIN".equals(role) ? "checked" : "" %>>
                                            <span class="switch-pill">管理员</span>
                                        </label>
                                    </div>
                                </div>

                                <div class="mb-3">
                                    <label for="username" class="form-label">
                                        <span class="customer-label" style="<%= "CUSTOMER".equals(role) ? "" : "display:none;" %>">邮箱</span>
                                        <span class="admin-label" style="<%= "ADMIN".equals(role) ? "" : "display:none;" %>">管理员账号</span>
                                    </label>
                                    <input type="text" class="form-control" id="username" name="username"
                                           placeholder="请输入邮箱或管理员账号" required>
                                </div>

                                <div class="mb-3">
                                    <label for="password" class="form-label">密码</label>
                                    <input type="password" class="form-control" id="password" name="password"
                                           placeholder="请输入密码" required>
                                </div>

                                <div class="d-grid gap-2">
                                    <button type="submit" class="btn btn-primary">
                                        登录
                                    </button>
                                    <a href="${pageContext.request.contextPath}/auth/register" class="btn btn-outline-secondary">
                                        注册新客户
                                    </a>
                                </div>
                            </form>
                        </div>
                        <div class="card-footer text-center text-muted small auth-tip border-0">
                            <span class="customer-label" style="<%= "CUSTOMER".equals(role) ? "" : "display:none;" %>">
                                客户使用注册邮箱登录。
                            </span>
                            <span class="admin-label" style="<%= "ADMIN".equals(role) ? "" : "display:none;" %>">
                                管理员由系统预先创建账号。
                            </span>
                            <div class="mt-2 text-muted extra-tip">
                                需要帮助？联系 support@example.com
                            </div>
                        </div>
                    </div>
                    <div class="login-meta">
                        <div class="meta-item">
                            <i class="bi bi-lock"></i> SSL 加密传输
                        </div>
                        <div class="meta-item">
                            <i class="bi bi-clock-history"></i> 最近登录保护
                        </div>
                        <div class="meta-item">
                            <i class="bi bi-phone"></i> 移动端自适应
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    (function() {
        const customerLabelNodes = document.querySelectorAll('.customer-label');
        const adminLabelNodes = document.querySelectorAll('.admin-label');
        const usernameInput = document.getElementById('username');
        const radios = document.querySelectorAll('input[name="role"]');
        function updateRole() {
            const isAdmin = document.getElementById('roleAdmin').checked;
            customerLabelNodes.forEach(n => n.style.display = isAdmin ? 'none' : '');
            adminLabelNodes.forEach(n => n.style.display = isAdmin ? '' : 'none');
            usernameInput.placeholder = isAdmin ? '请输入管理员账号' : '请输入邮箱';
        }
        radios.forEach(r => r.addEventListener('change', updateRole));
        updateRole();
    })();
</script>
</body>
</html>


