package dao;

import model.Customer;
import util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    public int insert(Customer customer) {
        String sql = "INSERT INTO Customer (Email, PasswordHash, Name, Address, Balance, CreditLevel, MonthlyLimit) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, customer.getEmail());
            ps.setString(2, customer.getPasswordHash());
            ps.setString(3, customer.getName());
            ps.setString(4, customer.getAddress());
            ps.setBigDecimal(5, customer.getBalance());
            ps.setObject(6, customer.getCreditLevel());
            ps.setBigDecimal(7, customer.getMonthlyLimit());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    public Customer findById(int customerId) {
        String sql = "SELECT CustomerID, Email, PasswordHash, Name, Address, Balance, CreditLevel, MonthlyLimit " +
                "FROM Customer WHERE CustomerID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
    }

    public Customer findByEmail(String email) {
        String sql = "SELECT CustomerID, Email, PasswordHash, Name, Address, Balance, CreditLevel, MonthlyLimit " +
                "FROM Customer WHERE Email = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
    }

    public List<Customer> findAll() {
        String sql = "SELECT CustomerID, Email, PasswordHash, Name, Address, Balance, CreditLevel, MonthlyLimit " +
                "FROM Customer ORDER BY CustomerID";
        List<Customer> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return list;
    }

    /**
     * 管理员端查询客户列表，支持按客户ID和姓名模糊查询
     */
    public List<Customer> queryForAdmin(String customerId, String name) {
        StringBuilder sql = new StringBuilder(
                "SELECT CustomerID, Email, PasswordHash, Name, Address, Balance, CreditLevel, MonthlyLimit " +
                        "FROM Customer WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (customerId != null && !customerId.trim().isEmpty()) {
            sql.append(" AND CustomerID = ?");
            try {
                params.add(Integer.parseInt(customerId.trim()));
            } catch (NumberFormatException e) {
                // 如果不是数字则忽略该条件
            }
        }
        if (name != null && !name.trim().isEmpty()) {
            sql.append(" AND Name LIKE ?");
            params.add("%" + name.trim() + "%");
        }
        sql.append(" ORDER BY CustomerID");

        List<Customer> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM Customer";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return 0;
    }

    /**
     * 收到客户款项后增加账户余额
     * 
     * @param customerId 客户ID
     * @param amount 增加的金额（必须大于0）
     * @return 成功返回1，失败返回0
     */
    public int addBalance(int customerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("增加金额必须大于0");
            return 0;
        }
        
        String sql = "UPDATE Customer SET Balance = COALESCE(Balance, 0) + ? WHERE CustomerID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setBigDecimal(1, amount);
            ps.setInt(2, customerId);
            int result = ps.executeUpdate();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    /**
     * 手动调整客户信用等级
     * 信用等级：1-5级
     * 1级：10%折扣，不能透支
     * 2级：15%折扣，不能透支
     * 3级：15%折扣，可透支，有额度限制
     * 4级：20%折扣，可透支，有额度限制
     * 5级：25%折扣，可透支，无额度限制
     * 
     * @param customerId 客户ID
     * @param creditLevel 新的信用等级（1-5）
     * @return 成功返回1，失败返回0
     */
    public int updateCreditLevel(int customerId, int creditLevel) {
        if (creditLevel < 1 || creditLevel > 5) {
            System.err.println("信用等级必须在1-5之间");
            return 0;
        }
        
        String sql = "UPDATE Customer SET CreditLevel = ? WHERE CustomerID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, creditLevel);
            ps.setInt(2, customerId);
            int result = ps.executeUpdate();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    /**
     * 手动调整客户透支额度（MonthlyLimit）
     * 
     * @param customerId 客户ID
     * @param monthlyLimit 新的月度透支额度（必须大于等于0）
     * @return 成功返回1，失败返回0
     */
    public int updateMonthlyLimit(int customerId, BigDecimal monthlyLimit) {
        if (monthlyLimit == null || monthlyLimit.compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("透支额度必须大于等于0");
            return 0;
        }
        
        String sql = "UPDATE Customer SET MonthlyLimit = ? WHERE CustomerID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setBigDecimal(1, monthlyLimit);
            ps.setInt(2, customerId);
            int result = ps.executeUpdate();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    /**
     * 客户端：更新姓名、邮箱、地址
     */
    public int updateBasicInfo(int customerId, String name, String email, String address) {
        String sql = "UPDATE Customer SET Name = ?, Email = ?, Address = ? WHERE CustomerID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, address);
            ps.setInt(4, customerId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    /**
     * 管理员端一次性更新客户余额、信用等级与月度透支额度
     */
    public int updateAccountAndCredit(int customerId, BigDecimal balance,
                                      Integer creditLevel, BigDecimal monthlyLimit) {
        String sql = "UPDATE Customer SET Balance = ?, CreditLevel = ?, MonthlyLimit = ? WHERE CustomerID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setBigDecimal(1, balance);
            if (creditLevel != null) {
                ps.setInt(2, creditLevel);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setBigDecimal(3, monthlyLimit);
            ps.setInt(4, customerId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    /**
     * 更新客户密码
     * 
     * @param customerId 客户ID
     * @param newPasswordHash 新密码（当前系统为明文存储；如需改为加密请在此处调整）
     * @return 成功返回1，失败返回0
     */
    public int updatePassword(int customerId, String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.trim().isEmpty()) {
            System.err.println("新密码不能为空");
            return 0;
        }
        
        String sql = "UPDATE Customer SET PasswordHash = ? WHERE CustomerID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, newPasswordHash.trim());
            ps.setInt(2, customerId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId((Integer) rs.getObject("CustomerID"));
        customer.setEmail(rs.getString("Email"));
        customer.setPasswordHash(rs.getString("PasswordHash"));
        customer.setName(rs.getString("Name"));
        customer.setAddress(rs.getString("Address"));
        customer.setBalance(rs.getBigDecimal("Balance"));
        customer.setCreditLevel((Integer) rs.getObject("CreditLevel"));
        customer.setMonthlyLimit(rs.getBigDecimal("MonthlyLimit"));
        return customer;
    }
}


