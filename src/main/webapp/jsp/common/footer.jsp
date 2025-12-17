<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // 获取数据库连接状态（暂时写死，后续可从应用上下文获取）
    String dbStatus = "已连接";
    String dbStatusClass = "text-success";
    
    // 可以后续从应用上下文获取真实状态
    // Object dbStatusObj = application.getAttribute("dbStatus");
    // if (dbStatusObj != null) {
    //     dbStatus = dbStatusObj.toString();
    //     dbStatusClass = dbStatus.equals("已连接") ? "text-success" : "text-danger";
    // }
%>
<!-- 页脚 -->
<footer class="footer mt-auto py-4">
    <div class="container-fluid">
        <div class="row">
            <div class="col-md-6 text-center text-md-start">
                <p class="mb-0">
                    © 2024 网上书店管理系统 - 数据库原理课程实验
                </p>
            </div>
            <div class="col-md-6 text-center text-md-end">
                <p class="mb-0">
                    <i class="bi bi-database"></i> 数据库状态: 
                    <span class="<%= dbStatusClass %>">
                        <i class="bi bi-circle-fill" style="font-size: 8px;"></i> <%= dbStatus %>
                    </span>
                </p>
            </div>
        </div>
    </div>
</footer>

