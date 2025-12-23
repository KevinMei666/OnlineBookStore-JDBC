<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String landingRedirect = null;
    Object userObj = session.getAttribute("currentUser");
    String currentRole = (String) session.getAttribute("currentRole");
    boolean isAdmin = "ADMIN".equals(currentRole);
    boolean isCustomer = "CUSTOMER".equals(currentRole);
    if (userObj == null || "".equals(userObj)) {
        landingRedirect = request.getContextPath() + "/landing.jsp";
        response.sendRedirect(landingRedirect);
        return;
    }
%>
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
    <% if (isAdmin) { %>
    <!-- 管理员主页：数据与入口 -->
    <%
        // 实时统计：书籍/订单/客户数量（管理员查看）
        int totalBooks = 0;
        int totalOrders = 0;
        int totalCustomers = 0;
        int todayNewOrders = 0;
        int todayShippedOrders = 0;
        int lowStockBooks = 0;
        int pendingPurchaseOrders = 0;
        int stockThreshold = 10; // 与库存管理默认阈值保持一致
        try {
            dao.BookDao bd = new dao.BookDao();
            dao.OrdersDao od = new dao.OrdersDao();
            dao.CustomerDao cd = new dao.CustomerDao();
            dao.ShipmentDao sd = new dao.ShipmentDao();
            dao.PurchaseOrderDao pod = new dao.PurchaseOrderDao();
            totalBooks = bd.countAll();
            totalOrders = od.countAll();
            totalCustomers = cd.countAll();
            todayNewOrders = od.countCreatedToday();
            todayShippedOrders = sd.countDistinctOrdersShippedToday();
            lowStockBooks = bd.countLowStock(stockThreshold);
            pendingPurchaseOrders = pod.countPending();
        } catch (Exception e) {
            // 保持默认值，避免报错
        }
    %>

    <!-- 顶部欢迎信息 + 统计卡片 -->
    <div class="row g-4 align-items-stretch">
        <div class="col-lg-8">
            <div class="card shadow-sm border-0 h-100">
                <div class="card-body py-4 px-4">
                    <h1 class="h3 mb-3">欢迎回来，<span class="text-primary"><%= userObj %></span></h1>
                    <p class="text-muted mb-4">
                        快速总览今日业务，管理书目、订单、采购与发货，全流程高效协同。
                    </p>
                    <div class="row g-3 text-center">
                        <div class="col-4">
                            <div class="card border-0 bg-light shadow-sm h-100">
                                <div class="card-body py-3">
                                    <div class="text-muted small">书籍总数</div>
                                    <div class="h4 mb-0 text-primary"><%= totalBooks %></div>
                                </div>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="card border-0 bg-light shadow-sm h-100">
                                <div class="card-body py-3">
                                    <div class="text-muted small">订单总数</div>
                                    <div class="h4 mb-0 text-success"><%= totalOrders %></div>
                                </div>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="card border-0 bg-light shadow-sm h-100">
                                <div class="card-body py-3">
                                    <div class="text-muted small">客户总数</div>
                                    <div class="h4 mb-0 text-warning"><%= totalCustomers %></div>
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
                        <li class="mb-2">• 今日新增订单：<span class="text-success fw-semibold"><%= todayNewOrders %></span></li>
                        <li class="mb-2">• 今日发货订单：<span class="text-primary fw-semibold"><%= todayShippedOrders %></span></li>
                        <li class="mb-2">• 库存预警书目：<span class="text-danger fw-semibold"><%= lowStockBooks %></span></li>
                        <li class="mb-2">• 待处理采购单：<span class="text-warning fw-semibold"><%= pendingPurchaseOrders %></span></li>
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
                            <a href("<%= request.getContextPath() %>/purchase/list" class="btn btn-warning btn-sm">进入</a>
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
    <% } else { %>
    <!-- 客户主页：宣传 + 轻运营 -->
    <div class="hero-card mb-4">
        <div class="d-flex flex-column flex-lg-row align-items-start align-items-lg-center justify-content-between gap-3">
            <div>
                <div class="badge bg-light text-primary mb-2">精选好书 · 闪电配送</div>
                <h1 class="h3 mb-3 text-white">发现你的下一本心动好书</h1>
                <p class="text-white-50 mb-3">海量正版图书，会员立享专属折扣与积分返利，支持一键下单与物流跟踪。</p>
                <div class="d-flex gap-2">
                    <a href="<%= request.getContextPath() %>/book" class="btn btn-light btn-sm text-primary fw-semibold">开始逛书</a>
                    <a href="<%= request.getContextPath() %>/order/list" class="btn btn-outline-light btn-sm">我的订单</a>
                </div>
            </div>
            <div class="text-white-75 small">
                <div class="d-flex flex-column gap-2">
                    <span>📦 当日下单 · 极速出库</span>
                    <span>🎁 积分抵现 · 会员特惠</span>
                    <span>🔍 智能检索 · 精准推荐</span>
                </div>
            </div>
        </div>
    </div>

    <div class="row g-3 mb-4">
        <div class="col-6 col-lg-3">
            <div class="card promo-card h-100">
                <div class="card-body">
                    <div class="text-primary fw-semibold mb-2">海量馆藏</div>
                    <div class="text-muted small">畅销/新书/经典分区，随时发现好书。</div>
                </div>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="card promo-card h-100">
                <div class="card-body">
                    <div class="text-success fw-semibold mb-2">会员折扣</div>
                    <div class="text-muted small">累积积分抵现，专属优惠不定期放送。</div>
                </div>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="card promo-card h-100">
                <div class="card-body">
                    <div class="text-warning fw-semibold mb-2">极速配送</div>
                    <div class="text-muted small">下单即刻处理，物流状态实时可查。</div>
                </div>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="card promo-card h-100">
                <div class="card-body">
                    <div class="text-danger fw-semibold mb-2">售后无忧</div>
                    <div class="text-muted small">支持订单跟踪与售后反馈，体验安心。</div>
                </div>
            </div>
        </div>
    </div>

    <div class="card shadow-sm border-0 mt-3">
        <div class="card-body py-4 px-4">
            <h2 class="h5 mb-3">快速入口</h2>
            <div class="row g-3">
                <div class="col-12 col-md-6 col-lg-4">
                    <div class="card h-100 border-0 bg-light shadow-sm">
                        <div class="card-body">
                            <h5 class="card-title text-primary mb-2">书目查询</h5>
                            <p class="card-text small text-muted mb-3">搜索书名、作者、关键字，快速找到心仪好书。</p>
                            <a href="<%= request.getContextPath() %>/book" class="btn btn-primary btn-sm">去看看</a>
                        </div>
                    </div>
                </div>
                <div class="col-12 col-md-6 col-lg-4">
                    <div class="card h-100 border-0 bg-light shadow-sm">
                        <div class="card-body">
                            <h5 class="card-title text-success mb-2">购物车/下单</h5>
                            <p class="card-text small text-muted mb-3">加入购物车、一键结算，下单即刻发货。</p>
                            <a href="<%= request.getContextPath() %>/order/cart" class="btn btn-success btn-sm">去下单</a>
                        </div>
                    </div>
                </div>
                <div class="col-12 col-md-6 col-lg-4">
                    <div class="card h-100 border-0 bg-light shadow-sm">
                        <div class="card-body">
                            <h5 class="card-title text-info mb-2">我的订单</h5>
                            <p class="card-text small text-muted mb-3">查看订单进度、支付状态与物流信息。</p>
                            <a href="<%= request.getContextPath() %>/order/list" class="btn btn-info btn-sm">去查看</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="card shadow-sm border-0 mt-3">
        <div class="card-body py-4 px-4">
            <h2 class="h5 mb-3">为什么选择我们</h2>
            <div class="row g-3">
                <div class="col-md-6">
                    <ul class="list-unstyled small mb-0">
                        <li class="mb-2">✓ 官方正版直采，品质保障</li>
                        <li class="mb-2">✓ 多种支付方式，安全便捷</li>
                        <li class="mb-2">✓ 智能推荐，贴合你的阅读口味</li>
                    </ul>
                </div>
                <div class="col-md-6">
                    <ul class="list-unstyled small mb-0">
                        <li class="mb-2">✓ 积分返利，会员专属折扣</li>
                        <li class="mb-2">✓ 物流跟踪，实时掌握配送进度</li>
                        <li class="mb-2">✓ 客服支持，售前售后随时响应</li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    <% } %>
</div>

<jsp:include page="/jsp/common/footer.jsp"/>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="js/common.js"></script>
</body>
</html>

