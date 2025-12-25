<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.OrderItem" %>
<%@ page import="dao.BookDao" %>
<%
    List<Map<String, Object>> pendingShipments = (List<Map<String, Object>>) request.getAttribute("pendingShipments");
    
    if (pendingShipments == null) {
        pendingShipments = new java.util.ArrayList<>();
    }
    
    BookDao bookDao = new BookDao();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>物流管理 - 网上书店管理系统</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <!-- 自定义样式 -->
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp"/>
    <jsp:include page="/jsp/common/message.jsp"/>
    
    <div class="container mt-3 mb-5">
        <div class="row">
            <div class="col-12">
                <!-- 物流管理订单列表 -->
                <div class="card">
                    <div class="card-header">
                        <i class="bi bi-table"></i> 物流管理
                        <span class="badge bg-primary ms-2"><%= pendingShipments.size() %> 项</span>
                    </div>
                    <div class="card-body">
                        <% if (pendingShipments.isEmpty()) { %>
                            <div class="alert alert-info text-center">
                                <i class="bi bi-info-circle"></i> 暂无订单
                            </div>
                        <% } else { %>
                            <div class="table-responsive">
                                <table class="table table-hover table-striped">
                                    <thead>
                                        <tr>
                                            <th width="8%">订单ID</th>
                                            <th width="8%">订单状态</th>
                                            <th width="20%">书名</th>
                                            <th width="12%">书号(ISBN)</th>
                                            <th width="10%">订购数量</th>
                                            <th width="10%">已发货数量</th>
                                            <th width="10%">待发货数量</th>
                                            <th width="22%">操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (Map<String, Object> item : pendingShipments) { 
                                            Integer orderId = (Integer) item.get("orderId");
                                            String orderStatus = (String) item.get("orderStatus");
                                            Integer bookId = (Integer) item.get("bookId");
                                            Integer orderQuantity = (Integer) item.get("orderQuantity");
                                            Integer shippedQuantity = (Integer) item.get("shippedQuantity");
                                            Integer pendingQuantity = (Integer) item.get("pendingQuantity");
                                            
                                            String statusUpper = orderStatus != null ? orderStatus.toUpperCase() : "";
                                            String statusBadgeClass = "";
                                            String statusText = "";
                                            
                                            switch (statusUpper) {
                                                case "PAID":
                                                    statusBadgeClass = "bg-info";
                                                    statusText = "已支付";
                                                    break;
                                                case "PARTIAL":
                                                    statusBadgeClass = "bg-warning";
                                                    statusText = "部分发货";
                                                    break;
                                                case "SHIPPED":
                                                    statusBadgeClass = "bg-success";
                                                    statusText = "已发货";
                                                    break;
                                                default:
                                                    statusBadgeClass = "bg-secondary";
                                                    statusText = orderStatus != null ? orderStatus : "未知";
                                            }
                                            
                                            boolean canShip = ("PAID".equals(statusUpper) || "PARTIAL".equals(statusUpper)) && pendingQuantity > 0;
                                            
                                            model.Book book = bookDao.findById(bookId);
                                            String bookTitle = book != null && book.getTitle() != null ? 
                                                book.getTitle() : "未知书名";
                                            String bookIsbn = book != null && book.getIsbn() != null && !book.getIsbn().trim().isEmpty() ? 
                                                book.getIsbn() : "-";
                                        %>
                                            <tr>
                                                <td><strong>#<%= orderId %></strong></td>
                                                <td>
                                                    <span class="badge <%= statusBadgeClass %>">
                                                        <%= statusText %>
                                                    </span>
                                                </td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/book/detail?bookId=<%= bookId %>" 
                                                       class="text-decoration-none">
                                                        <%= bookTitle %>
                                                    </a>
                                                </td>
                                                <td><%= bookIsbn %></td>
                                                <td><%= orderQuantity %></td>
                                                <td>
                                                    <span class="badge bg-info"><%= shippedQuantity %></span>
                                                </td>
                                                <td>
                                                    <% if (pendingQuantity > 0) { %>
                                                        <span class="badge bg-warning"><%= pendingQuantity %></span>
                                                    <% } else { %>
                                                        <span class="badge bg-success">已全部发货</span>
                                                    <% } %>
                                                </td>
                                                <td>
                                                    <% if (canShip) { %>
                                                        <button type="button" 
                                                                class="btn btn-sm btn-success" 
                                                                data-bs-toggle="modal" 
                                                                data-bs-target="#shipModal<%= orderId %>_<%= bookId %>">
                                                            <i class="bi bi-truck"></i> 发货
                                                        </button>
                                                    <% } else { %>
                                                        <span class="text-muted">已发货</span>
                                                    <% } %>
                                                </td>
                                            </tr>
                                            
                                            <% if (canShip) { %>
                                            <!-- 发货模态框 -->
                                            <div class="modal fade" id="shipModal<%= orderId %>_<%= bookId %>" tabindex="-1" 
                                                 aria-labelledby="shipModalLabel<%= orderId %>_<%= bookId %>" aria-hidden="true">
                                                <div class="modal-dialog">
                                                    <div class="modal-content">
                                                        <div class="modal-header">
                                                            <h5 class="modal-title" id="shipModalLabel<%= orderId %>_<%= bookId %>">
                                                                <i class="bi bi-truck"></i> 发货
                                                            </h5>
                                                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                        </div>
                                                        <form action="${pageContext.request.contextPath}/shipment/ship" method="POST">
                                                            <div class="modal-body">
                                                                <input type="hidden" name="orderId" value="<%= orderId %>">
                                                                <input type="hidden" name="bookId" value="<%= bookId %>">
                                                                <div class="mb-3">
                                                                    <label class="form-label">订单ID</label>
                                                                    <input type="text" class="form-control" value="#<%= orderId %>" readonly>
                                                                </div>
                                                                <div class="mb-3">
                                                                    <label class="form-label">书名</label>
                                                                    <input type="text" class="form-control" value="<%= bookTitle %>" readonly>
                                                                </div>
                                                                <div class="mb-3">
                                                                    <label class="form-label">订购数量</label>
                                                                    <input type="text" class="form-control" value="<%= orderQuantity %>" readonly>
                                                                </div>
                                                                <div class="mb-3">
                                                                    <label class="form-label">已发货数量</label>
                                                                    <input type="text" class="form-control" value="<%= shippedQuantity %>" readonly>
                                                                </div>
                                                                <div class="mb-3">
                                                                    <label for="shipQuantity<%= orderId %>_<%= bookId %>" class="form-label">
                                                                        发货数量 <span class="text-danger">*</span>
                                                                    </label>
                                                                    <input type="number" 
                                                                           class="form-control" 
                                                                           id="shipQuantity<%= orderId %>_<%= bookId %>" 
                                                                           name="shipQuantity" 
                                                                           min="1" 
                                                                           max="<%= pendingQuantity %>" 
                                                                           value="<%= pendingQuantity %>" 
                                                                           required>
                                                                    <small class="form-text text-muted">
                                                                        最多可发货：<%= pendingQuantity %> 本
                                                                    </small>
                                                                </div>
                                                                <div class="mb-3">
                                                                    <label for="carrier<%= orderId %>_<%= bookId %>" class="form-label">承运商 <span class="text-danger">*</span></label>
                                                                    <input type="text" 
                                                                           class="form-control" 
                                                                           id="carrier<%= orderId %>_<%= bookId %>" 
                                                                           name="carrier" 
                                                                           placeholder="例如：顺丰快递"
                                                                           required>
                                                                    <small class="form-text text-muted">
                                                                        请输入承运商名称
                                                                    </small>
                                                                </div>
                                                                <div class="mb-3">
                                                                    <label for="trackingNo<%= orderId %>_<%= bookId %>" class="form-label">运单号（自动生成）</label>
                                                                    <input type="text" 
                                                                           class="form-control" 
                                                                           id="trackingNo<%= orderId %>_<%= bookId %>" 
                                                                           name="trackingNo" 
                                                                           readonly
                                                                           style="background-color: #e9ecef; cursor: not-allowed;"
                                                                           value="">
                                                                    <small class="form-text text-muted">
                                                                        运单号将自动生成，格式：TRACK-时间戳
                                                                    </small>
                                                                </div>
                                                                <script>
                                                                    // 自动生成运单号预览
                                                                    (function() {
                                                                        var trackingNoInput = document.getElementById('trackingNo<%= orderId %>_<%= bookId %>');
                                                                        if (trackingNoInput) {
                                                                            var previewTrackingNo = 'TRACK-' + Date.now();
                                                                            trackingNoInput.value = previewTrackingNo;
                                                                        }
                                                                    })();
                                                                </script>
                                                            </div>
                                                            <div class="modal-footer">
                                                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                                                                <button type="submit" class="btn btn-success">确认发货</button>
                                                            </div>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>
                                            <% } %>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        <% } %>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="/jsp/common/footer.jsp"/>
    
    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <!-- 自定义JS -->
    <script src="${pageContext.request.contextPath}/js/common.js"></script>
</body>
</html>

