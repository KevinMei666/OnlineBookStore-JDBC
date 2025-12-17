<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    // 从request中获取消息
    String successMsg = (String) request.getAttribute("successMessage");
    String errorMsg = (String) request.getAttribute("errorMessage");
    String warningMsg = (String) request.getAttribute("warningMessage");
    String infoMsg = (String) request.getAttribute("infoMessage");
    
    // 从session中获取消息（使用后清除）
    if (successMsg == null) {
        successMsg = (String) session.getAttribute("successMessage");
        if (successMsg != null) {
            session.removeAttribute("successMessage");
        }
    }
    if (errorMsg == null) {
        errorMsg = (String) session.getAttribute("errorMessage");
        if (errorMsg != null) {
            session.removeAttribute("errorMessage");
        }
    }
    if (warningMsg == null) {
        warningMsg = (String) session.getAttribute("warningMessage");
        if (warningMsg != null) {
            session.removeAttribute("warningMessage");
        }
    }
    if (infoMsg == null) {
        infoMsg = (String) session.getAttribute("infoMessage");
        if (infoMsg != null) {
            session.removeAttribute("infoMessage");
        }
    }
%>
<!-- 消息提示区域 -->
<div id="message-container-page" class="container-fluid mt-3">
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill"></i> ${successMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill"></i> ${errorMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    
    <c:if test="${not empty warningMessage}">
        <div class="alert alert-warning alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-circle-fill"></i> ${warningMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    
    <c:if test="${not empty infoMessage}">
        <div class="alert alert-info alert-dismissible fade show" role="alert">
            <i class="bi bi-info-circle-fill"></i> ${infoMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    
    <%-- 使用JSP脚本显示消息（兼容性更好） --%>
    <% if (successMsg != null && !successMsg.isEmpty()) { %>
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill"></i> <%= successMsg %>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <% } %>
    
    <% if (errorMsg != null && !errorMsg.isEmpty()) { %>
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill"></i> <%= errorMsg %>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <% } %>
    
    <% if (warningMsg != null && !warningMsg.isEmpty()) { %>
        <div class="alert alert-warning alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-circle-fill"></i> <%= warningMsg %>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <% } %>
    
    <% if (infoMsg != null && !infoMsg.isEmpty()) { %>
        <div class="alert alert-info alert-dismissible fade show" role="alert">
            <i class="bi bi-info-circle-fill"></i> <%= infoMsg %>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <% } %>
</div>

<script>
    // 自动隐藏消息（5秒后）
    document.addEventListener('DOMContentLoaded', function() {
        const alerts = document.querySelectorAll('#message-container-page .alert');
        alerts.forEach(function(alert) {
            setTimeout(function() {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }, 5000);
        });
    });
</script>

