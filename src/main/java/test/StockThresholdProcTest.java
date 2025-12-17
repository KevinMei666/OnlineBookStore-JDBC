package test;

import util.DBUtil;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StockThresholdProcTest {

    public static void main(String[] args) throws Exception {
        System.out.println("=== 库存阈值自动缺书记录存储过程测试 ===");

        // 设置测试阈值（例如：库存低于 10 本时触发）
        int threshold = 10;

        System.out.println("-- 执行前：库存低于阈值 " + threshold + " 的图书 --");
        printLowStockBooks(threshold);

        System.out.println();
        System.out.println("-- 执行前：未处理的缺书记录数量 --");
        int shortageBefore = countUnprocessedShortageRecords();
        System.out.println("未处理的 ShortageRecord 数量 = " + shortageBefore);

        // 调用存储过程：CALL CheckStockAndCreateShortage(?);
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = DBUtil.getConnection();
            cs = conn.prepareCall("{CALL CheckStockAndCreateShortage(?)}");
            cs.setInt(1, threshold);
            cs.execute();
            System.out.println();
            System.out.println("已执行存储过程 CheckStockAndCreateShortage(" + threshold + ")");
        } catch (Exception e) {
            System.out.println("执行存储过程时出错（可能存储过程尚未创建）：");
            e.printStackTrace();
            return;
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
        System.out.println("-- 执行后：未处理的缺书记录数量 --");
        int shortageAfter = countUnprocessedShortageRecords();
        System.out.println("未处理的 ShortageRecord 数量 = " + shortageAfter);
        System.out.println("新增缺书记录 = " + (shortageAfter - shortageBefore));

        System.out.println();
        System.out.println("-- 最新生成的缺书记录（前5条） --");
        printLatestShortageRecords(5);

        System.out.println("=== StockThresholdProcTest 结束 ===");
    }

    private static void printLowStockBooks(int threshold) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT BookID, Title, StockQuantity FROM Book " +
                    "WHERE StockQuantity < ? ORDER BY StockQuantity ASC";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, threshold);
            rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                int bookId = rs.getInt("BookID");
                String title = rs.getString("Title");
                int stock = rs.getInt("StockQuantity");
                System.out.println("BookID=" + bookId + " | Title=" + title + " | Stock=" + stock);
                count++;
            }
            if (count == 0) {
                System.out.println("（无库存低于 " + threshold + " 的图书）");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
    }

    private static int countUnprocessedShortageRecords() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT COUNT(*) AS cnt FROM ShortageRecord WHERE Processed = 0";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("cnt");
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
    }

    private static void printLatestShortageRecords(int limit) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT ShortageID, BookID, Quantity, Date, SourceType, Processed " +
                    "FROM ShortageRecord ORDER BY Date DESC LIMIT ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, limit);
            rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                int shortageId = rs.getInt("ShortageID");
                int bookId = rs.getInt("BookID");
                int quantity = rs.getInt("Quantity");
                java.sql.Timestamp date = rs.getTimestamp("Date");
                String sourceType = rs.getString("SourceType");
                boolean processed = rs.getInt("Processed") != 0;
                System.out.println("ShortageID=" + shortageId
                        + " | BookID=" + bookId
                        + " | Quantity=" + quantity
                        + " | Date=" + date
                        + " | SourceType=" + sourceType
                        + " | Processed=" + processed);
                count++;
            }
            if (count == 0) {
                System.out.println("（无缺书记录）");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
    }
}

