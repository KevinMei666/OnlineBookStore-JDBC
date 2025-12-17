package test;

import dao.CustomerDao;
import model.Customer;

import java.math.BigDecimal;

/**
 * 测试客户信用等级和透支额度调整功能
 */
public class CustomerCreditTest {

    public static void main(String[] args) {
        System.out.println("=== 客户信用等级和透支额度调整功能测试 ===\n");

        CustomerDao customerDao = new CustomerDao();

        // 1. 准备测试客户
        System.out.println("1. 准备测试客户...");
        Customer customer = customerDao.findByEmail("test_user@example.com");
        if (customer == null) {
            System.out.println("   创建新测试客户");
            customer = new Customer();
            customer.setEmail("test_user@example.com");
            customer.setPasswordHash("test_hash");
            customer.setName("测试用户");
            customer.setAddress("测试地址");
            customer.setBalance(new BigDecimal("1000.00"));
            customer.setCreditLevel(1);
            customer.setMonthlyLimit(new BigDecimal("1000.00"));
            customerDao.insert(customer);
            customer = customerDao.findByEmail("test_user@example.com");
        }
        
        int customerId = customer.getCustomerId();
        int initialCreditLevel = customer.getCreditLevel() != null ? customer.getCreditLevel() : 1;
        BigDecimal initialMonthlyLimit = customer.getMonthlyLimit() != null ? customer.getMonthlyLimit() : BigDecimal.ZERO;
        
        System.out.println("   客户ID: " + customerId);
        System.out.println("   初始信用等级: " + initialCreditLevel);
        System.out.println("   初始透支额度: " + initialMonthlyLimit);

        // 2. 测试调整信用等级
        System.out.println("\n2. 测试调整信用等级...");
        int newCreditLevel = 3;
        System.out.println("   新信用等级: " + newCreditLevel + " (15%折扣，可透支，有额度限制)");
        
        int result = customerDao.updateCreditLevel(customerId, newCreditLevel);
        if (result > 0) {
            System.out.println("   ✓ 信用等级调整成功");
        } else {
            System.out.println("   ✗ 信用等级调整失败");
            return;
        }

        // 验证信用等级变化
        Customer updatedCustomer = customerDao.findById(customerId);
        if (updatedCustomer != null) {
            int actualCreditLevel = updatedCustomer.getCreditLevel() != null ? updatedCustomer.getCreditLevel() : 0;
            System.out.println("   更新后信用等级: " + actualCreditLevel);
            
            if (actualCreditLevel == newCreditLevel) {
                System.out.println("   ✓ 信用等级验证通过");
            } else {
                System.out.println("   ✗ 信用等级验证失败：实际等级与期望不符");
            }
        }

        // 3. 测试调整透支额度
        System.out.println("\n3. 测试调整透支额度...");
        BigDecimal newMonthlyLimit = new BigDecimal("3000.00");
        System.out.println("   新透支额度: " + newMonthlyLimit);
        
        result = customerDao.updateMonthlyLimit(customerId, newMonthlyLimit);
        if (result > 0) {
            System.out.println("   ✓ 透支额度调整成功");
        } else {
            System.out.println("   ✗ 透支额度调整失败");
            return;
        }

        // 验证透支额度变化
        updatedCustomer = customerDao.findById(customerId);
        if (updatedCustomer != null) {
            BigDecimal actualMonthlyLimit = updatedCustomer.getMonthlyLimit() != null ? updatedCustomer.getMonthlyLimit() : BigDecimal.ZERO;
            System.out.println("   更新后透支额度: " + actualMonthlyLimit);
            
            if (actualMonthlyLimit.compareTo(newMonthlyLimit) == 0) {
                System.out.println("   ✓ 透支额度验证通过");
            } else {
                System.out.println("   ✗ 透支额度验证失败：实际额度与期望不符");
            }
        }

        // 4. 测试调整到5级信用（最高级）
        System.out.println("\n4. 测试调整到5级信用（最高级）...");
        int level5 = 5;
        System.out.println("   调整到信用等级: " + level5 + " (25%折扣，可透支，无额度限制)");
        
        result = customerDao.updateCreditLevel(customerId, level5);
        if (result > 0) {
            System.out.println("   ✓ 5级信用调整成功");
        } else {
            System.out.println("   ✗ 5级信用调整失败");
        }

        updatedCustomer = customerDao.findById(customerId);
        if (updatedCustomer != null) {
            int actualLevel = updatedCustomer.getCreditLevel() != null ? updatedCustomer.getCreditLevel() : 0;
            System.out.println("   当前信用等级: " + actualLevel);
            if (actualLevel == level5) {
                System.out.println("   ✓ 5级信用验证通过");
            }
        }

        // 5. 测试边界情况：无效的信用等级
        System.out.println("\n5. 测试边界情况（无效的信用等级）...");
        int invalidLevel1 = 0;
        System.out.println("   尝试设置为无效等级: " + invalidLevel1);
        result = customerDao.updateCreditLevel(customerId, invalidLevel1);
        if (result == 0) {
            System.out.println("   ✓ 正确拒绝了无效等级（0）");
        } else {
            System.out.println("   ✗ 错误地接受了无效等级（0）");
        }

        int invalidLevel2 = 6;
        System.out.println("   尝试设置为无效等级: " + invalidLevel2);
        result = customerDao.updateCreditLevel(customerId, invalidLevel2);
        if (result == 0) {
            System.out.println("   ✓ 正确拒绝了无效等级（6）");
        } else {
            System.out.println("   ✗ 错误地接受了无效等级（6）");
        }

        // 6. 测试边界情况：负数的透支额度
        System.out.println("\n6. 测试边界情况（负数的透支额度）...");
        BigDecimal invalidLimit = new BigDecimal("-1000.00");
        System.out.println("   尝试设置为负数透支额度: " + invalidLimit);
        result = customerDao.updateMonthlyLimit(customerId, invalidLimit);
        if (result == 0) {
            System.out.println("   ✓ 正确拒绝了负数透支额度");
        } else {
            System.out.println("   ✗ 错误地接受了负数透支额度");
        }

        // 7. 测试设置为0透支额度（1-2级信用不允许透支）
        System.out.println("\n7. 测试设置为0透支额度...");
        BigDecimal zeroLimit = BigDecimal.ZERO;
        System.out.println("   设置透支额度为: " + zeroLimit);
        result = customerDao.updateMonthlyLimit(customerId, zeroLimit);
        if (result > 0) {
            System.out.println("   ✓ 0透支额度设置成功（适用于1-2级信用）");
        } else {
            System.out.println("   ✗ 0透支额度设置失败");
        }

        // 8. 综合测试：同时调整信用等级和透支额度
        System.out.println("\n8. 综合测试：同时调整信用等级和透支额度...");
        int finalCreditLevel = 4;
        BigDecimal finalMonthlyLimit = new BigDecimal("5000.00");
        System.out.println("   最终信用等级: " + finalCreditLevel + " (20%折扣，可透支，有额度限制)");
        System.out.println("   最终透支额度: " + finalMonthlyLimit);
        
        result = customerDao.updateCreditLevel(customerId, finalCreditLevel);
        if (result > 0) {
            System.out.println("   ✓ 信用等级调整成功");
        }
        
        result = customerDao.updateMonthlyLimit(customerId, finalMonthlyLimit);
        if (result > 0) {
            System.out.println("   ✓ 透支额度调整成功");
        }

        // 验证最终状态
        updatedCustomer = customerDao.findById(customerId);
        if (updatedCustomer != null) {
            int finalLevel = updatedCustomer.getCreditLevel() != null ? updatedCustomer.getCreditLevel() : 0;
            BigDecimal finalLimit = updatedCustomer.getMonthlyLimit() != null ? updatedCustomer.getMonthlyLimit() : BigDecimal.ZERO;
            
            System.out.println("   最终信用等级: " + finalLevel);
            System.out.println("   最终透支额度: " + finalLimit);
            
            if (finalLevel == finalCreditLevel && finalLimit.compareTo(finalMonthlyLimit) == 0) {
                System.out.println("   ✓ 综合测试验证通过");
            } else {
                System.out.println("   ✗ 综合测试验证失败");
            }
        }

        System.out.println("\n=== 测试完成 ===");
    }
}

