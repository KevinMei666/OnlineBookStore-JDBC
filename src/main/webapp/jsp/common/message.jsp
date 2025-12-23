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
<!-- 顶部右侧消息浮层（全局可见） -->
<div id="toast-container-page" class="toast-container position-fixed top-0 end-0 p-3 message-toast-container" aria-live="polite" aria-atomic="true">
    <c:if test="${not empty successMessage}">
        <div class="toast align-items-center toast-success border-0 shadow-sm" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="4500">
            <div class="d-flex align-items-center gap-2 px-3 py-2">
                <i class="bi bi-check-circle-fill"></i>
                <div class="toast-body">${successMessage}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="toast align-items-center toast-danger border-0 shadow-sm" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="6000">
            <div class="d-flex align-items-center gap-2 px-3 py-2">
                <i class="bi bi-exclamation-triangle-fill"></i>
                <div class="toast-body">${errorMessage}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    </c:if>
    <c:if test="${not empty warningMessage}">
        <div class="toast align-items-center toast-warning border-0 shadow-sm" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="5500">
            <div class="d-flex align-items-center gap-2 px-3 py-2">
                <i class="bi bi-exclamation-circle-fill"></i>
                <div class="toast-body">${warningMessage}</div>
                <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    </c:if>
    <c:if test="${not empty infoMessage}">
        <div class="toast align-items-center toast-info border-0 shadow-sm" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="4500">
            <div class="d-flex align-items-center gap-2 px-3 py-2">
                <i class="bi bi-info-circle-fill"></i>
                <div class="toast-body">${infoMessage}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    </c:if>

    <% if (successMsg != null && !successMsg.isEmpty()) { %>
        <div class="toast align-items-center toast-success border-0 shadow-sm" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="4500">
            <div class="d-flex align-items-center gap-2 px-3 py-2">
                <i class="bi bi-check-circle-fill"></i>
                <div class="toast-body"><%= successMsg %></div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    <% } %>
    <% if (errorMsg != null && !errorMsg.isEmpty()) { %>
        <div class="toast align-items-center toast-danger border-0 shadow-sm" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="6000">
            <div class="d-flex align-items-center gap-2 px-3 py-2">
                <i class="bi bi-exclamation-triangle-fill"></i>
                <div class="toast-body"><%= errorMsg %></div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    <% } %>
    <% if (warningMsg != null && !warningMsg.isEmpty()) { %>
        <div class="toast align-items-center toast-warning border-0 shadow-sm" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="5500">
            <div class="d-flex align-items-center gap-2 px-3 py-2">
                <i class="bi bi-exclamation-circle-fill"></i>
                <div class="toast-body"><%= warningMsg %></div>
                <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    <% } %>
    <% if (infoMsg != null && !infoMsg.isEmpty()) { %>
        <div class="toast align-items-center toast-info border-0 shadow-sm" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="4500">
            <div class="d-flex align-items-center gap-2 px-3 py-2">
                <i class="bi bi-info-circle-fill"></i>
                <div class="toast-body"><%= infoMsg %></div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    <% } %>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const container = document.getElementById('toast-container-page');
        if (!container) return;
        const toasts = container.querySelectorAll('.toast');
        toasts.forEach(function(toastEl) {
            const toast = new bootstrap.Toast(toastEl);
            toast.show();
        });
    });
</script>

