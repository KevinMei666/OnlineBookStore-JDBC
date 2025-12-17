<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="model.Customer" %>
<%
    Customer customer = (Customer) request.getAttribute("customer");
    
    if (customer == null) {
        response.sendRedirect(request.getContextPath() + "/customer/info");
        return;
    }
    
    int creditLevel = customer.getCreditLevel() != null ? customer.getCreditLevel() : 1;
    String creditDesc = "";
    switch (creditLevel) {
        case 1:
            creditDesc = "1级（无折扣，不可透支）";
            break;
        case 2:
            creditDesc = "2级（5%折扣，不可透支）";
            break;
        case 3:
            creditDesc = "3级（15%折扣，可透支，有额度限制）";
            break;
        case 4:
            creditDesc = "4级（20%折扣，可透支，有额度限制）";
            break;
        case 5:
            creditDesc = "5级（25%折扣，可透支，无额度限制）";
            break;
        default:
            creditDesc = "未知等级";
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>客户信息 - 网上书店管理系统</title>
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
                <h2 class="mb-4">
                    <i class="bi bi-person"></i> 客户信息
                </h2>
                
                <div class="row">
                    <!-- 基本信息 -->
                    <div class="col-md-6">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="bi bi-info-circle"></i> 基本信息
                            </div>
                            <div class="card-body">
                                <p class="mb-2">
                                    <strong>客户ID：</strong> <%= customer.getCustomerId() %>
                                </p>
                                <p class="mb-2">
                                    <strong>姓名：</strong> <%= customer.getName() != null ? customer.getName() : "未设置" %>
                                </p>
                                <p class="mb-2">
                                    <strong>邮箱：</strong> <%= customer.getEmail() != null ? customer.getEmail() : "未设置" %>
                                </p>
                                <p class="mb-0">
                                    <strong>地址：</strong> <%= customer.getAddress() != null ? customer.getAddress() : "未设置" %>
                                </p>
                            </div>
                        </div>
                    </div>
                    
                    <!-- 账户信息 -->
                    <div class="col-md-6">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="bi bi-wallet2"></i> 账户信息
                            </div>
                            <div class="card-body">
                                <p class="mb-2">
                                    <strong>账户余额：</strong> 
                                    <span class="fs-4 text-danger fw-bold">
                                        ¥<%= customer.getBalance() != null ? customer.getBalance() : "0.00" %>
                                    </span>
                                </p>
                                <p class="mb-2">
                                    <strong>信用等级：</strong> 
                                    <span class="badge bg-primary">
                                        <%= creditDesc %>
                                    </span>
                                </p>
                                <p class="mb-0">
                                    <strong>透支额度：</strong> 
                                    <span class="text-info fw-bold">
                                        ¥<%= customer.getMonthlyLimit() != null ? customer.getMonthlyLimit() : "0.00" %>
                                    </span>
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- 功能操作区域 -->
                <div class="card">
                    <div class="card-header">
                        <i class="bi bi-gear"></i> 账户操作
                    </div>
                    <div class="card-body">
                        <div class="row g-3">
                            <div class="col-md-4">
                                <button type="button" class="btn btn-primary w-100" data-bs-toggle="modal" data-bs-target="#addBalanceModal">
                                    <i class="bi bi-plus-circle"></i> 充值余额
                                </button>
                            </div>
                            <div class="col-md-4">
                                <button type="button" class="btn btn-warning w-100" data-bs-toggle="modal" data-bs-target="#updateCreditLevelModal">
                                    <i class="bi bi-star"></i> 调整信用等级
                                </button>
                            </div>
                            <div class="col-md-4">
                                <button type="button" class="btn btn-info w-100" data-bs-toggle="modal" data-bs-target="#updateMonthlyLimitModal">
                                    <i class="bi bi-credit-card"></i> 调整透支额度
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <!-- 充值余额模态框 -->
    <div class="modal fade" id="addBalanceModal" tabindex="-1" aria-labelledby="addBalanceModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="addBalanceModalLabel">
                        <i class="bi bi-plus-circle"></i> 充值余额
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <form action="${pageContext.request.contextPath}/customer/addBalance" method="POST">
                    <div class="modal-body">
                        <input type="hidden" name="customerId" value="<%= customer.getCustomerId() %>">
                        <div class="mb-3">
                            <label for="amount" class="form-label">充值金额 <span class="text-danger">*</span></label>
                            <input type="number" class="form-control" id="amount" name="amount" 
                                   step="0.01" min="0.01" required placeholder="请输入充值金额">
                            <small class="form-text text-muted">当前余额：¥<%= customer.getBalance() != null ? customer.getBalance() : "0.00" %></small>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                        <button type="submit" class="btn btn-primary">确认充值</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    
    <!-- 调整信用等级模态框 -->
    <div class="modal fade" id="updateCreditLevelModal" tabindex="-1" aria-labelledby="updateCreditLevelModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="updateCreditLevelModalLabel">
                        <i class="bi bi-star"></i> 调整信用等级
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <form action="${pageContext.request.contextPath}/customer/updateCredit" method="POST">
                    <div class="modal-body">
                        <input type="hidden" name="customerId" value="<%= customer.getCustomerId() %>">
                        <input type="hidden" name="action" value="updateCreditLevel">
                        <div class="mb-3">
                            <label for="creditLevel" class="form-label">信用等级 <span class="text-danger">*</span></label>
                            <select class="form-select" id="creditLevel" name="creditLevel" required>
                                <option value="1" <%= creditLevel == 1 ? "selected" : "" %>>1级（无折扣，不可透支）</option>
                                <option value="2" <%= creditLevel == 2 ? "selected" : "" %>>2级（5%折扣，不可透支）</option>
                                <option value="3" <%= creditLevel == 3 ? "selected" : "" %>>3级（15%折扣，可透支，有额度限制）</option>
                                <option value="4" <%= creditLevel == 4 ? "selected" : "" %>>4级（20%折扣，可透支，有额度限制）</option>
                                <option value="5" <%= creditLevel == 5 ? "selected" : "" %>>5级（25%折扣，可透支，无额度限制）</option>
                            </select>
                            <small class="form-text text-muted">当前等级：<%= creditDesc %></small>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                        <button type="submit" class="btn btn-warning">确认调整</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    
    <!-- 调整透支额度模态框 -->
    <div class="modal fade" id="updateMonthlyLimitModal" tabindex="-1" aria-labelledby="updateMonthlyLimitModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="updateMonthlyLimitModalLabel">
                        <i class="bi bi-credit-card"></i> 调整透支额度
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <form action="${pageContext.request.contextPath}/customer/updateCredit" method="POST">
                    <div class="modal-body">
                        <input type="hidden" name="customerId" value="<%= customer.getCustomerId() %>">
                        <input type="hidden" name="action" value="updateMonthlyLimit">
                        <div class="mb-3">
                            <label for="monthlyLimit" class="form-label">透支额度 <span class="text-danger">*</span></label>
                            <input type="number" class="form-control" id="monthlyLimit" name="monthlyLimit" 
                                   step="0.01" min="0" required placeholder="请输入透支额度">
                            <small class="form-text text-muted">当前透支额度：¥<%= customer.getMonthlyLimit() != null ? customer.getMonthlyLimit() : "0.00" %></small>
                            <small class="form-text text-muted">注意：1-2级信用不可透支，3-4级信用有额度限制，5级信用无额度限制</small>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                        <button type="submit" class="btn btn-info">确认调整</button>
                    </div>
                </form>
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

