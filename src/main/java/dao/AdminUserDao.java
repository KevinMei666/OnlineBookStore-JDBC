package dao;

import model.AdminUser;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminUserDao {

    /**
     * 根据用户名查询管理员
     *
     * 期待的表结构（请在数据库中手动创建）：
     *  AdminUser(
     *      AdminID INT PRIMARY KEY AUTO_INCREMENT,
     *      Username VARCHAR(50) NOT NULL UNIQUE,
     *      PasswordHash VARCHAR(100) NOT NULL,
     *      Name VARCHAR(100)
     *  )
     */
    public AdminUser findByUsername(String username) {
        String sql = "SELECT AdminID, Username, PasswordHash, Name FROM AdminUser WHERE Username = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                AdminUser admin = new AdminUser();
                admin.setAdminId((Integer) rs.getObject("AdminID"));
                admin.setUsername(rs.getString("Username"));
                admin.setPasswordHash(rs.getString("PasswordHash"));
                admin.setName(rs.getString("Name"));
                return admin;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
    }
}


