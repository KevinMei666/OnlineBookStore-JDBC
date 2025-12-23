<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>网上书店管理系统 - 欢迎页</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/style.css" rel="stylesheet">
</head>
<body class="auth-hero">
<div class="container">
    <div class="row justify-content-center">
        <div class="col-lg-10">
            <div class="row g-4 align-items-center">
                <div class="col-lg-6">
                    <div class="landing-hero">
                        <div class="d-inline-flex align-items-center gap-2 mb-3">
                            <span class="badge bg-primary text-white">全业务链</span>
                            <span class="text-muted small">书目 · 订单 · 采购 · 发货 · 财务</span>
                        </div>
                        <h1 class="mb-3">网上书店管理系统</h1>
                        <p class="mb-4 text-muted">
                            面向课程实验与演示，覆盖书目查询、购物车与下单、库存与缺书预警、采购补货、物流发货、客户与财务统计的完整流程。
                        </p>
                        <div class="d-flex gap-2 flex-wrap mb-4">
                            <span class="badge bg-primary text-white">书目/库存</span>
                            <span class="badge bg-success text-white">订单/发货</span>
                            <span class="badge bg-info text-white">采购/缺书</span>
                            <span class="badge bg-warning text-dark">报表/财务</span>
                        </div>
                        <div class="d-flex flex-wrap gap-2">
                            <a href="jsp/auth/login.jsp" class="btn btn-primary btn-lg">
                                立即登录
                            </a>
                            <a href="book" class="btn btn-outline-primary btn-lg">
                                浏览书目
                            </a>
                        </div>
                    </div>
                </div>
                <div class="col-lg-6">
                    <div class="row g-3">
                        <div class="col-12 col-sm-6">
                            <div class="landing-feature">
                                <div class="d-flex align-items-center gap-2 mb-2">
                                    <i class="bi bi-search text-primary"></i>
                                    <strong>智能查询</strong>
                                </div>
                                <p class="text-muted small mb-0">按书名、关键字、作者、出版社多条件检索。</p>
                            </div>
                        </div>
                        <div class="col-12 col-sm-6">
                            <div class="landing-feature">
                                <div class="d-flex align-items-center gap-2 mb-2">
                                    <i class="bi bi-cart-check text-success"></i>
                                    <strong>下单与发货</strong>
                                </div>
                                <p class="text-muted small mb-0">购物车、订单状态、部分/全部发货全流程覆盖。</p>
                            </div>
                        </div>
                        <div class="col-12 col-sm-6">
                            <div class="landing-feature">
                                <div class="d-flex align-items-center gap-2 mb-2">
                                    <i class="bi bi-truck text-info"></i>
                                    <strong>采购补货</strong>
                                </div>
                                <p class="text-muted small mb-0">缺书记录、采购单、到货入库与供应商管理。</p>
                            </div>
                        </div>
                        <div class="col-12 col-sm-6">
                            <div class="landing-feature">
                                <div class="d-flex align-items-center gap-2 mb-2">
                                    <i class="bi bi-bar-chart text-warning"></i>
                                    <strong>统计报表</strong>
                                </div>
                                <p class="text-muted small mb-0">财务统计、视图报表，辅助教学与分析。</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>





