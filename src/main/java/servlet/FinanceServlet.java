package servlet;

import dao.FinanceDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 财务统计Servlet
 */
public class FinanceServlet extends HttpServlet {
    
    private FinanceDao financeDao;
    
    @Override
    public void init() throws ServletException {
        super.init();
        financeDao = new FinanceDao();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        String currentRole = (String) session.getAttribute("currentRole");
        
        // 检查管理员权限
        if (!"ADMIN".equals(currentRole)) {
            session.setAttribute("warningMessage", "只有管理员可以访问财务统计页面");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/statistics")) {
            handleStatistics(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * 显示财务统计页面
     */
    private void handleStatistics(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 获取查询参数
        String period = request.getParameter("period"); // day, month, year
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        
        // 默认值：如果没有指定，查询最近30天的数据
        if (period == null || period.isEmpty()) {
            period = "day";
        }
        
        LocalDate today = LocalDate.now();
        if (startDate == null || startDate.isEmpty()) {
            startDate = today.minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        // 查询数据
        List<Map<String, Object>> expenses;
        List<Map<String, Object>> revenue;
        
        switch (period.toLowerCase()) {
            case "month":
                expenses = financeDao.getExpensesByMonth(startDate, endDate);
                revenue = financeDao.getRevenueByMonth(startDate, endDate);
                break;
            case "year":
                expenses = financeDao.getExpensesByYear(startDate, endDate);
                revenue = financeDao.getRevenueByYear(startDate, endDate);
                break;
            case "day":
            default:
                expenses = financeDao.getExpensesByDay(startDate, endDate);
                revenue = financeDao.getRevenueByDay(startDate, endDate);
                break;
        }
        
        // 计算盈利数据
        List<Map<String, Object>> profit = calculateProfit(expenses, revenue, period);
        
        // 计算总计
        BigDecimal totalExpenses = financeDao.getTotalExpenses();
        BigDecimal totalRevenue = financeDao.getTotalRevenue();
        BigDecimal totalProfit = totalRevenue.subtract(totalExpenses);
        
        // 设置请求属性
        request.setAttribute("period", period);
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        request.setAttribute("expenses", expenses);
        request.setAttribute("revenue", revenue);
        request.setAttribute("profit", profit);
        request.setAttribute("totalExpenses", totalExpenses);
        request.setAttribute("totalRevenue", totalRevenue);
        request.setAttribute("totalProfit", totalProfit);
        
        // 转发到JSP页面
        request.getRequestDispatcher("/jsp/admin/financeStatistics.jsp").forward(request, response);
    }
    
    /**
     * 计算盈利数据（收入 - 支出）
     */
    private List<Map<String, Object>> calculateProfit(
            List<Map<String, Object>> expenses, 
            List<Map<String, Object>> revenue,
            String period) {
        
        // 创建日期到金额的映射
        Map<String, BigDecimal> expenseMap = new java.util.HashMap<>();
        for (Map<String, Object> item : expenses) {
            String date = (String) item.get("date");
            BigDecimal amount = (BigDecimal) item.get("amount");
            expenseMap.put(date, amount);
        }
        
        Map<String, BigDecimal> revenueMap = new java.util.HashMap<>();
        for (Map<String, Object> item : revenue) {
            String date = (String) item.get("date");
            BigDecimal amount = (BigDecimal) item.get("amount");
            revenueMap.put(date, amount);
        }
        
        // 合并所有日期
        java.util.Set<String> allDates = new java.util.HashSet<>();
        allDates.addAll(expenseMap.keySet());
        allDates.addAll(revenueMap.keySet());
        
        // 计算盈利
        List<Map<String, Object>> profit = new java.util.ArrayList<>();
        List<String> sortedDates = new java.util.ArrayList<>(allDates);
        sortedDates.sort(String::compareTo);
        
        for (String date : sortedDates) {
            Map<String, Object> item = new java.util.HashMap<>();
            BigDecimal expenseAmount = expenseMap.getOrDefault(date, BigDecimal.ZERO);
            BigDecimal revenueAmount = revenueMap.getOrDefault(date, BigDecimal.ZERO);
            BigDecimal profitAmount = revenueAmount.subtract(expenseAmount);
            
            item.put("date", date);
            item.put("amount", profitAmount);
            profit.add(item);
        }
        
        return profit;
    }
}

