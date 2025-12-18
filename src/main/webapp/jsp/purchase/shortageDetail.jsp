<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="model.ShortageRecord" %>
<%@ page import="model.PurchaseOrder" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="dao.BookDao" %>
<%@ page import="dao.SupplierDao" %>
<%
    ShortageRecord record = (ShortageRecord) request.getAttribute("shortageRecord");
    List<PurchaseOrder> relatedOrders = (List<PurchaseOrder>) request.getAttribute("relatedPurchaseOrders");

    if (record == null) {
        response.sendRedirect(request.getContextPath() + "/purchase/shortage/list");
        return;
    }
    if (relatedOrders == null) {
        relatedOrders = new java.util.ArrayList<>();
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    BookDao bookDao = new BookDao();
    SupplierDao supplierDao = new SupplierDao();

    model.Book book = record.getBookId() != null ? bookDao.findById(record.getBookId()) : null;
    String bookTitle = book != null && book.getTitle() != null ? book.getTitle() : "未知书名";

    model.Supplier supplier = record.getSupplierId() != null ? supplierDao.findById(record.getSupplierId()) : null;
    String supplierName = supplier != null && supplier.getName() != null ? supplier.getName() : "未知供应商";

    Boolean processed = record.getProcessed();
    boolean isProcessed = processed != null && processed;

    String sourceType = record.getSourceType() != null ? record.getSourceType() : "UNKNOWN";
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>缺书记录详情 #<%= record.getShortageId() %> - 网上书店管理系统</title>
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

    <div class="container-fluid mt-4">
        <div class="row">
            <div class="col-12">
                <!-- 返回按钮 -->
                <a href="${pageContext.request.contextPath}/purchase/shortage/list" class="btn btn-outline-secondary mb-3">
                    <i class="bi bi-arrow-left"></i> 返回缺书记录列表
                </a>

                <!-- 缺书记录基本信息 -->
                <div class="card mb-4">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <span><i class="bi bi-info-circle"></i> 缺书记录基本信息</span>
                        <div>
                            <form method="post"
                                  action="${pageContext.request.contextPath}/purchase/shortage/createPo"
                                  class="d-inline"
                                  onsubmit="return confirm('确认根据该缺书记录生成采购单吗？');">
                                <input type="hidden" name="shortageId" value="<%= record.getShortageId() %>">
                                <button type="submit"
                                        class="btn btn-success btn-sm"
                                        <%= isProcessed ? "disabled" : "" %>>
                                    <i class="bi bi-box-seam"></i> 生成采购单
                                </button>
                            </form>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <p class="mb-2">
                                    <strong>缺书ID：</strong> #<%= record.getShortageId() %>
                                </p>
                                <p class="mb-2">
                                    <strong>书目：</strong>
                                    <a href="${pageContext.request.contextPath}/book/detail?bookId=<%= record.getBookId() %>"
                                       class="text-decoration-none">
                                        <%= bookTitle %>
                                    </a>
                                </p>
                                <p class="mb-2">
                                    <strong>供应商：</strong> <%= supplierName %>
                                </p>
                                <p class="mb-2">
                                    <strong>缺货数量：</strong>
                                    <span class="badge bg-danger">
                                        <%= record.getQuantity() != null ? record.getQuantity() : 0 %> 本
                                    </span>
                                </p>
                            </div>
                            <div class="col-md-6">
                                <p class="mb-2">
                                    <strong>记录时间：</strong>
                                    <%= record.getDate() != null ?
                                            record.getDate().format(formatter) : "未知" %>
                                </p>
                                <p class="mb-2">
                                    <strong>来源类型：</strong>
                                    <span class="badge bg-secondary">
                                        <%= sourceType %>
                                    </span>
                                </p>
                                <p class="mb-2">
                                    <strong>处理状态：</strong>
                                    <span class="badge <%= isProcessed ? "bg-success" : "bg-warning" %>">
                                        <%= isProcessed ? "已处理" : "未处理" %>
                                    </span>
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 关联采购单 -->
                <div class="card mb-4">
                    <div class="card-header">
                        <i class="bi bi-box-seam"></i> 关联采购单
                    </div>
                    <div class="card-body">
                        <% if (relatedOrders.isEmpty()) { %>
                            <div class="alert alert-info mb-0">
                                <i class="bi bi-info-circle"></i> 暂无与该缺书记录关联的采购单。
                            </div>
                        <% } else { %>
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th width="15%">采购单ID</th>
                                            <th width="25%">供应商</th>
                                            <th width="25%">创建时间</th>
                                            <th width="15%">状态</th>
                                            <th width="20%">操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <%
                                            for (PurchaseOrder po : relatedOrders) {
                                                String poStatus = po.getStatus() != null ? po.getStatus() : "CREATED";
                                                String poStatusNorm = poStatus != null ? poStatus.trim().toUpperCase() : "CREATED";
                                                String badgeClass;
                                                String text;
                                                if ("CREATED".equals(poStatusNorm)) {
                                                    badgeClass = "bg-warning";
                                                    text = "已创建";
                                                } else if ("COMPLETED".equals(poStatusNorm)) {
                                                    badgeClass = "bg-success";
                                                    text = "已完成";
                                                } else {
                                                    badgeClass = "bg-secondary";
                                                    text = poStatus;
                                                }

                                                model.Supplier poSupplier = po.getSupplierId() != null
                                                        ? supplierDao.findById(po.getSupplierId())
                                                        : null;
                                                String poSupplierName = poSupplier != null && poSupplier.getName() != null
                                                        ? poSupplier.getName()
                                                        : "未知供应商";
                                        %>
                                            <tr>
                                                <td><strong>#<%= po.getPoId() %></strong></td>
                                                <td><%= poSupplierName %></td>
                                                <td>
                                                    <%= po.getCreateDate() != null
                                                            ? po.getCreateDate().format(formatter)
                                                            : "未知" %>
                                                </td>
                                                <td>
                                                    <span class="badge <%= badgeClass %>"><%= text %></span>
                                                </td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/purchase/detail?poId=<%= po.getPoId() %>"
                                                       class="btn btn-sm btn-outline-primary">
                                                        <i class="bi bi-eye"></i> 查看采购单
                                                    </a>
                                                </td>
                                            </tr>
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


