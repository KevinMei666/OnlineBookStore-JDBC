<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="model.Customer" %>
<%
    Customer customer = (Customer) request.getAttribute("customer");
    if (customer == null) {
        response.sendRedirect(request.getContextPath() + "/customer/info");
        return;
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户中心 - 修改个人信息</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp"/>
    <jsp:include page="/jsp/common/message.jsp"/>

    <div class="container mt-4 mb-5">
        <div class="row justify-content-center">
            <div class="col-lg-8">
                <div class="card shadow-sm">
                    <div class="card-header d-flex align-items-center gap-2">
                        <i class="bi bi-person-gear"></i>
                        <span>修改个人信息</span>
                    </div>
                    <div class="card-body">
                        <form id="profileForm" action="${pageContext.request.contextPath}/customer/updateProfile" method="POST" class="row g-3">
                            <div class="col-12">
                                <label class="form-label">邮箱 <span class="text-danger">*</span></label>
                                <input type="email" name="email" class="form-control" required
                                       value="<%= customer.getEmail() != null ? customer.getEmail() : "" %>">
                                <small class="text-muted">用于登录和通知，请确保可用</small>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">姓名</label>
                                <input type="text" name="name" class="form-control"
                                       value="<%= customer.getName() != null ? customer.getName() : "" %>">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">联系方式/地址</label>
                                <input type="text" name="address" class="form-control"
                                       value="<%= customer.getAddress() != null ? customer.getAddress() : "" %>">
                            </div>
                            
                            <!-- 密码修改区域 -->
                            <div class="col-12">
                                <hr class="my-3">
                                <h6 class="mb-3">
                                    <i class="bi bi-lock"></i> 修改密码
                                </h6>
                            </div>
                            <div class="col-12">
                                <label class="form-label">当前密码</label>
                                <input type="password" name="oldPassword" class="form-control"
                                       placeholder="请输入当前密码以验证身份">
                                <small class="text-muted">修改密码时需要验证当前密码</small>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">新密码</label>
                                <input type="password" name="newPassword" class="form-control"
                                       placeholder="请输入新密码（至少6位）" minlength="6">
                                <small class="text-muted">密码长度至少6位</small>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">确认新密码</label>
                                <input type="password" name="confirmPassword" class="form-control"
                                       placeholder="请再次输入新密码" minlength="6">
                            </div>
                            <div class="col-12">
                                <small class="text-muted">
                                    <i class="bi bi-info-circle"></i> 提示：如果不需要修改密码，请将密码字段留空
                                </small>
                            </div>
                            
                            <div class="col-12 d-flex justify-content-end gap-2">
                                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/customer/info">
                                    <i class="bi bi-arrow-left"></i> 返回
                                </a>
                                <button type="submit" class="btn btn-primary" id="saveProfileBtn">
                                    <i class="bi bi-save"></i> 保存修改
                                </button>
                            </div>
                        </form>
                        
                        <script>
                            (function() {
                                // 保存按钮原始文本
                                var submitBtn = document.getElementById('saveProfileBtn');
                                var originalBtnText = submitBtn ? submitBtn.innerHTML : '';
                                
                                // 在表单提交时进行验证
                                document.getElementById('profileForm').addEventListener('submit', function(e) {
                                    var oldPassword = document.querySelector('input[name="oldPassword"]').value;
                                    var newPassword = document.querySelector('input[name="newPassword"]').value;
                                    var confirmPassword = document.querySelector('input[name="confirmPassword"]').value;
                                    
                                    // 如果填写了密码字段，需要验证
                                    if (oldPassword || newPassword || confirmPassword) {
                                        // 验证密码字段是否都填写
                                        if (!oldPassword || !newPassword || !confirmPassword) {
                                            e.preventDefault();
                                            e.stopImmediatePropagation(); // 立即阻止其他监听器
                                            alert('修改密码时，当前密码、新密码和确认密码都必须填写');
                                            // 延迟恢复按钮状态，确保在common.js执行后恢复
                                            setTimeout(function() {
                                                if (submitBtn) {
                                                    submitBtn.disabled = false;
                                                    submitBtn.innerHTML = originalBtnText;
                                                }
                                            }, 150);
                                            return false;
                                        }
                                        
                                        // 验证新密码长度
                                        if (newPassword.length < 6) {
                                            e.preventDefault();
                                            e.stopImmediatePropagation(); // 立即阻止其他监听器
                                            alert('新密码长度至少为6位');
                                            // 延迟恢复按钮状态
                                            setTimeout(function() {
                                                if (submitBtn) {
                                                    submitBtn.disabled = false;
                                                    submitBtn.innerHTML = originalBtnText;
                                                }
                                            }, 150);
                                            return false;
                                        }
                                        
                                        // 验证两次输入的新密码是否一致
                                        if (newPassword !== confirmPassword) {
                                            e.preventDefault();
                                            e.stopImmediatePropagation(); // 立即阻止其他监听器
                                            alert('两次输入的新密码不一致');
                                            // 延迟恢复按钮状态
                                            setTimeout(function() {
                                                if (submitBtn) {
                                                    submitBtn.disabled = false;
                                                    submitBtn.innerHTML = originalBtnText;
                                                }
                                            }, 150);
                                            return false;
                                        }
                                    }
                                    
                                    // 验证通过，允许表单提交（common.js会处理按钮状态）
                                });
                            })();
                        </script>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="/jsp/common/footer.jsp"/>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/common.js"></script>
</body>
</html>

