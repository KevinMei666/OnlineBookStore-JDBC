<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Inquiry" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    List<Inquiry> inquiries = (List<Inquiry>) request.getAttribute("inquiries");
    if (inquiries == null) {
        inquiries = new java.util.ArrayList<>();
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>我的询价记录 - 网上书店管理系统</title>
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
                        <i class="bi bi-question-circle"></i> 我的询价记录
                    </div>
                    <div class="card-body">
                        <% if (inquiries.isEmpty()) { %>
                            <div class="alert alert-info text-center">
                                <i class="bi bi-info-circle"></i> 暂无询价记录
                                <hr>
                                <a href="${pageContext.request.contextPath}/inquiry/create" class="btn btn-primary">
                                    <i class="bi bi-plus-circle"></i> 提交新的询价请求
                                </a>
                            </div>
                        <% } else { %>
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th width="20%">书名</th>
                                            <th width="10%">作者</th>
                                            <th width="15%">出版社</th>
                                            <th width="8%">数量</th>
                                            <th width="15%">询价日期</th>
                                            <th width="10%">状态</th>
                                            <th width="22%">管理员回复</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (Inquiry inquiry : inquiries) { %>
                                            <tr>
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
                                                        if ("PENDING".equals(status)) {
                                                            badgeClass = "bg-warning";
                                                        } else if ("QUOTED".equals(status)) {
                                                            badgeClass = "bg-success";
                                                        } else if ("REJECTED".equals(status)) {
                                                            badgeClass = "bg-danger";
                                                        }
                                                    %>
                                                    <span class="badge <%= badgeClass %>">
                                                        <%= inquiry.getStatusText() %>
                                                    </span>
                                                </td>
                                                <td>
                                                    <% if (inquiry.getAdminResponse() != null && !inquiry.getAdminResponse().trim().isEmpty()) { %>
                                                        <small class="text-muted">
                                                            <%= inquiry.getAdminResponse().length() > 50 ? 
                                                                inquiry.getAdminResponse().substring(0, 50) + "..." : 
                                                                inquiry.getAdminResponse() %>
                                                        </small>
                                                        <% if (inquiry.getResponseDate() != null) { %>
                                                            <br><small class="text-muted">
                                                                <%= inquiry.getResponseDate().format(formatter) %>
                                                            </small>
                                                        <% } %>
                                                    <% } else { %>
                                                        <span class="text-muted">-</span>
                                                    <% } %>
                                                </td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                            <div class="mt-3">
                                <a href="${pageContext.request.contextPath}/inquiry/create" class="btn btn-primary">
                                    <i class="bi bi-plus-circle"></i> 提交新的询价请求
                                </a>
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

