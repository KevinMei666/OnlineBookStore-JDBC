package test;

import util.DBUtil;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CreditAdjustmentProcTest {

    public static void main(String[] args) throws Exception {
        System.out.println("=== 月初信用等级批量调整存储过程测试 ===");

        System.out.println("-- 调整前客户信用信息 --");
        printAllCustomersBasicInfo();

        // 调用存储过程：CALL AdjustCustomerCreditMonthly();
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = DBUtil.getConnection();
            cs = conn.prepareCall("{CALL AdjustCustomerCreditMonthly()}");
            cs.execute();
            System.out.println();
            System.out.println("已执行存储过程 AdjustCustomerCreditMonthly()");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cs != null) {
                try {
                    cs.close();
                } catch (Exception ignored) {
                }
            }
            DBUtil.closeQuietly(conn);
        }

        System.out.println();
        System.out.println("-- 调整后客户信用信息 --");
        printAllCustomersBasicInfo();

        System.out.println("=== CreditAdjustmentProcTest 结束 ===");
    }

    private static void printAllCustomersBasicInfo() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT CustomerID, Name, Balance, CreditLevel, MonthlyLimit " +
                    "FROM Customer ORDER BY CustomerID";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                int customerId = rs.getInt("CustomerID");
                String name = rs.getString("Name");
                java.math.BigDecimal balance = rs.getBigDecimal("Balance");
                int creditLevel = rs.getInt("CreditLevel");
                java.math.BigDecimal monthlyLimit = rs.getBigDecimal("MonthlyLimit");
                System.out.println("CustomerID=" + customerId
                        + " | Name=" + name
                        + " | Balance=" + balance
                        + " | CreditLevel=" + creditLevel
                        + " | MonthlyLimit=" + monthlyLimit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
    }
}


