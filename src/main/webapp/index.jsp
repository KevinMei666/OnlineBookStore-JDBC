<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>网上书店管理系统 - 首页</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/style.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/jsp/common/header.jsp"/>

<div class="container mt-4 mb-5">
    <!-- 顶部欢迎信息 + 统计卡片 -->
    <div class="row g-4 align-items-stretch">
        <div class="col-lg-8">
            <div class="card shadow-sm border-0 h-100">
                <div class="card-body py-4 px-4">
                    <h1 class="h3 mb-3">欢迎使用 <span class="text-primary">网上书店管理系统</span></h1>
                    <p class="text-muted mb-4">
                        本系统用于演示数据库课程设计，涵盖书目管理、订单处理、采购与发货、客户管理以及统计报表等完整业务流程。
                    </p>
                    <div class="row g-3 text-center">
                        <div class="col-4">
                            <div class="card border-0 bg-light shadow-sm h-100">
                                <div class="card-body py-3">
                                    <div class="text-muted small">书籍总数</div>
                                    <div class="h4 mb-0 text-primary">1,280</div>
                                </div>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="card border-0 bg-light shadow-sm h-100">
                                <div class="card-body py-3">
                                    <div class="text-muted small">订单总数</div>
                                    <div class="h4 mb-0 text-success">3,560</div>
                                </div>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="card border-0 bg-light shadow-sm h-100">
                                <div class="card-body py-3">
                                    <div class="text-muted small">客户总数</div>
                                    <div class="h4 mb-0 text-warning">820</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-lg-4">
            <div class="card shadow-sm border-0 h-100">
                <div class="card-body py-4 px-4">
                    <h2 class="h5 mb-3">今日概览</h2>
                    <ul class="list-unstyled small mb-0">
                        <li class="mb-2">• 今日新增订单：<span class="text-success fw-semibold">36</span></li>
                        <li class="mb-2">• 今日发货订单：<span class="text-primary fw-semibold">28</span></li>
                        <li class="mb-2">• 库存预警书目：<span class="text-danger fw-semibold">5</span></li>
                        <li class="mb-2">• 待处理采购单：<span class="text-warning fw-semibold">3</span></li>
                    </ul>
                </div>
            </div>
        </div>
    </div>

    <!-- 功能快捷入口卡片 -->
    <div class="card shadow-sm border-0 mt-4">
        <div class="card-body py-4 px-4">
            <h2 class="h5 mb-3">功能快捷入口</h2>
            <div class="row g-3 mt-1">
                <div class="col-12 col-md-6 col-lg-4">
                    <div class="card h-100 border-0 bg-light shadow-sm">
                        <div class="card-body">
                            <h5 class="card-title text-primary mb-2">书目查询</h5>
                            <p class="card-text small text-muted mb-3">按书名、关键字、作者、出版社快速查询图书。</p>
                            <a href="<%= request.getContextPath() %>/book" class="btn btn-primary btn-sm">进入</a>
                        </div>
                    </div>
                </div>

                <div class="col-12 col-md-6 col-lg-4">
                    <div class="card h-100 border-0 bg-light shadow-sm">
                        <div class="card-body">
                            <h5 class="card-title text-success mb-2">创建订单</h5>
                            <p class="card-text small text-muted mb-3">浏览图书、加入购物车、一键创建客户订单。</p>
                            <a href="<%= request.getContextPath() %>/order/cart" class="btn btn-success btn-sm">进入</a>
                        </div>
                    </div>
                </div>

                <div class="col-12 col-md-6 col-lg-4">
                    <div class="card h-100 border-0 bg-light shadow-sm">
                        <div class="card-body">
                            <h5 class="card-title text-warning mb-2">采购管理</h5>
                            <p class="card-text small text-muted mb-3">维护采购单、到货入库及缺书预警记录。</p>
                            <a href="<%= request.getContextPath() %>/purchase/list" class="btn btn-warning btn-sm">进入</a>
                        </div>
                    </div>
                </div>

                <div class="col-12 col-md-6 col-lg-4">
                    <div class="card h-100 border-0 bg-light shadow-sm">
                        <div class="card-body">
                            <h5 class="card-title text-secondary mb-2">客户管理</h5>
                            <p class="card-text small text-muted mb-3">查看客户信息、余额、信用等级与历史订单。</p>
                            <a href="<%= request.getContextPath() %>/customer/info" class="btn btn-secondary btn-sm">进入</a>
                        </div>
                    </div>
                </div>

                <div class="col-12 col-md-6 col-lg-4">
                    <div class="card h-100 border-0 bg-light shadow-sm">
                        <div class="card-body">
                            <h5 class="card-title text-info mb-2">统计报表</h5>
                            <p class="card-text small text-muted mb-3">通过数据库视图展示多维度经营数据。</p>
                            <a href="<%= request.getContextPath() %>/report/views" class="btn btn-info btn-sm">进入</a>
                        </div>
                    </div>
                </div>

                <div class="col-12 col-md-6 col-lg-4">
                    <div class="card h-100 border-0 bg-light shadow-sm">
                        <div class="card-body">
                            <h5 class="card-title text-danger mb-2">发货管理</h5>
                            <p class="card-text small text-muted mb-3">处理订单发货与扣款，跟踪物流状态。</p>
                            <a href="<%= request.getContextPath() %>/shipment/list" class="btn btn-danger btn-sm">进入</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- 系统功能说明 -->
    <div class="card shadow-sm border-0 mt-4">
        <div class="card-body py-4 px-4">
            <h2 class="h5 mb-3">系统功能概览</h2>
            <div class="row">
                <div class="col-md-6">
                    <ul class="list-unstyled small mb-0">
                        <li class="mb-2">✓ 书目查询：支持多条件组合检索，便于快速定位目标图书。</li>
                        <li class="mb-2">✓ 订单处理：支持购物车、订单创建与订单状态管理。</li>
                        <li class="mb-2">✓ 发货管理：关联订单与物流信息，追踪履约全过程。</li>
                    </ul>
                </div>
                <div class="col-md-6">
                    <ul class="list-unstyled small mb-0">
                        <li class="mb-2">✓ 采购管理：支持缺书记录与自动补货策略的实验。</li>
                        <li class="mb-2">✓ 客户管理：维护客户资料、余额、信用等级及订单历史。</li>
                        <li class="mb-2">✓ 统计报表：通过数据库视图集中展示关键业务指标。</li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/jsp/common/footer.jsp"/>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="js/common.js"></script>
</body>
</html>

