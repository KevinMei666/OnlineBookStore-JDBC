<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.math.BigDecimal" %>
<%
    String period = (String) request.getAttribute("period");
    String startDate = (String) request.getAttribute("startDate");
    String endDate = (String) request.getAttribute("endDate");
    List<Map<String, Object>> expenses = (List<Map<String, Object>>) request.getAttribute("expenses");
    List<Map<String, Object>> revenue = (List<Map<String, Object>>) request.getAttribute("revenue");
    List<Map<String, Object>> profit = (List<Map<String, Object>>) request.getAttribute("profit");
    BigDecimal totalExpenses = (BigDecimal) request.getAttribute("totalExpenses");
    BigDecimal totalRevenue = (BigDecimal) request.getAttribute("totalRevenue");
    BigDecimal totalProfit = (BigDecimal) request.getAttribute("totalProfit");
    
    if (period == null) period = "day";
    if (expenses == null) expenses = new java.util.ArrayList<>();
    if (revenue == null) revenue = new java.util.ArrayList<>();
    if (profit == null) profit = new java.util.ArrayList<>();
    if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;
    if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
    if (totalProfit == null) totalProfit = BigDecimal.ZERO;
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>财务统计 - 网上书店管理系统</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
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
                    <i class="bi bi-cash-stack"></i> 财务统计
                </h2>
                
                <!-- 查询条件 -->
                <div class="card mb-4">
                    <div class="card-header">
                        <i class="bi bi-funnel"></i> 查询条件
                    </div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/finance/statistics" method="GET" class="row g-3">
                            <div class="col-md-3">
                                <label for="period" class="form-label">统计周期</label>
                                <select class="form-select" id="period" name="period" onchange="this.form.submit()">
                                    <option value="day" <%= "day".equals(period) ? "selected" : "" %>>按日</option>
                                    <option value="month" <%= "month".equals(period) ? "selected" : "" %>>按月</option>
                                    <option value="year" <%= "year".equals(period) ? "selected" : "" %>>按年</option>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label for="startDate" class="form-label">开始日期</label>
                                <input type="date" class="form-control" id="startDate" name="startDate" 
                                       value="<%= startDate != null ? startDate : "" %>" required>
                            </div>
                            <div class="col-md-3">
                                <label for="endDate" class="form-label">结束日期</label>
                                <input type="date" class="form-control" id="endDate" name="endDate" 
                                       value="<%= endDate != null ? endDate : "" %>" required>
                            </div>
                            <div class="col-md-3 d-flex align-items-end">
                                <button type="submit" class="btn btn-primary w-100">
                                    <i class="bi bi-search"></i> 查询
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
                
                <!-- 总计卡片 -->
                <div class="row mb-4">
                    <div class="col-md-4">
                        <div class="card text-white bg-danger">
                            <div class="card-body">
                                <h5 class="card-title">
                                    <i class="bi bi-arrow-down-circle"></i> 总支出
                                </h5>
                                <h2 class="mb-0">¥<%= String.format("%.2f", totalExpenses) %></h2>
                                <small class="text-white-50">已完成采购单总金额</small>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="card text-white bg-success">
                            <div class="card-body">
                                <h5 class="card-title">
                                    <i class="bi bi-arrow-up-circle"></i> 总收入
                                </h5>
                                <h2 class="mb-0">¥<%= String.format("%.2f", totalRevenue) %></h2>
                                <small class="text-white-50">已支付订单总金额</small>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="card <%= totalProfit.compareTo(BigDecimal.ZERO) >= 0 ? "text-white bg-info" : "text-white bg-warning" %>">
                            <div class="card-body">
                                <h5 class="card-title">
                                    <i class="bi bi-graph-up"></i> 总盈利
                                </h5>
                                <h2 class="mb-0">¥<%= String.format("%.2f", totalProfit) %></h2>
                                <small class="text-white-50">收入 - 支出</small>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- 图表 -->
                <div class="card mb-4">
                    <div class="card-header">
                        <i class="bi bi-bar-chart"></i> 财务趋势图
                    </div>
                    <div class="card-body">
                        <canvas id="financeChart" height="80"></canvas>
                    </div>
                </div>
                
                <!-- 数据表格 -->
                <div class="card">
                    <div class="card-header">
                        <i class="bi bi-table"></i> 详细数据
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover table-striped">
                                <thead>
                                    <tr>
                                        <th>日期</th>
                                        <th class="text-danger">支出</th>
                                        <th class="text-success">收入</th>
                                        <th class="text-info">盈利</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <%
                                        // 合并数据
                                        Map<String, BigDecimal> expenseMap = new java.util.HashMap<>();
                                        for (Map<String, Object> item : expenses) {
                                            expenseMap.put((String) item.get("date"), (BigDecimal) item.get("amount"));
                                        }
                                        
                                        Map<String, BigDecimal> revenueMap = new java.util.HashMap<>();
                                        for (Map<String, Object> item : revenue) {
                                            revenueMap.put((String) item.get("date"), (BigDecimal) item.get("amount"));
                                        }
                                        
                                        java.util.Set<String> allDates = new java.util.HashSet<>();
                                        allDates.addAll(expenseMap.keySet());
                                        allDates.addAll(revenueMap.keySet());
                                        
                                        List<String> sortedDates = new java.util.ArrayList<>(allDates);
                                        sortedDates.sort(String::compareTo);
                                        
                                        for (String date : sortedDates) {
                                            BigDecimal expenseAmount = expenseMap.getOrDefault(date, BigDecimal.ZERO);
                                            BigDecimal revenueAmount = revenueMap.getOrDefault(date, BigDecimal.ZERO);
                                            BigDecimal profitAmount = revenueAmount.subtract(expenseAmount);
                                    %>
                                    <tr>
                                        <td><%= date %></td>
                                        <td class="text-danger">¥<%= String.format("%.2f", expenseAmount) %></td>
                                        <td class="text-success">¥<%= String.format("%.2f", revenueAmount) %></td>
                                        <td class="<%= profitAmount.compareTo(BigDecimal.ZERO) >= 0 ? "text-info" : "text-warning" %>">
                                            ¥<%= String.format("%.2f", profitAmount) %>
                                        </td>
                                    </tr>
                                    <% } %>
                                    <% if (sortedDates.isEmpty()) { %>
                                    <tr>
                                        <td colspan="4" class="text-center text-muted">暂无数据</td>
                                    </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="/jsp/common/footer.jsp"/>
    
    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <!-- 图表脚本 -->
    <script>
        // 准备数据
        const expenseData = [
            <% for (int i = 0; i < expenses.size(); i++) { 
                Map<String, Object> item = expenses.get(i);
                String date = (String) item.get("date");
                BigDecimal amount = (BigDecimal) item.get("amount");
            %>
            { x: '<%= date %>', y: <%= amount != null ? amount : 0 %> }<%= i < expenses.size() - 1 ? "," : "" %>
            <% } %>
        ];
        
        const revenueData = [
            <% for (int i = 0; i < revenue.size(); i++) { 
                Map<String, Object> item = revenue.get(i);
                String date = (String) item.get("date");
                BigDecimal amount = (BigDecimal) item.get("amount");
            %>
            { x: '<%= date %>', y: <%= amount != null ? amount : 0 %> }<%= i < revenue.size() - 1 ? "," : "" %>
            <% } %>
        ];
        
        const profitData = [
            <% for (int i = 0; i < profit.size(); i++) { 
                Map<String, Object> item = profit.get(i);
                String date = (String) item.get("date");
                BigDecimal amount = (BigDecimal) item.get("amount");
            %>
            { x: '<%= date %>', y: <%= amount != null ? amount : 0 %> }<%= i < profit.size() - 1 ? "," : "" %>
            <% } %>
        ];
        
        // 合并所有日期
        const allDates = new Set();
        expenseData.forEach(item => allDates.add(item.x));
        revenueData.forEach(item => allDates.add(item.x));
        profitData.forEach(item => allDates.add(item.x));
        
        const sortedDates = Array.from(allDates).sort();
        
        // 创建映射
        const expenseMap = new Map();
        expenseData.forEach(item => expenseMap.set(item.x, item.y));
        
        const revenueMap = new Map();
        revenueData.forEach(item => revenueMap.set(item.x, item.y));
        
        const profitMap = new Map();
        profitData.forEach(item => profitMap.set(item.x, item.y));
        
        // 准备图表数据
        const expenseValues = sortedDates.map(date => expenseMap.get(date) || 0);
        const revenueValues = sortedDates.map(date => revenueMap.get(date) || 0);
        const profitValues = sortedDates.map(date => profitMap.get(date) || 0);
        
        // 创建图表
        const ctx = document.getElementById('financeChart').getContext('2d');
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: sortedDates,
                datasets: [
                    {
                        label: '支出',
                        data: expenseValues,
                        borderColor: 'rgb(220, 53, 69)',
                        backgroundColor: 'rgba(220, 53, 69, 0.1)',
                        tension: 0.1,
                        fill: false
                    },
                    {
                        label: '收入',
                        data: revenueValues,
                        borderColor: 'rgb(25, 135, 84)',
                        backgroundColor: 'rgba(25, 135, 84, 0.1)',
                        tension: 0.1,
                        fill: false
                    },
                    {
                        label: '盈利',
                        data: profitValues,
                        borderColor: 'rgb(13, 110, 253)',
                        backgroundColor: 'rgba(13, 110, 253, 0.1)',
                        tension: 0.1,
                        fill: false
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        callbacks: {
                            label: function(context) {
                                return context.dataset.label + ': ¥' + context.parsed.y.toFixed(2);
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return '¥' + value.toFixed(2);
                            }
                        }
                    }
                }
            }
        });
    </script>
</body>
</html>

