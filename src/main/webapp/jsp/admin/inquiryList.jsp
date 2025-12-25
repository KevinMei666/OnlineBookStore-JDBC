<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Inquiry" %>
<%@ page import="dao.CustomerDao" %>
<%@ page import="model.Customer" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    List<Inquiry> inquiries = (List<Inquiry>) request.getAttribute("inquiries");
    if (inquiries == null) {
        inquiries = new java.util.ArrayList<>();
    }
    CustomerDao customerDao = new CustomerDao();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>询价管理 - 网上书店管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp"/>
    <jsp:include page="/jsp/common/message.jsp"/>
    
    <div class="container mt-4">
        <div class="row">
            <div class="col-12">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-primary text-white">
                        <i class="bi bi-question-circle"></i> 询价管理
                    </div>
                    <div class="card-body">
                        <% if (inquiries.isEmpty()) { %>
                            <div class="alert alert-info text-center">
                                <i class="bi bi-info-circle"></i> 暂无询价记录
                            </div>
                        <% } else { %>
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th width="6%">询价ID</th>
                                            <th width="9%">客户</th>
                                            <th width="15%">书名</th>
                                            <th width="9%">作者</th>
                                            <th width="11%">出版社</th>
                                            <th width="5%">数量</th>
                                            <th width="10%">询价日期</th>
                                            <th width="8%">状态</th>
                                            <th width="12%">回复日期</th>
                                            <th width="15%">操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (Inquiry inquiry : inquiries) { 
                                            Customer customer = customerDao.findById(inquiry.getCustomerId());
                                            String customerName = customer != null ? customer.getName() : "未知客户";
                                        %>
                                            <tr>
                                                <td><%= inquiry.getInquiryId() %></td>
                                                <td>
                                                    <small><%= customerName %></small><br>
                                                    <small class="text-muted">ID: <%= inquiry.getCustomerId() %></small>
                                                </td>
                                                <td><strong><%= inquiry.getBookTitle() != null ? inquiry.getBookTitle() : "-" %></strong></td>
                                                <td><%= inquiry.getAuthor() != null ? inquiry.getAuthor() : "-" %></td>
                                                <td><%= inquiry.getPublisher() != null ? inquiry.getPublisher() : "-" %></td>
                                                <td><%= inquiry.getQuantity() %></td>
                                                <td>
                                                    <% if (inquiry.getInquiryDate() != null) { %>
                                                        <%= inquiry.getInquiryDate().format(formatter) %>
                                                    <% } else { %>
                                                        -
                                                    <% } %>
                                                </td>
                                                <td>
                                                    <% 
                                                        String status = inquiry.getStatus();
                                                        String badgeClass = "bg-secondary";
                                                        String statusIcon = "";
                                                        if ("PENDING".equals(status)) {
                                                            badgeClass = "bg-warning text-dark";
                                                            statusIcon = "<i class='bi bi-clock-history'></i> ";
                                                        } else if ("QUOTED".equals(status)) {
                                                            badgeClass = "bg-success";
                                                            statusIcon = "<i class='bi bi-check-circle'></i> ";
                                                        } else if ("REJECTED".equals(status)) {
                                                            badgeClass = "bg-danger";
                                                            statusIcon = "<i class='bi bi-x-circle'></i> ";
                                                        }
                                                    %>
                                                    <span class="badge <%= badgeClass %>">
                                                        <%= statusIcon %><%= inquiry.getStatusText() %>
                                                    </span>
                                                    <% if (inquiry.getAdminResponse() != null && !inquiry.getAdminResponse().trim().isEmpty()) { %>
                                                        <br><small class="text-muted">
                                                            <i class="bi bi-chat-dots"></i> 已回复
                                                        </small>
                                                    <% } %>
                                                </td>
                                                <td>
                                                    <% if (inquiry.getResponseDate() != null) { %>
                                                        <span class="text-success">
                                                            <i class="bi bi-calendar-check"></i> 
                                                            <%= inquiry.getResponseDate().format(formatter) %>
                                                        </span>
                                                    <% } else { %>
                                                        <span class="text-muted">
                                                            <i class="bi bi-dash-circle"></i> 未回复
                                                        </span>
                                                    <% } %>
                                                </td>
                                                <td>
                                                    <% 
                                                        boolean isReplied = inquiry.getAdminResponse() != null && !inquiry.getAdminResponse().trim().isEmpty();
                                                        String buttonClass = "PENDING".equals(status) ? "btn-primary" : "btn-outline-primary";
                                                        String buttonText = "PENDING".equals(status) ? "回复" : (isReplied ? "查看回复" : "查看/修改");
                                                        String buttonIcon = isReplied ? "bi-eye" : "bi-reply";
                                                    %>
                                                    <button type="button" class="btn btn-sm <%= buttonClass %>" 
                                                            data-bs-toggle="modal" 
                                                            data-bs-target="#responseModal<%= inquiry.getInquiryId() %>">
                                                        <i class="bi <%= buttonIcon %>"></i> 
                                                        <%= buttonText %>
                                                    </button>
                                                </td>
                                            </tr>
                                            
                                            <!-- 回复模态框 -->
                                            <div class="modal fade" id="responseModal<%= inquiry.getInquiryId() %>" tabindex="-1">
                                                <div class="modal-dialog">
                                                    <div class="modal-content">
                                                        <div class="modal-header">
                                                            <h5 class="modal-title">回复询价请求 #<%= inquiry.getInquiryId() %></h5>
                                                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                        </div>
                                                        <form action="${pageContext.request.contextPath}/admin/inquiry/respond" method="POST">
                                                            <input type="hidden" name="inquiryId" value="<%= inquiry.getInquiryId() %>">
                                                            <div class="modal-body">
                                                                <div class="mb-3">
                                                                    <label class="form-label"><strong>书名：</strong></label>
                                                                    <p><%= inquiry.getBookTitle() %></p>
                                                                </div>
                                                                <div class="mb-3">
                                                                    <label class="form-label"><strong>客户：</strong></label>
                                                                    <p><%= customerName %> (ID: <%= inquiry.getCustomerId() %>)</p>
                                                                </div>
                                                                <div class="mb-3">
                                                                    <label class="form-label"><strong>数量：</strong></label>
                                                                    <p><%= inquiry.getQuantity() %></p>
                                                                </div>
                                                                <div class="mb-3">
                                                                    <label for="status<%= inquiry.getInquiryId() %>" class="form-label">状态</label>
                                                                    <select class="form-select" id="status<%= inquiry.getInquiryId() %>" name="status" required>
                                                                        <option value="PENDING" <%= "PENDING".equals(status) ? "selected" : "" %>>待处理</option>
                                                                        <option value="QUOTED" <%= "QUOTED".equals(status) ? "selected" : "" %>>已报价</option>
                                                                        <option value="REJECTED" <%= "REJECTED".equals(status) ? "selected" : "" %>>已拒绝</option>
                                                                    </select>
                                                                </div>
                                                                <div class="mb-3">
                                                                    <label for="adminResponse<%= inquiry.getInquiryId() %>" class="form-label">
                                                                        回复内容
                                                                        <% if (inquiry.getAdminResponse() != null && !inquiry.getAdminResponse().trim().isEmpty()) { %>
                                                                            <span class="badge bg-success ms-2">已填写</span>
                                                                        <% } else { %>
                                                                            <span class="badge bg-warning text-dark ms-2">未填写</span>
                                                                        <% } %>
                                                                    </label>
                                                                    <textarea class="form-control" id="adminResponse<%= inquiry.getInquiryId() %>" 
                                                                              name="adminResponse" rows="4" 
                                                                              placeholder="请输入报价信息、价格、预计到货时间等，或拒绝原因"><%= inquiry.getAdminResponse() != null ? inquiry.getAdminResponse() : "" %></textarea>
                                                                    <% if (inquiry.getResponseDate() != null) { %>
                                                                        <small class="text-muted">
                                                                            <i class="bi bi-clock"></i> 回复时间：<%= inquiry.getResponseDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) %>
                                                                        </small>
                                                                    <% } %>
                                                                </div>
                                                            </div>
                                                            <div class="modal-footer">
                                                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                                                                <button type="submit" class="btn btn-primary">保存回复</button>
                                                            </div>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>
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
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

