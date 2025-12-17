package test;

import dao.CustomerDao;
import model.Customer;

import java.math.BigDecimal;

/**
 * 测试客户余额增加功能
 */
public class CustomerBalanceTest {

    public static void main(String[] args) {
        System.out.println("=== 客户余额增加功能测试 ===\n");

        CustomerDao customerDao = new CustomerDao();

        // 1. 查找或创建测试客户
        System.out.println("1. 准备测试客户...");
        Customer customer = customerDao.findByEmail("test_user@example.com");
        if (customer == null) {
            System.out.println("   创建新测试客户");
            customer = new Customer();
            customer.setEmail("test_user@example.com");
            customer.setPasswordHash("test_hash");
            customer.setName("测试用户");
            customer.setAddress("测试地址");
            customer.setBalance(new BigDecimal("100.00"));
            customer.setCreditLevel(3);
            customer.setMonthlyLimit(new BigDecimal("2000.00"));
            customerDao.insert(customer);
            customer = customerDao.findByEmail("test_user@example.com");
        }
        
        int customerId = customer.getCustomerId();
        BigDecimal initialBalance = customer.getBalance() != null ? customer.getBalance() : BigDecimal.ZERO;
        System.out.println("   客户ID: " + customerId);
        System.out.println("   初始余额: " + initialBalance);

        // 2. 测试增加余额
        System.out.println("\n2. 测试增加余额...");
        BigDecimal depositAmount = new BigDecimal("500.00");
        System.out.println("   增加金额: " + depositAmount);
        
        int result = customerDao.addBalance(customerId, depositAmount);
        if (result > 0) {
            System.out.println("   ✓ 余额增加成功");
        } else {
            System.out.println("   ✗ 余额增加失败");
            return;
        }

        // 3. 验证余额变化
        System.out.println("\n3. 验证余额变化...");
        Customer updatedCustomer = customerDao.findById(customerId);
        if (updatedCustomer != null) {
            BigDecimal newBalance = updatedCustomer.getBalance() != null ? updatedCustomer.getBalance() : BigDecimal.ZERO;
            BigDecimal expectedBalance = initialBalance.add(depositAmount);
            
            System.out.println("   更新后余额: " + newBalance);
            System.out.println("   期望余额: " + expectedBalance);
            
            if (newBalance.compareTo(expectedBalance) == 0) {
                System.out.println("   ✓ 余额验证通过");
            } else {
                System.out.println("   ✗ 余额验证失败：实际余额与期望不符");
            }
        } else {
            System.out.println("   ✗ 无法查询到客户信息");
        }

        // 4. 测试多次增加余额
        System.out.println("\n4. 测试多次增加余额...");
        BigDecimal depositAmount2 = new BigDecimal("200.00");
        System.out.println("   再次增加金额: " + depositAmount2);
        
        Customer beforeCustomer = customerDao.findById(customerId);
        BigDecimal balanceBefore = beforeCustomer.getBalance() != null ? beforeCustomer.getBalance() : BigDecimal.ZERO;
        System.out.println("   增加前余额: " + balanceBefore);
        
        result = customerDao.addBalance(customerId, depositAmount2);
        if (result > 0) {
            System.out.println("   ✓ 第二次余额增加成功");
        } else {
            System.out.println("   ✗ 第二次余额增加失败");
        }
        
        Customer afterCustomer = customerDao.findById(customerId);
        BigDecimal balanceAfter = afterCustomer.getBalance() != null ? afterCustomer.getBalance() : BigDecimal.ZERO;
        System.out.println("   增加后余额: " + balanceAfter);
        System.out.println("   余额增量: " + balanceAfter.subtract(balanceBefore));
        
        if (balanceAfter.subtract(balanceBefore).compareTo(depositAmount2) == 0) {
            System.out.println("   ✓ 多次增加余额验证通过");
        } else {
            System.out.println("   ✗ 多次增加余额验证失败");
        }

        // 5. 测试边界情况：增加0或负数
        System.out.println("\n5. 测试边界情况（增加0或负数）...");
        BigDecimal invalidAmount = new BigDecimal("-100.00");
        System.out.println("   尝试增加负数金额: " + invalidAmount);
        result = customerDao.addBalance(customerId, invalidAmount);
        if (result == 0) {
            System.out.println("   ✓ 正确拒绝了负数金额");
        } else {
            System.out.println("   ✗ 错误地接受了负数金额");
        }

        System.out.println("\n=== 测试完成 ===");
    }
}

