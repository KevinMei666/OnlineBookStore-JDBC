<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="model.ShortageRecord" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="dao.BookDao" %>
<%@ page import="dao.SupplierDao" %>
<%
    List<ShortageRecord> shortageRecords = (List<ShortageRecord>) request.getAttribute("shortageRecords");
    String filter = request.getParameter("filter");
    
    if (shortageRecords == null) {
        shortageRecords = new java.util.ArrayList<>();
    }
    if (filter == null) {
        filter = "all";
    }
    
    // 根据筛选条件过滤记录
    if ("unprocessed".equals(filter)) {
        List<ShortageRecord> filtered = new java.util.ArrayList<>();
        for (ShortageRecord record : shortageRecords) {
            if (record.getProcessed() == null || !record.getProcessed()) {
                filtered.add(record);
            }
        }
        shortageRecords = filtered;
    }
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    BookDao bookDao = new BookDao();
    SupplierDao supplierDao = new SupplierDao();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>缺书记录管理 - 网上书店管理系统</title>
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
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <div></div>
                    <a href="${pageContext.request.contextPath}/purchase/shortage/create" class="btn btn-primary">
                        <i class="bi bi-plus-circle"></i> 创建缺书记录
                    </a>
                </div>
                
                <!-- 筛选区域 -->
                <div class="card mb-4">
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/purchase/shortage/list" method="GET" class="d-flex gap-2 align-items-end">
                            <div class="flex-grow-1">
                                <label for="filter" class="form-label">筛选条件</label>
                                <select class="form-select" id="filter" name="filter" onchange="this.form.submit()">
                                    <option value="all" <%= "all".equals(filter) ? "selected" : "" %>>全部记录</option>
                                    <option value="unprocessed" <%= "unprocessed".equals(filter) ? "selected" : "" %>>未处理</option>
                                </select>
                            </div>
                            <div>
                                <a href="${pageContext.request.contextPath}/purchase/shortage/list" class="btn btn-outline-secondary">
                                    <i class="bi bi-arrow-clockwise"></i> 重置
                                </a>
                            </div>
                        </form>
                    </div>
                </div>
                
                <!-- 缺书记录列表 -->
                <div class="card">
                    <div class="card-header">
                        <i class="bi bi-table"></i> 缺书记录列表
                        <span class="badge bg-primary ms-2"><%= shortageRecords.size() %> 条</span>
                    </div>
                    <div class="card-body">
                        <% if (shortageRecords.isEmpty()) { %>
                            <div class="alert alert-info text-center">
                                <i class="bi bi-info-circle"></i> 暂无缺书记录
                            </div>
                        <% } else { %>
                            <div class="table-responsive">
                                <table class="table table-hover table-striped">
                                    <thead>
                                        <tr>
                                            <th width="8%">缺书ID</th>
                                            <th width="25%">书名</th>
                                            <th width="15%">供应商</th>
                                            <th width="12%">缺货数量</th>
                                            <th width="18%">记录时间</th>
                                            <th width="12%">处理状态</th>
                                            <th width="10%">操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (ShortageRecord record : shortageRecords) { 
                                            model.Book book = bookDao.findById(record.getBookId() != null ? record.getBookId() : 0);
                                            String bookTitle = book != null && book.getTitle() != null ? 
                                                book.getTitle() : "未知书名";
                                            
                                            model.Supplier supplier = null;
                                            if (record.getSupplierId() != null) {
                                                supplier = supplierDao.findById(record.getSupplierId());
                                            }
                                            String supplierName = supplier != null && supplier.getName() != null ? 
                                                supplier.getName() : "未知供应商";
                                            
                                            Boolean processed = record.getProcessed();
                                            boolean isProcessed = processed != null && processed;
                                        %>
                                            <tr>
                                                <td><strong>#<%= record.getShortageId() %></strong></td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/book/detail?bookId=<%= record.getBookId() %>" 
                                                       class="text-decoration-none">
                                                        <%= bookTitle %>
                                                    </a>
                                                </td>
                                                <td><%= supplierName %></td>
                                                <td>
                                                    <span class="badge bg-danger">
                                                        <%= record.getQuantity() != null ? record.getQuantity() : 0 %> 本
                                                    </span>
                                                </td>
                                                <td>
                                                    <%= record.getDate() != null ? 
                                                        record.getDate().format(formatter) : "未知" %>
                                                </td>
                                                <td>
                                                    <span class="badge <%= isProcessed ? "bg-success" : "bg-warning" %>">
                                                        <%= isProcessed ? "已处理" : "未处理" %>
                                                    </span>
                                                </td>
                                                <td>
                                                    <div class="d-flex align-items-center gap-2 flex-wrap">
                                                    <a href="${pageContext.request.contextPath}/purchase/shortage/detail?shortageId=<%= record.getShortageId() %>" 
                                                           class="btn btn-sm btn-outline-primary"
                                                           style="white-space: nowrap;">
                                                        <i class="bi bi-eye"></i> 查看
                                                    </a>
                                                    <a href="${pageContext.request.contextPath}/purchase/shortage/createPo?shortageId=<%= record.getShortageId() %>"
                                                       class="btn btn-sm btn-success"
                                                       style="white-space: nowrap;"
                                                       <%= isProcessed ? "onclick=\"return false;\" style=\"pointer-events: none; opacity: 0.5;\"" : "" %>>
                                                        <i class="bi bi-box-seam"></i> 生成采购单
                                                    </a>
                                                    </div>
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

