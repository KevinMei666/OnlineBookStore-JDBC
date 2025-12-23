package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DBUtil {

    private static final String JDBC_URL =
            "jdbc:mysql://localhost:3306/OnlineBookStore?useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String JDBC_USER = "Hydralune";
    private static final String JDBC_PASSWORD = "54idt@Mysql";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("缺少 MySQL 驱动: com.mysql.cj.jdbc.Driver", e);
        }
    }

    private DBUtil() {
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
        } catch (SQLException e) {
            throw new IllegalStateException("获取数据库连接失败", e);
        }
    }

    public static void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static void closeQuietly(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static void closeQuietly(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static void closeQuietly(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static void closeQuietly(ResultSet rs, PreparedStatement ps, Connection conn) {
        closeQuietly(rs);
        closeQuietly(ps);
        closeQuietly(conn);
    }
}
