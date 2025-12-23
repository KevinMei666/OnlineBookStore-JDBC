<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>客户注册 - 网上书店管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body class="auth-hero login-hero neutral">
<div class="container login-shell">
    <div class="login-grid balanced">
        <div class="login-info">
            <div class="login-info-header">
                <span class="badge bg-light text-primary border-0">欢迎注册</span>
                <span class="text-muted small">网上书店管理系统</span>
            </div>
            <h1 class="mb-2 fw-bold">创建您的账户，开启一站式购书体验</h1>
            <p class="text-muted mb-3 small">
                注册即享查书、下单、物流跟踪与积分折扣，多角色协同让学习与运营更高效。
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
                    <i class="bi bi-gift text-primary"></i>
                    <div>
                        <div class="fw-semibold">积分折扣</div>
                        <div class="text-muted small">注册即享积分成长与会员折扣</div>
                    </div>
                </div>
                <div class="point">
                    <i class="bi bi-people text-primary"></i>
                    <div>
                        <div class="fw-semibold">多角色协同</div>
                        <div class="text-muted small">客户与管理员分角色入口</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="login-form-wrap">
            <div class="card auth-card shadow-sm border-0">
                <div class="card-header border-0 text-center pb-0">
                    <h4 class="mb-1"><i class="bi bi-person-plus"></i> 注册新客户</h4>
                    <div class="text-muted small">填写基本信息完成注册</div>
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

                    <form action="${pageContext.request.contextPath}/auth/register" method="post" class="login-form">
                        <div class="mb-3">
                            <label for="email" class="form-label">邮箱<span class="text-danger">*</span></label>
                            <input type="email" class="form-control" id="email" name="email"
                                   placeholder="请输入常用邮箱" required>
                        </div>

                        <div class="mb-3">
                            <label for="name" class="form-label">姓名<span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="name" name="name"
                                   placeholder="请输入您的姓名" required>
                        </div>

                        <div class="mb-3">
                            <label for="address" class="form-label">收货地址</label>
                            <textarea class="form-control" id="address" name="address" rows="2"
                                      placeholder="用于收货的详细地址"></textarea>
                        </div>

                        <div class="mb-3">
                            <label for="password" class="form-label">密码<span class="text-danger">*</span></label>
                            <input type="password" class="form-control" id="password" name="password"
                                   placeholder="请输入登录密码" minlength="6" required>
                        </div>

                        <div class="mb-3">
                            <label for="confirmPassword" class="form-label">确认密码<span class="text-danger">*</span></label>
                            <input type="password" class="form-control" id="confirmPassword" name="confirmPassword"
                                   placeholder="请再次输入密码" minlength="6" required>
                        </div>

                        <div class="mb-3 form-text">
                            初始信用等级默认为 1 级，余额为 0，后续可由管理员根据消费情况调整信用等级和额度。
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary">
                                立即注册
                            </button>
                            <a href="${pageContext.request.contextPath}/auth/login" class="btn btn-outline-secondary">
                                返回登录
                            </a>
                        </div>
                    </form>
                </div>
                <div class="card-footer text-center text-muted small auth-tip border-0">
                    注册即表示您同意本系统的使用规范。
                </div>
            </div>
            <div class="login-meta">
                <div class="meta-item">
                    <i class="bi bi-lock"></i> SSL 加密传输
                </div>
                <div class="meta-item">
                    <i class="bi bi-clock-history"></i> 最近注册保护
                </div>
                <div class="meta-item">
                    <i class="bi bi-phone"></i> 移动端自适应
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

