<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>统计报表视图演示 - 网上书店管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
    <style>
        body {
            background-color: #f5f7fb;
        }
        .report-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .report-header h2 {
            margin-bottom: 0;
        }
        .report-intro {
            color: #6c757d;
            font-size: 0.9rem;
        }
        .nav-tabs .nav-link {
            font-size: 0.9rem;
            padding: 0.6rem 0.9rem;
        }
        .tab-pane h5 {
            font-size: 1rem;
            font-weight: 600;
            margin-bottom: 0.75rem;
        }
        .table thead th {
            white-space: nowrap;
        }
        .view-badge {
            font-size: 0.75rem;
            letter-spacing: 0.03em;
        }
    </style>
</head>
<body>
<jsp:include page="/jsp/common/header.jsp"/>

<div class="container mt-4 mb-5">
    <div class="card shadow-sm border-0">
        <div class="card-body">
            <div class="report-header mb-3">
                <div>
                    <h2 class="h4">统计报表视图演示</h2>
                    <div class="report-intro">
                        通过数据库视图（View）统一封装统计逻辑，前端页面只需简单查询即可完成多维度报表展示。
                    </div>
                </div>
                <span class="badge bg-primary-subtle text-primary border border-primary view-badge">
                    VIEW DEMO
                </span>
            </div>

            <ul class="nav nav-tabs mb-3" id="viewTabs" role="tablist">
                <li class="nav-item" role="presentation">
                    <button class="nav-link active" id="book-basic-tab" data-bs-toggle="tab" data-bs-target="#book-basic"
                            type="button" role="tab" aria-controls="book-basic" aria-selected="true">
                        书籍基本信息
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="book-detail-tab" data-bs-toggle="tab" data-bs-target="#book-detail"
                            type="button" role="tab" aria-controls="book-detail" aria-selected="false">
                        书籍详细信息
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="customer-info-tab" data-bs-toggle="tab" data-bs-target="#customer-info"
                            type="button" role="tab" aria-controls="customer-info" aria-selected="false">
                        客户信息视图
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="customer-orders-tab" data-bs-toggle="tab" data-bs-target="#customer-orders"
                            type="button" role="tab" aria-controls="customer-orders" aria-selected="false">
                        客户订单视图
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="order-items-tab" data-bs-toggle="tab" data-bs-target="#order-items"
                            type="button" role="tab" aria-controls="order-items" aria-selected="false">
                        订单明细视图
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="shipment-detail-tab" data-bs-toggle="tab" data-bs-target="#shipment-detail"
                            type="button" role="tab" aria-controls="shipment-detail" aria-selected="false">
                        发货明细视图
                    </button>
                </li>
            </ul>

            <div class="tab-content pt-2" id="viewTabsContent">
        <!-- v_book_basic_info -->
        <div class="tab-pane fade show active" id="book-basic" role="tabpanel" aria-labelledby="book-basic-tab">
            <h5 class="text-primary">视图：v_book_basic_info</h5>
            <p class="text-muted small mb-2">展示书籍的基础信息，用于快速浏览当前在售图书情况。</p>
            <div class="table-responsive">
                <table class="table table-striped table-hover table-bordered table-sm align-middle bg-white">
                    <thead class="table-light">
                    <tr>
                        <c:forEach var="col" items="${bookBasicColumns}">
                            <th scope="col">${col}</th>
                        </c:forEach>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="row" items="${bookBasicData}">
                        <tr>
                            <c:forEach var="value" items="${row}">
                                <td>${value}</td>
                            </c:forEach>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- v_book_detail -->
        <div class="tab-pane fade" id="book-detail" role="tabpanel" aria-labelledby="book-detail-tab">
            <h5 class="text-primary">视图：v_book_detail</h5>
            <p class="text-muted small mb-2">在基础信息之上补充作者、关键词等内容，用于满足复杂检索。</p>
            <div class="table-responsive">
                <table class="table table-striped table-hover table-bordered table-sm align-middle bg-white">
                    <thead class="table-light">
                    <tr>
                        <c:forEach var="col" items="${bookDetailColumns}">
                            <th scope="col">${col}</th>
                        </c:forEach>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="row" items="${bookDetailData}">
                        <tr>
                            <c:forEach var="value" items="${row}">
                                <td>${value}</td>
                            </c:forEach>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- v_customer_info -->
        <div class="tab-pane fade" id="customer-info" role="tabpanel" aria-labelledby="customer-info-tab">
            <h5 class="text-success">视图：v_customer_info</h5>
            <p class="text-muted small mb-2">整合客户基础资料、余额与信用等级，便于运营与风控分析。</p>
            <div class="table-responsive">
                <table class="table table-striped table-hover table-bordered table-sm align-middle bg-white">
                    <thead class="table-light">
                    <tr>
                        <c:forEach var="col" items="${customerInfoColumns}">
                            <th scope="col">${col}</th>
                        </c:forEach>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="row" items="${customerInfoData}">
                        <tr>
                            <c:forEach var="value" items="${row}">
                                <td>${value}</td>
                            </c:forEach>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- v_customer_orders -->
        <div class="tab-pane fade" id="customer-orders" role="tabpanel" aria-labelledby="customer-orders-tab">
            <h5 class="text-success">视图：v_customer_orders</h5>
            <p class="text-muted small mb-2">按客户维度聚合订单信息，用于分析客户价值与订单行为。</p>
            <div class="table-responsive">
                <table class="table table-striped table-hover table-bordered table-sm align-middle bg-white">
                    <thead class="table-light">
                    <tr>
                        <c:forEach var="col" items="${customerOrdersColumns}">
                            <th scope="col">${col}</th>
                        </c:forEach>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="row" items="${customerOrdersData}">
                        <tr>
                            <c:forEach var="value" items="${row}">
                                <td>${value}</td>
                            </c:forEach>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- v_order_items_detail -->
        <div class="tab-pane fade" id="order-items" role="tabpanel" aria-labelledby="order-items-tab">
            <h5 class="text-info">视图：v_order_items_detail</h5>
            <p class="text-muted small mb-2">从订单与明细角度展开，支持按书籍、时间、客户等多维统计。</p>
            <div class="table-responsive">
                <table class="table table-striped table-hover table-bordered table-sm align-middle bg-white">
                    <thead class="table-light">
                    <tr>
                        <c:forEach var="col" items="${orderItemsColumns}">
                            <th scope="col">${col}</th>
                        </c:forEach>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="row" items="${orderItemsData}">
                        <tr>
                            <c:forEach var="value" items="${row}">
                                <td>${value}</td>
                            </c:forEach>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- v_shipment_detail -->
        <div class="tab-pane fade" id="shipment-detail" role="tabpanel" aria-labelledby="shipment-detail-tab">
            <h5 class="text-warning">视图：v_shipment_detail</h5>
            <p class="text-muted small mb-2">串联订单与发货信息，用于追踪物流状态与履约效率。</p>
            <div class="table-responsive">
                <table class="table table-striped table-hover table-bordered table-sm align-middle bg-white">
                    <thead class="table-light">
                    <tr>
                        <c:forEach var="col" items="${shipmentDetailColumns}">
                            <th scope="col">${col}</th>
                        </c:forEach>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="row" items="${shipmentDetailData}">
                        <tr>
                            <c:forEach var="value" items="${row}">
                                <td>${value}</td>
                            </c:forEach>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
            </div>
        </div>
    </div>

<jsp:include page="/jsp/common/footer.jsp"/>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>


