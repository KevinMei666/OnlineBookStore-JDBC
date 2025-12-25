package dao;

import util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 财务统计DAO
 * 用于查询书店的开支、收入和盈利数据
 */
public class FinanceDao {

    /**
     * 查询指定日期范围内的采购支出（按日统计）
     * 注意：使用CreateDate统计，因为采购单创建时即承诺支付，符合权责发生制
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return List<Map<String, Object>> 包含 date（日期）和 amount（金额）的列表
     */
    public List<Map<String, Object>> getExpensesByDay(String startDate, String endDate) {
        String sql = "SELECT DATE(CreateDate) AS date, SUM(TotalAmount) AS amount " +
                "FROM PurchaseOrder " +
                "WHERE Status = 'COMPLETED' " +
                "AND DATE(CreateDate) >= ? AND DATE(CreateDate) <= ? " +
                "GROUP BY DATE(CreateDate) " +
                "ORDER BY date ASC";
        
        return executeDateQuery(sql, startDate, endDate);
    }

    /**
     * 查询指定日期范围内的采购支出（按月统计）
     */
    public List<Map<String, Object>> getExpensesByMonth(String startDate, String endDate) {
        String sql = "SELECT DATE_FORMAT(CreateDate, '%Y-%m') AS date, SUM(TotalAmount) AS amount " +
                "FROM PurchaseOrder " +
                "WHERE Status = 'COMPLETED' " +
                "AND DATE(CreateDate) >= ? AND DATE(CreateDate) <= ? " +
                "GROUP BY DATE_FORMAT(CreateDate, '%Y-%m') " +
                "ORDER BY date ASC";
        
        return executeDateQuery(sql, startDate, endDate);
    }

    /**
     * 查询指定日期范围内的采购支出（按年统计）
     */
    public List<Map<String, Object>> getExpensesByYear(String startDate, String endDate) {
        String sql = "SELECT DATE_FORMAT(CreateDate, '%Y') AS date, SUM(TotalAmount) AS amount " +
                "FROM PurchaseOrder " +
                "WHERE Status = 'COMPLETED' " +
                "AND DATE(CreateDate) >= ? AND DATE(CreateDate) <= ? " +
                "GROUP BY DATE_FORMAT(CreateDate, '%Y') " +
                "ORDER BY date ASC";
        
        return executeDateQuery(sql, startDate, endDate);
    }

    /**
     * 查询指定日期范围内的订单收入（按日统计）
     * 注意：只统计已支付的订单（PAID, PARTIAL, SHIPPED），排除未支付的CREATED订单
     * 使用OrderDate统计，因为订单创建时如果支付成功，OrderDate即为收入确认日期
     */
    public List<Map<String, Object>> getRevenueByDay(String startDate, String endDate) {
        String sql = "SELECT DATE(OrderDate) AS date, SUM(TotalAmount) AS amount " +
                "FROM Orders " +
                "WHERE Status IN ('PAID', 'PARTIAL', 'SHIPPED') " +
                "AND TotalAmount > 0 " +
                "AND DATE(OrderDate) >= ? AND DATE(OrderDate) <= ? " +
                "GROUP BY DATE(OrderDate) " +
                "ORDER BY date ASC";
        
        return executeDateQuery(sql, startDate, endDate);
    }

    /**
     * 查询指定日期范围内的订单收入（按月统计）
     */
    public List<Map<String, Object>> getRevenueByMonth(String startDate, String endDate) {
        String sql = "SELECT DATE_FORMAT(OrderDate, '%Y-%m') AS date, SUM(TotalAmount) AS amount " +
                "FROM Orders " +
                "WHERE Status IN ('PAID', 'PARTIAL', 'SHIPPED') " +
                "AND TotalAmount > 0 " +
                "AND DATE(OrderDate) >= ? AND DATE(OrderDate) <= ? " +
                "GROUP BY DATE_FORMAT(OrderDate, '%Y-%m') " +
                "ORDER BY date ASC";
        
        return executeDateQuery(sql, startDate, endDate);
    }

    /**
     * 查询指定日期范围内的订单收入（按年统计）
     */
    public List<Map<String, Object>> getRevenueByYear(String startDate, String endDate) {
        String sql = "SELECT DATE_FORMAT(OrderDate, '%Y') AS date, SUM(TotalAmount) AS amount " +
                "FROM Orders " +
                "WHERE Status IN ('PAID', 'PARTIAL', 'SHIPPED') " +
                "AND TotalAmount > 0 " +
                "AND DATE(OrderDate) >= ? AND DATE(OrderDate) <= ? " +
                "GROUP BY DATE_FORMAT(OrderDate, '%Y') " +
                "ORDER BY date ASC";
        
        return executeDateQuery(sql, startDate, endDate);
    }

    /**
     * 执行日期范围查询的通用方法
     */
    private List<Map<String, Object>> executeDateQuery(String sql, String startDate, String endDate) {
        List<Map<String, Object>> result = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, startDate);
            ps.setString(2, endDate);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("date", rs.getString("date"));
                BigDecimal amount = rs.getBigDecimal("amount");
                item.put("amount", amount != null ? amount : BigDecimal.ZERO);
                result.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        
        return result;
    }

    /**
     * 获取总支出（已完成采购单的总金额）
     * 注意：只统计已完成的采购单，排除未完成的CREATED采购单和金额为0的采购单
     */
    public BigDecimal getTotalExpenses() {
        String sql = "SELECT COALESCE(SUM(TotalAmount), 0) AS total " +
                "FROM PurchaseOrder " +
                "WHERE Status = 'COMPLETED' " +
                "AND TotalAmount > 0";
        
        return executeSingleValueQuery(sql);
    }

    /**
     * 获取总收入（已支付订单的总金额）
     * 注意：只统计已支付的订单，排除未支付的CREATED订单和金额为0的订单
     */
    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(TotalAmount), 0) AS total " +
                "FROM Orders " +
                "WHERE Status IN ('PAID', 'PARTIAL', 'SHIPPED') " +
                "AND TotalAmount > 0";
        
        return executeSingleValueQuery(sql);
    }

    /**
     * 执行单值查询
     */
    private BigDecimal executeSingleValueQuery(String sql) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        
        return BigDecimal.ZERO;
    }
}

